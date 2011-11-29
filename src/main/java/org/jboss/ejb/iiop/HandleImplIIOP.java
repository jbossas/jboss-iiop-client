/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb.iiop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.spi.HandleDelegate;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.CORBA.Stub;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;

/**
 * A CORBA-based EJBObject handle implementation.
 */
public class HandleImplIIOP implements Handle, Serializable {

    private static final long serialVersionUID = -601170775289648475L;

    /**
     * This handle encapsulates an stringfied CORBA reference for an
     * <code>EJBObject</code>.
     */
    private String ior;

    /**
     * The stub class
     */
    private transient Class<?> stubClass = EJBObject.class;

    /**
     * Constructs an <code>HandleImplIIOP</code>.
     *
     * @param ior An stringfied CORBA reference for an <code>EJBObject</code>.
     */
    public HandleImplIIOP(String ior) {
        this.ior = ior;
    }

    /**
     * Constructs an <tt>HandleImplIIOP</tt>.
     *
     * @param obj An <code>EJBObject</code>.
     */
    public HandleImplIIOP(EJBObject obj) {
        this((org.omg.CORBA.Object) obj);
    }

    /**
     * Constructs an <tt>HandleImplIIOP</tt>.
     *
     * @param obj A CORBA reference for an <code>EJBObject</code>.
     */
    public HandleImplIIOP(org.omg.CORBA.Object obj) {
        this.ior = getOrb().object_to_string(obj);
        this.stubClass = obj.getClass();
    }

    /**
     * Obtains the <code>EJBObject</code> represented by this handle.
     *
     * @return a reference to an <code>EJBObject</code>.
     * @throws java.rmi.RemoteException
     */
    public EJBObject getEJBObject() throws RemoteException {
        try {
            org.omg.CORBA.Object obj = getOrb().string_to_object(ior);
            return narrow(obj);
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Could not get EJBObject from Handle", e);
        }
    }

    private EJBObject narrow(org.omg.CORBA.Object obj) throws ClassCastException, RemoteException {
        // Null object - this should probably throw some kind of error?
        if (obj == null)
            return null;

        if (stubClass.isAssignableFrom(obj.getClass()))
            return (EJBObject) obj;

        if (stubClass == EJBObject.class)
            return (EJBObject) PortableRemoteObject.narrow(obj, EJBObject.class);

        // Create the stub from the stub class
        try {
            Stub stub = (Stub) stubClass.newInstance();
            stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate());
            return (EJBObject) stub;
        } catch (Exception e) {
            throw new RemoteException("Error creating stub", e);
        }
    }

    private void writeObject(ObjectOutputStream oostream) throws IOException {
        final HandleDelegate delegate = getHandleDelegate();
        delegate.writeEJBObject(getEJBObject(), oostream);
    }

    private void readObject(ObjectInputStream oistream) throws IOException, ClassNotFoundException {
        EJBObject obj = getHandleDelegate().readEJBObject(oistream);
        this.ior = getOrb().object_to_string((org.omg.CORBA.Object) obj);
        this.stubClass = obj.getClass();
    }

    private HandleDelegate getHandleDelegate() {
        try {
            return (HandleDelegate) new InitialContext().lookup("java:comp/HandleDelegate");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private ORB getOrb() {
        try {
            return (ORB) new InitialContext().lookup("java:comp/ORB");
        } catch (NamingException e) {
            //if our first attempt fails we attempt with a non standard lookup that
            //should be availble from any context, as long as we are on JBoss
            try {
                return (ORB) new InitialContext().lookup("java:jboss/ORB");
            } catch (NamingException e1) {

            }
            throw new RuntimeException(e);
        }
    }
}
