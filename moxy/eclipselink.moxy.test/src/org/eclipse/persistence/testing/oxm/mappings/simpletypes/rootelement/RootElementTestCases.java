/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.oxm.mappings.simpletypes.rootelement;

// JDK imports
import java.io.InputStream;

// TopLink imports
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;

public class RootElementTestCases extends XMLMappingTestCases {
	private final static String XML_RESOURCE = "org/eclipse/persistence/testing/oxm/mappings/simpletypes/rootelement/SimpleRootElementTest.xml";
	private final static String CONTROL_EMPLOYEE_NAME = "Jane Doh";
	private XMLMarshaller xmlMarshaller;
	
	public RootElementTestCases(String name) throws Exception {
		super(name);
		setControlDocument(XML_RESOURCE);
		setProject(new EmployeeProject());
	}

	protected Object getControlObject() {
		Employee employee = new Employee();
		employee.setName(CONTROL_EMPLOYEE_NAME);
		
		return employee;
	}
	
}