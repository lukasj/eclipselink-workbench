//Copyright (c) 1998, 2006, Oracle. All rights reserved. 
package org.eclipse.persistence.testing.sdo.helper.xmlhelper.loadandsave;

import java.io.StringReader;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;
import junit.textui.TestRunner;
import org.eclipse.persistence.sdo.SDOConstants;

public class LoadAndSaveXMLEncodingAndVersionTestCases extends LoadAndSaveTestCases {
    private static String VERSION = "1.0";
    private static String ENCODING = "windows-1252";
    public LoadAndSaveXMLEncodingAndVersionTestCases(String name) {
        super(name);
    }

    protected String getSchemaName() {
        return "./org/eclipse/persistence/testing/sdo/helper/xmlhelper/Customer.xsd";
    }

    protected String getControlFileName() {
        return ("./org/eclipse/persistence/testing/sdo/helper/xmlhelper/simpleElementEncoding.xml");
    }

    protected String getNoSchemaControlFileName() {        
        return ("./org/eclipse/persistence/testing/sdo/helper/xmlhelper/simpleElementEncodingNoSchema.xml");
    }

    protected String getControlRootURI() {
        return "http://www.example.org";
    }

    protected String getControlDataObjectFileName() {
        return ("./org/eclipse/persistence/testing/sdo/helper/xmlhelper/simpleElement.xml");
    }
    
    protected String getControlRootName() {
        return "customer";
    }
    
    protected String getRootInterfaceName() {
        return "CustomerType";
    }

    public void registerTypes() {
        Type stringType = typeHelper.getType("commonj.sdo", "String");

        // create a new Type for Customers
        DataObject customerType = dataFactory.create("commonj.sdo", "Type");
        customerType.set("uri", getControlRootURI());
        customerType.set("name", "customer");

        // create a first name property        
        addProperty(customerType, "firstName", stringType, true, false, true);
        
        // create a last name property        
        addProperty(customerType, "lastName", stringType, true, false, true);
              
        // now define the Customer type so that customers can be made
        Type customerSDOType = typeHelper.define(customerType);
        
        DataObject propDO = dataFactory.create(SDOConstants.SDO_PROPERTY);
        propDO.set("name", getControlRootName());
        propDO.set("type", customerSDOType);
        typeHelper.defineOpenContentProperty(getControlRootURI(), propDO);
    }

    public static void main(String[] args) {
        String[] arguments = { "-c", "org.eclipse.persistence.testing.sdo.helper.xmlhelper.loadandsave.LoadAndSaveXMLEncodingAndVersionTestCases" };
        TestRunner.main(arguments);
    }

    protected void compareXML(String controlFileName, String testString, boolean compareNodes) throws Exception {
        String controlString = getControlString(controlFileName);
        log("Expected:" + controlString);
        log("Actual  :" + testString);

        assertEquals(removeWhiteSpaceFromString(controlString), removeWhiteSpaceFromString(testString));
    }
    
    protected void verifyAfterLoad(XMLDocument document) {
        boolean passed = document.getEncoding().equals(ENCODING) && document.getXMLVersion().equals(VERSION);
        
        assertTrue(passed);
    }
    
    
}