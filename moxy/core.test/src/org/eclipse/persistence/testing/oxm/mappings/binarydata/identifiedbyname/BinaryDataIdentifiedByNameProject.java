/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/
package org.eclipse.persistence.testing.oxm.mappings.binarydata.identifiedbyname;

import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLConstants;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.mappings.XMLBinaryDataMapping;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.testing.oxm.mappings.binarydata.Employee;

public class BinaryDataIdentifiedByNameProject extends Project {
    //public NamespaceResolver namespaceResolver;
    public BinaryDataIdentifiedByNameProject(//
    NamespaceResolver namespaceResolver) {
        addDescriptor(getEmployeeDescriptor(namespaceResolver));
    }

    private XMLDescriptor getEmployeeDescriptor(NamespaceResolver aNSResolver) {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(Employee.class);
        descriptor.setDefaultRootElement("employee");

        XMLDirectMapping idMapping = new XMLDirectMapping();
        idMapping.setAttributeName("id");
        idMapping.setXPath("@id");
        descriptor.addMapping(idMapping);

        XMLBinaryDataMapping photoMapping = new XMLBinaryDataMapping();
        photoMapping.setAttributeName("photo");
        //photoMapping.setXPath("photos/list/photo");///text()");   
        XMLField field = new XMLField("photo");///text()");
        field.setSchemaType(XMLConstants.BASE_64_BINARY_QNAME);
        photoMapping.setField(field);

        descriptor.addMapping(photoMapping);
        if (aNSResolver != null) {
            descriptor.setNamespaceResolver(aNSResolver);
        }

        photoMapping.setShouldInlineBinaryData(false);
        photoMapping.setSwaRef(false);
        photoMapping.setMimeType("image");

        //photoMapping.setCollectionContentType(java.awt.Image.class);
        //photoMapping.setCollectionContentType(ClassConstants.APBYTE);
        return descriptor;
    }
}