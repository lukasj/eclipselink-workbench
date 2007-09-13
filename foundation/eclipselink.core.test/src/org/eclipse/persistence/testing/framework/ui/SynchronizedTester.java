/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.framework.ui;

public interface SynchronizedTester {

    /**
     * Stop the selected test.
     */
    public void finishedTest();

    /**
     * Called whenever the part throws an exception.
     * @param exception java.lang.Throwable
     */
    public void notifyException(Throwable exception);
}