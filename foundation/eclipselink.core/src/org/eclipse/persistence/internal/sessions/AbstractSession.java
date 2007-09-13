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

import java.util.*;
import java.io.*;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorQueryManager;
import org.eclipse.persistence.internal.helper.*;
import org.eclipse.persistence.internal.helper.linkedlist.LinkedNode;
import org.eclipse.persistence.internal.helper.linkedlist.ExposedNodeLinkedList;
import org.eclipse.persistence.internal.indirection.ProxyIndirectionPolicy;
import org.eclipse.persistence.platform.database.DatabasePlatform;
import org.eclipse.persistence.platform.server.ServerPlatform;
import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.expressions.*;
import org.eclipse.persistence.history.*;
import org.eclipse.persistence.internal.identitymaps.*;
import org.eclipse.persistence.internal.history.*;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.databaseaccess.Platform;
import org.eclipse.persistence.internal.descriptors.*;
import org.eclipse.persistence.exceptions.*;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.ObjectCopyingPolicy;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.sessions.SessionEventManager;
import org.eclipse.persistence.sessions.ExternalTransactionController;
import org.eclipse.persistence.sessions.Login;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.eclipse.persistence.logging.DefaultSessionLog;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.remote.CacheSynchronizationManager;
import org.eclipse.persistence.internal.sequencing.Sequencing;
import org.eclipse.persistence.sessions.coordination.CommandProcessor;
import org.eclipse.persistence.sessions.coordination.CommandManager;
import org.eclipse.persistence.sessions.coordination.Command;

/**
 * Implementation of org.eclipse.persistence.sessions.Session
 * The public interface should be used.
 * @see org.eclipse.persistence.sessions.Session
 *
 * <p>
 * <b>Purpose</b>: Define the interface and common protocol of a TopLink compliant session.
 * <p>
 * <b>Description</b>: The session is the primary interface into TopLink,
 * the application should do all of its reading and writing of objects through the session.
 * The session also manages transactions and units of work.  Normally the session
 * is passed and used by the application controler objects.  Controler objects normally
 * sit behind the GUI and perform the buiness processes required for the application,
 * they should perform all explict database access and database access should be avoided from
 * the domain object model.  Do not use a globally accessable session instance, doing so does
 * not allow for multiple sessions.  Multiple sessions may required when performing things like
 * data migration or multiple database access, as well the unit of work feature requires the usage
 * of multiple session instances.  Although session is abstract, any users of its subclasses
 * should only cast the variables to Session to allow usage of any of its subclasses.
 * <p>
 * <b>Responsibilities</b>:
 *    <ul>
 *    <li> Connecting/disconnecting.
 *    <li> Reading and writing objects.
 *    <li> Transaction and unit of work support.
 *    <li> Identity maps and caching.
 *    </ul>
 * @see DatabaseSessionImpl
 */
public abstract class AbstractSession implements org.eclipse.persistence.sessions.Session, CommandProcessor, java.io.Serializable, java.lang.Cloneable {
    /** ExceptionHandler handles database exceptions. */
    transient protected ExceptionHandler exceptionHandler;

    /** IntegrityChecker catch all the descriptor Exceptions.  */
    transient protected IntegrityChecker integrityChecker;

    /** The project stores configuration information, such as the descriptors and login. */
    transient protected Project project;

    /** Ensure mutual exclusion of the session's transaction state across multiple threads.*/
    transient protected ConcurrencyManager transactionMutex;

    /** Manages the live object cache.*/
    protected IdentityMapAccessor identityMapAccessor;

    /** If Transactions were externally started */
    protected boolean wasJTSTransactionInternallyStarted;

    /** The connection to the data store. */
    transient protected Accessor accessor;

    /** Allow the datasource platform to be cached. */
    transient protected Platform platform;

    /** Stores predefine reusable queries.*/
    transient protected Map queries;
    
    /** Stores predefined not yet parsed EJBQL queries.*/
    transient protected List ejbqlPlaceHolderQueries;

    /** Resolves referencial integrity on commits. */
    transient protected CommitManager commitManager;

    /** Tool that log performance information. */
    transient protected SessionProfiler profiler;

    /** Support being owned by a session broker. */
    transient protected AbstractSession broker;

    /** Used to identify a session when using the session broker. */
    protected String name;

    /** Keep track of active units of work. */
    transient protected int numberOfActiveUnitsOfWork;

    /** Destination for logged messages and SQL. */
    transient protected SessionLog sessionLog;

    /** When logging the name of the session is typed: class name + system hashcode. */
    transient protected String logSessionString;

    /** Stores the event listeners for this session. */
    transient protected SessionEventManager eventManager;

    /** Allow for user defined properties. */
    protected Map properties;

    /** Delegate that handles synchronizing a UnitOfWork with an external transaction. */
    transient protected ExternalTransactionController externalTransactionController;

    /** Last descriptor accessed, use to optimize descriptor lookup. */
    transient protected ClassDescriptor lastDescriptorAccessed;

    // bug 3078039: move EJBQL alias > descriptor map from Session to Project (MWN)

    /** This attribute is used to provide all required information for distributed cache synchronisation */
    transient protected CacheSynchronizationManager cacheSynchronizationManager;

    /** Used to determine If a session is in a Broker or not */
    protected boolean isInBroker;
    protected boolean usesOldCommit;

    /**
     * Used to connect this session to TopLink cluster for distributed command
     */
    transient protected CommandManager commandManager;

    /**
     * Determined whether changes should be propagated to TopLink cluster
     */
    protected boolean shouldPropagateChanges;

    /** Used to determine If a session is in a profile or not */
    protected boolean isInProfile;
    
    /** PERF: Allow for finalizers to be enabled, currently enables client-session finalize. */
    protected boolean isFinalizersEnabled = false;
    
    /** List of active command threads. */
    protected ExposedNodeLinkedList activeCommandThreads;

    /**
     * INTERNAL:
     * Create and return a new session.
     * This should only be called if the database login information is not know at the time of creation.
     * Normally it is better to call the constructor that takes the login information as an argument
     * so that the session can initialize itself to the platform information given in the login.
     */
    protected AbstractSession() {
        this.name = "";
        initializeIdentityMapAccessor();
        // PERF - move to lazy init (3286091)
        this.numberOfActiveUnitsOfWork = 0;
        this.isInBroker = false;
        this.usesOldCommit = false;
    }

    /**
     * INTERNAL:
     * Create a blank session, used for proxy session.
     */
    protected AbstractSession(int nothing) {
    }

    /**
     * PUBLIC:
     * Create and return a new session.
     * By giving the login information on creation this allows the session to initialize itself
     * to the platform given in the login. This constructor does not return a connected session.
     * To connect the session to the database login() must be sent to it. The login(userName, password)
     * method may also be used to connect the session, this allows for the user name and password
     * to be given at login but for the other database information to be provided when the session is created.
     */
    public AbstractSession(Login login) {
        this(new org.eclipse.persistence.sessions.Project(login));
    }

    /**
     * PUBLIC:
     * Create and return a new session.
     * This constructor does not return a connected session.
     * To connect the session to the database login() must be sent to it. The login(userName, password)
     * method may also be used to connect the session, this allows for the user name and password
     * to be given at login but for the other database information to be provided when the session is created.
     */
    public AbstractSession(org.eclipse.persistence.sessions.Project project) {
        this();
        this.project = project;
        if (project.getDatasourceLogin() == null) {
            throw ValidationException.projectLoginIsNull(this);
        }
    }

    /**
     * INTERNAL:
     * Called by a sessions queries to obtain individual query ids.
     * CR #2698903
     */
    public long getNextQueryId() {
        return QueryCounter.getCount();
    }

    /**
     * INTERNAL:
     * Return a unit of work for this session not registered with the JTS transaction.
     */
    public UnitOfWorkImpl acquireNonSynchronizedUnitOfWork() {
        setNumberOfActiveUnitsOfWork(getNumberOfActiveUnitsOfWork() + 1);
        UnitOfWorkImpl unitOfWork = new UnitOfWorkImpl(this);
        unitOfWork.setUseOldCommit(this.usesOldCommit());
        if (shouldLog(SessionLog.FINER, SessionLog.TRANSACTION)) {
            log(SessionLog.FINER, SessionLog.TRANSACTION, "acquire_unit_of_work_with_argument", String.valueOf(System.identityHashCode(unitOfWork)));
        }
        return unitOfWork;
    }

    /**
     * INTERNAL:
     * Constructs a HistoricalSession given a valid AsOfClause.
     */
    public org.eclipse.persistence.sessions.Session acquireHistoricalSession(AsOfClause clause) throws ValidationException {
        if ((clause == null) || (clause.getValue() == null)) {
            throw ValidationException.cannotAcquireHistoricalSession();
        }
        if (!getProject().hasGenericHistorySupport() && !hasBroker() && ((getPlatform() == null) || !getPlatform().isOracle())) {
            throw ValidationException.historicalSessionOnlySupportedOnOracle();
        }
        return new HistoricalSession(this, clause);
    }

    /**
     * PUBLIC:
     * Return a unit of work for this session.
     * The unit of work is an object level transaction that allows
     * a group of changes to be applied as a unit.
     *
     * @see UnitOfWorkImpl
     */
    public UnitOfWorkImpl acquireUnitOfWork() {
        UnitOfWorkImpl unitOfWork = acquireNonSynchronizedUnitOfWork();
        unitOfWork.registerWithTransactionIfRequired();

        return unitOfWork;
    }

    /**
     * PUBLIC:
     * Add an alias for the descriptor
     */
    public void addAlias(String alias, ClassDescriptor descriptor) {
        project.addAlias(alias, descriptor);
    }

    /**
     * PUBLIC:
     * Add the query to the session queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public void addQuery(String name, DatabaseQuery query) {
        query.setName(name);
        addQuery(query);
    }

    /**
     * INTERNAL:
     * Return all pre-defined not yet parsed EJBQL queries.
     * @see #getAllQueries()
     */
    public void addEjbqlPlaceHolderQuery(DatabaseQuery query) {
        getEjbqlPlaceHolderQueries().add(query);
    }

    /**
     * INTERNAL:
     * Add the query to the session queries.
     */
    protected void addQuery(DatabaseQuery query) {
        Vector queriesByName = (Vector)getQueries().get(query.getName());
        if (queriesByName == null) {
            // lazily create Vector in Hashtable.
            queriesByName = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
            getQueries().put(query.getName(), queriesByName);
        }

        // Check that we do not already have a query that matched it
        for (Enumeration enumtr = queriesByName.elements(); enumtr.hasMoreElements();) {
            DatabaseQuery existingQuery = (DatabaseQuery)enumtr.nextElement();
            if (Helper.areTypesAssignable(query.getArgumentTypes(), existingQuery.getArgumentTypes())) {
                throw ValidationException.existingQueryTypeConflict(query, existingQuery);
            }
        }
        queriesByName.add(query);
    }

    /**
     * INTERNAL:
     * Called by beginTransaction() to start a transaction.
     * This starts a real database transaction.
     */
    protected void basicBeginTransaction() throws DatabaseException {
        try {
            getAccessor().beginTransaction(this);
        } catch (RuntimeException exception) {
            handleException(exception);
        }
    }

    /**
     * INTERNAL:
     * Called after transaction is completed (committed or rolled back)
     */
    public void afterTransaction(boolean committed, boolean isExternalTransaction) {
    }


    
    /**
     * INTERNAL:
     * Allow calling session to be passed.     
     * 
     * The calling session is the session who actually invokes commit or rollback transaction, 
     * it is used to determine whether the connection needs to be closed when using external connection pool.
     * The connection with a externalConnectionPool used by synchronized UOW should leave open until 
     * afterCompletion call back; the connection with a externalConnectionPool used by other type of session 
     * should be closed after transaction was finised.
     * 
     * Called by commitTransaction() to commit a transaction.
     * This commits the active transaction.
     */
    protected void basicCommitTransaction(AbstractSession callingSession) throws DatabaseException {
        try {
            getAccessor().commitTransaction(this,callingSession);
        } catch (RuntimeException exception) {
            handleException(exception);
        }
    }

    
    /**
     * INTERNAL:
     * Allow calling session to be passed.     
     * 
     * The calling session is the session who actually invokes commit or rollback transaction, 
     * it is used to determine whether the connection needs to be closed when using external connection pool.
     * The connection with a externalConnectionPool used by synchronized UOW should leave open until 
     * afterCompletion call back; the connection with a externalConnectionPool used by other type of session 
     * should be closed after transaction was finised.
     * 
     * Called by rollbackTransaction() to rollback a transaction.
     * This rollsback the active transaction.
     */
    protected void basicRollbackTransaction(AbstractSession callingSession) throws DatabaseException {
        try {
            getAccessor().rollbackTransaction(this,callingSession);
        } catch (RuntimeException exception) {
            handleException(exception);
        }
    }

