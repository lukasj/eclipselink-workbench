/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.sdo.helper.xsdhelper.defineandgenerate;

import junit.framework.Test;
import junit.framework.TestSuite;

public class XSDHelperDefineAndGenerateTestSuite {
    public XSDHelperDefineAndGenerateTestSuite() {
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("All XSDHelper Define And Generate Round Trip Tests");
        suite.addTest(new TestSuite(IDREFTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateNillableTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateMimeTypeOnXSDTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateMimeTypeOnPropertyTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateMimeTypeOnXSDManyTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateMimeTypeOnPropertyManyTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateBug5893546TestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateAppInfoTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateAppInfoTNSTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateSequencesTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateSequencesPurchaseOrderTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateNameCollisionsTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateWrapperTypeTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateChoicesTestCases.class));
        suite.addTest(new TestSuite(DefineAndGenerateBidirectionalTestCases.class));
        return suite;
    }
}