/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.sessions;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;

/**
 * <p>
 * <b>Purpose</b>:Use this Connector to build a java.sql.Connection by
 * directly instantiating the Driver, as opposed to using the DriverManager.
 *
 * @author Big Country
 * @since TOPLink/Java 2.1
 */
public class DirectConnector extends DefaultConnector {

    /**
     * PUBLIC:
     * Construct a Connector with default settings (Sun JDBC-ODBC bridge).
     * Although this does not really make sense for a "direct" Connector -
     * the Sun JdbcOdbcDriver works fine with the DriverManager.
     */
    public DirectConnector() {
        super();
    }

    /**
     * PUBLIC:
     * Construct a Connector with the specified settings.
     */
    public DirectConnector(String driverClassName, String driverURLHeader, String databaseURL) {
        super(driverClassName, driverURLHeader, databaseURL);
    }

    /**
     * INTERNAL: 
     * Indicates whether DriverManager should be used.
     * @return boolean 
     */
     public boolean shouldUseDriverManager(Properties properties, Session session) {
         return false;
     }
}