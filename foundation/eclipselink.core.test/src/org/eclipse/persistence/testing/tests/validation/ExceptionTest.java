/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.validation;

import org.eclipse.persistence.exceptions.EclipseLinkException;
import org.eclipse.persistence.testing.framework.AutoVerifyTestCase;
import org.eclipse.persistence.testing.framework.TestErrorException;


public abstract class ExceptionTest extends AutoVerifyTestCase {
    public EclipseLinkException caughtException;
    public EclipseLinkException expectedException;

    public void reset() {
        getSession().getIdentityMapAccessor().initializeIdentityMaps();
    }

    protected void verify() {
        if (caughtException == null) {
            throw new TestErrorException("The proper exception was not thrown:" + org.eclipse.persistence.internal.helper.Helper.cr() + "caught exception was null! \n\n[EXPECTING] " + expectedException);
        }

        if (caughtException.getErrorCode() != expectedException.getErrorCode()) {
            throw new TestErrorException("The proper exception was not thrown:" + org.eclipse.persistence.internal.helper.Helper.cr() + "[CAUGHT] " + caughtException + "\n\n[EXPECTING] " + expectedException);
        }
    }
}