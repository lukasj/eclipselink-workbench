/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.platform.server.jboss;

import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.transaction.jboss.JBossTransactionController;
import org.eclipse.persistence.platform.server.ServerPlatformBase;

/**
 * PUBLIC:
 *
 * This is the concrete subclass responsible for representing JBoss-specific server behaviour.
 *
 * This platform overrides:
 *
 * getExternalTransactionControllerClass(): to use an JBoss-specific controller class
 *
 */
public class JBossPlatform extends ServerPlatformBase {

    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public JBossPlatform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * for JBoss. This is read-only.
     *
     * @return Class externalTransactionControllerClass
     *
     * @see org.eclipse.persistence.transaction.JTATransactionController
     * @see ServerPlatformBase.isJTAEnabled()
     * @see ServerPlatformBase.disableJTA()
     * @see ServerPlatformBase.initializeExternalTransactionController()
     */
    public Class getExternalTransactionControllerClass() {
    	if (externalTransactionControllerClass == null){
    		externalTransactionControllerClass = JBossTransactionController.class;
    	}
        return externalTransactionControllerClass;
    }
}