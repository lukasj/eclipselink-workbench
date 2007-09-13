/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.sessionsxml;

import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.testing.framework.TestCase;
import org.eclipse.persistence.internal.sessions.factories.XMLSessionConfigLoader;
import org.eclipse.persistence.sessions.factories.SessionManager;


/**
 * Load a Session from an xml file that conforms to TopLink XML Schema Session
 */
public abstract class RcmBasicTest extends TestCase {
    public String sessionsXmlFileName = "sessions xml file name";
    public String sessionName = "rcm_test_session";
    public DatabaseSession loadedSession;

    /**
     * Load the session
     */
    protected void setup() {
        SessionManager.getManager().getSessions().remove(sessionName);
        loadedSession = (DatabaseSession)SessionManager.getManager().getSession(new XMLSessionConfigLoader(sessionsXmlFileName), sessionName, this.getClass().getClassLoader(), false, true);
    }

    /**
     * Remove the session from the singleton SessionManager
     */
    public void reset() {
        SessionManager.getManager().destroySession(sessionName);
    }
}