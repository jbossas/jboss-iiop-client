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

    private Class remoteClass;

    private Class homeClass;

    private Class pkClass;

    private boolean session;

    private boolean statelessSession;

    private HomeHandle home;

    /**
     * no-arg ctor for serialization
     */
    public EJBMetaDataImplIIOP() {
    }

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
}
