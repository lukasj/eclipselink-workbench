/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  


package org.eclipse.persistence.testing.models.jpa.xml.inheritance;

import java.io.*;
import static javax.persistence.InheritanceType.*;

public class TireInfo implements Serializable {
    protected Integer id;
    protected Integer pressure;

    public TireInfo() {}

    public Integer getId() {
        return id;
    }

	public void setId(Integer id) { 
        this.id = id; 
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

}