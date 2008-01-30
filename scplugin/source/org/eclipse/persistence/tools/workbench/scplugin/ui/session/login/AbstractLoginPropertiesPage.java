/*******************************************************************************
* Copyright (c) 2007 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0, which accompanies this distribution and is available at
* http://www.eclipse.org/legal/epl-v10.html.
*
* Contributors:
*     Oracle - initial API and implementation
******************************************************************************/
package org.eclipse.persistence.tools.workbench.scplugin.ui.session.login;

// Mapping Workbench
import org.eclipse.persistence.tools.workbench.framework.context.WorkbenchContextHolder;
import org.eclipse.persistence.tools.workbench.framework.ui.view.ScrollablePropertiesPage;
import org.eclipse.persistence.tools.workbench.scplugin.model.adapter.DatabaseSessionAdapter;
import org.eclipse.persistence.tools.workbench.uitools.app.PropertyAspectAdapter;
import org.eclipse.persistence.tools.workbench.uitools.app.PropertyValueModel;


/**
 * The abstract definition of an Login properties page.
 *
 * @version 10.0.3
 * @author Pascal Filion
 */
abstract class AbstractLoginPropertiesPage extends ScrollablePropertiesPage
{
	/**
	 * Creates a new <code>AbstractEisLoginPropertiesPage</code>.
	 *
	 * @param nodeHolder The holder of {@link DatabaseSessionNode}
	 */
	public AbstractLoginPropertiesPage(PropertyValueModel nodeHolder, WorkbenchContextHolder contextHolder)
	{
		super(nodeHolder, contextHolder);
	}

	/**
	 * Creates the selection holder that will hold the user object to be edited
	 * by this page.
	 *
	 * @param nodeHolder The holder of {@link DatabaseSessionNode}
	 * @return The <code>PropertyValueModel</code> containing the {@link EisLoginAdapter}
	 * to be edited by this page
	 */
	protected PropertyValueModel buildSelectionHolder()
	{
		return new PropertyAspectAdapter(super.buildSelectionHolder(),
													DatabaseSessionAdapter.LOGIN_CONFIG_PROPERTY)
		{
			protected Object getValueFromSubject()
			{
				DatabaseSessionAdapter session = (DatabaseSessionAdapter) subject;
				return session.getLogin();
			}
		};
	}
}