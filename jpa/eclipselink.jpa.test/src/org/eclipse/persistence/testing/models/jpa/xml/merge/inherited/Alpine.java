/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.models.jpa.xml.merge.inherited;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import static javax.persistence.TemporalType.DATE;

public class Alpine extends Beer  {
    public enum Classification { STRONG, BITTER, SWEET }
    
    private Date bestBeforeDate;
    private Classification classification;
    @Transient private String localTransientString;
    
    public static int ALPINE_PRE_PERSIST_COUNT = 0;
    
    public Alpine() {}
    
    @PrePersist
    public void celebrate() {
        ALPINE_PRE_PERSIST_COUNT++;
    }
    
    // Overidden in XML
    @Column(name="BB_DATE")
    @Temporal(DATE)
    public Date getBestBeforeDate() {
        return bestBeforeDate;
    }
    
    // This annotation should be valid and the property should not be persisted
    public String getLocalTransientString() {
        return localTransientString;
    }

    public Classification getClassification() {
        return classification;    
    }

    public void setBestBeforeDate(Date bestBeforeDate) {
        this.bestBeforeDate = bestBeforeDate;
    }
    
    public void setLocalTransientString(String localTransientString) {
        this.localTransientString=localTransientString;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }
    
    public boolean equals(Object anotherAlpine) {
        if (anotherAlpine.getClass() != Alpine.class) {
            return false;
        }
        
        return (getId().equals(((Alpine)anotherAlpine).getId()));
    }
}