    /**
     * INTERNAL:
     * Attempts to begin an external transaction.
     * Returns true only in one case -
     * extenal transaction has been internally started during this method call:
     * wasJTSTransactionInternallyStarted()==false in the beginning of this method and
     * wasJTSTransactionInternallyStarted()==true in the end of this method.
     */
    public boolean beginExternalTransaction() {
        boolean externalTransactionHasBegun = false;
        if (hasExternalTransactionController() && !wasJTSTransactionInternallyStarted()) {
            try {
                getExternalTransactionController().beginTransaction(this);
            } catch (RuntimeException exception) {
                handleException(exception);
            }
            if (wasJTSTransactionInternallyStarted()) {
                externalTransactionHasBegun = true;
                log(SessionLog.FINER, SessionLog.TRANSACTION, "external_transaction_has_begun_internally");
            }
        }
        return externalTransactionHasBegun;
    }

    /**
     * PUBLIC:
     * Begin a transaction on the database.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException if the database connection is lost or the begin is rejected.
     * @exception ConcurrencyException if this session's transaction is aquired by another thread and a timeout occurs.
     *
     * @see #isInTransaction()
     */
    public void beginTransaction() throws DatabaseException, ConcurrencyException {
        // If there is no db transaction in progress
        // beginExternalTransaction() starts an external transaction -
        // provided externalTransactionController is used, and there is
        // no active external transaction - so we have to start one internally.
        if (!isInTransaction()) {
            beginExternalTransaction();
        }

        // For unit of work and client session multi threading is allowed as they are a context,
        // this is required for JTS/RMI/CORBA/EJB stuff where the server thread can be different across calls.
        if (isClientSession()) {
            getTransactionMutex().setActiveThread(Thread.currentThread());
        }

        // Ensure mutual exclusion and call subclass specific begin.
        getTransactionMutex().acquire();
        if (!getTransactionMutex().isNested()) {
            getEventManager().preBeginTransaction();
            basicBeginTransaction();
            getEventManager().postBeginTransaction();
        }
    }

    /**
     * PUBLIC:
     * clear the integrityChecker. IntegrityChecker holds all the ClassDescriptor Exceptions.
     */
    public void clearIntegrityChecker() {
        setIntegrityChecker(null);
    }

    /**
     * INTERNAL:
     * clear the lastDescriptorAccessed.
     */
    public void clearLastDescriptorAccessed() {
        lastDescriptorAccessed = null;
    }

    /**
     * PUBLIC:
     * Clear the profiler, this will end the current profile opperation.
     */
    public void clearProfile() {
        setProfiler(null);
    }

    /**
     * INTERNAL:
     * Clones the descriptor
     */
    public Object clone() {
        // An alternative to this process should be found
        try {
            return super.clone();
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Attempts to commit the running internally started external transaction.
     * Returns true only in one case -
     * extenal transaction has been internally committed during this method call:
     * wasJTSTransactionInternallyStarted()==true in the beginning of this method and
     * wasJTSTransactionInternallyStarted()==false in the end of this method.
     */
    public boolean commitExternalTransaction() {
        boolean externalTransactionHasCommitted = false;
        if (hasExternalTransactionController() && wasJTSTransactionInternallyStarted()) {
            try {
                getExternalTransactionController().commitTransaction(this);
            } catch (RuntimeException exception) {
                handleException(exception);
            }
            if (!wasJTSTransactionInternallyStarted()) {
                externalTransactionHasCommitted = true;
                log(SessionLog.FINER, SessionLog.TRANSACTION, "external_transaction_has_committed_internally");
            }
        }
        return externalTransactionHasCommitted;
    }

    /**
     * PUBLIC:
     * Commit the active database transaction.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException most databases validate changes as they are done,
     * normally errors do not occur on commit unless the disk fails or the connection is lost.
     * @exception ConcurrencyException if this session is not within a transaction.
     */
    public void commitTransaction() throws DatabaseException, ConcurrencyException {
        this.commitTransaction(this);
    }
    /**
     * PUBLIC:
     * Allow calling session to be passed.     
     * 
     * The calling session is the session who actually invokes commit or rollback transaction, 
     * it is used to determine whether the connection needs to be closed when using external connection pool.
     * The connection with a externalConnectionPool used by synchronized UOW should leave open until 
     * afterCompletion call back; the connection with a externalConnectionPool used by other type of session 
     * should be closed after transaction was finised.
     * 
     * Commit the active database transaction.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException most databases validate changes as they are done,
     * normally errors do not occur on commit unless the disk fails or the connection is lost.
     * @exception ConcurrencyException if this session is not within a transaction.
     */
    public void commitTransaction(AbstractSession callingSession) throws DatabaseException, ConcurrencyException {
        // Release mutex and call subclass specific commit.
        if (!getTransactionMutex().isNested()) {
            getEventManager().preCommitTransaction();
            basicCommitTransaction(callingSession);
            getEventManager().postCommitTransaction();
        }

        // This MUST not be in a try catch or finally as if the commit failed the transaction is still open.
        getTransactionMutex().release();

        // If there is no db transaction in progress
        // if there is an active external transaction 
        // which was started internally - it should be committed internally, too.
        if (!isInTransaction()) {
            commitExternalTransaction();
        }
    }
    
    
    /**
     * INTERNAL:
     * Return if the two object match completely.
     * This checks the objects attributes and their private parts.
     */
    public boolean compareObjects(Object firstObject, Object secondObject) {
        if ((firstObject == null) && (secondObject == null)) {
            return true;
        }

        if ((firstObject == null) || (secondObject == null)) {
            return false;
        }

        if (!(firstObject.getClass().equals(secondObject.getClass()))) {
            return false;
        }

        ObjectBuilder builder = getDescriptor(firstObject.getClass()).getObjectBuilder();

        return builder.compareObjects(builder.unwrapObject(firstObject, this), builder.unwrapObject(secondObject, this), this);
    }

    /**
     * TESTING:
     * Return true if the object do not match.
     * This checks the objects attributes and their private parts.
     */
    public boolean compareObjectsDontMatch(Object firstObject, Object secondObject) {
        return !this.compareObjects(firstObject, secondObject);
    }

    /**
     * ADVANCED:
     * Return if their is an object for the primary key.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public boolean containsObjectInIdentityMap(Object domainObject) {
        return getIdentityMapAccessorInstance().containsObjectInIdentityMap(domainObject);
    }

    /**
     * ADVANCED:
     * Return if their is an object for the primary key.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public boolean containsObjectInIdentityMap(Vector primaryKey, Class theClass) {
        return getIdentityMapAccessorInstance().containsObjectInIdentityMap(primaryKey, theClass);
    }

    /**
     * PUBLIC:
     * Return true if the pre-defined query is defined on the session.
     */
    public boolean containsQuery(String queryName) {
        return getQueries().containsKey(queryName);
    }

    /**
     * PUBLIC:
     * Return a complete copy of the object.
     * This can be used to obtain a scatch copy of an object,
     * or for templatizing an existing object into another new object.
     * The object and all of its privately owned parts will be copied, the object's primary key will be reset to null.
     *
     * @see #copyObject(Object, ObjectCopyingPolicy)
     */
    public Object copyObject(Object original) {
        return copyObject(original, new ObjectCopyingPolicy());
    }

    /**
     * PUBLIC:
     * Return a complete copy of the object.
     * This can be used to obtain a scatch copy of an object,
     * or for templatizing an existing object into another new object.
     * The object copying policy allow for the depth, and reseting of the primary key to null, to be specified.
     */
    public Object copyObject(Object original, ObjectCopyingPolicy policy) {
        if (original == null) {
            return null;
        }

        ClassDescriptor descriptor = getDescriptor(original);
        if (descriptor == null) {
            return original;
        }

        policy.setSession(this);
        return descriptor.getObjectBuilder().copyObject(original, policy);
    }

    /**
     * INTERNAL:
     * Copy the read only classes from the unit of work
     *
     * Added Nov 8, 2000 JED for Patch 2.5.1.8
     * Ref: Prs 24502
     */
    public Vector copyReadOnlyClasses() {
        return getDefaultReadOnlyClasses();
    }

    /**
     * PUBLIC:
     * delete all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be deleted as a unit.
     * The objects will be deleted through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void deleteAllObjects(Collection domainObjects) throws DatabaseException, OptimisticLockException {
        for (Iterator objectsEnum = domainObjects.iterator(); objectsEnum.hasNext();) {
            deleteObject(objectsEnum.next());
        }
    }

    /**
     * PUBLIC:
     * delete all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be deleted as a unit.
     * The objects will be deleted through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void deleteAllObjects(Vector domainObjects) throws DatabaseException, OptimisticLockException {
        for (Enumeration objectsEnum = domainObjects.elements(); objectsEnum.hasMoreElements();) {
            deleteObject(objectsEnum.nextElement());
        }
    }

    /**
     * PUBLIC:
     * Delete the object and all of its privately owned parts from the database.
     * The delete operation can be customized through using a delete query.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * An database error is not raised if the object is already deleted or no rows are effected.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     *
     * @see DeleteObjectQuery
     */
    public Object deleteObject(Object domainObject) throws DatabaseException, OptimisticLockException {
        DeleteObjectQuery query = new DeleteObjectQuery();
        query.setObject(domainObject);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * PUBLIC:
     * Return if the object exists on the database or not.
     * This always checks existence on the database.
     */
    public boolean doesObjectExist(Object object) throws DatabaseException {
        DoesExistQuery query = new DoesExistQuery();
        query.setObject(object);
        query.checkDatabaseForDoesExist();
        query.setIsExecutionClone(true);
        return ((Boolean)executeQuery(query)).booleanValue();
    }

    /**
     * PUBLIC:
     * Turn off logging
     */
    public void dontLogMessages() {
        setLogLevel(SessionLog.OFF);
    }

    /**
     * INTERNAL:
     * End the operation timing.
     */
    public void endOperationProfile(String operationName) {
        if (isInProfile()) {
            getProfiler().endOperationProfile(operationName);
        }
    }

    /**
     * INTERNAL:
     * End the operation timing.
     */
    public void endOperationProfile(String operationName, DatabaseQuery query, int weight) {
        if (isInProfile()) {
            getProfiler().endOperationProfile(operationName, query, weight);
        }
    }

    /**
     * INTERNAL:
     * Updates the value of SessionProfiler state
     */
    public void updateProfile(String operationName, Object value) {
        if (isInProfile()) {
            getProfiler().update(operationName, value);
        }
    }

    /**
     * INTERNAL:
     * Updates the count of SessionProfiler event
     */
    public void incrementProfile(String operationName) {
        if (isInProfile()) {
            getProfiler().occurred(operationName);
        }
    }

    /**
     * INTERNAL:
     * Overridden by subclasses that do more than just execute the call.
     * Executes the call directly on this session and does not check which
     * session it should have executed on.
     */
    public Object executeCall(Call call, AbstractRecord translationRow, DatabaseQuery query) throws DatabaseException {
        //** sequencing refactoring
        if (query.getAccessor() == null) {
            query.setAccessor(getAccessor());
        }
        try {
            return query.getAccessor().executeCall(call, translationRow, this);
        } finally {
            if (call.isFinished()) {
                query.setAccessor(null);
            }
        }
    }

    /**
     * PUBLIC:
     * Execute the call on the database.
     * The row count is returned.
     * The call can be a stored procedure call, SQL call or other type of call.
     * <p>Example:
     * <p>session.executeNonSelectingCall(new SQLCall("Delete from Employee");
     *
     * @see #executeSelectingCall(Call)
     */
    public int executeNonSelectingCall(Call call) throws DatabaseException {
        DataModifyQuery query = new DataModifyQuery();
        query.setIsExecutionClone(true);
        query.setCall(call);
        Integer value = (Integer)executeQuery(query);
        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
    }

    /**
     * PUBLIC:
     * Execute the sql on the database.
     * <p>Example:
     * <p>session.executeNonSelectingSQL("Delete from Employee");
     * @see #executeNonSelectingCall(Call)
	 * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     */
    public void executeNonSelectingSQL(String sqlString) throws DatabaseException {
        executeNonSelectingCall(new SQLCall(sqlString));
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName) throws DatabaseException {
        DatabaseQuery query = getQuery(queryName);

        if (query == null) {
            throw QueryException.queryNotDefined(queryName);
        }

        return executeQuery(query);
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass) throws DatabaseException {
        ClassDescriptor descriptor = getDescriptor(domainClass);

        if (descriptor == null) {
            throw QueryException.descriptorIsMissingForNamedQuery(domainClass, queryName);
        }

        DatabaseQuery query = (DatabaseQuery)descriptor.getQueryManager().getQuery(queryName);

        if (query == null) {
            throw QueryException.queryNotDefined(queryName, domainClass);
        }

        return executeQuery(query);
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Object arg1) throws DatabaseException {
        Vector argumentValues = new Vector();
        argumentValues.addElement(arg1);
        return executeQuery(queryName, domainClass, argumentValues);
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2) throws DatabaseException {
        Vector argumentValues = new Vector();
        argumentValues.addElement(arg1);
        argumentValues.addElement(arg2);
        return executeQuery(queryName, domainClass, argumentValues);
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2, Object arg3) throws DatabaseException {
        Vector argumentValues = new Vector();
        argumentValues.addElement(arg1);
        argumentValues.addElement(arg2);
        argumentValues.addElement(arg3);
        return executeQuery(queryName, domainClass, argumentValues);
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Vector argumentValues) throws DatabaseException {
        ClassDescriptor descriptor = getDescriptor(domainClass);

        if (descriptor == null) {
            throw QueryException.descriptorIsMissingForNamedQuery(domainClass, queryName);
        }

        DatabaseQuery query = (DatabaseQuery)descriptor.getQueryManager().getQuery(queryName, argumentValues);

        if (query == null) {
            throw QueryException.queryNotDefined(queryName, domainClass);
        }

        return executeQuery(query, argumentValues);
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Object arg1) throws DatabaseException {
        Vector argumentValues = new Vector();
        argumentValues.addElement(arg1);
        return executeQuery(queryName, argumentValues);
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Object arg1, Object arg2) throws DatabaseException {
        Vector argumentValues = new Vector();
        argumentValues.addElement(arg1);
        argumentValues.addElement(arg2);
        return executeQuery(queryName, argumentValues);
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Object arg1, Object arg2, Object arg3) throws DatabaseException {
        Vector argumentValues = new Vector();
        argumentValues.addElement(arg1);
        argumentValues.addElement(arg2);
        argumentValues.addElement(arg3);
        return executeQuery(queryName, argumentValues);
    }

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Vector argumentValues) throws DatabaseException {
        DatabaseQuery query = getQuery(queryName, argumentValues);

        if (query == null) {
            throw QueryException.queryNotDefined(queryName);
        }

        return executeQuery(query, argumentValues);
    }

    /**
     * PUBLIC:
     * Execute the database query.
     * A query is a database operation such as reading or writting.
     * The query allows for the operation to be customized for such things as,
     * performance, depth, caching, etc.
     *
     * @see DatabaseQuery
     */
    public Object executeQuery(DatabaseQuery query) throws DatabaseException {
        return executeQuery(query, EmptyRecord.getEmptyRecord());
    }

    /**
     * PUBLIC:
     * Return the results from exeucting the database query.
     * the arguments are passed in as a vector
     */
    public Object executeQuery(DatabaseQuery query, Vector argumentValues) throws DatabaseException {
        if (query == null) {
            throw QueryException.queryNotDefined();
        }

        AbstractRecord row = query.rowFromArguments(argumentValues);

        return executeQuery(query, row);
    }

    /**
     * INTERNAL:
     * Return the results from exeucting the database query.
     * the arguments should be a database row with raw data values.
     */
    public Object executeQuery(DatabaseQuery query, AbstractRecord row) throws DatabaseException {
        if (hasBroker()) {
            if (!((query.isDataModifyQuery() || query.isDataReadQuery()) && (query.getSessionName() == null))) {
                return getBroker().executeQuery(query, row);
            }
        }

        if (query == null) {
            throw QueryException.queryNotDefined();
        }

        //CR#2272
        log(SessionLog.FINEST, SessionLog.QUERY, "execute_query", query);

        try {
            getEventManager().preExecuteQuery(query);
            Object result;
            if (isInProfile()) {
                result = getProfiler().profileExecutionOfQuery(query, row, this);
            } else {
                result = internalExecuteQuery(query, row);
            }
            getEventManager().postExecuteQuery(query, result);
            return result;
        } catch (RuntimeException exception) {
            if (exception instanceof QueryException) {
                QueryException queryException = (QueryException)exception;
                if (queryException.getQuery() == null) {
                    queryException.setQuery(query);
                }
                if (queryException.getQueryArguments() == null) {
                    queryException.setQueryArguments(row);
                }
                if (queryException.getSession() == null) {
                    queryException.setSession(this);
                }
            } else if (exception instanceof DatabaseException) {
                DatabaseException databaseException = (DatabaseException)exception;
                if (databaseException.getQuery() == null) {
                    databaseException.setQuery(query);
                }
                if (databaseException.getQueryArgumentsRecord() == null) {
                    databaseException.setQueryArguments(row);
                }
                if (databaseException.getSession() == null) {
                    databaseException.setSession(this);
                }
            }
            return handleException(exception);
        }
    }

    /**
     * PUBLIC:
     * Execute the call on the database and return the result.
     * The call must return a value, if no value is return executeNonSelectCall must be used.
     * The call can be a stored procedure call, SQL call or other type of call.
     * A vector of database rows is returned, database row implements Java 2 Map which should be used to access the data.
     * <p>Example:
     * <p>session.executeSelectingCall(new SQLCall("Select * from Employee");
     *
     * @see #executeNonSelectingCall(Call)
     */
    public Vector executeSelectingCall(Call call) throws DatabaseException {
        DataReadQuery query = new DataReadQuery();
        query.setCall(call);
        query.setIsExecutionClone(true);
        return (Vector)executeQuery(query);
    }

    /**
     * PUBLIC:
     * Execute the sql on the database and return the result.
     * It must return a value, if no value is return executeNonSelectingSQL must be used.
     * A vector of database rows is returned, database row implements Java 2 Map which should be used to access the data.
 	 * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     * <p>Example:
     * <p>session.executeSelectingCall("Select * from Employee");
     *
     * @see #executeSelectingCall(Call)
     */
    public Vector executeSQL(String sqlString) throws DatabaseException {
        return executeSelectingCall(new SQLCall(sqlString));
    }

    /**
     * INTERNAL:
     * Return the lowlevel database accessor.
     * The database accesor is used for direct database access.
     */
    public Accessor getAccessor() {
        if ((accessor == null) && (project != null) && (project.getDatasourceLogin() != null)) {
            synchronized (this) {
                if ((accessor == null) && (project != null) && (project.getDatasourceLogin() != null)) {
                    // PERF: lazy init, not always required.
                    accessor = project.getDatasourceLogin().buildAccessor();
                }
            }
        }
        return accessor;
    }

    /**
     * INTERNAL:
     * Return the lowlevel database accessor.
     * The database accesor is used for direct database access.
     * If sessionBroker is used, the right accessor for this
     * broker will be returned.
     */
    public Accessor getAccessor(Class domainClass) {
        return getAccessor();
    }

    /**
     * INTERNAL:
     * Return the lowlevel database accessor.
     * The database accesor is used for direct database access.
     * If sessionBroker is used, the right accessor for this
     * broker will be returned based on the session name.
     */
    public Accessor getAccessor(String sessionName) {
        return getAccessor();
    }

    /**
     * INTERNAL:
     */
    public ExposedNodeLinkedList getActiveCommandThreads() {
        if (activeCommandThreads == null) {
            activeCommandThreads = new ExposedNodeLinkedList();
        }
        
        return activeCommandThreads;
    }
    
    /**
     * PUBLIC:
     * Return the active session for the current active external (JTS) transaction.
     * This should only be used with JTS and will return the session if no external transaction exists.
     */
    public org.eclipse.persistence.sessions.Session getActiveSession() {
        org.eclipse.persistence.sessions.Session activeSession = getActiveUnitOfWork();
        if (activeSession == null) {
            activeSession = this;
        }

        return activeSession;
    }

    /**
     * PUBLIC:
     * Return the active unit of work for the current active external (JTS) transaction.
     * This should only be used with JTS and will return null if no external transaction exists.
     */
    public org.eclipse.persistence.sessions.UnitOfWork getActiveUnitOfWork() {
        if (hasExternalTransactionController()) {
            return getExternalTransactionController().getActiveUnitOfWork();
        }

        /* Steven Vo:  CR# 2517
           Get from the server session since the external transaction controller could be
           null out from the client session by TL WebLogic 5.1 to provide non-jts transaction
           operations
          */
        if (isClientSession()) {
            return ((org.eclipse.persistence.sessions.server.ClientSession)this).getParent().getActiveUnitOfWork();
        }

        return null;
    }

    /**
     * INTERNAL:
     * Returns the alias descriptors hashtable.
     */
    public Map getAliasDescriptors() {
        return project.getAliasDescriptors();
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the expression is too complex an exception will be thrown.
     * @deprecated
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow) throws QueryException {
        return getAllFromIdentityMap(selectionCriteria, theClass, translationRow, new InMemoryQueryIndirectionPolicy());
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the expression is too complex an exception will be thrown.
     * Only return objects that are invalid in the cache if specified.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean shouldReturnInvalidatedObjects) throws QueryException {
        return getIdentityMapAccessorInstance().getAllFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, shouldReturnInvalidatedObjects);
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the expression is too complex an exception will be thrown.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy) throws QueryException {
        return getIdentityMapAccessorInstance().getAllFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy);
    }

