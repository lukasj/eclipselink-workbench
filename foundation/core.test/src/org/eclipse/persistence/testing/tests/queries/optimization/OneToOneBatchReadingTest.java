/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.queries.optimization;

import java.util.Vector;

import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.framework.*;
import org.eclipse.persistence.testing.models.legacy.*;

public class OneToOneBatchReadingTest extends TestCase {
    public Vector V;

    public OneToOneBatchReadingTest() {
        setDescription("Tests batch reading using 1 to 1 mapping and composite primary key");
    }

    public void reset() {
        getAbstractSession().rollbackTransaction();
        getSession().getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    public void setup() {
        getAbstractSession().beginTransaction();
    }

    public void test() {
        ReadAllQuery q = new ReadAllQuery();
        q.setReferenceClass(Shipment.class);
        q.addBatchReadAttribute("employee");
        q.setSelectionCriteria(q.getExpressionBuilder().get("employee").get("address").equal(q.getExpressionBuilder().getParameter("ADDRESS")));
        q.addArgument("ADDRESS");
        Vector r = new Vector();
        r.addElement("885 Meadowlands Dr.");
        UnitOfWork uow = getSession().acquireUnitOfWork();
        V = (java.util.Vector)uow.executeQuery(q, r);
    }

    public void verify() {
        Shipment S = (Shipment)V.firstElement();
        if (S.employee == null) {
            throw new TestWarningException("Test failed. Batched objects were not read");
        }
    }
}