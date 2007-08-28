/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.oxm.mappings.simpletypes.documentpreservation;


// JDK imports
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

// XML imports
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

// TopLink imports
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.oxm.XMLUnmarshallerHandler;
import org.eclipse.persistence.oxm.platform.DOMPlatform;
import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;
import org.eclipse.persistence.sessions.factories.XMLProjectReader;
import org.eclipse.persistence.sessions.factories.XMLProjectWriter;

public class DocumentPreservationTestCases extends XMLMappingTestCases {
    private final static String XML_RESOURCE = "org/eclipse/persistence/testing/oxm/mappings/simpletypes/documentpreservation/DocumentPreservationTestIn.xml";
    private final static String XML_CONTROL_RESOURCE = "org/eclipse/persistence/testing/oxm/mappings/simpletypes/documentpreservation/DocumentPreservationTestOut.xml";
    private final static String CONTROL_EMPLOYEE_NAME = "Jane Doe";
    private final static String CONTROL_EMPLOYEE_PHONE = "(613)444-1234";
    private XMLContext xmlContext;

    public DocumentPreservationTestCases(String name) throws Exception {
        super(name);
        setControlDocument(XML_CONTROL_RESOURCE);
        setProject(new EmployeeProject());
        //this test is only run with doc pres turned on
        useDocPres = true;
    }

    protected Object getControlObject() {
        Object result;

        try {
            InputStream inputStream = ClassLoader.getSystemResourceAsStream(XML_RESOURCE);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder parser = builderFactory.newDocumentBuilder();
            Document document = parser.parse(inputStream);
            removeEmptyTextNodes(document);

            result = getXMLContext().createUnmarshaller().unmarshal(document);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Employee employee = (Employee)result;
        employee.setName(CONTROL_EMPLOYEE_NAME);

        return employee;
    }

    private XMLContext getXMLContext() {
        if (!deploymentXML) {
            if (xmlContext == null) {
                xmlContext = new XMLContext(new EmployeeProject());
                xmlContext.getSession(0).getDatasourceLogin().setPlatform(new DOMPlatform());
            }
            return xmlContext;
        } else {
            if (xmlContext != null) {
                return xmlContext;
            }
            StringWriter stringWriter = new StringWriter();
            XMLProjectWriter writer = new XMLProjectWriter();
            writer.write(new EmployeeProject(), stringWriter);

            StringReader reader = new StringReader(stringWriter.toString());

            XMLProjectReader projectReader = new XMLProjectReader();
            Project newProject = projectReader.read(reader);

            XMLContext newContext = new XMLContext(newProject);

            newContext.getSession(0).getDatasourceLogin().setPlatform(new DOMPlatform());
            xmlContext = newContext;
            return newContext;
        }
    }

    public void testXMLToObjectFromInputStream() throws Exception {
        InputStream stream = ClassLoader.getSystemResourceAsStream(XML_CONTROL_RESOURCE);
        Object testObject = getXMLContext().createUnmarshaller().unmarshal(stream);
        xmlToObjectTest(testObject);
    }

    /*
        public void testXMLToObjectFromDocument() throws Exception {
            Object testObject = getXMLContext().createUnmarshaller().unmarshal(getControlDocument());
            xmlToObjectTest(testObject);
        }
    */
    public void testXMLToObjectFromURL() throws Exception {
        java.net.URL url = ClassLoader.getSystemResource(XML_CONTROL_RESOURCE);
        Object testObject = getXMLContext().createUnmarshaller().unmarshal(url);
        xmlToObjectTest(testObject);
    }

    public void testObjectToXMLDocument() throws Exception {
        Document testDocument = getXMLContext().createMarshaller().objectToXML(getWriteControlObject());
        objectToXMLDocumentTest(testDocument);
    }

    public void testObjectToXMLStringWriter() throws Exception {
        StringWriter writer = new StringWriter();
        XMLMarshaller marshaller = getXMLContext().createMarshaller();
        marshaller.setFormattedOutput(false);
        marshaller.marshal(getWriteControlObject(), writer);

        StringReader reader = new StringReader(writer.toString());
        InputSource inputSource = new InputSource(reader);
        Document testDocument = parser.parse(inputSource);
        objectToXMLDocumentTest(testDocument);
    }

    public void testUnmarshallerHandler() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        XMLUnmarshallerHandler xmlUnmarshallerHandler = getXMLContext().createUnmarshaller().getUnmarshallerHandler();
        xmlReader.setContentHandler(xmlUnmarshallerHandler);

        InputStream inputStream = ClassLoader.getSystemResourceAsStream(XML_CONTROL_RESOURCE);
        InputSource inputSource = new InputSource(inputStream);
        xmlReader.parse(inputSource);

        xmlToObjectTest(xmlUnmarshallerHandler.getResult());
    }
    
    public void testObjectToContentHandler() throws Exception {
        // DO NOTHING BECAUSE CONTENT HANDLER CAN NOT READ COMMENTS
    }
    
}