/*******************************************************************************
* Copyright (c) 2007 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0, which accompanies this distribution and is available at
* http://www.eclipse.org/legal/epl-v10.html.
*
* Contributors:
*     Oracle - initial API and implementation
******************************************************************************/
package org.eclipse.persistence.tools.workbench.platformsplugin.ui;

import org.eclipse.persistence.tools.workbench.framework.Plugin;
import org.eclipse.persistence.tools.workbench.framework.PluginFactory;
import org.eclipse.persistence.tools.workbench.framework.context.ApplicationContext;

/**
 * straightforward singleton implementation
 */
public final class PlatformsPluginFactory
	implements PluginFactory
{
	// singleton
	private static PluginFactory INSTANCE;


	/**
	 * Return the singleton.
	 */
	public static synchronized PluginFactory instance() {
		if (INSTANCE == null) {
			INSTANCE = new PlatformsPluginFactory();
		}
		return INSTANCE;
	}

	/**
	 * Ensure non-instantiability.
	 */
	private PlatformsPluginFactory() {
		super();
	}


	// ********** PluginFactory implementation **********

	public Plugin createPlugin(ApplicationContext context) {
		return new PlatformsPlugin();
	}

}