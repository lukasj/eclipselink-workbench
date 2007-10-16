/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.jpa;

import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Iterator;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.descriptors.DescriptorIterator;
import org.eclipse.persistence.internal.helper.IdentityHashtable;
import org.eclipse.persistence.internal.localization.ExceptionLocalization;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.OptimisticLockException;
import org.eclipse.persistence.internal.sessions.MergeManager;
import org.eclipse.persistence.internal.sessions.UnitOfWorkChangeSet;
import org.eclipse.persistence.internal.sessions.ObjectChangeSet;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;


public class RepeatableWriteUnitOfWork extends UnitOfWorkImpl {
    
    /** Used to store the final UnitOfWorkChangeSet for merge into the shared cache */
    protected UnitOfWorkChangeSet cumulativeUOWChangeSet;
    /** Used to store objects already deleted from the db and unregistered */
    protected IdentityHashtable unregisteredDeletedObjectsCloneToBackupAndOriginal;
    
    /** Used to determine if UnitOfWork should commit and rollback transactions 
     * This is used when an EntityTransaction is controlling the transaction
     */
    protected boolean shouldTerminateTransaction;
    
    /** Used to determine if UnitOfWork.synchronizeAndResume method should
     * resume (the normal behaviour); or alternatively clear the UnitOfWork.
     */
    protected boolean shouldClearForCloseInsteadOfResume = false;
    
    public RepeatableWriteUnitOfWork(org.eclipse.persistence.internal.sessions.AbstractSession parentSession){
        super(parentSession);
        this.shouldTerminateTransaction = true;
        this.shouldNewObjectsBeCached = true;
    }
    
    /**
     * INTERNAL:
     * Call this method if the uow will no longer used for comitting transactions:
     * all the changes sets will be dereferenced, and (optionally) the cache cleared.
     * If the uow is not released, but rather kept around for ValueHolders, then identity maps shouldn't be cleared:
     * the parameter value should be 'false'. The lifecycle set to Birth so that uow ValueHolder still could be used.
     * Alternatively, if called from release method then everything should go and therefore parameter value should be 'true'.
     * In this case lifecycle won't change - uow.release (optionally) calls this method when it (uow) is already dead.
     * The reason for calling this method from release is to free maximum memory right away:
     * the uow might still be referenced by objects using UOWValueHolders (though they shouldn't be around
     * they still might).
     */
    public void clearForClose(boolean shouldClearCache){
        super.clearForClose(shouldClearCache);
        this.cumulativeUOWChangeSet = null;
        this.unregisteredDeletedObjectsCloneToBackupAndOriginal = null;
    }
    
    /**
     * INTERNAL:
     * Indicates whether clearForClose methor should be called by release method.
     */
    public boolean shouldClearForCloseOnRelease() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Commit the changes to any objects to the parent.
     */
    public void commitRootUnitOfWork() throws DatabaseException, OptimisticLockException {
        commitToDatabaseWithChangeSet(false);
        // unit of work has been committed so it's ok to set the cumulative into the UOW for merge
        if(this.cumulativeUOWChangeSet != null) {
            this.cumulativeUOWChangeSet.mergeUnitOfWorkChangeSet((UnitOfWorkChangeSet)this.getUnitOfWorkChangeSet(), this, true);
            setUnitOfWorkChangeSet(this.cumulativeUOWChangeSet);
        }

        commitTransactionAfterWriteChanges(); // this method will commit the transaction
                                              // and set the transaction flags appropriately

        // Merge after commit	
        mergeChangesIntoParent();
    }

