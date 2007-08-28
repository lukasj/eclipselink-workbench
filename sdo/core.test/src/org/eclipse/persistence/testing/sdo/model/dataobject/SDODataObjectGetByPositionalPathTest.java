/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.sdo.model.dataobject;

import junit.textui.TestRunner;
import org.eclipse.persistence.sdo.SDOConstants;
import org.eclipse.persistence.sdo.SDOProperty;
import org.eclipse.persistence.exceptions.SDOException;

public class SDODataObjectGetByPositionalPathTest extends SDODataObjectGetByPositionalPathTestCases {
    public SDODataObjectGetByPositionalPathTest(String name) {
        super(name);
    }
    
     public static void main(String[] args) {
        String[] arguments = { "-c", "org.eclipse.persistence.testing.sdo.model.dataobject.SDODataObjectGetByPositionalPathTest" };
        TestRunner.main(arguments);
    }

    // normal test: a/b.0/c as path
    public void testGetByPositionalPathString() {
        ((SDOProperty)dataObject_c.getInstanceProperty(PROPERTY_NAME_C)).setType(SDOConstants.SDO_STRING);

        dataObject_c.set(property_c, "test");

        this.assertEquals("test", dataObject_a.get(property));

    }

    // normal test: a/b.0/c as path
    public void testGetByPositionalPathStringObj() {
        this.assertEquals(dataObject_c, dataObject_a.get("PName-a/PName-b.0"));

    }

    // normal test: a/b.0/c as path
    public void testGetByPositionalPathStringNameWithDot() {

        /*property_c = new SDOProperty();
        property_c.setName(PROPERTY_NAME_C+".");
        property_c.setType(SDOConstants.SDO_STRING);
        type_c.addDeclaredProperty(property_c);
        dataObject_c.setType(type_c);
        */
        dataObject_a.set(property4, "test");

        this.assertEquals("test", dataObject_a.get(property4));

    }

    // purpose: test a nonexisted dataobject in the path
    public void testGetByPositionalPathStringWithDataObjectNotInPosition() {
        ((SDOProperty)dataObject_c.getInstanceProperty(PROPERTY_NAME_C)).setType(SDOConstants.SDO_STRING);

        dataObject_c.set(property_c, "test");

        //Changed test since out of bounds now throws an exception SDO Jira 81
        //this.assertNull(dataObject_a.get("PName-a/PName-b.1/PName-c"));
        try {
            Object value = dataObject_a.get("PName-a/PName-b.1/PName-c");
            fail("An IndexOutOfBoundsException was expected but didn't occur");
        } catch (IndexOutOfBoundsException e) {
            //pass
        }
    }

    // purpose: test one of properties is not existed in path
    public void testGetByPositionalPathStringWithDataObjectNotExistedProperty() {
        ((SDOProperty)dataObject_c.getInstanceProperty(PROPERTY_NAME_C)).setType(SDOConstants.SDO_STRING);

        dataObject_c.set(property_c, "test");

        this.assertNull(dataObject_a.get("PName-a/PName-f.0/PName-c"));       
    }

    // purpose: test one of properties is not existed in path
    public void testGetByPositionalPathStringWithPropertyNameContainingDot() {
        ((SDOProperty)dataObject_c.getInstanceProperty(PROPERTY_NAME_C)).setType(SDOConstants.SDO_STRING);
        dataObject_c.set(property_c, "test");

        this.assertNull(dataObject_a.get(UNDEFINED_PATH));
        //this.assertNull(dataObject_a.get(property));

        /*try{
          dataObject_a.get(property);
          fail("IllegalArgumentException should be thrown");
        }catch(IllegalArgumentException e){}*/
    }

    // purpose: test path as ".."
    public void testGetByPositionalPathStringWithContainerPath() {
        ((SDOProperty)dataObject_c.getInstanceProperty(PROPERTY_NAME_C)).setType(SDOConstants.SDO_STRING);

        dataObject_c.set(property_c, "test");

        this.assertTrue(dataObject_a == dataObject_b.get(".."));

    }

    // purpose: test path as "/"
    public void testGetByPositionalPathStringWithRootPath() {
        ((SDOProperty)dataObject_c.getInstanceProperty(PROPERTY_NAME_C)).setType(SDOConstants.SDO_STRING);

        dataObject_c.set(property_c, "test");

        this.assertTrue(dataObject_a == dataObject_c.get("/"));

    }
}