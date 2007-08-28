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
import org.eclipse.persistence.testing.framework.AutoVerifyTestCase;
import org.eclipse.persistence.testing.framework.TestErrorException;
import org.eclipse.persistence.internal.sessions.factories.XMLSessionConfigLoader;
import org.eclipse.persistence.sessions.factories.SessionManager;


/**
 * Tests the getSession(String, String) API from SessionManager.
 *
 * Bug #3821922
 *
 * @author Guy Pelletier
 * @version 1.0
 * @date September 27, 2004
 */
public class SessionManagerGetSessionStringStringTest extends AutoVerifyTestCase {
    DatabaseSession m_session = null;

    public SessionManagerGetSessionStringStringTest() {
        setDescription("Tests the getSession(String, String) API from SessionManager");
    }

    public void reset() {
        if ((m_session != null) && m_session.isConnected()) {
            m_session.logout();
            SessionManager.getManager().getSessions().remove("EmployeeSession");
            m_session = null;
        }
    }

    public void test() {
        //this change for making tests pass on oc4j server, suggested by James
        //m_session = (DatabaseSession)SessionManager.getManager().getSession("EmployeeSession", "org/eclipse/persistence/testing/models/sessionsxml/XMLSchemaSession.xml");
        m_session = (DatabaseSession)SessionManager.getManager().getSession(new XMLSessionConfigLoader("org/eclipse/persistence/testing/models/sessionsxml/XMLSchemaSession.xml"), "EmployeeSession", getClass().getClassLoader(), true, true);
    }

    protected void verify() {
        if (m_session == null) {
            throw new TestErrorException("Session did not load properly");
        } else if (!m_session.isConnected()) {
            throw new TestErrorException("Session did not log in automatically");
        }
    }
}