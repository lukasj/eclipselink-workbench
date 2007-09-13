/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.queries.inmemory;

import java.util.Vector;
import org.eclipse.persistence.testing.models.employee.domain.PhoneNumber;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.testing.framework.*;

/**
 * Test selecting using an object's primary key to ensure that it does not go to the databaase.
 */
public class UnitOfWorkConformWithOrderTest extends AutoVerifyTestCase {
    public UnitOfWorkConformWithOrderTest() {
        setDescription("Test that the query maintains order for non changed objects.");
    }

    public void test() {
        // Make a query an search Number greaterThan "00005"
        ExpressionBuilder emp = new ExpressionBuilder();
        Expression exp = emp.get("areaCode").equal(613);
        ReadAllQuery query = new ReadAllQuery(PhoneNumber.class);
        query.setSelectionCriteria(exp);
        query.conformResultsInUnitOfWork();// set Conforming
        query.addOrdering(emp.get("owner").get("lastName").descending());
        UnitOfWork uow = getSession().acquireUnitOfWork();
        Vector v = (Vector)uow.executeQuery(query);
        Vector v2 = (Vector)uow.executeQuery(query);

        if (!v.equals(v2)) {
            throw new TestErrorException("Order not maintained when conforming");
        }
    }
}