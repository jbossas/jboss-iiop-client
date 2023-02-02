/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.ejb.iiop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;

import jakarta.ejb.EJBHome;
import jakarta.ejb.HomeHandle;
import jakarta.ejb.spi.HandleDelegate;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.CORBA.Stub;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;

/**
 * A CORBA-based EJB home handle implementation.
 *
 */
public class HomeHandleImplIIOP implements HomeHandle {

    private static final long serialVersionUID = 965281969551395661L;

    /**
     * This handle encapsulates an stringfied CORBA reference for an
     * <code>EJBHome</code>.
     */
    private String ior;

    /**
     * The stub class
     */
    private transient Class<?> stubClass = EJBHome.class;

    /**
     * Constructs a <code>HomeHandleImplIIOP</code>.
     *
     * @param ior An stringfied CORBA reference for an <code>EJBHome</code>.
     */
    public HomeHandleImplIIOP(final String ior) {
        this.ior = ior;
    }

    /**
     * Constructs a <tt>HomeHandleImplIIOP</tt>.
     *
     * @param home An <code>EJBHome</code>.
     */
    public HomeHandleImplIIOP(final EJBHome home) {
        this((org.omg.CORBA.Object) home);
    }

    /**
     * Constructs a <tt>HomeHandleImplIIOP</tt>.
     *
     * @param home A CORBA reference for an <code>EJBHome</code>.
     */
    public HomeHandleImplIIOP(final org.omg.CORBA.Object home) {
        this.ior = getOrb().object_to_string(home);
        this.stubClass = home.getClass();
    }

    /**
     * Obtains the <code>EJBHome</code> represented by this home handle.
     *
     * @return a reference to an <code>EJBHome</code>.
     * @throws java.rmi.RemoteException
     */
    public EJBHome getEJBHome() throws RemoteException {
        try {
            final org.omg.CORBA.Object obj = getOrb().string_to_object(ior);
            return narrow(obj);
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Could not get EJBHome from HomeHandle", e);
        }
    }

    private EJBHome narrow(org.omg.CORBA.Object obj) throws ClassCastException, RemoteException {
        // Null object - this should probably throw some kind of error?
        if (obj == null)
            return null;

        // Already the correct type
        if (stubClass.isAssignableFrom(obj.getClass()))
            return (EJBHome) obj;

        // Backwards compatibility - almost certainly wrong!
        if (stubClass == EJBHome.class)
            return (EJBHome) PortableRemoteObject.narrow(obj, EJBHome.class);

        // Create the stub from the stub class
        try {
            final Stub stub = (Stub) stubClass.newInstance();
            stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate());
            return (EJBHome) stub;
        } catch (Exception e) {
            throw new RemoteException("Error creating stub", e);
        }
    }

    private void writeObject(final ObjectOutputStream oostream) throws IOException {
        getHandleDelegate().writeEJBHome(getEJBHome(), oostream);
    }

    private void readObject(final ObjectInputStream oistream) throws IOException, ClassNotFoundException {
        final EJBHome obj = getHandleDelegate().readEJBHome(oistream);
        this.ior = getOrb().object_to_string((org.omg.CORBA.Object) obj);
        this.stubClass = obj.getClass();
    }


    private HandleDelegate getHandleDelegate(){
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HomeHandleImplIIOP that = (HomeHandleImplIIOP) o;

        if (ior != null ? !ior.equals(that.ior) : that.ior != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ior != null ? ior.hashCode() : 0;
    }
}
