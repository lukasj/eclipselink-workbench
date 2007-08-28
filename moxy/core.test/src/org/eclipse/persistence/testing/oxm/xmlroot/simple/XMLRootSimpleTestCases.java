/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.oxm.xmlroot.simple;

import java.io.InputStream;
import junit.textui.TestRunner;
import org.eclipse.persistence.oxm.XMLRoot;
import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;
import org.w3c.dom.Document;

public class XMLRootSimpleTestCases extends XMLMappingTestCases {
    private final static String XML_RESOURCE = "org/eclipse/persistence/testing/oxm/xmlroot/simple/employee.xml";
    protected final static String CONTROL_OBJECT = "Joe Oracle";
    protected final static String CONTROL_ELEMENT_NAME = "employee-name";
    protected final static String CONTROL_PREFIX = "oxm";
    protected final static String CONTROL_NAMESPACE_URI = "test";

    public XMLRootSimpleTestCases(String name) throws Exception {
        super(name);
        setControlDocument(getXMLResource());
        setProject(new XMLRootSimpleProject());
    }

    public static void main(String[] args) {
        String[] arguments = { "-c", "org.eclipse.persistence.testing.oxm.xmlroot.simple.XMLRootSimpleTestCases" };
        TestRunner.main(arguments);
    }

    public Object getControlObject() {
        XMLRoot xmlRoot = new XMLRoot();
        xmlRoot.setLocalName(CONTROL_ELEMENT_NAME);
        xmlRoot.setNamespaceURI(CONTROL_NAMESPACE_URI);
        xmlRoot.setObject(CONTROL_OBJECT);
        return xmlRoot;
    }

    public Object getWriteControlObject() {
        XMLRoot xmlRoot = new XMLRoot();
        xmlRoot.setLocalName(CONTROL_ELEMENT_NAME);
        xmlRoot.setNamespaceURI(CONTROL_NAMESPACE_URI);
        xmlRoot.setObject(CONTROL_OBJECT);
        return xmlRoot;
    }

    public String getXMLResource() {
        return XML_RESOURCE;
    }

    // Unmarshal tests
    public void testXMLToObjectFromInputStream() throws Exception {
        InputStream instream = ClassLoader.getSystemResourceAsStream(getXMLResource());
        Object testObject = xmlUnmarshaller.unmarshal(instream, String.class);
        instream.close();
        xmlToObjectTest(testObject);
    }

    public void testXMLToObjectFromDocument() throws Exception {
        Object testObject = xmlUnmarshaller.unmarshal(getControlDocument(), String.class);
        xmlToObjectTest(testObject);
    }

    public void testXMLToObjectFromURL() throws Exception {
        java.net.URL url = ClassLoader.getSystemResource(getXMLResource());
        Object testObject = xmlUnmarshaller.unmarshal(url, String.class);
        xmlToObjectTest(testObject);
    }

    public void xmlToObjectTest(Object testObject) throws Exception {
        log("\n**testXMLDocumentToObject**");
        log("Expected:");
        log(getReadControlObject().toString());
        log("Actual:");
        log(testObject.toString());

        XMLRoot controlObj = (XMLRoot)getReadControlObject();
        XMLRoot testObj = (XMLRoot)testObject;

        this.assertEquals(controlObj.getLocalName(), testObj.getLocalName());        
        this.assertEquals(controlObj.getNamespaceURI(), testObj.getNamespaceURI());
        this.assertEquals(controlObj.getObject(), testObj.getObject());
    }

    // DOES NOT APPLY
    public void testUnmarshallerHandler() throws Exception {
    }

    public Document getWriteControlDocument() throws Exception {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("org/eclipse/persistence/testing/oxm/xmlroot/simple/employee-write.xml");
        Document writeDocument = parser.parse(inputStream);
        removeEmptyTextNodes(controlDocument);
        inputStream.close();
        return writeDocument;
    }
}