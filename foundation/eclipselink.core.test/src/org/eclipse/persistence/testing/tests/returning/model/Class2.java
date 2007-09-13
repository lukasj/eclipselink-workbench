/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.returning.model;

public class Class2 extends BaseClass {
    public Class2() {
        super();
    }

    public Class2(double a, double b) {
        super(a, b);
    }

    public Class2(double a, double b, double c) {
        super(a, b, c);
    }

    public Class2(double c) {
        super(c);
    }

    public String getFieldAName() {
        return "A2";
    }

    public String getFieldBName() {
        return "B2";
    }

    public Object clone() {
        Class2 clone = new Class2();
        clone.setAB(getA(), getB());
        clone.setC(getC());
        return clone;
    }
}