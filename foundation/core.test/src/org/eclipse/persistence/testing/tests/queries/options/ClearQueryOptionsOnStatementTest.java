/* Copyright (c) 2007, Oracle. All rights reserved. */
package org.eclipse.persistence.testing.tests.queries.options;

import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.exceptions.*;
import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.internal.helper.*;
import org.eclipse.persistence.testing.framework.*;
import org.eclipse.persistence.internal.databaseaccess.*;

import java.math.*;
import java.util.*;
import java.sql.*;
import java.lang.reflect.*;

/**
 * Test to verify that max rows, query timeout and result set fetch size are cleared
 * on PreparedStatement objects utilized by TopLink. After a query has been executed,
 * these settings must be cleared so that other queries do not use these options 
 * that are set local to each query. 
 * For Bug 5709179 - MAX-ROWS/TIMEOUT NOT RESET ON CACHED STATEMENTS
 * @author dminsky
 */
public class ClearQueryOptionsOnStatementTest extends AutoVerifyTestCase {

    private List employeesCreated;

    public ClearQueryOptionsOnStatementTest() {
        super();
        setDescription("This test verifies max rows, query timeout & result set fetch size are cleared on prepared statements");
    }
    
    public void setup() {
        // must enable statement caching
        getDatabaseSession().getLogin().cacheAllStatements();
        getDatabaseSession().getIdentityMapAccessor().initializeAllIdentityMaps();
        UnitOfWork uow = getDatabaseSession().acquireUnitOfWork();
        employeesCreated = new ArrayList(10);
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(190), "Jak"));
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(191), "Daxter"));
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(192), "Ratchet"));
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(193), "Clank"));
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(194), "Crash"));
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(195), "Sonic"));
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(196), "Mario"));
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(197), "Luigi"));
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(198), "Peach"));
        employeesCreated.add(new QueryOptionEmployee(new BigDecimal(199), "Bowser"));
        uow.registerAllObjects(employeesCreated);
        uow.commit();
    }
    
    public void test() {
        DatabaseSession session = getDatabaseSession();
        testQueryTimeoutReset(session); 
        testMaxRowsReset(session);
        testResultSetFetchSizeReset(session);   
    }
    
    public void testMaxRowsReset(Session session) {
        // MAX ROWS
        // 1. Execute query to read employees with a max-rows set to 4
        ReadAllQuery query = new ReadAllQuery(QueryOptionEmployee.class);
        query.setMaxRows(4);
        List employees = (List) session.executeQuery(query);
        
        // 2. Check employees read = 4 per MaxRows setting - just with an assert
        if (employees.size() != 4) {
            throw new TestErrorException("Max Rows reset - Rows returned: " + employees.size() + " (expecting 4)");
        }
        
        // 3. Execute another (new) query 100 times with same SQL & no max-rows setting
        for (int iteration = 0; iteration < 100; iteration++) {
            query = new ReadAllQuery(QueryOptionEmployee.class);
            employees = (List) session.executeQuery(query);
            if (employees.size() <= 4) {
                throw new TestErrorException("Max Rows reset - Rows returned: " + employees.size() + " (expecting >= 10)");
            }
        }
    }

    public void testResultSetFetchSizeReset(Session session) {
        // Resultset fetch size
        ReadAllQuery query = new ReadAllQuery(QueryOptionEmployee.class);
        query.useScrollableCursor();
        
        String sql = "SELECT ID, NAME, HISTORY_ID FROM QUERY_OPTION_EMPLOYEE";
        int fetchSize = 100;
        query.setSQLString(sql);
        query.setFetchSize(fetchSize);
        
        // The statement cache is protected - need to obtain the internal hashtable from the accessor 
        org.eclipse.persistence.internal.sessions.DatabaseSessionImpl impl = 
            (org.eclipse.persistence.internal.sessions.DatabaseSessionImpl) session;
        DatabaseAccessor accessor = (DatabaseAccessor) impl.getAccessor();
        Hashtable statementCache = null;
        try {
            Method method = Helper.getDeclaredMethod(
                DatabaseAccessor.class, 
                "getStatementCache", 
                new Class[]{});
            statementCache = (Hashtable) method.invoke(accessor, new Object[] {});
        } catch (Exception nsme) {
            throwError("Could not invoke DatabaseAccessor>>getStatementCache()", nsme);
        }

        // now cache the statement's previous fetch size
        int previousFetchSize = 0;
        Statement statement = (Statement) statementCache.get(sql);
        if (statement != null) {
            try {
                previousFetchSize = statement.getFetchSize();
            } catch (SQLException sqle) {
                throwError("Error whilst invoking intial Statement>>getFetchSize()", sqle);
            }
        }

        // execute query        
        ScrollableCursor cursor = (ScrollableCursor) session.executeQuery(query);
        List employees = new ArrayList();
        while (cursor.hasNext()) {
            employees.add(cursor.next());
        }
        cursor.close();

        // now check the statement
        int postQueryFetchSize = 0;
        statement = (Statement) statementCache.get(sql);
        if (statement != null) {
            try {
                postQueryFetchSize = statement.getFetchSize();
            } catch (SQLException sqle) {
                throwError("Error whilst invoking secondary Statement>>getFetchSize()", sqle);
            }
        }
        
        if (postQueryFetchSize == fetchSize) {
            throwError("Statement fetch size was not reset");
        }

    }
    
    public void testQueryTimeoutReset(Session session) {
        boolean query1TimedOut = false;
        boolean query2TimedOut = false;
        
        String sql;
        if(getSession().getLogin().getDatasourcePlatform().isDB2())
        {
          sql = "SELECT SUM(e.EMP_ID) from EMPLOYEE e , EMPLOYEE b, EMPLOYEE c,EMPLOYEE d";
        } else if (getSession().getLogin().getDatasourcePlatform().isSybase())
        {
          sql = "SELECT SUM(e.EMP_ID) from EMPLOYEE a , EMPLOYEE b, EMPLOYEE c, EMPLOYEE d, EMPLOYEE e, EMPLOYEE f, EMPLOYEE g";
        } else
        {
          sql = "SELECT SUM(e.EMP_ID) from EMPLOYEE e , EMPLOYEE b, EMPLOYEE c, EMPLOYEE c, EMPLOYEE b, EMPLOYEE c, EMPLOYEE c";
        }
        // set the lowest timeout value on a query which is virtually guaranteed to produce a timeout
        try {
            DataReadQuery query = new DataReadQuery();
            query.setSQLString(sql);
            query.setQueryTimeout(1);
            session.executeQuery(query);
        } catch (Exception e) {
            if (e instanceof DatabaseException) {
                // cache value for debug purposes
                query1TimedOut = true;
            }
        }
        
        // do not set a timeout on the query, and test for a timeout
        try {
            DataReadQuery query = new DataReadQuery();
            query.setSQLString(sql);
            session.executeQuery(query);
        } catch (Exception e) {
            if (e instanceof DatabaseException) {
                query2TimedOut = true;
            }
        }
        
        // we're interested in if query 2 timed out
        // if no timeout value was set, query 2 should not produce a timeout
        if (query2TimedOut == true) {
            throw new TestErrorException("Query timeout occurred - PreparedStatement query timeout setting not cleared");
        }
    }
    
    public void reset() {
        getDatabaseSession().getLogin().dontCacheAllStatements();
        UnitOfWork uow = getDatabaseSession().acquireUnitOfWork();
        uow.deleteAllObjects(employeesCreated);
        uow.commit();
        getDatabaseSession().getIdentityMapAccessor().initializeAllIdentityMaps();        
    }

}