    /**
     * ADVANCED:
     * Answers the past time this session is as of.  Indicates whether or not this
     * is a special historical session where all objects are read relative to a
     * particular point in time.
     * @return An immutable object representation of the past time.
     * <code>null</code> if no clause set, or this a regular session.
     * @see #acquireHistoricalSession(org.eclipse.persistence.history.AsOfClause)
     */
    public AsOfClause getAsOfClause() {
        return null;
    }

    /**
     * INTERNAL:
     * Allow the session to be used from a session broker.
     */
    public AbstractSession getBroker() {
        return broker;
    }

    /**
     * INTERNAL:
     * The session that this query is executed against when not in transaction.
     * The session containing the shared identity map.
     * <p>
     * In most cases this is the root ServerSession or DatabaseSession.
     * <p>
     * In cases where objects are not to be cached in the global identity map
     * an alternate session may be returned:
     * <ul>
     * <li>A ClientSession if in transaction
     * <li>An isolated ClientSession or HistoricalSession
     * <li>A registered session of a root SessionBroker
     * </ul>
     */
    public AbstractSession getRootSession(DatabaseQuery query) {
        return getParentIdentityMapSession(query, false, true);
    }

    /**
     * INTERNAL:
     * Gets the next link in the chain of sessions followed by a query's check
     * early return, the chain of sessions with identity maps all the way up to
     * the root session.
     */
    public AbstractSession getParentIdentityMapSession(DatabaseQuery query) {
        return getParentIdentityMapSession(query, false, false);
    }

    /**
     * INTERNAL:
     * Gets the next link in the chain of sessions followed by a query's check
     * early return, the chain of sessions with identity maps all the way up to
     * the root session.
     * <p>
     * Used for session broker which delegates to registered sessions, or UnitOfWork
     * which checks parent identity map also.
     * @param canReturnSelf true when method calls itself.  If the path
     * starting at <code>this</code> is acceptable.  Sometimes true if want to
     * move to the first valid session, i.e. executing on ClientSession when really
     * should be on ServerSession.
     * @param terminalOnly return the session we will execute the call on, not
     * the next step towards it.
     * @return this if there is no next link in the chain
     */
    public AbstractSession getParentIdentityMapSession(DatabaseQuery query, boolean canReturnSelf, boolean terminalOnly) {
        return this;
    }

    /**
     * INTERNAL:
     * Gets the session which this query will be executed on.
     * Generally will be called immediately before the call is translated,
     * which is immediately before session.executeCall.
     * <p>
     * Since the execution session also knows the correct datasource platform
     * to execute on, it is often used in the mappings where the platform is
     * needed for type conversion, or where calls are translated.
     * <p>
     * Is also the session with the accessor.  Will return a ClientSession if
     * it is in transaction and has a write connection.
     * @return a session with a live accessor
     * @param query may store session name or reference class for brokers case
     */
    public AbstractSession getExecutionSession(DatabaseQuery query) {
        return this;
    }

    /**
     * ADVANCED:
     * Returns the Synchronization Policy for this session.
     */
    public CacheSynchronizationManager getCacheSynchronizationManager() {
        return cacheSynchronizationManager;
    }

    /**
     * INTERNAL:
     * Return if the commit manager has been set.
     */
    public boolean hasCommitManager() {
        return commitManager != null;
    }

    /**
     * INTERNAL:
     * The commit manager is used to resolve referncial integrity on commits of multiple objects.
     * All brokered sessions share the same commit manager.
     */
    public CommitManager getCommitManager() {
        if (hasBroker()) {
            return getBroker().getCommitManager();
        }

        // PERF: lazy init, not always required, not required for client sessions
        if (commitManager == null) {
            commitManager = new CommitManager(this);
        }
        return commitManager;
    }

    /**
     * INTERNAL:
     * Returns the set of read-only classes that gets assigned to each newly created UnitOfWork.
     *
     * @see org.eclipse.persistence.sessions.Project#setDefaultReadOnlyClasses(Vector)
     */
    public Vector getDefaultReadOnlyClasses() {
        //Bug#3911318  All brokered sessions share the same DefaultReadOnlyClasses.
        if (hasBroker()) {
            return getBroker().getDefaultReadOnlyClasses();
        }
        return getProject().getDefaultReadOnlyClasses();
    }

