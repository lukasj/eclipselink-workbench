/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.sessions.changesets;

import org.eclipse.persistence.sessions.Record;

/**
 * <p>
 * <b>Purpose</b>: To Provide API to the TransformationMappingChangeRecord.
 * <p>
 * <b>Description</b>: This changeRecord stores the particular database row that was changed in the mapping.
 * <p>
 */
public interface TransformationMappingChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * This method is used to access the changes of the fields in a transformation mapping.
     * @return org.eclipse.persistence.sessions.Record
     */
    public Record getRecord();
}