/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.xdb;

import org.eclipse.persistence.testing.framework.*;
import org.eclipse.persistence.sessions.*;

public class InsertWithNullTest extends TestCase {

    public InsertWithNullTest() {
        setDescription("Tests inserting an object with NULL XMLType fields.");
    }

    public void setup() {
    }

    public void reset() {
    }

    public void test() {
        Employee_XML emp = new Employee_XML();
        emp.id = 1024;
        emp.firstName = "Fred";
        emp.lastName = "Flintstone";
        emp.gender = "Male";

        UnitOfWork uow = this.getSession().acquireUnitOfWork();
        uow.registerObject(emp);
        uow.commit();
    }

    public void verify() {
    }
}
