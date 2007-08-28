/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.conversion;

import java.util.Calendar;

import java.sql.*;

import java.math.*;

import org.eclipse.persistence.platform.database.oracle.Oracle9Platform;

//This test retrieves all the classes that can be converted to a given class by 
//calling getDataTypesConvertedTo() in Oracle9Platform. 
public class DataTypesConvertedToAClassForOracle9Test extends DataTypesConvertedToAClassTest {

    protected Class[] convertedToClasses = 
        new Class[] { BigDecimal.class, BigInteger.class, Boolean.class, Byte.class, byte[].class, Byte[].class, 
                      Calendar.class, Character.class, Character[].class, char[].class, java.sql.Date.class, Double.class, 
                      Float.class, Integer.class, Long.class, Number.class, Short.class, String.class, Timestamp.class, 
                      Time.class, java.util.Date.class, oracle.sql.TIMESTAMP.class };

    public DataTypesConvertedToAClassForOracle9Test() {
        setDescription("Test getDataTypesConvertedTo() in Oracle9Platform.");
    }

    public void setup() {
        cm = getSession().getPlatform();
    }

    protected boolean isChar(Class aClass) {
        return super.isChar(aClass) || aClass == Oracle9Platform.NCHAR || aClass == Oracle9Platform.NSTRING || 
            aClass == Oracle9Platform.NCLOB;
    }
}