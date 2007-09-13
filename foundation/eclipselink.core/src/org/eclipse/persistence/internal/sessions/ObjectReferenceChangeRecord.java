/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.sessions;

import org.eclipse.persistence.descriptors.ClassDescriptor;

/**
 * <p>
 * <b>Purpose</b>: To record the changes for an attribute that references a single Object
 * @see RelatedClasses prototype.changeset.CollectionChangeRecord,prototype.changeset.SingleObjectChangeRecord
 */
public class ObjectReferenceChangeRecord extends ChangeRecord implements org.eclipse.persistence.sessions.changesets.ObjectReferenceChangeRecord {

    /** This is the object change set that the attribute points to. */
    protected ObjectChangeSet newValue;
    
    /** A reference to the old value must also be sotred.  This is only required for the commit and must never be serialized. */
    protected transient Object oldValue;

    /**
     * INTERNAL:
     * This default constructor is reference internally by SDK XML project to mapp this class
     */
    public ObjectReferenceChangeRecord() {
        super();
    }

    /**
     * INTERNAL:
     * This Constructor is used to create an ObjectReferenceChangeRecord With an owner
     * @param owner prototype.changeset.ObjectChangeSet
     */
    public ObjectReferenceChangeRecord(ObjectChangeSet owner) {
        this.owner = owner;
    }

    /**
     * ADVANCED:
     * Returns the new reference for this object
     * @return prototype.changeset.ObjectChangeSet
     */
    public org.eclipse.persistence.sessions.changesets.ObjectChangeSet getNewValue() {
        return newValue;
    }

    /**
     * INTERNAL:
     * This method will be used to merge one record into another
     */
    public void mergeRecord(ChangeRecord mergeFromRecord, UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        ObjectChangeSet localChangeSet = mergeToChangeSet.findOrIntegrateObjectChangeSet((ObjectChangeSet)((ObjectReferenceChangeRecord)mergeFromRecord).getNewValue(), mergeFromChangeSet);
        this.newValue = localChangeSet;
    }

    /**
     * INTERNAL:
     * Ensure this change record is ready to by sent remotely for cache synchronization
     * In general, this means setting the CacheSynchronizationType on any ObjectChangeSets
     * associated with this ChangeRecord
     */
    public void prepareForSynchronization(AbstractSession session) {
        if ((newValue != null) && (((org.eclipse.persistence.internal.sessions.ObjectChangeSet)newValue).getSynchronizationType() == ClassDescriptor.UNDEFINED_OBJECT_CHANGE_BEHAVIOR)) {
            ClassDescriptor descriptor = session.getDescriptor(newValue.getClassType(session));
            int syncType = descriptor.getCacheSynchronizationType();
            ((org.eclipse.persistence.internal.sessions.ObjectChangeSet)newValue).setSynchronizationType(syncType);
            ((org.eclipse.persistence.internal.sessions.ObjectChangeSet)newValue).prepareChangeRecordsForSynchronization(session);
        }
    }

    /**
     * This method sets the value of the change to be made.
     * @param newValue prototype.changeset.ObjectChangeSet
     */
    public void setNewValue(org.eclipse.persistence.sessions.changesets.ObjectChangeSet newValue) {
        this.newValue = (ObjectChangeSet)newValue;
    }

    /**
     * This method sets the value of the change to be made.
     * @param newValue prototype.changeset.ObjectChangeSet
     */
    public void setNewValue(ObjectChangeSet newValue) {
        this.newValue = newValue;
    }
    
    /**
     * Return the old value of the object reference.
     * This is used during the commit for private-owned references.
     */
    public Object getOldValue() {
        return oldValue;
    }
    
    /**
     * Set the old value of the object reference.
     * This is used during the commit for private-owned references.
     */
    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * INTERNAL:
     * This method will be used to update the objectsChangeSets references
     */
    public void updateReferences(UnitOfWorkChangeSet mergeToChangeSet, UnitOfWorkChangeSet mergeFromChangeSet) {
        this.setNewValue(mergeToChangeSet.findOrIntegrateObjectChangeSet((ObjectChangeSet)this.getNewValue(), mergeFromChangeSet));
    }
}