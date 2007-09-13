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

import java.util.*;
import java.sql.*;
import java.io.*;
import org.eclipse.persistence.exceptions.*;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.queries.ModifyQuery;
import org.eclipse.persistence.sessions.SessionProfiler;

/**
 * INTERNAL:
 *    ParameterizedSQLBatchWritingMechanism is a private class, used by the DatabaseAccessor. it provides the required
 *  behaviour for batching statements, for write, with parameter binding turned on.<p>
 *    In the future TopLink may be modified to control the order of statements passed to the accessor.  This
 *  would prevent checking the last executed statement to ensure that they match.<p>
 *
 *    @since OracleAS TopLink 10<i>g</i> (9.0.4)
 */
public class ParameterizedSQLBatchWritingMechanism implements BatchWritingMechanism {

    /**
     * This memeber variable stores the reference to the DatabaseAccessor that is
     * using this Mechanism to handle the batch writing
     */
    protected DatabaseAccessor databaseAccessor;

    /**
     *  This member variable is used to keep track of the last SQL string that was executed
     *  by this mechanism.  If the current string and previous string match then simply
     *  bind in the arguments, otherwise end previous batch and start a new batch
     */
    protected DatabaseCall previousCall;

    /**
     * This variable contains a list of the parameters list passed into the query
     */
    protected ArrayList parameters;
    protected DatabaseCall lastCallAppended;
    
    //bug 4241441: used to keep track of the values returned from the driver via addBatch and executeStatment
    protected int executionCount;
    //bug 4241441: increments with each addBatch call.  Used to compare against value returned from driver for 
    //  optimistic locking
    protected int statementCount;

    public ParameterizedSQLBatchWritingMechanism(DatabaseAccessor databaseAccessor) {
        this.databaseAccessor = databaseAccessor;
        this.parameters = new ArrayList(10);
    }

    /**
     * INTERNAL:
     * This method is called by the DatabaseAccessor to add this statement to the list of statements
     * being batched.  This call may result in the Mechanism executing the batched statements and
     * possibly, switching out the mechanisms
     */
    public void appendCall(AbstractSession session, DatabaseCall dbCall) {
        if (dbCall.hasParameters()) {
            //make an equality check on the String, because if we are caching statements then
            //we will not have to perform the string comparison multiple times.
            if (this.previousCall == null) {
                this.previousCall = dbCall;
                this.parameters.add(dbCall.getParameters());
            } else {
                if (this.previousCall.getSQLString().equals(dbCall.getSQLString()) && (this.parameters.size() < this.databaseAccessor.getLogin().getPlatform().getMaxBatchWritingSize())) {
                    this.parameters.add(dbCall.getParameters());
                } else {
                    executeBatchedStatements(session);
                    this.appendCall(session, dbCall);
                }
            }
            this.lastCallAppended = dbCall;
            // feature for bug 4104613, allows users to force statements to flush on execution
            if (((ModifyQuery) dbCall.getQuery()).forceBatchStatementExecution())
            {
              executeBatchedStatements(session);
            }
        } else {
            executeBatchedStatements(session);
            switchMechanisms(session, dbCall);
        }
    }

    /**
     * INTERNAL:
     * This method is used to clear the batched statements without the need to execute the statements first
     * This is used in the case of rollback.
     */
    public void clear() {
        this.previousCall = null;
        this.parameters.clear();
        statementCount = executionCount  = 0;
    }

