/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.jpa.metadata.queries;

/**
 * Object to hold onto query hints metadata. Use this object to preserve 
 * information like multiples and order of specification. We lose that by 
 * using a java hash object directly.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataQueryHint {
    protected String m_name;
    protected String m_value;
    
    /**
     * INTERNAL:
     */
    public MetadataQueryHint(String name, String value) {
        m_name = name;
        m_value = value;
    }
    
    /**
     * INTERNAL:
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * INTERNAL:
     */
    public String getValue() {
        return m_value;
    }
}