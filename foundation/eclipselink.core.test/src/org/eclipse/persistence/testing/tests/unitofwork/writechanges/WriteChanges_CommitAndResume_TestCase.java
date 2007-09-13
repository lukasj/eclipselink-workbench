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

import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.testing.framework.AutoVerifyTestCase;


/**
 *  @author  smcritch
 */
public class WriteChanges_CommitAndResume_TestCase extends AutoVerifyTestCase {
    public WriteChanges_CommitAndResume_TestCase() {
        super();
    }

    public void test() {
        UnitOfWork uow = getSession().acquireUnitOfWork();
        uow.writeChanges();
        uow.commitAndResume();
    }
}