    /**
     * INTERNAL:
     * Traverse the object to find references to objects not registered in this unit of work.
     */
    public void discoverUnregisteredNewObjects(IdentityHashtable clones, final IdentityHashtable knownNewObjects, final IdentityHashtable unregisteredExistingObjects, final IdentityHashtable visitedObjects) {
        // This define an inner class for process the itteration operation, don't be scared, its just an inner class.
        DescriptorIterator iterator = new DescriptorIterator() {
            public void iterate(Object object) {
                // If the object is read-only the do not continue the traversal.
                if (isClassReadOnly(object.getClass(), this.getCurrentDescriptor())) {
                    this.setShouldBreak(true);
                    return;
                }
                
                /* CR3440: Steven Vo
                 * Include the case that object is original then do nothing.
                 */
                if (isSmartMerge() && isOriginalNewObject(object)) {
                    return;
                } else if (!isObjectRegistered(object)) { // Don't need to check for aggregates, as iterator does not iterate on them by default.
                    if (checkForUnregisteredExistingObject(object)) { // Always ignore unregistered existing objects in JPA.
                        // If the object exists we need to keep a record of this object to ignore it,
                        // also need to stop iterating over it.
                        unregisteredExistingObjects.put(object, object);
                        this.setShouldBreak(true);
                        return;
                    } else {
                        // This means it is a unregistered new object, only persist it if cascading, otherwise throw error.
                        if ((getCurrentMapping() instanceof ForeignReferenceMapping) && ((ForeignReferenceMapping)getCurrentMapping()).isCascadePersist()) {
                            ((RepeatableWriteUnitOfWork)getSession()).registerNewObjectForPersist(object, visitedObjects);
                            knownNewObjects.put(object, object);
                        } else {
                            throw new IllegalStateException(ExceptionLocalization.buildMessage("new_object_found_during_commit", new Object[]{object}));
                        }
                    }
                }
            }
        };

        // Set the collection in the UnitofWork to be this list.
        setUnregisteredExistingObjects(unregisteredExistingObjects);

        iterator.setVisitedObjects(visitedObjects);
        iterator.setResult(knownNewObjects);
        iterator.setSession(this);
        // When using wrapper policy in EJB the iteration should stop on beans,
        // this is because EJB forces beans to be registered anyway and clone identity can be violated
        // and the violated clones references to session objects should not be traversed.
        iterator.setShouldIterateOverWrappedObjects(false);
        
        for (Enumeration clonesEnum = clones.keys(); clonesEnum.hasMoreElements(); ) {        
            iterator.startIterationOn(clonesEnum.nextElement());
        }
    }
    
    /**
     * INTERNAL:
     * Has writeChanges() been attempted on this UnitOfWork?  It may have
     * either suceeded or failed but either way the UnitOfWork is in a highly
     * restricted state.
     */
    public boolean isAfterWriteChangesButBeforeCommit() {
        //dont' check for writechanges failure.
        return (getLifecycle() == CommitTransactionPending);
    }

    /**
     * INTERNAL:
     * Return if the object has been deleted in this unit of work.
     */
    public boolean isObjectDeleted(Object object) {
        if(super.isObjectDeleted(object)) {
            return true;
        } else {
            if(unregisteredDeletedObjectsCloneToBackupAndOriginal != null) {
                if(unregisteredDeletedObjectsCloneToBackupAndOriginal.containsKey(object)) {
                    return true;
                }
            }
            if (hasObjectsDeletedDuringCommit()) {
                return getObjectsDeletedDuringCommit().containsKey(object);
            } else {
                return false;
            }
        }
    }

    /**
     * INTERNAL:
     * For synchronized units of work, dump SQL to database
     */
    public void issueSQLbeforeCompletion() {

        super.issueSQLbeforeCompletion(false);

        if (this.cumulativeUOWChangeSet != null && this.getUnitOfWorkChangeSet() != null){
            // unit of work has been committed so it's ok to set the cumulative into the UOW for merge
            this.cumulativeUOWChangeSet.mergeUnitOfWorkChangeSet((UnitOfWorkChangeSet)this.getUnitOfWorkChangeSet(), this, true);
            setUnitOfWorkChangeSet(this.cumulativeUOWChangeSet);
        }

        commitTransactionAfterWriteChanges(); // this method will commit the transaction
                                              // and set the transaction flags appropriately
    }
    
    /**
     * INTERNAL:
     * Merge the attributes of the clone into the unit of work copy.
     */
    public Object mergeCloneWithReferences(Object rmiClone, MergeManager manager) {
        Object mergedObject = super.mergeCloneWithReferences(rmiClone, manager);
        
        //iterate over new objects, assign sequences and put in the identitymap
        IdentityHashMap itable = manager.getMergedNewObjects();
        Iterator i = itable.values().iterator();
        while ( i.hasNext() ){
            Object newObjectClone = i.next();
            assignSequenceNumber(newObjectClone);
            registerNewObjectInIdentityMap(newObjectClone, null);
        }
        
        return mergedObject;
    }

