/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.jaxb.xmlidref;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="address")
public class Address {
    @XmlID
    @XmlAttribute(name="aid")
    public String id;
    
    @XmlElement(name="street")
    public String street;
    
    @XmlElement(name="city")
	public String city;
    
    @XmlElement(name="country")
	public String country;
    
    @XmlElement(name="zip")
	public String zip;
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Address)) {
			return false;
		}
		Address tgtAddress = (Address) obj;
		return (tgtAddress.city.equals(city) &&
				tgtAddress.country.equals(country) &&
				tgtAddress.id.equals(id) &&
				tgtAddress.street.equals(street) &&
				tgtAddress.zip.equals(zip));
	}
}