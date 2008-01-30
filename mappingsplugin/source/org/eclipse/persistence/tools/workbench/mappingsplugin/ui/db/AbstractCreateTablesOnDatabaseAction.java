/*******************************************************************************
* Copyright (c) 2007 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0, which accompanies this distribution and is available at
* http://www.eclipse.org/legal/epl-v10.html.
*
* Contributors:
*     Oracle - initial API and implementation
******************************************************************************/
package org.eclipse.persistence.tools.workbench.mappingsplugin.ui.db;

import java.text.NumberFormat;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.eclipse.persistence.tools.workbench.framework.app.ApplicationNode;
import org.eclipse.persistence.tools.workbench.framework.context.WorkbenchContext;
import org.eclipse.persistence.tools.workbench.framework.ui.dialog.ExceptionDialog;
import org.eclipse.persistence.tools.workbench.mappingsmodel.db.MWDatabase;
import org.eclipse.persistence.tools.workbench.uitools.LabelArea;

import org.eclipse.persistence.exceptions.EclipseLinkException;

abstract class AbstractCreateTablesOnDatabaseAction extends AbstractTableGenerationAction {

	protected AbstractCreateTablesOnDatabaseAction(WorkbenchContext context) {
		super(context);
	}
	
	/**
	 * return the tables we need to work with
	 */
	protected abstract Collection buildTables();
		
	protected void execute() {
		if ( ! this.confirmTableCreation()) {
			return;
		}
		try {
			this.checkDevelopmentLoginSpec("CREATE_ON_DATABASE_DIALOG.title");
		} catch (IllegalStateException ex) {
			return;
		}

		Collection tables = this.buildTables();
		try { 
			this.database().generateTables(tables);
		} catch (EclipseLinkException exception) {
			ExceptionDialog dialog = new ExceptionDialog(
					resourceRepository().getString("EXCEPTION_DURING_TABLE_GEN"),
					exception,
					this.getWorkbenchContext(),
					resourceRepository().getString("EXCEPTION_DURING_TABLE_GEN.title"));
			dialog.show();
			return;
		}

		JOptionPane.showMessageDialog(
				this.getWorkbenchContext().getCurrentWindow(),
				this.resourceRepository().getString("CREATE_ON_DATABASE_DONE_DIALOG.message", new Object[] { NumberFormat.getInstance().format(tables.size()) }),
				this.resourceRepository().getString("CREATE_ON_DATABASE_DONE_DIALOG.title"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	private boolean confirmTableCreation() {
		int option = JOptionPane.showConfirmDialog(
							this.getWorkbenchContext().getCurrentWindow(),
							new LabelArea(this.resourceRepository().getString("TABLES_WILL_BE_OVERWRITTEN_DIALOG.message")),
							this.resourceRepository().getString("TABLES_WILL_BE_OVERWRITTEN_DIALOG.title"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
		return option == JOptionPane.YES_OPTION;
	}

	protected boolean shouldBeEnabled(ApplicationNode selectedNode) {
		return this.database().isConnected();
	}

	protected String[] enabledPropertyNames() {
		return new String[] {MWDatabase.CONNECTED_PROPERTY};
	}

}