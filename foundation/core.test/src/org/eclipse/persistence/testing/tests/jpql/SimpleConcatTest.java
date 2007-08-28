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

import java.util.*;
import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.expressions.*;
import org.eclipse.persistence.testing.models.employee.domain.*;

public class SimpleConcatTest extends org.eclipse.persistence.testing.tests.jpql.JPQLTestCase {
    public void setup() {
        Employee emp = (Employee)getSomeEmployees().firstElement();

        String partOne;
        String partTwo;
        String ejbqlString;

        partOne = emp.getFirstName().substring(0, 2);

        ExpressionBuilder builder = new ExpressionBuilder();
        Expression whereClause = builder.get("firstName").concat(builder.get("lastName")).like(partOne + "%");

        ReadAllQuery raq = new ReadAllQuery();
        raq.setReferenceClass(Employee.class);
        raq.setSelectionCriteria(whereClause);

        Vector employees = (Vector)getSession().executeQuery(raq);

        ejbqlString = "SELECT OBJECT(emp) FROM Employee emp WHERE ";
        ejbqlString = ejbqlString + "CONCAT(emp.firstName,emp.lastName) LIKE ";
        ejbqlString = ejbqlString + "\"" + partOne + "%\"";

        setEjbqlString(ejbqlString);
        setOriginalOject(employees);
        super.setup();
    }
}