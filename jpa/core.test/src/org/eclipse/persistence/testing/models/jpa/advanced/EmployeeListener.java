/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/


package org.eclipse.persistence.testing.models.jpa.advanced;

import javax.persistence.*;
import java.util.EventListener;

public class EmployeeListener implements EventListener {
    public static int PRE_PERSIST_COUNT = 0;
    public static int POST_PERSIST_COUNT = 0;
    public static int PRE_REMOVE_COUNT = 0;
    public static int POST_REMOVE_COUNT = 0;
    public static int PRE_UPDATE_COUNT = 0;
    public static int POST_UPDATE_COUNT = 0;
    public static int POST_LOAD_COUNT = 0;

    // preUpdate will remove this prefix from firstName and lastName
    public static String PRE_UPDATE_NAME_PREFIX = "PRE_UPDATE_NAME_PREFIX";
    
	@PrePersist
	public void prePersist(Object emp) {
        PRE_PERSIST_COUNT++;
	}

	@PostPersist
	public void postPersist(Object emp) {
        POST_PERSIST_COUNT++;
	}

	@PreRemove
	public void preRemove(Object emp) {
        PRE_REMOVE_COUNT++;
	}

	@PostRemove
	public void postRemove(Object emp) {
        POST_REMOVE_COUNT++;
	}

	@PreUpdate
	public void preUpdate(Object emp) {
        PRE_UPDATE_COUNT++;
        Employee employee = (Employee)emp;
        if(employee.getFirstName() != null && employee.getFirstName().startsWith(PRE_UPDATE_NAME_PREFIX)) {
            employee.setFirstName(employee.getFirstName().substring(PRE_UPDATE_NAME_PREFIX.length()));
        }
        if(employee.getLastName() != null && employee.getLastName().startsWith(PRE_UPDATE_NAME_PREFIX)) {
            employee.setLastName(employee.getLastName().substring(PRE_UPDATE_NAME_PREFIX.length()));
        }
	}

	@PostUpdate
	public void postUpdate(Object emp) {
        POST_UPDATE_COUNT++;
	}

	@PostLoad
	public void postLoad(Employee emp) {
        POST_LOAD_COUNT++;
	}
}