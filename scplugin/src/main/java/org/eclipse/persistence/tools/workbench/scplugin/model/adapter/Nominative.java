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
package org.eclipse.persistence.tools.workbench.scplugin.model.adapter;

/**
 * Names are often used to uniquely identify a model object within a set of
 * objects of the same type. This interface allows those objects to be handled
 * by client code that is only interested in that uniquely-identifying name.
 *
 * @version 10.1.3
 * @author Pascal Filion
 */
public interface Nominative
{
    /**
     * Notifies a change in this nominative's name.
     */
    public static final String NAME_PROPERTY = "name";

    /**
     * Returns the object's name.
     *
     * @return A non-<code>null</code> string representing the name of the object
     */
    String getName();
}
