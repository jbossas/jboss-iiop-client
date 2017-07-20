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

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.HomeHandle;

/**
 *The {@link EJBMetaData} implementation used in remote IIOP invocations.
 *
 * @author Stuart Douglas
 */
public class EJBMetaDataImplIIOP implements EJBMetaData, Serializable {

    private static final long serialVersionUID = 34627983207130L;

    private final Class remoteClass;

    private final Class homeClass;

    private final Class pkClass;

    private final boolean session;

    private final boolean statelessSession;

    private final HomeHandle home;

    public EJBMetaDataImplIIOP(final Class remoteClass, final Class homeClass, final Class pkClass, final boolean session,
                               final boolean statelessSession, final HomeHandle home) {
        this.remoteClass = remoteClass;
        this.homeClass = homeClass;
        this.pkClass = pkClass;
        this.session = session;
        this.statelessSession = statelessSession;
        this.home = home;
    }


    /**
     * Obtains the home interface of the enterprise Bean.
     */
    public EJBHome getEJBHome() {
        try {
            return home.getEJBHome();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtains the <code>Class</code> object for the enterprise Bean's home
     * interface.
     */
    public Class getHomeInterfaceClass() {
        return homeClass;
    }

    /**
     * Obtains the <code>Class</code> object for the enterprise Bean's remote
     * interface.
     */
    public Class getRemoteInterfaceClass() {
        return remoteClass;
    }

    /**
     * Obtains the <code>Class</code> object for the enterprise Bean's primary
     * key class.
     */
    public Class getPrimaryKeyClass() {
        if (session)
            throw new RuntimeException("A session bean does not have a primary key class");
        return pkClass;
    }

    /**
     * Tests if the enterprise Bean's type is "session".
     *
     * @return true if the type of the enterprise Bean is session bean.
     */
    public boolean isSession() {
        return session;
    }

    /**
     * Tests if the enterprise Bean's type is "stateless session".
     *
     * @return true if the type of the enterprise Bean is stateless session.
     */
    public boolean isStatelessSession() {
        return statelessSession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EJBMetaDataImplIIOP that = (EJBMetaDataImplIIOP) o;

        if (session != that.session) return false;
        if (statelessSession != that.statelessSession) return false;
        if (home != null ? !home.equals(that.home) : that.home != null) return false;
        if (homeClass != null ? !homeClass.equals(that.homeClass) : that.homeClass != null) return false;
        if (pkClass != null ? !pkClass.equals(that.pkClass) : that.pkClass != null) return false;
        if (remoteClass != null ? !remoteClass.equals(that.remoteClass) : that.remoteClass != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = remoteClass != null ? remoteClass.hashCode() : 0;
        result = 31 * result + (homeClass != null ? homeClass.hashCode() : 0);
        result = 31 * result + (pkClass != null ? pkClass.hashCode() : 0);
        result = 31 * result + (session ? 1 : 0);
        result = 31 * result + (statelessSession ? 1 : 0);
        result = 31 * result + (home != null ? home.hashCode() : 0);
        return result;
    }
}
