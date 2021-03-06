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
package org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.classfile;

import java.io.File;

import org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClassRepository;
import org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClassRepositoryFactory;


/**
 * Not much to say here: This is a factory for generating instances of
 * the "classfile" implementation of ExternalClassRepository, which uses a
 * ClassFile for generating ExternalClasses etc.
 */
public final class CFExternalClassRepositoryFactory
    implements ExternalClassRepositoryFactory
{

    /** the singleton */
    private static ExternalClassRepositoryFactory INSTANCE;


    /**
     * Singleton support.
     */
    public static synchronized ExternalClassRepositoryFactory instance() {
        if (INSTANCE == null) {
            INSTANCE = new CFExternalClassRepositoryFactory();
        }
        return INSTANCE;
    }

    /**
     * Private constructor - use the singleton.
     */
    private CFExternalClassRepositoryFactory() {
        super();
    }

    /**
     * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClassRepositoryFactory#buildClassRepository(java.io.File[])
     */
    @Override
    public ExternalClassRepository buildClassRepository(File[] classpath) {
        return new CFExternalClassRepository(classpath);
    }

}
