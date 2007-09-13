/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.jpql;

import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.expressions.*;
import org.eclipse.persistence.testing.models.employee.domain.*;

public class DistinctTest extends org.eclipse.persistence.testing.tests.jpql.JPQLTestCase {
    public void setup() {
        // Get an original employee...
        ReadAllQuery raq = new ReadAllQuery();

        ExpressionBuilder employee = new ExpressionBuilder();
        Expression whereClause = employee.get("lastName").equal("Smith");

        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);
        raq.useDistinct();

        setOriginalOject(getSession().executeQuery(raq));

        // Set the ejbql to be tested
        setEjbqlString("SELECT DISTINCT OBJECT(emp) FROM Employee emp WHERE emp.lastName = \'Smith\'");

        super.setup();
    }

    public void test() throws Exception {
        super.test();
    }

    public void verify() throws Exception {
        super.verify();
    }
}