/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.sessions.factories.model.transport;


/**
 * INTERNAL:
 */
public abstract class TransportManagerConfig {
    private String m_onConnectionError;

    public TransportManagerConfig() {
    }

    public void setOnConnectionError(String onConnectionError) {
        m_onConnectionError = onConnectionError;
    }

    public String getOnConnectionError() {
        return m_onConnectionError;
    }
}