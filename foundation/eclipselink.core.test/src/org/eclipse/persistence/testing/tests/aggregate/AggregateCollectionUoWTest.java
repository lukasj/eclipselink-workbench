/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.aggregate;

import java.util.*;
import org.eclipse.persistence.testing.framework.*;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.framework.WriteObjectTest;
import org.eclipse.persistence.testing.models.aggregate.Company;
import org.eclipse.persistence.testing.models.aggregate.Agent;
import org.eclipse.persistence.testing.models.aggregate.Customer;
import org.eclipse.persistence.testing.models.aggregate.Dependant;
import org.eclipse.persistence.testing.models.aggregate.House;
import org.eclipse.persistence.testing.models.aggregate.Oid;

public class AggregateCollectionUoWTest extends WriteObjectTest {
    public Object unitOfWorkWorkingCopy;
    public UnitOfWork unitOfWork;

    public AggregateCollectionUoWTest() {
        super();
    }

    public AggregateCollectionUoWTest(Object originalObject) {
        super(originalObject);
    }

    protected void changeUnitOfWorkWorkingCopy() {
        if (unitOfWorkWorkingCopy instanceof Agent) {
            Agent agent = (Agent)this.unitOfWorkWorkingCopy;
            agent.setLastName("Jackson");
            Vector customers = agent.getCustomers();
            Customer customer1 = (Customer)customers.firstElement();
            customer1.setName("Vince Carter");
            //customer1.removeDependant((Dependant) customer1.getDependants().firstElement());
            customer1.addDependant(new Dependant("lily", 12));
            //agent.removeCustomer((Customer) customers.lastElement());
            Vector houses = agent.getHouses();
            agent.removeHouse((House)houses.firstElement());
            House house2 = (House)houses.lastElement();
            house2.setDescriptions("do not buy it, it collapses -:)");
            Oid newInsurancePolicyId = new Oid();
            newInsurancePolicyId.setOid(new Integer(893453));
            house2.setInsuranceId(newInsurancePolicyId);
            House newHouse = new House();
            newHouse.setLocation("123 Slater Street");
            newHouse.setDescriptions("every convinent to who works with The Object People");
            agent.addHouse(newHouse);
            Customer newCustomer = new Customer();
            newCustomer.setName("Micheal Chang");
            newCustomer.setIncome(1000000);
            newCustomer.setCompany(Company.example4());
            Vector changDependnants = new Vector(3);
            changDependnants.addElement(new Dependant("Susan", 9));
            changDependnants.addElement(new Dependant("Julie", 5));
            changDependnants.addElement(new Dependant("David", 2));
            newCustomer.setDependants(changDependnants);
            agent.addCustomer(newCustomer);
        } else {
            //do nothing for the time being
        }
    }

    protected void setup() {
        super.setup();

        // Acquire unit of work
        this.unitOfWork = getSession().acquireUnitOfWork();

        this.unitOfWorkWorkingCopy = this.unitOfWork.registerObject(this.objectToBeWritten);
        changeUnitOfWorkWorkingCopy();
        // Use the original session for comparision
        if (!compareObjects(this.originalObject, this.objectToBeWritten)) {
            throw new TestErrorException("The original object was changed through changing the clone.");
        }

        //testing optimistic locking
        //	getSession().executeNonSelectingSQL("UPDATE HOUSE SET LOCATION = 'no where' WHERE (LOCATION = '33D King Edward Street')");
    }

    protected void test() {
        try {
            this.unitOfWork.commit();
        } catch (org.eclipse.persistence.exceptions.OptimisticLockException ex) {
            new TestWarningException("Optimistic locking exception thrown when object was changed outside during the transaction");
        }
    }

    protected void verify() {
        // Use the original session for comparision veryify that the changes were merged correctly
        if (!compareObjects(this.unitOfWorkWorkingCopy, this.objectToBeWritten)) {
            throw new TestErrorException("The original object did not receive the changes correctly, in the merge.");
        }

        // Verify that the changes were made on the database correctly.
        getSession().getIdentityMapAccessor().initializeIdentityMaps();
        this.objectFromDatabase = getSession().executeQuery(this.query);

        if (!(compareObjects(this.objectToBeWritten, this.objectFromDatabase))) {
            throw new TestErrorException("The object inserted into the database, '" + this.objectFromDatabase + "' does not match the original, '" + this.objectToBeWritten + ".");
        }
    }
}