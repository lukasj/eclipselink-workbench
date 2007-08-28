/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.distributedcache;

import org.eclipse.persistence.testing.models.directmap.DirectMapMappingsSystem;
import org.eclipse.persistence.testing.framework.*;
import org.eclipse.persistence.testing.models.employee.relational.EmployeeSystem;

public class DistributedCacheModel extends TestModel {
    public DistributedCacheModel() {
        setDescription("This suite tests functionality simulating distributed cache merge.");
    }

    public void addTests() {
        TestSuite suite1 = new TestSuite();
        suite1.setName("Many To Many Test Suite");
        suite1.addTest(new ManyToManyMergeTest());
        TestSuite suite2 = new TestSuite();
        suite2.setName("One To Many Test Suite");
        suite2.addTest(new OneToManyMergeTest());
        TestSuite suite3 = new TestSuite();
        suite3.setName("DirectCollection Test Suite");
        suite3.addTest(new DirectCollectionMergeTest());
        TestSuite suite4 = new TestSuite();
        suite4.setName("DirectMap Test Suite");
        suite4.addTest(new DirectMapMergeTest());

        addTest(suite1);
        addTest(suite2);
        addTest(suite3);
        addTest(suite4);
    }

    public void addRequiredSystems() {
        addRequiredSystem(new EmployeeSystem());
        addRequiredSystem(new DirectMapMappingsSystem());
    }
}