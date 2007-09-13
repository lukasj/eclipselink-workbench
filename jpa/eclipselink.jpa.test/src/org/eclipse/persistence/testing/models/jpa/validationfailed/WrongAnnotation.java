/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  


/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.models.jpa.validationfailed;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;


@Entity
public class WrongAnnotation{
	private Integer id;
	private String firstName;
	private String lastName;
    
	public WrongAnnotation () {
    }
    
	@Id
	public Integer getId() { 
        return id; 
    }
    
	public void setId(Integer id) { 
        this.id = id; 
    }

    @Column(name="F_NAME")
	public String getFirstName() { 
        return firstName; 
    }

	public void setFirstName(String firstName){
		this.firstName = firstName;
	};
    

    // Not defined in the XML, this should get processed.
    //this is the wrong annotaion which causes the ValidationException
    @Column(name="F_NAME")
	public String getLastName() { 
        return lastName; 
    }

	public void setLastName(String lastName){
		this.lastName = lastName;
	}

}