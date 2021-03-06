/*
 * Copyright (c) 1998, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.tools.workbench.mappingsplugin.ui.mapping.relational;

import org.eclipse.persistence.tools.workbench.framework.app.SelectionActionsPolicy;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.relational.MWVariableOneToOneMapping;
import org.eclipse.persistence.tools.workbench.mappingsplugin.ui.descriptor.MappingDescriptorNode;
import org.eclipse.persistence.tools.workbench.mappingsplugin.ui.mapping.MappingNode;


public final class VariableOneToOneMappingNode extends MappingNode {


    public VariableOneToOneMappingNode(MWVariableOneToOneMapping value, SelectionActionsPolicy mappingNodeTypePolicy, MappingDescriptorNode parent) {
        super(value, mappingNodeTypePolicy, parent);
    }


    // ************** AbstractApplicationNode overrides *************

    @Override
    protected String accessibleNameKey() {
        return "ACCESSIBLE_TRANSFORMATION_MAPPING_NODE";
    }

    // ************** ApplicationNode implementation *************

    @Override
    public String helpTopicID() {
        return "mapping.variableOneToOne";
    }

    @Override
    protected String buildIconKey() {
        return this.getDescriptorNode().mappingHelpTopicPrefix() + ".variableOneToOne";
    }


    // ********** MWApplicationNode overrides **********

    @Override
    protected Class propertiesPageClass() {
        return VariableOneToOneMappingTabbedPropertiesPage.class;
    }

}