    /**
     * INTERNAL:
     * This method is used internally to update the tracked objects if required
     */
    public void updateChangeTrackersIfRequired(Object objectToWrite, ObjectChangeSet changeSetToWrite, UnitOfWorkImpl uow, ClassDescriptor descriptor) {
        descriptor.getObjectChangePolicy().updateWithChanges(objectToWrite, changeSetToWrite, uow, descriptor);
    }

    /**
     * INTERNAL:
     * This method will cause the all of the tracked objects at this level to have
     * their changes written to the database.  It will then decrement the depth
     * level.
     */
    public void writeChanges() {
            if (unregisteredDeletedObjectsCloneToBackupAndOriginal == null) {
                unregisteredDeletedObjectsCloneToBackupAndOriginal = new IdentityHashtable(2);
            }
            if (getUnitOfWorkChangeSet() == null) {
                setUnitOfWorkChangeSet(new UnitOfWorkChangeSet());
            }
            // This also assigns sequence numbers and discover unregistered new objects.
            calculateChanges(getCloneMapping(), (UnitOfWorkChangeSet)getUnitOfWorkChangeSet(), true);
            // Write those changes to the database.
            UnitOfWorkChangeSet changeSet = (UnitOfWorkChangeSet)getUnitOfWorkChangeSet();
            if (!changeSet.hasChanges() && !changeSet.hasForcedChanges() && ! this.hasDeletedObjects() && ! this.hasModifyAllQueries()){
            	return;
            }
            try {
                commitToDatabaseWithPreBuiltChangeSet(changeSet, false);
                writesCompleted();
            } catch (RuntimeException exception) {
                setLifecycle(WriteChangesFailed);
                throw exception;
            }

            Enumeration enumtr = getNewObjectsCloneToOriginal().keys();
            while (enumtr.hasMoreElements()) {
                Object clone = enumtr.nextElement();
                Object original = getNewObjectsCloneToOriginal().get(clone);
                if (original != null) {
                    // No longer new to this unit of work, so need to store original.
                    getCloneToOriginals().put(clone, original);
                }
            }
            getNewObjectsCloneToOriginal().clear();
            getNewObjectsOriginalToClone().clear();
            
            // bug 4730595: fix puts deleted objects in the UnitOfWorkChangeSet as they are removed.
            getDeletedObjects().clear();
            // Unregister all deleted objects,
            // keep them along with their original and backup values in unregisteredDeletedObjectsCloneToBackupAndOriginal.
            Enumeration enumDeleted = getObjectsDeletedDuringCommit().keys();
            while (enumDeleted.hasMoreElements()) {
                Object deletedObject = enumDeleted.nextElement();
                Object[] backupAndOriginal = {getCloneMapping().get(deletedObject), getCloneToOriginals().get(deletedObject)};
                unregisteredDeletedObjectsCloneToBackupAndOriginal.put(deletedObject, backupAndOriginal);
                unregisterObject(deletedObject);
            }
            getObjectsDeletedDuringCommit().clear();

            if (this.cumulativeUOWChangeSet == null) {
                this.cumulativeUOWChangeSet = (UnitOfWorkChangeSet)getUnitOfWorkChangeSet();
            } else {
                // Merge those changes back into the backup clones and the final uowChangeSet.
                this.cumulativeUOWChangeSet.mergeUnitOfWorkChangeSet((UnitOfWorkChangeSet)getUnitOfWorkChangeSet(), this, true);
            }
            // Clean up, new objects are now existing.
            setUnitOfWorkChangeSet(new UnitOfWorkChangeSet());
        }

