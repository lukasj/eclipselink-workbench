/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.distributedservers.rcm;

import java.util.Hashtable;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.testing.tests.distributedservers.DistributedServer;
import org.eclipse.persistence.testing.tests.distributedservers.DistributedServersModel;
import org.eclipse.persistence.testing.tests.isolatedsession.IsolatedEmployee;
import org.eclipse.persistence.testing.framework.TestErrorException;


/**
 * Bug 3587273
 * Ensure objects that are isolated are not sent by cache synchronization.
 */
public class IsolatedObjectNotSentTest extends ConfigurableCacheSyncDistributedTest {
    protected IsolatedEmployee employee = null;
    protected Expression expression = null;
    protected IsolatedEmployee distributedEmployee = null;

    public IsolatedObjectNotSentTest() {
        setDescription("Test to ensure that objects that are set as isolated will not be sent over Cache Synchronization.");
    }

    public IsolatedObjectNotSentTest(Hashtable cacheSyncConfigValues) {
        setDescription("Test to ensure that objects that are set as isolated will not be sent over Cache Synchronization.");
    }

    public void setup() {
        super.setup();
        ExpressionBuilder employees = new ExpressionBuilder();
        expression = employees.get("firstName").equal("Andy");
        expression = expression.and(employees.get("lastName").equal("McDurmont"));
        // ensure our employee is in one of the distributed caches
        DistributedServer server = (DistributedServer)DistributedServersModel.getDistributedServers().get(0);
        distributedEmployee = (IsolatedEmployee)server.getDistributedSession().readObject(IsolatedEmployee.class, expression);
    }

    public void test() {
        employee = (IsolatedEmployee)getSession().readObject(IsolatedEmployee.class, expression);

        UnitOfWork uow = getSession().acquireUnitOfWork();
        IsolatedEmployee employeeClone = (IsolatedEmployee)uow.registerObject(employee);
        employeeClone.setSalary(employeeClone.getSalary() + 1000);
        uow.commit();
    }

    public void verify() {
        distributedEmployee = (IsolatedEmployee)getObjectFromDistributedCache(employee);
        if (distributedEmployee.getSalary() == employee.getSalary()) {
            throw new TestErrorException("The employee was sent by cache synchronization, but should not have been since it is isolated.");
        }
    }
}