package org.jboss.ejb.iiop;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;

/**
 *The {@link EJBMetaData} implementation used in remote IIOP invocations.
 *
 * @author Stuart Douglas
 */
public class EJBMetaDataIIOP implements EJBMetaData {

    private final Class remoteClass;

    private final Class homeClass;

    private final Class pkClass;

    private final boolean session;

    private final boolean statelessSession;

    private final EJBHome home;

    public EJBMetaDataIIOP(final Class remoteClass, final Class homeClass, final Class pkClass, final boolean session,
                           final boolean statelessSession, final EJBHome home) {
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
        return home;
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