    /**
     * ADVANCED:
     * Return the descriptor specified for the class.
     * If the class does not have a descriptor but implements an interface that is also implemented
     * by one of the classes stored in the hashtable, that descriptor will be stored under the
     * new class. If a descriptor does not exist for the Class parameter, a ValidationException will 
     * be thrown. If the passed Class parameter is null, then null will be returned. 
     */
    public ClassDescriptor getClassDescriptor(Class theClass) {
        if (theClass == null) {
            return null;
        }
        ClassDescriptor desc = getDescriptor(theClass);
        if (desc instanceof ClassDescriptor) {
            return (ClassDescriptor)desc;
        } else {
            throw ValidationException.cannotCastToClass(desc, desc.getClass(), ClassDescriptor.class);
        }
    }

    /**
     * ADVANCED:
     * Return the descriptor specified for the object's class.
     * If a descriptor does not exist for the Object parameter, a ValidationException will 
     * be thrown. If the passed Object parameter is null, then null will be returned. 
     */
    public ClassDescriptor getClassDescriptor(Object domainObject) {
        if (domainObject == null) {
            return null;
        }
        ClassDescriptor desc = getDescriptor(domainObject);
        if (desc instanceof ClassDescriptor) {
            return (ClassDescriptor)desc;
        } else {
            throw ValidationException.cannotCastToClass(desc, desc.getClass(), ClassDescriptor.class);
        }
    }

    /**
     * PUBLIC:
     * Return the descriptor for  the alias.
     * UnitOfWork delegates this to the parent
     */
    public ClassDescriptor getClassDescriptorForAlias(String alias) {
        return project.getDescriptorForAlias(alias);
    }

    /**
     * ADVANCED:
     * Return the descriptor specified for the class.
     * If the class does not have a descriptor but implements an interface that is also implemented
     * by one of the classes stored in the hashtable, that descriptor will be stored under the
     * new class. If the passed Class is null, null will be returned.
     */
    public ClassDescriptor getDescriptor(Class theClass) {
        if (theClass == null) {
            return null;
        }

        // Optimize descriptor lookup through caching the last one accessed.
        ClassDescriptor lastDescriptor = this.lastDescriptorAccessed;
        if ((lastDescriptor != null) && (lastDescriptor.getJavaClass().equals(theClass))) {
            return lastDescriptor;
        }

        ClassDescriptor descriptor = (ClassDescriptor)getDescriptors().get(theClass);

        if ((descriptor == null) && hasBroker()) {
            // Also check the broker
            descriptor = getBroker().getDescriptor(theClass);
        }
        if (descriptor == null) {
            // Allow for an event listener to lazy register the descriptor for a class.
            getEventManager().missingDescriptor(theClass);
            descriptor = (ClassDescriptor)getDescriptors().get(theClass);
        }

        if (descriptor == null) {
            // This allows for the correct descriptor to be found if the class implements an interface,
            // or extends a class that a descriptor is register for.
            // This is used by EJB to find the descriptor for a stub and remote to unwrap it,
            // and by inheritance to allow for subclasses that have no additional state to not require a descriptor.
            if (!theClass.isInterface()) {
                Class[] interfaces = theClass.getInterfaces();
                for (int index = 0; index < interfaces.length; ++index) {
                    Class interfaceClass = (Class)interfaces[index];
                    descriptor = getDescriptor(interfaceClass);
                    if (descriptor != null) {
                        getDescriptors().put(interfaceClass, descriptor);
                        break;
                    }
                }
                if (descriptor == null) {
                    descriptor = getDescriptor(theClass.getSuperclass());
                }
            }
        }

        // Cache for optimization.
        this.lastDescriptorAccessed = descriptor;

        return descriptor;
    }

    /**
     * ADVANCED:
     * Return the descriptor specified for the object's class.
     * If the passed Object is null, null will be returned.
     */
    public ClassDescriptor getDescriptor(Object domainObject) {
        if (domainObject == null) {
            return null;
        }
        //Bug#3947714  Check and trigger the proxy here
        if (getProject().hasProxyIndirection()) {
            return getDescriptor(ProxyIndirectionPolicy.getValueFromProxy(domainObject).getClass());
        }
        return getDescriptor(domainObject.getClass());
    }

    /**
     * PUBLIC:
     * Return the descriptor for  the alias
     */
    public ClassDescriptor getDescriptorForAlias(String alias) {
        return project.getDescriptorForAlias(alias);
    }

    /**
     * ADVANCED:
     * Return all registered descriptors.
     */
    public Map getDescriptors() {
        return getProject().getDescriptors();
    }

    /**
     * ADVANCED:
     * Return all pre-defined not yet parsed EJBQL queries.
     * @see #getAllQueries()
     */
    public List getEjbqlPlaceHolderQueries() {
        // PERF: lazy init, not normally required.
        if (ejbqlPlaceHolderQueries == null) {
            ejbqlPlaceHolderQueries = new Vector();
        }
        return ejbqlPlaceHolderQueries;
    }

    /**
     * PUBLIC:
     * Return the event manager.
     * The event manager can be used to register for various session events.
     */
    public SessionEventManager getEventManager() {
        if (eventManager == null) {
            synchronized (this) {
                if (eventManager == null) {
                    // PERF: lazy init.
                    eventManager = new SessionEventManager(this);
                }
            }
        }
        return eventManager;
    }

    /**
     * INTERNAL:
     * Return a string which represents my ExceptionHandler's class
     * Added for F2104: Properties.xml
     * - gn
     */
    public String getExceptionHandlerClass() {
        String className = null;
        try {
            className = getExceptionHandler().getClass().getName();
        } catch (Exception exception) {
            return null;
        }
        return className;
    }

    /**
     * PUBLIC:
     * Return the ExceptionHandler.Exception handler can catch errors that occur on queries or during database access.
     */
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * PUBLIC:
     * Used for JTS integration.  If your application requires to have JTS control transactions instead of TopLink an
     * external transaction controler must be specified.
     * TopLink provides JTS controlers for several JTS implementations including JTS 1.0, Weblogic 5.1 and WebSphere 3.0.
     *
     * @see org.eclipse.persistence.transaction.JTATransactionController
     */
    public ExternalTransactionController getExternalTransactionController() {
        return externalTransactionController;
    }

    /**
     * ADVANCED:
     * Return the object from the identity with primary and class of the given object.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object getFromIdentityMap(Object domainObject) {
        return getIdentityMapAccessorInstance().getFromIdentityMap(domainObject);
    }

    /**
     * ADVANCED:
     * Return the object from the identity with the primary and class.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass) {
        return getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, theClass);
    }

    /**
     * ADVANCED:
     * Return the object from the identity with the primary and class.
     * Only return Invalidated objects if requested.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass, boolean shouldReturnInvalidatedObjects) {
        return getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, theClass, shouldReturnInvalidatedObjects);
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     * @deprecated Since 4.0.1
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow) throws QueryException {
        return getFromIdentityMap(selectionCriteria, theClass, translationRow, new InMemoryQueryIndirectionPolicy());
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy) throws QueryException {
        return getIdentityMapAccessorInstance().getFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy);
    }

    /**
     * PUBLIC:
     * The IdentityMapAccessor is the preferred way of accessing IdentityMap funcitons
     * This will return an object which implements an interface which exposes all public
     * IdentityMap functions.
     */
    public org.eclipse.persistence.sessions.IdentityMapAccessor getIdentityMapAccessor() {
        return identityMapAccessor;
    }

    /**
     * INTERNAL:
     * Return the internally available IdentityMapAccessor instance.
     */
    public org.eclipse.persistence.internal.sessions.IdentityMapAccessor getIdentityMapAccessorInstance() {
        return identityMapAccessor;
    }

    /**
     * INTERNAL:
     * Required public for testing access
     * Although this is an internal method, we suspect some customers are using it
     * we will deprecate this method instead of removing it.
     * @deprecated
     */
    public IdentityMapManager getIdentityMapManager() {
        return getIdentityMapAccessorInstance().getIdentityMapManager();
    }

    /**
     * PUBLIC:
     * Returns the integrityChecker.IntegrityChecker holds all the ClassDescriptor Exceptions.
     */
    public IntegrityChecker getIntegrityChecker() {
        // BUG# 2700595 - Lazily create an IntegrityChecker if one has not already been created.
        if (integrityChecker == null) {
            integrityChecker = new IntegrityChecker();
        }

        return integrityChecker;
    }

    /**
     * PUBLIC:
     * Return the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference defaults to a writer on System.out.
     * To enable logging, logMessages must be turned on.
     *
     * @see #logMessages()
     */
    public Writer getLog() {
        return getSessionLog().getWriter();
    }

    /**
     * INTERNAL:
     * Return the name of the session: class name + system hashcode.
     * <p>
     * This should be the implementation of toString(), and also the
     * value should be calculated in the constructor for it is used all the
     * time.  However everything is lazily initialized now and the value is
     * transient for the system hashcode could vary?
     */
    public String getLogSessionString() {
        if (logSessionString == null) {
            StringWriter writer = new StringWriter();
            writer.write(getSessionTypeString());
            writer.write("(");
            writer.write(String.valueOf(System.identityHashCode(this)));
            writer.write(")");
            logSessionString = writer.toString();
        }
        return logSessionString;
    }

    /**
     * INTERNAL:
     * Returns the type of session, its class.
     * <p>
     * Override to hide from the user when they are using an internal subclass
     * of a known class.
     * <p>
     * A user does not need to know that their UnitOfWork is a
     * non-deferred UnitOfWork, or that their ClientSession is an
     * IsolatedClientSession.
     */
    public String getSessionTypeString() {
        return Helper.getShortClassName(getClass());
    }

    /**
     * OBSOLETE:
     * Return the login, the login holds any database connection information given.
     * This has been replaced by getDatasourceLogin to make use of the Login interface
     * to support non-relational datasources,
     * if DatabaseLogin API is required it will need to be cast.
     */
    public DatabaseLogin getLogin() {
        try {
            return (DatabaseLogin)getDatasourceLogin();
        } catch (ClassCastException wrongType) {
            throw ValidationException.notSupportedForDatasource();
        }
    }

    /**
     * PUBLIC:
     * Return the login, the login holds any database connection information given.
     * This return the Login interface and may need to be cast to the datasource specific implementation.
     */
    public Login getDatasourceLogin() {
        return getProject().getDatasourceLogin();
    }

    /**
     * PUBLIC:
     * Return the name of the session.
     * This is used with the session broker, or to give the session a more meaningful name.
     */
    public String getName() {
        return name;
    }

    /**
     * ADVANCED:
     * Return the sequnce number from the database
     */
    public Number getNextSequenceNumberValue(Class domainClass) {
        return (Number)getSequencing().getNextValue(domainClass);
    }

    /**
     * INTERNAL:
     * Return the number of units of work connected.
     */
    public int getNumberOfActiveUnitsOfWork() {
        return numberOfActiveUnitsOfWork;
    }

    /**
     * INTERNAL:
     * Return the database platform currently connected to.
     * The platform is used for database specific behavoir.
     * NOTE: this must only be used for relational specific usage,
     * it will fail for non-relational datasources.
     */
    public DatabasePlatform getPlatform() {
        // PERF: Cache the platform.
        if (platform == null) {
            platform = getDatasourceLogin().getPlatform();
        }
        return (DatabasePlatform)platform;
    }

    /**
     * INTERNAL:
     * Return the database platform currently connected to.
     * The platform is used for database specific behavoir.
     */
    public Platform getDatasourcePlatform() {
        // PERF: Cache the platform.
        if (platform == null) {
            platform = getDatasourceLogin().getDatasourcePlatform();
        }
        return platform;
    }

    /**
     * INTERNAL:
     * Marked internal as this is not customer API but helper methods for
     * accessing the server platform from within TopLink's other sessions types
     * (ie not DatabaseSession)
     */
    public ServerPlatform getServerPlatform() {
        return null;
    }

    /**
     * INTERNAL:
     * Return the database platform currently connected to
     * for specified class.
     * The platform is used for database specific behavoir.
     */
    public Platform getPlatform(Class domainClass) {
        // PERF: Cache the platform.
        if (platform == null) {
            platform = getDatasourcePlatform();
        }
        return platform;
    }

    /**
     * PUBLIC:
     * Return the profiler.
     * The profiler is a tool that can be used to determine performance bottlenecks.
     * The profiler can be queries to print summaries and configure for logging purposes.
     */
    public SessionProfiler getProfiler() {
        return profiler;
    }

    /**
     * PUBLIC:
     * Return the project, the project holds configuartion information including the descriptors.
     */
    public org.eclipse.persistence.sessions.Project getProject() {
        return project;
    }

    /**
     * ADVANCED:
     * Allow for user defined properties.
     */
    public Map getProperties() {
        if (properties == null) {
            properties = new HashMap(5);
        }
        return properties;
    }

    /**
     * INTERNAL:
     * Allow to check for user defined properties.
     */
    public boolean hasProperties() {
        return ((properties != null) && !properties.isEmpty());
    }

    /**
     * ADVANCED:
     * Returns the user defined property.
     */
    public Object getProperty(String name) {
        if(this.properties==null){
            return null;
        }
        return getProperties().get(name);
    }

    /**
     * ADVANCED:
     * Return all pre-defined queries.
     * @see #getAllQueries()
     */
    public Map getQueries() {
        // PERF: lazy init, not normally required.
        if (queries == null) {
            queries = new HashMap(5);
        }
        return queries;
    }

