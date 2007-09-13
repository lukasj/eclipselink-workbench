/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.oxm.mappings.serializedobject;

import org.eclipse.persistence.testing.oxm.mappings.serializedobject.SerializableAddress;

public class Employee {
	public SerializableAddress hexAddress;
	public SerializableAddress base64Address;
	
	public SerializableAddress getHexAddress() {
		return hexAddress;
	}
	
	public SerializableAddress getBase64Address() {
		return base64Address;
	}
	
	public void setHexAddress(SerializableAddress addr) {
		hexAddress = addr;
	}

	public void setBase64Address(SerializableAddress addr) {
		base64Address = addr;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof Employee)) {
			return false;
		}
		return getHexAddress().equals(((Employee)obj).getHexAddress()) && getBase64Address().equals(((Employee)obj).getBase64Address());
	}

}