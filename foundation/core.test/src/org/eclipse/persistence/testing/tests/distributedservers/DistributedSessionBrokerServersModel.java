/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.distributedservers;

import org.eclipse.persistence.sessions.broker.SessionBroker;
import org.eclipse.persistence.sessions.DatabaseSession;

public class DistributedSessionBrokerServersModel extends DistributedServersModel {

    public DistributedSessionBrokerServersModel() {
        super();
        setDescription("This suite tests updating objects with changed parts for SessionBroker.");
    }

    boolean hasSetSessionBroker;

    /**
     * This method returns the system to the original state
     */
    public void reset() {
        super.reset();
        if (hasSetSessionBroker && getSession().isSessionBroker()) {
            getDatabaseSession().logout();
            DatabaseSession originalSession = (DatabaseSession)((SessionBroker)getSession()).getSessionsByName().values().iterator().next();
            originalSession.login();
            getExecutor().setSession(originalSession);
            hasSetSessionBroker = false;
        }
    }

    /**
     * This method sets up the distributed servers and the registry
     */
    public void setup() {
        if (!hasSetSessionBroker && !getSession().isSessionBroker()) {
            getDatabaseSession().logout();
            DatabaseSession originalSession = (DatabaseSession)getSession();
            SessionBroker broker = new SessionBroker();
            broker.registerSession(originalSession.getName(), originalSession);
            broker.setSessionLog(originalSession.getSessionLog());
            broker.login();
            getExecutor().setSession(broker);
            hasSetSessionBroker = true;
        }
        super.setup();
    }
}