    /**
     * PUBLIC:
     * Return the pre-defined queries in this session.
     * A single vector containing all the queries is returned.
     *
     * @see #getQueries()
     */
    public Vector getAllQueries() {
        Vector allQueries = new Vector();
        for (Iterator vectors = getQueries().values().iterator(); vectors.hasNext();) {
            allQueries.addAll((Vector)vectors.next());
        }
        return allQueries;
    }

    /**
     * PUBLIC:
     * Return the query from the session pre-defined queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public DatabaseQuery getQuery(String name) {
        return getQuery(name, null);
    }

    /**
     * PUBLIC:
     * Return the query from the session pre-defined queries with the given name and argument types.
     * This allows for common queries to be pre-defined, reused and executed by name.
     * This method should be used if the Session has multiple queries with the same name but
     * different arguments.
     *
     * @see #getQuery(String)
     */
    public DatabaseQuery getQuery(String name, Vector arguments) {
        Vector queries = (Vector)getQueries().get(name);
        if ((queries == null) || queries.isEmpty()) {
            return null;
        }

        // Short circuit the simple, most common case of only one query.
        if (queries.size() == 1) {
            return (DatabaseQuery)queries.firstElement();
        }

        // CR#3754; Predrag; mar 19/2002;
        // We allow multiple named queries with the same name but
        // different argument set; we can have only one query with
        // no arguments; Vector queries is not sorted;
        // When asked for the query with no parameters the
        // old version did return the first query - wrong: 
        // return (DatabaseQuery) queries.firstElement();
        int argumentTypesSize = 0;
        if (arguments != null) {
            argumentTypesSize = arguments.size();
        }
        Vector argumentTypes = new Vector(argumentTypesSize);
        for (int i = 0; i < argumentTypesSize; i++) {
            argumentTypes.addElement(arguments.elementAt(i).getClass());
        }
        for (Enumeration queriesEnum = queries.elements(); queriesEnum.hasMoreElements();) {
            DatabaseQuery query = (DatabaseQuery)queriesEnum.nextElement();
            if (Helper.areTypesAssignable(argumentTypes, query.getArgumentTypes())) {
                return query;
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     * Return the Sequencing object used by the session.
     */
    public Sequencing getSequencing() {
        return null;
    }

    /**
     * INTERNAL:
     * Return the session to be used for the class.
     * Used for compatibility with the session broker.
     */
    public AbstractSession getSessionForClass(Class domainClass) {
        if (hasBroker()) {
            return getBroker().getSessionForClass(domainClass);
        }
        return this;
    }

    /**
     * PUBLIC:
     * Return the session log to which an accessor logs messages and SQL.
     * If not set, this will default to a session log on a writer on System.out.
     * To enable logging, logMessages must be turned on.
     *
     * @see #logMessages()
     */
    public SessionLog getSessionLog() {
        if (sessionLog == null) {
            setSessionLog(new DefaultSessionLog());
        }
        return sessionLog;
    }

    /**
     * INTERNAL:
     * The transaction mutex ensure mutual exclusion on transaction across multiple threads.
     */
    public ConcurrencyManager getTransactionMutex() {
        // PERF: not always required, defer.
        if (transactionMutex == null) {
            synchronized (this) {
                if (transactionMutex == null) {
                    transactionMutex = new ConcurrencyManager();
                }
            }
        }
        return transactionMutex;
    }

    /**
     * ADVANCED:
     * Extract the write lock value from the identity map.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object getWriteLockValue(Object domainObject) {
        return (Object)getIdentityMapAccessorInstance().getWriteLockValue(domainObject);
    }

    /**
     * ADVANCED:
     * Extract the write lock value from the identity map.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object getWriteLockValue(Vector primaryKey, Class theClass) {
        return (Object)getIdentityMapAccessorInstance().getWriteLockValue(primaryKey, theClass);
    }

    /**
     * PUBLIC:
     * Allow any WARNING level exceptions that occur within TopLink to be logged and handled by the exception handler.
     */
    public Object handleException(RuntimeException exception) throws RuntimeException {
        if ((exception instanceof EclipseLinkException)) {
            EclipseLinkException topLinkException = (EclipseLinkException)exception;
            if (topLinkException.getSession() == null) {
                topLinkException.setSession(this);
            }

            //Bug#3559280  Avoid logging an exception twice
            if (!topLinkException.hasBeenLogged()) {
                logThrowable(SessionLog.WARNING, null, exception);
                topLinkException.setHasBeenLogged(true);
            }
        } else {
            logThrowable(SessionLog.WARNING, null, exception);
        }
        if (hasExceptionHandler()) {
            return getExceptionHandler().handleException(exception);
        } else {
            throw exception;
        }
    }

    /**
     * INTERNAL:
     * Allow the session to be used from a session broker.
     */
    public boolean hasBroker() {
        return broker != null;
    }

    /**
     * ADVANCED:
     * Return true if a cache synchronisation manager exists.
     */
    public boolean hasCacheSynchronizationManager() {
        return cacheSynchronizationManager != null;
    }

    /**
     * ADVANCED:
     * Return true if a descriptor exists for the given class.
     */
    public boolean hasDescriptor(Class theClass) {
        if (theClass == null) {
            return false;
        }

        return getDescriptors().get(theClass) != null;
    }

    /**
     * PUBLIC:
     * Return if an exception handler is present.
     */
    public boolean hasExceptionHandler() {
        if (exceptionHandler == null) {
            return false;
        }
        return true;
    }

    /**
     * PUBLIC:
     * Used for JTA integration.  If your application requires to have JTA control transactions instead of TopLink an
     * external transaction controler must be specified.  TopLink provides JTA controlers for JTA 1.0 and application
     * servers.
     * @see org.eclipse.persistence.transaction.JTATransactionController
     */
    public boolean hasExternalTransactionController() {
        return externalTransactionController != null;
    }

    /**
     * PUBLIC:
     * Reset the entire object cache.
     * <p> NOTE: be careful using this method. This method blows away both this session's and its parents caches,
     * this includes the server cache or any other cache. This throws away any objects that have been read in.
     * Extream caution should be used before doing this because object identity will no longer
     * be maintained for any objects currently read in.  This should only be called
     * if the application knows that it no longer has references to object held in the cache.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public void initializeAllIdentityMaps() {
        getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    /**
     * PUBLIC:
     * Reset the identity map for only the instances of the class.
     * For inheritance the user must make sure that they only use the root class.
     * Caution must be used in doing this to ensure that the objects within the identity map
     * are not referenced from other objects of other classes or from the application.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public void initializeIdentityMap(Class theClass) {
        getIdentityMapAccessorInstance().initializeIdentityMap(theClass);
    }

    /**
     * INTERNAL:
     * Set up the IdentityMapManager.  This method allows subclasses of Session to override
     * the default IdentityMapManager functionality.
     */
    public void initializeIdentityMapAccessor() {
        this.identityMapAccessor = new org.eclipse.persistence.internal.sessions.IdentityMapAccessor(this, new IdentityMapManager(this));
    }

    /**
     * PUBLIC:
     * Reset the entire local object cache.
     * This throws away any objects that have been read in.
     * Extream caution should be used before doing this because object identity will no longer
     * be maintained for any objects currently read in.  This should only be called
     * if the application knows that it no longer has references to object held in the cache.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public void initializeIdentityMaps() {
        getIdentityMapAccessorInstance().initializeIdentityMaps();
    }

    /**
     * PUBLIC:
     * Insert the object and all of its privately owned parts into the database.
     * Insert should only be used if the application knows that the object is new,
     * otherwise writeObject should be used.
     * The insert operation can be customized through using an insert query.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     *
     * @see InsertObjectQuery
     * @see #writeObject(Object)
     */
    public Object insertObject(Object domainObject) throws DatabaseException {
        InsertObjectQuery query = new InsertObjectQuery();
        query.setObject(domainObject);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * INTERNAL:
     * Return the results from exeucting the database query.
     * The arguments should be a database row with raw data values.
     * This method is provided to allow subclasses to change the default querying behavoir.
     * All querying goes through this method.
     */
    public Object internalExecuteQuery(DatabaseQuery query, AbstractRecord databaseRow) throws DatabaseException {
        return query.execute(this, databaseRow);
    }

    /**
     * INTERNAL:
     * Returns true if the session is a session Broker.
     */
    public boolean isBroker() {
        return false;
    }

    /**
     * INTERNAL:
     * Returns true if the session is in a session Broker.
     */
    public boolean isInBroker() {
        return isInBroker;
    }

    /**
     * PUBLIC:
     * Return if the class is defined as read-only.
     */
    public boolean isClassReadOnly(Class theClass) {
        ClassDescriptor descriptor = getDescriptor(theClass);
        return isClassReadOnly(theClass, descriptor);
    }

    /**
     * INTERNAL:
     * Return if the class is defined as read-only.
     * PERF: Pass descriptor to avoid re-lookup.
     */
    public boolean isClassReadOnly(Class theClass, ClassDescriptor descriptor) {
        if ((descriptor != null) && descriptor.shouldBeReadOnly()) {
            return true;
        }
        if (theClass != null) {
            return getDefaultReadOnlyClasses().contains(theClass);
        }
        return false;
    }
    
    /**
     * PUBLIC:
     * Return if this session is a client session.
     */
    public boolean isClientSession() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this session is connected to the database.
     */
    public boolean isConnected() {
        if (getAccessor() == null) {
            return false;
        }

        return getAccessor().isConnected();
    }

    /**
     * PUBLIC:
     * Return if this session is a database session.
     */
    public boolean isDatabaseSession() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this session is a distributed session.
     */
    public boolean isDistributedSession() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if a profiler is being used.
     */
    public boolean isInProfile() {
        return isInProfile;
    }

    /**
     * PUBLIC:
     * Allow for user deactive a profiler
     */
    public void setIsInProfile(boolean inProfile) {
        this.isInProfile = inProfile;
    }
    
    /**
     * INTERNAL:
     * Set if this session is contained in a SessionBroker.
     */
    public void setIsInBroker(boolean isInBroker) {
        this.isInBroker = isInBroker;
    }
    
    /**
     * PUBLIC:
     * Return if this session's decendants should use finalizers.
     * The allows certain finalizers such as in ClientSesion to be enabled.
     * These are disable by default for performance reasons.
     */
    public boolean isFinalizersEnabled() {
        return isFinalizersEnabled;
    }
    
    /**
     * INTERNAL:
     * Register a finalizer to release this session.
     */
    public void registerFinalizer() {
        // Ensure the finalizer is referenced until the session is gc'd.
        setProperty("finalizer", new SessionFinalizer(this));
    }
    
    /**
     * INTERNAL:
     * Return if this session is a historical session.
     */
    public boolean isHistoricalSession() {
        return false;
    }

    /**
     * PUBLIC:
     * Set if this session's decendants should use finalizers.
     * The allows certain finalizers such as in ClientSesion to be enabled.
     * These are disable by default for performance reasons.
     */
    public void setIsFinalizersEnabled(boolean isFinalizersEnabled) {
        this.isFinalizersEnabled = isFinalizersEnabled;
    }

    /**
     * PUBLIC:
     * Return if the session is currently in the progress of a database transaction.
     * Because nested transactions are allowed check if the transaction mutex has been aquired.
     */
    public boolean isInTransaction() {
        return getTransactionMutex().isAcquired();
    }

    /**
     * PUBLIC:
     * Return if this session is remote.
     */
    public boolean isRemoteSession() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this session is a unit of work.
     */
    public boolean isRemoteUnitOfWork() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this session is a server session.
     */
    public boolean isServerSession() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this session is a session broker.
     */
    public boolean isSessionBroker() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this session is a unit of work.
     */
    public boolean isUnitOfWork() {
        return false;
    }

    /**
     * ADVANCED:
     * Extract and return the primary key from the object.
     */
    public Vector keyFromObject(Object domainObject) throws ValidationException {
        ClassDescriptor descriptor = getDescriptor(domainObject);
        return keyFromObject(domainObject, descriptor);
    }

    /**
     * ADVANCED:
     * Extract and return the primary key from the object.
     */
    public Vector keyFromObject(Object domainObject, ClassDescriptor descriptor) throws ValidationException {
        if (descriptor == null) {
            throw ValidationException.missingDescriptor(domainObject.getClass().getName());
        }
        Object implemention = descriptor.getObjectBuilder().unwrapObject(domainObject, this);
        if (implemention == null) {
            return null;
        }
        return descriptor.getObjectBuilder().extractPrimaryKeyFromObject(implemention, this);
    }

    /**
     * PUBLIC:
     * Log the log entry.
     */
    public void log(SessionLogEntry entry) {
        if (shouldLog(entry.getLevel(), entry.getNameSpace())) {
            if (entry.getSession() == null) {// Used for proxy session.
                entry.setSession(this);
            }
            getSessionLog().log(entry);
        }
    }

    /**
     * OBSOLETE:
     * @deprecated    Replaced by log(org.eclipse.persistence.logging.SessionLogEntry)
     * @see #log(org.eclipse.persistence.logging.SessionLogEntry)
     */
    public void log(org.eclipse.persistence.sessions.SessionLogEntry entry) {
        if (shouldLog(entry.getLevel(), entry.getNameSpace())) {
            if (entry.getSession() == null) {// Used for proxy session.
                entry.setSession(this);
            }
            getSessionLog().log(entry);
        }
    }

    /**
     * OBSOLETE:
     * @deprecated    Replaced by log(int level, String category, String message, Object[] params)
     * @see #log(int level, String category, String message, Object[] params)
     */
    public void logDebug(String message, Object[] arguments) {
        log(SessionLog.FINEST, null, message, arguments);
    }

    /**
     * OBSOLETE:
     * @deprecated    Replaced by log(int level, String message, Object[] params, Accessor accessor, boolean shouldTranslate)
     * @see #log(int, String, String)
     */
    public void logDebug(String message) {
        log(SessionLog.FINEST, message, (Object[])null, null, false);
    }

    /**
     * OBSOLETE:
     * @deprecated    Replaced by logThrowable(int level, String category, Throwable throwable)
     * @see #logThrowable(int level, String category, Throwable throwable)
     */
    public void logException(Exception exception) {
        logThrowable(SessionLog.SEVERE, null, exception);
    }

    /**
     * OBSOLETE:
     * @deprecated    Replaced by log(int level, String category, String message, Object[] params)
     * @see #log(int level, String category, String message, Object[] params)
     */
    public void logMessage(String message, Object[] arguments) {
        log(SessionLog.FINER, null, message, arguments);
    }

    /**
     * Log a untranslated message to the TopLink log at FINER level.
     */
    public void logMessage(String message) {
        log(SessionLog.FINER, message, (Object[])null, null, false);
    }

    /**
     * INTERNAL:
     * Log the message for the specified accessor.  The parameter 'message' is a string that needs to be
     * translated.
     * @deprecated
     */
    public void logMessage(String message, Object[] arguments, Accessor accessor) {
        log(SessionLog.FINER, message, arguments, accessor);
    }

    /**
     * INTERNAL:
     * Log the message for the specified accessor.  The parameter 'message' is a translated string or a string
     * that does not need to be translated.
     * @deprecated
     */
    public void logMessage(String message, Accessor accessor) {
        log(SessionLog.FINER, message, (Object[])null, accessor, false);
    }

    /**
     * OBSOLETE:
     * @deprecated    Replaced by setLogLevel(int level);
     * @see #setLogLevel(int)
     */
    public void logMessages() {
        setShouldLogMessages(true);
    }

    /**
     * INTERNAL:
     * A call back to do session specific preparation of a query.
     * <p>
     * The call back occurs soon before we clone the query for execution,
     * meaning that if this method needs to clone the query then the caller will
     * determine that it doesn't need to clone the query itself.
     */
    public DatabaseQuery prepareDatabaseQuery(DatabaseQuery query) {
        if (!isUnitOfWork() && query.isObjectLevelReadQuery()) {
            return ((ObjectLevelReadQuery)query).prepareOutsideUnitOfWork(this);
        } else {
            return query;
        }
    }
    
    /**
     * PUBLIC:
     * Used to print all the objects in the identity map of the passed in class.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public void printIdentityMap(Class businessClass) {
        getIdentityMapAccessorInstance().printIdentityMap(businessClass);
    }

    /**
     * PUBLIC:
     * Used to print all the objects in every identity map in this session.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public void printIdentityMaps() {
        getIdentityMapAccessorInstance().printIdentityMaps();
    }

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object putInIdentityMap(Object domainObject) {
        return getIdentityMapAccessorInstance().putInIdentityMap(domainObject);
    }

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object putInIdentityMap(Object domainObject, Vector key) {
        return getIdentityMapAccessorInstance().putInIdentityMap(domainObject, key);
    }

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object putInIdentityMap(Object domainObject, Vector key, Object writeLockValue) {
        return getIdentityMapAccessorInstance().putInIdentityMap(domainObject, key, writeLockValue);
    }

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * The readTime may also be included in the cache key as it is constructed
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public Object putInIdentityMap(Object domainObject, Vector key, Object writeLockValue, long readTime) {
        return getIdentityMapAccessorInstance().putInIdentityMap(domainObject, key, writeLockValue, readTime);
    }

    /**
     * PUBLIC:
     * Read all of the instances of the class from the database.
     * This operation can be customized through using a ReadAllQuery,
     * or through also passing in a selection criteria.
     *
     * @see ReadAllQuery
     * @see #readAllObjects(Class, Expression)
     */
    public Vector readAllObjects(Class domainClass) throws DatabaseException {
        ReadAllQuery query = new ReadAllQuery();
        query.setIsExecutionClone(true);
        query.setReferenceClass(domainClass);
        return (Vector)executeQuery(query);
    }

    /**
     * PUBLIC:
     * Read all of the instances of the class from the database return through execution the SQL string.
     * The SQL string must be a valid SQL select statement or selecting stored procedure call.
     * This operation can be customized through using a ReadAllQuery.
 	 * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     *
     * @see ReadAllQuery
     */
    public Vector readAllObjects(Class domainClass, String sqlString) throws DatabaseException {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(domainClass);
        query.setSQLString(sqlString);
        query.setIsExecutionClone(true);
        return (Vector)executeQuery(query);
    }

    /**
     * PUBLIC:
     * Read all the instances of the class from the database returned through execution the Call string.
     * The Call can be an SQLCall or JPQLCall.
     *
     * example: session.readAllObjects(Employee.class, new SQLCall("SELECT * FROM EMPLOYEE"));
     * @see Call
     */
    public Vector readAllObjects(Class referenceClass, Call aCall) throws DatabaseException {
        ReadAllQuery raq = new ReadAllQuery();        
        raq.setReferenceClass(referenceClass);
        raq.setCall(aCall);
        raq.setIsExecutionClone(true);
        return (Vector)executeQuery(raq);
    }

    /**
     * PUBLIC:
     * Read all of the instances of the class from the database matching the given expression.
     * This operation can be customized through using a ReadAllQuery.
     *
     * @see ReadAllQuery
     */
    public Vector readAllObjects(Class domainClass, Expression expression) throws DatabaseException {
        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(domainClass);
        query.setSelectionCriteria(expression);
        query.setIsExecutionClone(true);
        return (Vector)executeQuery(query);
    }

    /**
     * PUBLIC:
     * Read the first instance of the class from the database.
     * This operation can be customized through using a ReadObjectQuery,
     * or through also passing in a selection criteria.
     *
     * @see ReadObjectQuery
     * @see #readAllObjects(Class, Expression)
     */
    public Object readObject(Class domainClass) throws DatabaseException {
        ReadObjectQuery query = new ReadObjectQuery();
        query.setReferenceClass(domainClass);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * PUBLIC:
     * Read the first instance of the class from the database return through execution the SQL string.
     * The SQL string must be a valid SQL select statement or selecting stored procedure call.
     * This operation can be customized through using a ReadObjectQuery.
 	 * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     *
     * @see ReadObjectQuery
     */
    public Object readObject(Class domainClass, String sqlString) throws DatabaseException {
        ReadObjectQuery query = new ReadObjectQuery();
        query.setReferenceClass(domainClass);
        query.setSQLString(sqlString);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * PUBLIC:
     * Read the first instance of the class from the database returned through execution the Call string.
     * The Call can be an SQLCall or JPQLCall.
     *
     * example: session.readObject(Employee.class, new SQLCall("SELECT * FROM EMPLOYEE"));
     * @see SQLCall
     * @see JPQLCall
     */
    public Object readObject(Class domainClass, Call aCall) throws DatabaseException {
        ReadObjectQuery query = new ReadObjectQuery();
        query.setReferenceClass(domainClass);
        query.setCall(aCall);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * PUBLIC:
     * Read the first instance of the class from the database matching the given expression.
     * This operation can be customized through using a ReadObjectQuery.
     *
     * @see ReadObjectQuery
     */
    public Object readObject(Class domainClass, Expression expression) throws DatabaseException {
        ReadObjectQuery query = new ReadObjectQuery();
        query.setReferenceClass(domainClass);
        query.setSelectionCriteria(expression);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * PUBLIC:
     * Use the example object to consruct a read object query by the objects primary key.
     * This will read the object from the database with the same primary key as the object
     * or null if no object is found.
     */
    public Object readObject(Object object) throws DatabaseException {
        ReadObjectQuery query = new ReadObjectQuery();
        query.setSelectionObject(object);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * PUBLIC:
     * Refresh the attributes of the object and of all of its private parts from the database.
     * The object will be pessimisticly locked on the database for the duration of the transaction.
     * If the object is already locked this method will wait until the lock is released.
     * A no wait option is available through setting the lock mode.
     * @see #refreshAndLockObject(Object, lockMode)
     */
    public Object refreshAndLockObject(Object object) throws DatabaseException {
        return refreshAndLockObject(object, ObjectBuildingQuery.LOCK);
    }

    /**
     * PUBLIC:
     * Refresh the attributes of the object and of all of its private parts from the database.
     * The object will be pessimisticly locked on the database for the duration of the transaction.
     * <p>Lock Modes: ObjectBuildingQuery.NO_LOCK, LOCK, LOCK_NOWAIT
     */
    public Object refreshAndLockObject(Object object, short lockMode) throws DatabaseException {
        ReadObjectQuery query = new ReadObjectQuery();
        query.setSelectionObject(object);
        query.refreshIdentityMapResult();
        query.cascadePrivateParts();
        query.setLockMode(lockMode);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * PUBLIC:
     * Refresh the attributes of the object and of all of its private parts from the database.
     * This can be used to ensure the object is up to date with the database.
     * Caution should be used when using this to make sure the application has no un commited
     * changes to the object.
     */
    public Object refreshObject(Object object) throws DatabaseException {
        return refreshAndLockObject(object, ObjectBuildingQuery.NO_LOCK);
    }

    /**
     * PUBLIC:
     * Release the session.
     * This does nothing by default, but allows for other sessions such as the ClientSession to do something.
     */
    public void release() {
    }

    /**
     * INTERNAL:
     * Release the unit of work, if lazy release the connection.
     */
    public void releaseUnitOfWork(UnitOfWorkImpl unitOfWork) {
        // Nothing is required by default, allow subclasses to do cleanup.
        setNumberOfActiveUnitsOfWork(getNumberOfActiveUnitsOfWork() - 1);
    }

    /**
     * ADVANCED:
     * Remove the object from the object cache.
     * Caution should be used when calling to avoid violating object identity.
     * The application should only call this is it knows that no references to the object exist.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public void removeFromIdentityMap(Object domainObject) {
        getIdentityMapAccessorInstance().removeFromIdentityMap(domainObject);
    }

    /**
     * ADVANCED:
     * Remove the object from the object cache.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public void removeFromIdentityMap(Vector key, Class theClass) {
        getIdentityMapAccessorInstance().removeFromIdentityMap(key, theClass);
    }

    /**
     * PUBLIC:
     * Remove the user defined property.
     */
    public void removeProperty(String property) {
        getProperties().remove(property);
    }

    /**
     * PUBLIC:
     * Remove all queries with the given queryName regardless of the argument types.
     *
     * @see #removeQuery(String, Vector)
     */
    public void removeQuery(String queryName) {
        getQueries().remove(queryName);
    }

    /**
     * PUBLIC:
     * Remove the specific query with the given queryName and argumentTypes.
     */
    public void removeQuery(String queryName, Vector argumentTypes) {
        Vector queries = (Vector)getQueries().get(queryName);
        if (queries == null) {
            return;
        } else {
            DatabaseQuery query = null;
            for (Enumeration enumtr = queries.elements(); enumtr.hasMoreElements();) {
                query = (DatabaseQuery)enumtr.nextElement();
                if (Helper.areTypesAssignable(argumentTypes, query.getArgumentTypes())) {
                    break;
                }
            }
            if (query != null) {
                queries.remove(query);
            }
        }
    }

    /**
     * PROTECTED:
     * Attempts to rollback the running internally started external transaction.
     * Returns true only in one case -
     * extenal transaction has been internally rolled back during this method call:
     * wasJTSTransactionInternallyStarted()==true in the beginning of this method and
     * wasJTSTransactionInternallyStarted()==false in the end of this method.
     */
    protected boolean rollbackExternalTransaction() {
        boolean externalTransactionHasRolledBack = false;
        if (hasExternalTransactionController() && wasJTSTransactionInternallyStarted()) {
            try {
                getExternalTransactionController().rollbackTransaction(this);
            } catch (RuntimeException exception) {
                handleException(exception);
            }
            if (!wasJTSTransactionInternallyStarted()) {
                externalTransactionHasRolledBack = true;
                log(SessionLog.FINER, SessionLog.TRANSACTION, "external_transaction_has_rolled_back_internally");
            }
        }
        return externalTransactionHasRolledBack;
    }

    /**
     * PUBLIC:
     * Rollback the active database transaction.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException if the database connection is lost or the rollback fails.
     * @exception ConcurrencyException if this session is not within a transaction.
     */
    public void rollbackTransaction() throws DatabaseException, ConcurrencyException {
        this.rollbackTransaction(this);
    }
    
    /**
     * PUBLIC:
     * Allow calling session to be passed.     
     * 
     * The calling session is the session who actually invokes commit or rollback transaction, 
     * it is used to determine whether the connection needs to be closed when using external connection pool.
     * The connection with a externalConnectionPool used by synchronized UOW should leave open until 
     * afterCompletion call back; the connection with a externalConnectionPool used by other type of session 
     * should be closed after transaction was finised.
     * 
     * Rollback the active database transaction.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException if the database connection is lost or the rollback fails.
     * @exception ConcurrencyException if this session is not within a transaction.
     */
    public void rollbackTransaction(AbstractSession callingSession) throws DatabaseException, ConcurrencyException {
        // Ensure release of mutex and call subclass specific release.
        try {
            if (!getTransactionMutex().isNested()) {
                getEventManager().preRollbackTransaction();
                basicRollbackTransaction(callingSession);
                getEventManager().postRollbackTransaction();
            }
        } finally {
            getTransactionMutex().release();

            // If there is no db transaction in progress
            // if there is an active external transaction 
            // which was started internally - it should be rolled back internally, too.
            if (!isInTransaction()) {
                rollbackExternalTransaction();
            }
        }
    }
    

    /**
     * INTERNAL:
     * Set the accessor.
     */
    public void setAccessor(Accessor accessor) {
        this.accessor = accessor;
    }

    /**
     * INTERNAL:
     * Allow the session to be used from a session broker.
     */
    public void setBroker(AbstractSession broker) {
        this.broker = broker;
    }

    /**
     * ADVANCED:
     * Sets the cache synchronization manager for this session.
     * This is used to broadcast changes to this session's cache to other sessions on other servers.
     */
    public void setCacheSynchronizationManager(CacheSynchronizationManager cacheSynchronizationManager) {
        this.cacheSynchronizationManager = cacheSynchronizationManager;
        if (cacheSynchronizationManager != null) {
            cacheSynchronizationManager.setSession(this);
        }
    }

    /**
     * INTERNAL:
     * The commit manager is used to resolve referncial integrity on commits of multiple objects.
     */
    public void setCommitManager(CommitManager commitManager) {
        this.commitManager = commitManager;
    }

    /**
     * INTERNAL:
     * Set the event manager.
     * The event manager can be used to register for various session events.
     */
    public void setEventManager(SessionEventManager eventManager) {
        if (eventManager != null) {
            this.eventManager = eventManager;
        } else {
            this.eventManager = new SessionEventManager();
        }
        this.eventManager.setSession(this);
    }

    /**
     * PUBLIC:
     * Set the exceptionHandler.
     * Exception handler can catch errors that occur on queries or during database access.
     */
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Used for JTS integration internally by ServerPlatform.
     */
    public void setExternalTransactionController(ExternalTransactionController externalTransactionController) {
        this.externalTransactionController = externalTransactionController;
        if (externalTransactionController == null) {
            return;
        }
        externalTransactionController.setSession(this);
    }

    /**
     * PUBLIC:
     * set the integrityChecker. IntegrityChecker holds all the ClassDescriptor Exceptions.
     */
    public void setIntegrityChecker(IntegrityChecker integrityChecker) {
        this.integrityChecker = integrityChecker;
    }

    /**
     * PUBLIC:
     * Set the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference defaults to a writer on System.out.
     * To enable logging logMessages() is used.
     *
     * @see #logMessages()
     */
    public void setLog(Writer log) {
        getSessionLog().setWriter(log);
    }

    /**
     * PUBLIC:
     * Set the login.
     */
    public void setLogin(DatabaseLogin login) {
        setDatasourceLogin(login);
    }

    /**
     * PUBLIC:
     * Set the login.
     */
    public void setLogin(Login login) {
        setDatasourceLogin(login);
    }

    /**
     * PUBLIC:
     * Set the login.
     */
    public void setDatasourceLogin(Login login) {
        getProject().setDatasourceLogin(login);
        this.platform = null;
    }

    /**
     * PUBLIC:
     * Set the name of the session.
     * This is used with the session broker.
     */
    public void setName(String name) {
        this.name = name;
    }

    protected void setNumberOfActiveUnitsOfWork(int numberOfActiveUnitsOfWork) {
        this.numberOfActiveUnitsOfWork = numberOfActiveUnitsOfWork;
    }

    /**
     * PUBLIC:
     * Set the profiler for the session.
     * This allows for performance operations to be profiled.
     */
    public void setProfiler(SessionProfiler profiler) {
        this.profiler = profiler;
        if (profiler != null) {
            profiler.setSession(this);
            setIsInProfile(getProfiler().getProfileWeight() != SessionProfiler.NONE);
            // Clear cached flag that bybasses the profiler check.
            getIdentityMapAccessorInstance().getIdentityMapManager().clearCacheAccessPreCheck();
        } else {
            setIsInProfile(false);
        }
    }

    /**
     * INTERNAL:
     * Set the project, the project holds configuartion information including the descriptors.
     */
    public void setProject(org.eclipse.persistence.sessions.Project project) {
        this.project = project;
    }

    /**
     * INTERNAL:
     * Set the user defined properties.
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    /**
     * PUBLIC:
     * Allow for user defined properties.
     */
    public void setProperty(String propertyName, Object propertyValue) {
        getProperties().put(propertyName, propertyValue);
    }

    protected void setQueries(Map queries) {
        this.queries = queries;
    }

    /**
     * PUBLIC:
     * Set the session log to which an accessor logs messages and SQL.
     * If not set, this will default to a session log on a writer on System.out.
     * To enable logging, log level can not be OFF.
     * Also set a backpointer to this session in SessionLog.
     *
     * @see #logMessage(String)
     */
    public void setSessionLog(SessionLog sessionLog) {
        this.sessionLog = sessionLog;
        if ((sessionLog != null) && (sessionLog.getSession() == null)) {
            sessionLog.setSession(this);
        }
    }

    /**
     * OBSOLETE:
     * Replaced by setSessionLog(org.eclipse.persistence.logging.SessionLog);
     * @deprecated
     * @see #setSessionLog(org.eclipse.persistence.logging.SessionLog)
     */
    public void setSessionLog(org.eclipse.persistence.sessions.SessionLog sessionLog) {
        setSessionLog((org.eclipse.persistence.logging.SessionLog)sessionLog);
    }

    /**
     * OBSOLETE:
     * Replaced by setLogLevel(int, String);
     * @deprecated
     * @see #setLogLevel(int)
     */
    public void setShouldLogMessages(boolean shouldLogMessages) {
        if (shouldLogMessages && (getLogLevel(null) > SessionLog.FINER)) {
            setLogLevel(SessionLog.FINER);
        } else if (!shouldLogMessages) {
            setLogLevel(SessionLog.OFF);
        }
    }

    /**
     * ADVANCED:
     * Customers may experience errors with older code, that uses query events on commit, and 9.0.4 of TopLink.
     * Setting this flag to true should remove those errors.  Any new code should not use this flag but
     * instead use API on DescriptorEvent to update the values in the Objects and the
     * UnitOfWork Change Sets as well;
     * @deprecated
    */
    public void setUseOldCommit(boolean oldCommit) {
        this.usesOldCommit = oldCommit;
    }

    protected void setTransactionMutex(ConcurrencyManager transactionMutex) {
        this.transactionMutex = transactionMutex;
    }

    /**
     * INTERNAL:
     * Return if a JTS transaction was started by the session.
     * The session will start a JTS transaction if a unit of work or transaction is begun without a JTS transaction present.
     */
    public void setWasJTSTransactionInternallyStarted(boolean wasJTSTransactionInternallyStarted) {
        this.wasJTSTransactionInternallyStarted = wasJTSTransactionInternallyStarted;
    }

    /**
     * PUBLIC:
     * Return if logging is enabled (false if log level is OFF)
     */
    public boolean shouldLogMessages() {
        if (getLogLevel(null) == SessionLog.OFF) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * INTERNAL:
     * Start the operation timing.
     */
    public void startOperationProfile(String operationName) {
        if (isInProfile()) {
            getProfiler().startOperationProfile(operationName);
        }
    }

    /**
     * INTERNAL:
     * Start the operation timing.
     */
    public void startOperationProfile(String operationName, DatabaseQuery query, int weight) {
        if (isInProfile()) {
            getProfiler().startOperationProfile(operationName, query, weight);
        }
    }

    /**
     * Print the connection status with the session.
     */
    public String toString() {
        StringWriter writer = new StringWriter();
        writer.write(getSessionTypeString() + "(" + Helper.cr() + "\t" + getAccessor() + Helper.cr() + "\t" + getDatasourcePlatform() + ")");
        return writer.toString();
    }

    /**
     * INTERNAL:
     * Unwrap the object if required.
     * This is used for the wrapper policy support and EJB.
     */
    public Object unwrapObject(Object proxy) {
        return getDescriptor(proxy).getObjectBuilder().unwrapObject(proxy, this);
    }

    /**
     * PUBLIC:
     * Update the object and all of its privately owned parts in the database.
     * Update should only be used if the application knows that the object is new,
     * otherwise writeObject should be used.
     * The update operation can be customized through using an update query.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     *
     * @see UpdateObjectQuery
     * @see #writeObject(Object)
     */
    public Object updateObject(Object domainObject) throws DatabaseException, OptimisticLockException {
        UpdateObjectQuery query = new UpdateObjectQuery();
        query.setObject(domainObject);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * ADVANCED:
     * Update the write lock value in the identity map.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public void updateWriteLockValue(Object domainObject, Object writeLockValue) {
        getIdentityMapAccessorInstance().updateWriteLockValue(domainObject, writeLockValue);
    }

    /**
     * ADVANCED:
     * Update the write lock value in the identity map.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see org.eclipse.persistence.sessions.IdentityMapAccessor
     */
    public void updateWriteLockValue(Vector primaryKey, Class theClass, Object writeLockValue) {
        getIdentityMapAccessorInstance().updateWriteLockValue(primaryKey, theClass, writeLockValue);
    }

    /**
     * This should not be used, and isn't but for some reason was on the interface.
     * This should be removed next release.
     * @deprecated
     */
    public boolean usesExternalTransactionController() {
        return accessor.usesExternalTransactionController();
    }

    /**
     * ADVANCED:
     * Customers may experience errors with older code, that uses query events on commit, and 9.0.4 of TopLink.
     * Setting this flag to true should remove those errors.  Any new code should not use this flag but
     * instead use API on DescriptorEvent to update the values in the Objects and the
     * UnitOfWork Change Sets as well;
    */
    public boolean usesOldCommit() {
        return this.usesOldCommit;
    }

    /**
     * ADVANCED:
     * This can be used to help debugging an object identity problem.
     * An object identity problem is when an object in the cache references an object not in the cache.
     * This method will validate that all cached objects are in a correct state.
     */
    public void validateCache() {
        getIdentityMapAccessorInstance().validateCache();
    }

    /**
     * INTERNAL:
     * This method will be used to update the query with any settings required
     * For this session.  It can also be used to validate execution.
     */
    public void validateQuery(DatabaseQuery query) {
        // a no-op for this class
    }

    /**
     * TESTING:
     * This is used by testing code to ensure that a deletion was successful.
     */
    public boolean verifyDelete(Object domainObject) {
        ObjectBuilder builder = getDescriptor(domainObject).getObjectBuilder();
        Object implementation = builder.unwrapObject(domainObject, this);

        return builder.verifyDelete(implementation, this);
    }

    /**
     * INTERNAL:
     * Return if a JTS transaction was started by the session.
     * The session will start a JTS transaction if a unit of work or transaction is begun without a JTS transaction present.
     */
    public boolean wasJTSTransactionInternallyStarted() {
        return wasJTSTransactionInternallyStarted;
    }

    /**
     * INTERNAL:
     * Wrap the object if required.
     * This is used for the wrapper policy support and EJB.
     */
    public Object wrapObject(Object implementation) {
        return getDescriptor(implementation).getObjectBuilder().wrapObject(implementation, this);
    }

    /**
     * INTERNAL:
     * Write all of the objects and all of their privately owned parts in the database.
     * The allows for a group of new objects to be commited as a unit.
     * The objects will be commited through a single transactions and any
     * foreign keys/circular references between the objects will be resolved.
     */
    protected void writeAllObjects(IdentityHashtable domainObjects) throws DatabaseException, OptimisticLockException {
        getCommitManager().commitAllObjects(domainObjects);
    }

    /**
     * INTERNAL:
     * Write all of the objects and all of their privately owned parts in the database.
     * The allows for a group of new objects to be commited as a unit.
     * The objects will be commited through a single transactions and any
     * foreign keys/circular references between the objects will be resolved.
     */
    protected void writeAllObjectsWithChangeSet(UnitOfWorkChangeSet uowChangeSet) throws DatabaseException, OptimisticLockException {
        getCommitManager().commitAllObjectsWithChangeSet(uowChangeSet);
    }

    /**
     * PUBLIC:
     * Write the object and all of its privately owned parts in the database.
     * Write will determine if an insert or an update should be done,
     * it may go to the database to determine this (by default will check the identity map).
     * The write operation can be customized through using an write query.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     *
     * @see WriteObjectQuery
     * @see #insertObject(Object)
     * @see #updateObject(Object)
     */
    public Object writeObject(Object domainObject) throws DatabaseException, OptimisticLockException {
        WriteObjectQuery query = new WriteObjectQuery();
        query.setObject(domainObject);
        query.setIsExecutionClone(true);
        return executeQuery(query);
    }

    /**
     * INTERNAL:
     * This method notifies the accessor that a particular sets of writes has
     * completed.  This notification can be used for such thing as flushing the
     * batch mechanism
     */
    public void writesCompleted() {
        getAccessor().writesCompleted(this);
    }

    /**
      * INTERNAL:
      * RemoteCommandManager method. This is a required method in order
      * to implement the CommandProcessor interface.
      * Process the remote command from the RCM. The command may have come from
      * any remote session or application. Since this is a TopLink session we can
      * always assume that the object that we receive here will be a Command object.
      */
    public void processCommand(Object command) {
        ((Command)command).executeWithSession(this);
    }

    /**
     * PUBLIC:
     * Return the CommandManager that allows this session to act as a
     * CommandProcessor and receive or propagate commands from/to the
     * TopLink cluster.
     *
     * @see CommandManager
     * @return The CommandManager instance that controls the remote command
     * service for this session
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * ADVANCED:
     * Set the CommandManager that allows this session to act as a
     * CommandProcessor and receive or propagate commands from/to the
     * TopLink cluster.
     *
     * @see CommandManager
     * @param mgr The CommandManager instance to control the remote command
     * service for this session
     */
    public void setCommandManager(CommandManager mgr) {
        commandManager = mgr;
    }

    /**
     * PUBLIC:
     * Return whether changes should be propagated to other sessions or applications
     * in a TopLink cluster through the Remote Command Manager mechanism. In order for
     * this to occur the CommandManager must be set.
     *
     * @see #setCommandManager(CommandManager)
     * @return True if propagation is set to occur, false if not
     */
    public boolean shouldPropagateChanges() {
        return shouldPropagateChanges;
    }

    /**
     * ADVANCED:
     * Set whether changes should be propagated to other sessions or applications
     * in a TopLink cluster through the Remote Command Manager mechanism. In order for
     * this to occur the CommandManager must be set.
     *
     * @see #setCommandManager(CommandManager)
     * @param choice If true (and the CommandManager is set) then propagation will occur
     */
    public void setShouldPropagateChanges(boolean choice) {
        shouldPropagateChanges = choice;
    }

    /**
     * INTERNAL:
     * RemoteCommandManager method. This is a required method in order
     * to implement the CommandProcessor interface.
     * Return true if a message at the specified log level would be logged given the
     * log level setting of this session. This can be used by the CommandManager to
     * know whether it should even bother to create the localized strings and call
     * the logMessage method, or if it would only find that the message would not be
     * logged because the session level does not permit logging. The log level passed
     * in will be one of the constants LOG_ERROR, LOG_WARNING, LOG_INFO, and LOG_DEBUG
     * defined in the CommandProcessor interface.
     */
    public boolean shouldLogMessages(int logLevel) {
        if (LOG_ERROR == logLevel) {
            return getLogLevel(SessionLog.PROPAGATION) <= SessionLog.SEVERE;
        }
        if (LOG_WARNING == logLevel) {
            return getLogLevel(SessionLog.PROPAGATION) <= SessionLog.WARNING;
        }
        if (LOG_INFO == logLevel) {
            return getLogLevel(SessionLog.PROPAGATION) <= SessionLog.FINER;
        }
        if (LOG_DEBUG == logLevel) {
            return getLogLevel(SessionLog.PROPAGATION) <= SessionLog.FINEST;
        }
        return false;
    }

    /**
     * INTERNAL:
     * RemoteCommandManager method. This is a required method in order
     * to implement the CommandProcessor interface.
     * Log the specified message string at the specified level if it should be logged
     * given the log level setting in this session. The log level passed in will be one
     * of the constants LOG_ERROR, LOG_WARNING, LOG_INFO, and LOG_DEBUG defined in the
     * CommandProcessor interface.
     */
    public void logMessage(int logLevel, String message) {
        if (shouldLogMessages(logLevel)) {
            int level;
            switch (logLevel) {
            case CommandProcessor.LOG_ERROR:
                level = SessionLog.SEVERE;
                break;
            case CommandProcessor.LOG_WARNING:
                level = SessionLog.WARNING;
                break;
            case CommandProcessor.LOG_INFO:
                level = SessionLog.FINER;
                break;
            case CommandProcessor.LOG_DEBUG:
                level = SessionLog.FINEST;
                break;
            default:
                level = SessionLog.ALL;
                ;
            }
            log(level, message, null, null, false);
        }
    }

    /**
     * PUBLIC:
     * <p>
     * Return the log level
     * </p><p>
     *
     * @return the log level
     * </p><p>
     * @param category  the string representation of a TopLink category, e.g. "sql", "transaction" ...
     * </p>
     */
    public int getLogLevel(String category) {
        return getSessionLog().getLevel(category);
    }

    /**
     * PUBLIC:
     * <p>
     * Return the log level
     * </p><p>
     * @return the log level
     * </p>
     */
    public int getLogLevel() {
        return getSessionLog().getLevel();
    }

    /**
     * PUBLIC:
     * <p>
     * Set the log level
     * </p><p>
     *
     * @param level     the new log level
     * </p>
     */
    public void setLogLevel(int level) {
        getSessionLog().setLevel(level);
    }

    /**
     * PUBLIC:
     * <p>
     * Check if a message of the given level would actually be logged.
     * </p><p>
     *
     * @return true if the given message level will be logged
     * </p><p>
     * @param level  the log request level
     * @param category  the string representation of a TopLink category
     * </p>
     */
    public boolean shouldLog(int Level, String category) {
        return getSessionLog().shouldLog(Level, category);
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message with level and category that needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param category  the string representation of a TopLink category.
     * </p>
     */
    public void log(int level, String category, String message) {
        if (!shouldLog(level, category)) {
            return;
        }
        log(level, category, message, (Object[])null);
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message with level, category and a parameter that needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param category  the string representation of a TopLink category.
     * </p><p>
     * @param param  a parameter of the message
     * </p>
     */
    public void log(int level, String category, String message, Object param) {
        if (!shouldLog(level, category)) {
            return;
        }
        log(level, category, message, new Object[] { param });
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message with level, category and two parameters that needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param category  the string representation of a TopLink category.
     * </p><p>
     * @param param1  a parameter of the message
     * </p><p>
     * @param param2  second parameter of the message
     * </p>
     */
    public void log(int level, String category, String message, Object param1, Object param2) {
        if (!shouldLog(level, category)) {
            return;
        }
        log(level, category, message, new Object[] { param1, param2 });
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message with level, category and three parameters that needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param category  the string representation of a TopLink category.
     * </p><p>
     * @param param1  a parameter of the message
     * </p><p>
     * @param param2  second parameter of the message
     * </p><p>
     * @param param3  third parameter of the message
     * </p>
     */
    public void log(int level, String category, String message, Object param1, Object param2, Object param3) {
        if (!shouldLog(level, category)) {
            return;
        }
        log(level, category, message, new Object[] { param1, param2, param3 });
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message with level, category and an array of parameters that needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param category  the string representation of a TopLink category.
     * </p><p>
     * @param params  array of parameters to the message
     * </p>
     */
    public void log(int level, String category, String message, Object[] params) {
        log(level, category, message, params, null);
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message with level, category, parameters and accessor that needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param params  array of parameters to the message
     * </p><p>
     * @param accessor  the connection that generated the log entry
     * </p><p>
     * @param category  the string representation of a TopLink category.
     * </p>
     */
    public void log(int level, String category, String message, Object[] params, Accessor accessor) {
        log(level, category, message, params, accessor, true);
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message with level, category, parameters and accessor.  shouldTranslate determines if the message needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param params  array of parameters to the message
     * </p><p>
     * @param accessor  the connection that generated the log entry
     * </p><p>
     * @param category  the string representation of a TopLink category.
     * </p><p>
     * @param shouldTranslate  true if the message needs to be translated.
     * </p>
     */
    public void log(int level, String category, String message, Object[] params, Accessor accessor, boolean shouldTranslate) {
        if (shouldLog(level, category)) {
            startOperationProfile(SessionProfiler.Logging);
            log(new SessionLogEntry(level, category, this, message, params, accessor, shouldTranslate));
            endOperationProfile(SessionProfiler.Logging);
        }
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message with level, parameters and accessor that needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param params  array of parameters to the message
     * </p><p>
     * @param accessor  the connection that generated the log entry
     * </p>
     */
    public void log(int level, String message, Object[] params, Accessor accessor) {
        log(level, message, params, accessor, true);
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message with level, parameters and accessor.  shouldTranslate determines if the message needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param params  array of parameters to the message
     * </p><p>
     * @param accessor  the connection that generated the log entry
     * </p><p>
     * @param shouldTranslate  true if the message needs to be translated.
     * </p>
     */
    public void log(int level, String message, Object[] params, Accessor accessor, boolean shouldTranslate) {
        if (shouldLog(level, null)) {
            startOperationProfile(SessionProfiler.Logging);
            log(new SessionLogEntry(level, this, message, params, accessor, shouldTranslate));
            endOperationProfile(SessionProfiler.Logging);
        }
    }

    /**
     * PUBLIC:
     * <p>
     * Log a throwable with level and category.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param category  the string representation of a TopLink category.
     * </p><p>
     * @param throwable  a Throwable
     * </p>
     */
    public void logThrowable(int level, String category, Throwable throwable) {
        // Must not create the log if not logging as is a performance issue.
        if (shouldLog(level, category)) {
            startOperationProfile(SessionProfiler.Logging);
            log(new SessionLogEntry(this, level, category, throwable));
            endOperationProfile(SessionProfiler.Logging);
        }
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a severe level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void severe(String message, String category) {
        log(SessionLog.SEVERE, category, message);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a warning level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void warning(String message, String category) {
        log(SessionLog.WARNING, category, message);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a info level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void info(String message, String category) {
        log(SessionLog.INFO, category, message);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a config level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void config(String message, String category) {
        log(SessionLog.CONFIG, category, message);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a fine level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void fine(String message, String category) {
        log(SessionLog.FINE, category, message);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a finer level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void finer(String message, String category) {
        log(SessionLog.FINER, category, message);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a finest level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void finest(String message, String category) {
        log(SessionLog.FINEST, category, message);
    }

    /**
     * PUBLIC:
     * Allow any SEVERE level exceptions that occur within TopLink to be logged and handled by the exception handler.
     */
    public Object handleSevere(RuntimeException exception) throws RuntimeException {
        logThrowable(SessionLog.SEVERE, null, exception);
        if (hasExceptionHandler()) {
            return getExceptionHandler().handleException(exception);
        } else {
            throw exception;
        }
    }

    /**
      * INTERNAL:
      */
    public void releaseReadConnection(Accessor connection) {
        //bug 4668234 -- used to only release connections on server sessions but should always release
        //do nothing -- overidden in UnitOfWork,ClientSession and ServerSession
    }
    
    /**
     * INTERNAL:
     * This method will be used to copy all toplink named queries defined in descriptors into the session.
     * @param allowSameQueryNameDiffArgsCopyToSession  if the value is true, it allow 
     * multiple queries of the same name but different arguments to be copied to the session.
     */
    public void copyDescriptorNamedQueries(boolean allowSameQueryNameDiffArgsCopyToSession){
        Vector descriptors = getProject().getOrderedDescriptors();
        for( Iterator descItr = descriptors.iterator(); descItr.hasNext(); ){
            Map queries  = ((ClassDescriptor)descItr.next()).getQueryManager().getQueries();
            if(queries!=null && queries.size()>0){
                for(Iterator keyValueItr = queries.entrySet().iterator();keyValueItr.hasNext();){
                    Map.Entry entry = (Map.Entry) keyValueItr.next();
                    Vector thisQueries = (Vector)entry.getValue();
                    if(thisQueries!=null && thisQueries.size()>0){
                        for(Iterator thisQueriesItr=thisQueries.iterator();thisQueriesItr.hasNext();){
                            DatabaseQuery queryToBeAdded = (DatabaseQuery)thisQueriesItr.next();
                            if(allowSameQueryNameDiffArgsCopyToSession){
                                addQuery(queryToBeAdded);
                            } else { 
                                if(getQuery(queryToBeAdded.getName())==null){
                                    addQuery(queryToBeAdded);
                                }else{
                                    this.log(SessionLog.WARNING, SessionLog.PROPERTIES, "descriptor_named_query_cannot_be_added", new Object[]{queryToBeAdded,queryToBeAdded.getName(),queryToBeAdded.getArgumentTypes()});
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}