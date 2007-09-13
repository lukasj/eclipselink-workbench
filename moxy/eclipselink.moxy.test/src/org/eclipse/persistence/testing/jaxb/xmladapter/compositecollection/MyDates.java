/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.jaxb.xmladapter.compositecollection;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dates")
public class MyDates {
    @XmlElement(name="date")
    @XmlJavaTypeAdapter(MyDateAdapter.class)
    public ArrayList<Date> dateList;
    
    public boolean equals(Object obj) {
        if (!(obj instanceof MyDates)) {
            return false;
        }
        ArrayList<Date> dates = ((MyDates) obj).dateList;
        for (Iterator<Date> dateIt = dates.iterator(); dateIt.hasNext(); ) {
            Date date = dateIt.next();
            if (!this.dateList.contains(date)) {
                return false;
            }
        }
        return true;
    }
}