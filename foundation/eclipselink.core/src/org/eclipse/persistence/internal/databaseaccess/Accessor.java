/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.databaseaccess;

import java.util.Vector;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.sessions.Login;
import org.eclipse.persistence.queries.Call;

/**
 * INTERNAL:
 * Accessor defines the interface used primarily by the assorted
 * TopLink Sessions to interact with a data store. In "normal"
 * TopLink this data store is a relational database. But this interface
 * also allows developers using the TopLink SDK to develop Accessors
 * to other, non-relational, data stores.<p>
 *
 * Accessors must implement the following behavior: <ul>
 *    <li>connect to and disconnect from the data store
 *    <li>handle transaction contexts
 * <li>execute calls that read, insert, update, and delete data
 * <li>keep track of concurrently executing calls
 * <li>supply metadata about the data store
 * </ul>
 *
 * @see org.eclipse.persistence.internal.sessions.AbstractSession
 * @see Call
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 */
public interface Accessor extends Cloneable {

    /**
     * To be called after JTS transaction has been completed (committed or rolled back)
     */
    public void afterJTSTransaction();

    /**
     * Begin a transaction on the data store.
     */
    void beginTransaction(AbstractSession session) throws DatabaseException;

    /**
     * Return a clone of the accessor.
     */
    Object clone();

    /**
     * Close the accessor's connection.
     * This is used only for external connection pooling
     * when it is intended for the connection to be reconnected in the future.
     */
    void closeConnection();

    /**
     * Commit a transaction on the data store.
     */
    void commitTransaction(AbstractSession session) throws DatabaseException;

    /**
     * Allow calling session to be passed.
     *      
     * The calling session is the session who actually invokes commit or rollback transaction, 
     * it is used to determine whether the connection needs to be closed when using external connection pool.
     * The connection with a externalConnectionPool used by synchronized UOW should leave open until 
     * afterCompletion call back; the connection with a externalConnectionPool used by other type of session 
     * should be closed after transaction was finised.
     *
     * Commit a transaction on the data store 
     */
    void commitTransaction(AbstractSession session, AbstractSession callingSession) throws DatabaseException;
    /**
     * Connect to the data store using the configuration
     * information in the login.
     */

    void connect(Login login, AbstractSession session) throws DatabaseException;

    /**
     * Decrement the number of calls in progress.
     * Used for external pooling.
     */
    void decrementCallCount();

    /**
     * Disconnect from the data store.
     */
    void disconnect(AbstractSession session) throws DatabaseException;

    /**
     * Execute the call.
     * The actual behavior of the execution depends on the type of call.
     * The call may be parameterized where the arguments are in the translation row.
     * The row will be empty if there are no parameters.
     * @return a row, a collection of rows, a row count, or a cursor
     */
    Object executeCall(Call call, AbstractRecord translationRow, AbstractSession session) throws DatabaseException;

    /**
     * Execute any deferred select calls.  This method will generally be called
     * after one or more select calls have been collected in a LOBValueWriter (to be
     * executed after all insert calls are executed).
     * Bug 2804663.
     *
     * @see org.eclipse.persistence.internal.helper.LOBValueWriter#buildAndExecuteCallForLocator(DatabaseCall,Session,Accessor)
     */
    void flushSelectCalls(AbstractSession session);

    /**
     * Return the number of calls currently in progress.
     * Used for load balancing and external pooling.
     */
    int getCallCount();

    /**
     * Return the column metadata for the specified
     * selection criteria.
     */
    Vector getColumnInfo(String catalog, String schema, String tableName, String columnName, AbstractSession session) throws DatabaseException;

    /**
     * Return the JDBC connection for relational accessors.
     * This will fail for non-relational accessors.
     */
    java.sql.Connection getConnection();

    /**
     * Return the driver level connection,
     * this will need to be cast to the implementation class for the data access type being used.
     */
    Object getDatasourceConnection();

    /**
     * Return the table metadata for the specified
     * selection criteria.
     */
    Vector getTableInfo(String catalog, String schema, String tableName, String[] types, AbstractSession session) throws DatabaseException;

    /**
     * Increment the number of calls in progress.
     * Used for external pooling.
     */
    void incrementCallCount(AbstractSession session);

    /**
     * Return whether the accessor is connected to the data store.
     */
    boolean isConnected();

    /**
     * Reconnect to the database. This can be used if the connection was
     * temporarily disconnected or if it timed out.
     */
    void reestablishConnection(AbstractSession session) throws DatabaseException;

    /**
     * Roll back a transaction on the data store.
     */
    void rollbackTransaction(AbstractSession session) throws DatabaseException;


    /**
     * Allow calling session to be passed.     
     * 
     * The calling session is the session who actually invokes commit or rollback transaction, 
     * it is used to determine whether the connection needs to be closed when using external connection pool.
     * The connection with a externalConnectionPool used by synchronized UOW should leave open until 
     * afterCompletion call back; the connection with a externalConnectionPool used by other type of session 
     * should be closed after transaction was finised.
     * 
     * Roll back a transaction on the data store.
     */
    void rollbackTransaction(AbstractSession session,AbstractSession callingSession) throws DatabaseException;
    
    /**
     * Return whether the accessor uses an external
     * transaction controller (e.g. JTS).
     */
    boolean usesExternalTransactionController();

    /**
     * This method will be called after a series of writes have been issued to
     * mark where a particular set of writes has completed.  It will be called
     * from commitTransaction and may be called from writeChanges.   Its main
     * purpose is to ensure that the batched statements have been executed
     */
    public void writesCompleted(AbstractSession session);
}