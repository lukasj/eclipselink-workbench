/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.workbenchintegration.ExpressionPersistence;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.testing.framework.AutoVerifyTestCase;
import org.eclipse.persistence.testing.framework.TestErrorException;
import org.eclipse.persistence.testing.framework.TestWarningException;


/**
 * Test that the original expression created in-memory matches the one read from XML.
 */
public class ExpressionPersistenceTest extends AutoVerifyTestCase {
    protected DatabaseQuery basicQuery;
    protected DatabaseQuery systemQuery;
    protected String queryName;

    public ExpressionPersistenceTest(String queryName, DatabaseQuery query) {
        this.basicQuery = query;
        this.queryName = queryName;
        setName(getName() + ":" + queryName);
        setDescription("Test that expressions persisted by the WorkBench in the deployent XML works correctly");
    }

    public void test() {
        if ((queryName.startsWith("AddStandardDeviationReportQuery") || 
             queryName.startsWith("AddVarianceReportQuery")) && 
            (getSession().getPlatform().isSybase() || getSession().getPlatform().isSQLServer())) {
            throw new TestWarningException("The test is not supported on this database.");
        }
        getSession().executeQuery(basicQuery);
        systemQuery = 
                (DatabaseQuery)((ClassDescriptor)getSession().getDescriptor(org.eclipse.persistence.testing.models.employee.domain.Employee.class)).getQueryManager().getQuery(queryName);
        getSession().executeQuery(systemQuery);
    }

    public void verify() {
        if (!basicQuery.getCall().getSQLString().equals(systemQuery.getCall().getSQLString())) {
            throw new TestErrorException("Persisted query not the same as original");
        }
    }
}