    /**
     * INTERNAL:
     * This method is used by the DatabaseAccessor to clear the batched statements in the
     * case that a non batchable statement is being execute
     */
    public void executeBatchedStatements(AbstractSession session) {
        if (this.parameters.isEmpty()) {
            return;
        }

        session.log(SessionLog.FINER, SessionLog.SQL, "begin_batch_statements", null, this.databaseAccessor);
        if (session.shouldLog(SessionLog.FINE, SessionLog.SQL)) {
            session.log(SessionLog.FINE, SessionLog.SQL, this.previousCall.getSQLString(), null, this.databaseAccessor, false);
            // took this loggin part from SQLCall
            for (Iterator iterator = this.parameters.iterator(); iterator.hasNext();) {
                StringWriter writer = new StringWriter();
                DatabaseCall.appendLogParameters((Collection)iterator.next(), this.databaseAccessor, writer, session);                
                session.log(SessionLog.FINE, SessionLog.SQL, writer.toString(), null, this.databaseAccessor, false);
            }
        }
        session.log(SessionLog.FINER, SessionLog.SQL, "end_batch_statements", null, this.databaseAccessor);

        try {
            this.databaseAccessor.incrementCallCount(session);// Decrement occurs in close.
            //bug 4241441: need to keep track of rows modified and throw opti lock exception if needed
            PreparedStatement statement = this.prepareBatchStatements(session);
            executionCount += this.databaseAccessor.executeJDK12BatchStatement(statement, this.lastCallAppended, session, true);
            this.databaseAccessor.writeStatementsCount++;
            
            if (this.previousCall.hasOptimisticLock() && (executionCount!=statementCount)){
                throw OptimisticLockException.batchStatementExecutionFailure();
            }
        } finally {
            // Reset the batched sql string
            //we MUST clear the mechanism here in order to append the new statement.
            this.clear();
        }
    }

    /**
     * INTERNAL:
     * Swaps out the Mechanism for the other Mechanism
     */
    protected void switchMechanisms(AbstractSession session, DatabaseCall dbCall) {
        this.databaseAccessor.setActiveBatchWritingMechanismToDynamicSQL();
        this.databaseAccessor.getActiveBatchWritingMechanism().appendCall(session, dbCall);
    }

    /**
     * INTERNAL:
     * This method is used to build the parameterized batch statement for the JDBC2.0 specification
     */
    protected PreparedStatement prepareBatchStatements(AbstractSession session) throws DatabaseException {
        PreparedStatement statement = null;

        try {
            session.startOperationProfile(SessionProfiler.SQL_PREPARE, null, SessionProfiler.ALL);
            try {
                boolean shouldUnwrapConnection = session.getPlatform().usesNativeBatchWriting();
                statement = (PreparedStatement)this.databaseAccessor.prepareStatement(this.previousCall, session, shouldUnwrapConnection);
                databaseAccessor.getPlatform().prepareBatchStatement(statement);
                // iterate over the parameter lists that were batched.
                for (int statementIndex = 0; statementIndex < this.parameters.size();
                         ++statementIndex) {
                    // TopLink uses Vector internall, may want to change this
                    Vector parameterList = (Vector)this.parameters.get(statementIndex);
                    for (int index = 0; index < parameterList.size(); index++) {
                        session.getPlatform().setParameterValueInDatabaseCall(parameterList, statement, index, session);
                    }

                    //batch the parameters to the statement
                    statementCount++;
                    executionCount += this.databaseAccessor.getPlatform().addBatch(statement);
                }
            } finally {
                session.endOperationProfile(SessionProfiler.SQL_PREPARE, null, SessionProfiler.ALL);
            }
        } catch (SQLException exception) {
            try {// Ensure that the statement is closed, but still ensure that the real exception is thrown.
                this.databaseAccessor.closeStatement(statement, session, null);
            } catch (SQLException closeException) {
            }
            throw DatabaseException.sqlException(exception, this.databaseAccessor, session);
        } catch (RuntimeException exception) {
            try {// Ensure that the statement is closed, but still ensure that the real exception is thrown.
                this.databaseAccessor.closeStatement(statement, session, null);
            } catch (SQLException closeException) {
            }
            throw exception;
        }
        return statement;
    }

    /**
     * INTERNAL:
     * Sets the accessor that this mechanism will be used
     */
    public void setAccessor(DatabaseAccessor accessor) {
        this.databaseAccessor = accessor;
    }
}