/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.oxm.mappings.transformation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;

/**
 *  @version $Header: TransformationMappingTestSuite.java 17-apr-2007.11:15:15 mmacivor Exp $
 *  @author  mmacivor
 *  @since   release specific (what release of product did this appear in)
 */

public class TransformationMappingTestSuite extends TestCase 
{
	public static Test suite() 
	{    
		TestSuite suite = new TestSuite("Transformation Mapping Suite");
		suite.addTestSuite(TransformationMappingTestCases.class);
		suite.addTestSuite(TransformationMappingErrorTestCases.class);
    suite.addTestSuite(TransformationMappingAnyCollectionTestCases.class);
    suite.addTestSuite(TransformationMappingAnyObjectTestCases.class);
    suite.addTestSuite(TransformationMappingCompositeCollectionTestCases.class);
    suite.addTestSuite(TransformationMappingCompositeObjectTestCases.class);
    suite.addTestSuite(TransformationMappingNullTestCases.class);
    
		return suite;
	}
  
    public static void main(String[] args) {
        String[] arguments = { "-c", "org.eclipse.persistence.testing.oxm.mappings.transformation.TransformationMappingTestSuite" };
        junit.textui.TestRunner.main(arguments);
    }
}