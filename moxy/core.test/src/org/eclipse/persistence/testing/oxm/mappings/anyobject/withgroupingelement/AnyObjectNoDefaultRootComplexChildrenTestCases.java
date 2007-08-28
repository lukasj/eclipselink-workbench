/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/
package org.eclipse.persistence.testing.oxm.mappings.anyobject.withgroupingelement;

import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;
import org.eclipse.persistence.testing.oxm.mappings.anyobject.withgroupingelement.Child;
import org.eclipse.persistence.testing.oxm.mappings.anyobject.withgroupingelement.Root;

public class AnyObjectNoDefaultRootComplexChildrenTestCases extends XMLMappingTestCases {
    public static final String XML_RESOURCE_PATH = "org/eclipse/persistence/testing/oxm/mappings/anycollection/withgroupingelement/no_default_root_element_children.xml";

    public AnyObjectNoDefaultRootComplexChildrenTestCases(String name) throws Exception {
        super(name);
        Project p = new AnyObjectNoDefaultRootWithGroupingElementProject();
        setProject(p);
        setControlDocument(XML_RESOURCE_PATH);
    }

    public Object getControlObject() {
        Root root = new Root();
        Child child = new Child();
        child.setContent("child's text");
        root.setAny(child);
        return root;
    }

    // override superclass testcase since it is invalid here
    public void testXMLToObjectFromInputStream() throws Exception {
    }

    // override superclass testcase since it is invalid here
    public void testXMLToObjectFromURL() throws Exception {
    }

    // override superclass testcase since it is invalid here
    public void testUnmarshallerHandler() throws Exception {
    }
}