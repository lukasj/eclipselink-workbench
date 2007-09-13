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


/**
 * <p>
 * <b>Purpose</b>: This interface defines the API for the ChangeRecord that holds the changes made to a direct collection attribute of
 * an object.
 * <p>
 * <b>Description</b>: Collections are compared to each other and added and removed objects are
 * recorded seperately
 */
public interface DirectCollectionChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * This method returns the collection of Primitive Objects that were added to the collection.
     * @return java.util.Vector
     */
    public java.util.Vector getAddObjectList();

    /**
     * ADVANCED:
     * This method returns the collection of Primitive Objects that were removed to the collection.
     * @return java.util.Vector
     */
    public java.util.Vector getRemoveObjectList();
}