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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;

import org.eclipse.persistence.tools.workbench.framework.context.WorkbenchContext;
import org.eclipse.persistence.tools.workbench.framework.context.WorkbenchContextHolder;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.relational.MWRelationalFieldTransformerAssociation;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.relational.MWRelationalTransformationMapping;
import org.eclipse.persistence.tools.workbench.mappingsplugin.ui.mapping.FieldTransformerAssociationsPanel;
import org.eclipse.persistence.tools.workbench.mappingsplugin.ui.mapping.TransformerEditingDialog;
import org.eclipse.persistence.tools.workbench.uitools.app.ValueModel;


final class RelationalFieldTransformerAssociationsPanel
	extends FieldTransformerAssociationsPanel
{
	// **************** Constructors ******************************************
	
	/** Expects a MWRelationalTransformationMapping object */
	public RelationalFieldTransformerAssociationsPanel(ValueModel transformationMappingHolder, WorkbenchContextHolder contextHolder) {
		super(transformationMappingHolder, contextHolder);
	}
	
	
	// **************** Initialization ****************************************
	
	protected ActionListener buildAddFieldTransformerAssociationAction() {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MWRelationalTransformationMapping transformationMapping = 
					(MWRelationalTransformationMapping) RelationalFieldTransformerAssociationsPanel.this.getSubjectHolder().getValue();
				WorkbenchContext context = RelationalFieldTransformerAssociationsPanel.this.getWorkbenchContext();
				
				if (transformationMapping.parentDescriptorIsAggregate()) {
					TransformerEditingDialog.promptToAddFieldTransformerAssociationForAggregate(transformationMapping, context);
				}
				else {
					RelationalFieldTransformerAssociationEditingDialog.promptToAddFieldTransformerAssociation(transformationMapping, context);
				}
			}
		};
	}
	
	protected ActionListener buildEditFieldTransformerAssociationAction() {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MWRelationalTransformationMapping transformationMapping = 
					(MWRelationalTransformationMapping) RelationalFieldTransformerAssociationsPanel.this.getSubjectHolder().getValue();
				MWRelationalFieldTransformerAssociation association = 
					(MWRelationalFieldTransformerAssociation) RelationalFieldTransformerAssociationsPanel.this.selectedFieldTransformerAssociation();
				WorkbenchContext context = 
					RelationalFieldTransformerAssociationsPanel.this.getWorkbenchContext();
				
				if (transformationMapping.parentDescriptorIsAggregate()) {
					TransformerEditingDialog.promptToEditFieldTransformerAssociationForAggregate(association, context);
				}
				else {
					RelationalFieldTransformerAssociationEditingDialog.promptToEditFieldTransformerAssociation(association, context);
				}
			}
		};
	}
}