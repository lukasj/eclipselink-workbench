/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.mapping;

import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.models.mapping.Employee1;

/**
 * Do the basic WriteObjectTest except on a multiple table. The method of setting up the multiple tables
 * can be chosen by passing the amendment method name in the constructor.
 */
public class MultipleTableWriteObjectTest extends org.eclipse.persistence.testing.framework.WriteObjectTest {
    String amendmentMethodName;

    public MultipleTableWriteObjectTest(Object originalObject, String amendmentMethodName) {
        super(originalObject);
        this.amendmentMethodName = amendmentMethodName;
        setName("MultipleTableWriteObjectTest(" + amendmentMethodName + "," + originalObject + ")");
    }

    protected void setup() {
        super.setup();
        try {
            // Get the Method
            Class[] parms = new Class[1];
            parms[0] = DatabaseSession.class;
            java.lang.reflect.Method method = Employee1.class.getMethod(this.amendmentMethodName, parms);

            // Invoke it.
            Object[] objectParameters = new Object[1];
            objectParameters[0] = getSession();
            method.invoke(Employee1.class, objectParameters);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new org.eclipse.persistence.testing.framework.TestErrorException("Problem finding or invoking Method on " + getClass() + ": " + this.amendmentMethodName);
        }
    }
}