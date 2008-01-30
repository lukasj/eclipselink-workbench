/*******************************************************************************
* Copyright (c) 2007 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0, which accompanies this distribution and is available at
* http://www.eclipse.org/legal/epl-v10.html.
*
* Contributors:
*     Oracle - initial API and implementation
******************************************************************************/
package org.eclipse.persistence.tools.workbench.mappingsplugin.ui.mapping.relational;

import org.eclipse.persistence.tools.workbench.framework.app.SelectionActionsPolicy;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.MWConverterMapping;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.relational.MWDirectToFieldMapping;
import org.eclipse.persistence.tools.workbench.mappingsplugin.ui.descriptor.MappingDescriptorNode;
import org.eclipse.persistence.tools.workbench.mappingsplugin.ui.mapping.MappingNode;
import org.eclipse.persistence.tools.workbench.utility.node.Node;


public final class DirectToFieldMappingNode 
	extends MappingNode 
{
	protected static final String[] DIRECT_MAPPING_ICON_PROPERTY_NAMES = {
			Node.HAS_BRANCH_PROBLEMS_PROPERTY,
			MWConverterMapping.CONVERTER_PROPERTY
	};

	// **************** Constructors ******************************************
	
	public DirectToFieldMappingNode(MWDirectToFieldMapping value, SelectionActionsPolicy mappingNodeTypePolicy, MappingDescriptorNode parent) {
		super(value, mappingNodeTypePolicy, parent);
	}

	
	// ************** ApplicationNode implementation *************
	
	protected String[] iconPropertyNames() {
		return DIRECT_MAPPING_ICON_PROPERTY_NAMES;
	}

	public String helpTopicID() {
		return this.getDescriptorNode().mappingHelpTopicPrefix() + ".directToField";
	}

	protected String buildIconKey() {
		return ((MWDirectToFieldMapping) getMapping()).iconKey();		
	}

	
	// ************** AbstractApplicationNode overrides *************

	protected String accessibleNameKey() {
		return ((MWDirectToFieldMapping) getMapping()).accessibleNameKey();
	}

	
	// ********** MWApplicationNode overrides **********

	protected Class propertiesPageClass() {
		return DirectToFieldMappingPropertiesPage.class;
	}

}