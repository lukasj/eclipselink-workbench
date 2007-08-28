/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.oxm.mappings.simpletypes;


// JUnit imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.persistence.testing.oxm.mappings.simpletypes.rootelement.RootElementWithCommentTestCases;

public class SimpleTypeMappingTestSuite extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite("Simple Type Test Suite");
        suite.addTestSuite(org.eclipse.persistence.testing.oxm.mappings.simpletypes.rootelement.RootElementTestCases.class);
				suite.addTestSuite(RootElementWithCommentTestCases.class);
        suite.addTestSuite(org.eclipse.persistence.testing.oxm.mappings.simpletypes.rootelement.RootElementNullTestCases.class);
        suite.addTestSuite(org.eclipse.persistence.testing.oxm.mappings.simpletypes.childelement.ChildElementTestCases.class);
        suite.addTestSuite(org.eclipse.persistence.testing.oxm.mappings.simpletypes.childcollection.ChildCollectionTestCases.class);
        suite.addTestSuite(org.eclipse.persistence.testing.oxm.mappings.simpletypes.documentpreservation.DocumentPreservationTestCases.class);
        suite.addTestSuite(org.eclipse.persistence.testing.oxm.mappings.simpletypes.typetranslator.childelement.TypeTranslatorTestCases.class);
        suite.addTestSuite(org.eclipse.persistence.testing.oxm.mappings.simpletypes.typetranslator.rootelement.TypeTranslatorTestCases.class);
        suite.addTestSuite(org.eclipse.persistence.testing.oxm.mappings.simpletypes.inheritance.InheritanceRootTestCases.class);
        //remove this test for now as we don't currently support mapping to positional text nodes
        //suite.addTestSuite(org.eclipse.persistence.testing.oxm.mappings.simpletypes.rootelement.positional.PositionalRootElementTestCases.class);
        return suite;
    }

    public static void main(String[] args) {
        String[] arguments = { "-c", "org.eclipse.persistence.testing.oxm.mappings.simpletypes.SimpleTypeMappingTestSuite" };
        junit.swingui.TestRunner.main(arguments);
    }
}