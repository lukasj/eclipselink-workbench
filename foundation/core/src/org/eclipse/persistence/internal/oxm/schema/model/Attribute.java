/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.oxm.schema.model;

public class Attribute extends SimpleComponent {
    public static final String OPTIONAL = "optional";
    public static final String REQUIRED = "required";
    public static final String PROHIBITED = "prohibited";
    private String use;
    private String ref;

    public Attribute() {
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getUse() {
        return use;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }
}