    /**
     * INTERNAL:
     * Called only by registerNewObjectForPersist method,
     * and only if newObject is not already registered.
     * If newObject is found in
     * unregisteredDeletedObjectsCloneToBackupAndOriginal then it's re-registered,
     * otherwise the superclass method called.
     */
    protected void registerNotRegisteredNewObjectForPersist(Object newObject, ClassDescriptor descriptor) {
        if(unregisteredDeletedObjectsCloneToBackupAndOriginal != null) {
            Object[] backupAndOriginal = (Object[])unregisteredDeletedObjectsCloneToBackupAndOriginal.remove(newObject);
            if(backupAndOriginal != null) {
                // backup
                getCloneMapping().put(newObject, backupAndOriginal[0]);
                // original
                registerNewObjectClone(newObject, backupAndOriginal[1]);

                // Check if the new objects should be cached.
                registerNewObjectInIdentityMap(newObject, newObject);
                
                return;
            }
        }
        super.registerNotRegisteredNewObjectForPersist(newObject, descriptor);
    }

    /**
     * INTERNAL:
     * This is internal to the uow, transactions should not be used explictly in a uow.
     * The uow shares its parents transactions.
     */
    public void rollbackTransaction() throws DatabaseException {
        if (this.shouldTerminateTransaction || getParent().getTransactionMutex().isNested()){
            super.rollbackTransaction();
        }else{
            //rollback called which means txn failed.
            //but rollback was stopped by entitytransaction which means the
            //transaction will want to call release later.  Make sure release
            //will rollback transaction.
            setWasTransactionBegunPrematurely(true);
        }
    }

    /**
     * INTERNAL
     * Synchronize the clones and update their backup copies.
     * Called after commit and commit and resume.
     */
    public void synchronizeAndResume() {
        if(this.shouldClearForCloseInsteadOfResume()) {
            this.clearForClose(false);
        } else {
            this.cumulativeUOWChangeSet = null;
            this.unregisteredDeletedObjectsCloneToBackupAndOriginal = null;
            super.synchronizeAndResume();
        }
    }
    
    /**
     * INTERNAL:
     * Called only by UnitOfWorkIdentityMapAccessor.getAndCloneCacheKeyFromParent method.
     * Return unregisteredDeletedClone corresponding to the passed original, or null
     */
    public Object getUnregisteredDeletedCloneForOriginal(Object original) {
        if(unregisteredDeletedObjectsCloneToBackupAndOriginal != null) {
            Enumeration keys = unregisteredDeletedObjectsCloneToBackupAndOriginal.keys();
            Enumeration values = unregisteredDeletedObjectsCloneToBackupAndOriginal.elements();
            while(keys.hasMoreElements()) {
                Object deletedObjectClone = keys.nextElement();
                Object[] backupAndOriginal = (Object[])values.nextElement();
                Object currentOriginal = backupAndOriginal[1];
                if(original == currentOriginal) {
                    return deletedObjectClone;
                }
            }
        }
        return null;
    }
    
  /**
   * INTERNAL:
   * Wraps the org.eclipse.persistence.exceptions.OptimisticLockException in a  
   * javax.persistence.OptimisticLockException. This conforms to the EJB3 specs
   * @param commitTransaction 
   */
    protected void commitToDatabase(boolean commitTransaction) {
        try {
            super.commitToDatabase(commitTransaction);
        } catch (org.eclipse.persistence.exceptions.OptimisticLockException ole) {
            throw new javax.persistence.OptimisticLockException(ole);
        }
    }

    /**
     * INTERNAL:
     * This is internal to the uow, transactions should not be used explictly in a uow.
     * The uow shares its parents transactions.
     */
    public void commitTransaction() throws DatabaseException {
        if (this.shouldTerminateTransaction || getParent().getTransactionMutex().isNested()){
            super.commitTransaction();
        }
    }

    public void setShouldTerminateTransaction(boolean shouldTerminateTransaction) {
        this.shouldTerminateTransaction = shouldTerminateTransaction;
    }
    
    public void setShouldClearForCloseInsteadOfResume(boolean shouldClearForCloseInsteadOfResume) {
        this.shouldClearForCloseInsteadOfResume = shouldClearForCloseInsteadOfResume;
    }

    public boolean shouldClearForCloseInsteadOfResume() {
        return shouldClearForCloseInsteadOfResume;
    }
}