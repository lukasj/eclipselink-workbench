/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.unitofwork;

import java.util.Vector;

import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.testing.framework.TestSystem;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;


public class UOWSystem extends TestSystem {

    public void addDescriptors(DatabaseSession session) {
        Vector descriptors = new Vector();

        descriptors.addElement(MailAddress.descriptor());
        descriptors.addElement(Person.descriptor());
        descriptors.addElement(Contact.descriptor());
        descriptors.addElement(Weather.descriptor());
        descriptors.addElement(ConcurrentAddress.descriptor());
        descriptors.addElement(ConcurrentPerson.descriptor());
        session.addDescriptors(descriptors);
    }

    public void createTables(DatabaseSession session) {
        SchemaManager schemaManager = new SchemaManager((DatabaseSession)session);

        schemaManager.replaceObject(Contact.tableDefinition());
        schemaManager.replaceObject(Person.tableDefinition());
        schemaManager.replaceObject(MailAddress.tableDefinition());
        schemaManager.replaceObject(Weather.tableDefinition());
        schemaManager.replaceObject(ConcurrentAddress.tableDefinition());
        schemaManager.replaceObject(ConcurrentPerson.tableDefinition());

        schemaManager.createSequences();
    }

    public void populate(DatabaseSession session) {
        UnitOfWork uow = session.acquireUnitOfWork();

        Person employee1 = Person.example1();
        Person employee2 = Person.example2();
        Weather weather1 = Weather.example1();
        Weather weather2 = Weather.example2();

        uow.registerObject(employee1);
        uow.registerObject(employee2);
        uow.registerObject(weather1);
        uow.registerObject(weather2);
        uow.registerObject(ConcurrentPerson.example());
        uow.commit();
    }
}