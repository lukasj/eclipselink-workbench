/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.workbenchintegration;

import java.util.Iterator;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.testing.models.inheritance.Animal;
import org.eclipse.persistence.testing.models.inheritance.InheritanceSystem;

public class InheritanceWorkbenchIntegrationSystem extends InheritanceSystem {
    public static String PROJECT_FILE = "MWIntegrationTestInheritanceProject";

    /**
     * Override the constructor for inheritance system to allow us to read and write XML
     */
    public InheritanceWorkbenchIntegrationSystem() {
        super();
        
        // Must clear 1-way transformation added in amendment, otherwise will be added twice.
        ClassDescriptor descriptor = (ClassDescriptor)project.getDescriptor(Animal.class);
        for (Iterator mappings = descriptor.getMappings().iterator(); mappings.hasNext(); ) {
            DatabaseMapping mapping = (DatabaseMapping) mappings.next();
            if (mapping.isWriteOnly()) {
                descriptor.getMappings().remove(mapping);
                break;
            }
        }
        
        buildProject();
    }

    /**
     * Write and read the base project to/from deployment XML.
     */
    protected void buildProject() {
        project = WorkbenchIntegrationSystemHelper.buildProjectXML(project, PROJECT_FILE, getClass().getClassLoader());
    }
}