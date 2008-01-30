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

import org.eclipse.persistence.tools.workbench.framework.context.WorkbenchContext;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.MWConverter;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.relational.MWDirectToFieldMapping;


class MapAsRelationalDirectToFieldAction extends MapAsRelationalDirectMapping {

	MWDirectToFieldMapping selectedMapping;

	protected MapAsRelationalDirectToFieldAction(WorkbenchContext context) {
		super(context);
	}

	protected void initialize() {
		super.initialize();
		this.initializeIcon("mapping.directToField");
		this.initializeText("MAP_AS_DIRECT_TO_FIELD_ACTION");
		this.initializeMnemonic("MAP_AS_DIRECT_TO_FIELD_ACTION");
		this.initializeToolTipText("MAP_AS_DIRECT_TO_FIELD_ACTION.toolTipText");	
	}


	protected String converterType() {
		return MWConverter.NO_CONVERTER;
	}
}