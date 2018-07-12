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
package org.eclipse.persistence.tools.workbench.scplugin.ui.pool;

import java.awt.Component;

import org.eclipse.persistence.tools.workbench.framework.context.WorkbenchContext;
import org.eclipse.persistence.tools.workbench.scplugin.ui.pool.basic.EisPoolLoginPropertiesPage;


public class EisPoolTabbedPropertiesPage extends PoolTabbedPropertiesPage {

    public EisPoolTabbedPropertiesPage(WorkbenchContext context) {
        super(context);
    }

    @Override
    protected Component buildLoginPropertiesPage() {
        return new EisPoolLoginPropertiesPage( this.getNodeHolder(), getWorkbenchContextHolder());
    }
}
