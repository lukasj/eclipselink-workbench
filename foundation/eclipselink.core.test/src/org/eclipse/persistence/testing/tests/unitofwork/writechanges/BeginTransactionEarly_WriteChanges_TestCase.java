/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.unitofwork.writechanges;

import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.testing.framework.AutoVerifyTestCase;
import org.eclipse.persistence.testing.framework.TestErrorException;


/**
 *  @author  smcritch
 */
public class BeginTransactionEarly_WriteChanges_TestCase extends AutoVerifyTestCase {
    public void test() {
        UnitOfWork uow = getSession().acquireUnitOfWork();

        uow.beginEarlyTransaction();

        uow.writeChanges();
        uow.commit();

        if (((UnitOfWorkImpl)uow).isInTransaction()) {
            ((UnitOfWorkImpl)uow).rollbackTransaction();
            throw new TestErrorException("After beginning transaction early and writing changes, still in transaction afterwards.");
        }
    }
}