/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.jaxb.schemagen.customizedmapping.xmltype;

import org.eclipse.persistence.jaxb.compiler.Generator;
import org.eclipse.persistence.jaxb.javamodel.reflection.JavaModelImpl;
import org.eclipse.persistence.jaxb.javamodel.reflection.JavaModelInputImpl;

import java.io.File;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import junit.framework.TestCase;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Schema generation tests - based on the JAXB 2.0 TCK: 
 *     java2schema/CustomizedMapping/classes/XMLRootElement 
 */
public class SchemaGenXMLTypeTestCases extends TestCase {
    public SchemaGenXMLTypeTestCases(String name) throws Exception {
        super(name);
    }

    public void testBaseType001cPositive() throws Exception {
        boolean exception = false;
        String msg = null;
        String src = "org/eclipse/persistence/testing/jaxb/schemagen/customizedmapping/xmltype/BaseType001p.xml";
        String tmpdir = System.getenv("T_WORK");
        try {
            Class[] jClasses = new Class[] { BaseType001c.class };
            Generator gen = new Generator(new JavaModelInputImpl(jClasses, new JavaModelImpl()));
            gen.generateSchemaFiles(tmpdir, null);
            SchemaFactory sFact = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            Schema theSchema = sFact.newSchema(new File(tmpdir + "/schema0.xsd"));
            Validator validator = theSchema.newValidator();
            StreamSource ss = new StreamSource(new File(src)); 
            validator.validate(ss);
        } catch (Exception ex) {
            exception = true;
            msg = ex.toString();
        }
        assertTrue("Schema validation failed unexpectedly: " + msg, exception==false);
    }

    public void testBaseType001cNegative() throws Exception {
        boolean exception = false;
        String src = "org/eclipse/persistence/testing/jaxb/schemagen/customizedmapping/xmltype/BaseType001n.xml";
        String tmpdir = System.getenv("T_WORK");
        try {
            Class[] jClasses = new Class[] { BaseType001c.class };
            Generator gen = new Generator(new JavaModelInputImpl(jClasses, new JavaModelImpl()));
            gen.generateSchemaFiles(tmpdir, null);
            SchemaFactory sFact = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            Schema theSchema = sFact.newSchema(new File(tmpdir + "/schema0.xsd"));
            Validator validator = theSchema.newValidator();
            StreamSource ss = new StreamSource(new File(src)); 
            validator.validate(ss);
        } catch (Exception ex) {
            exception = true;
        }
        assertFalse("Schema validation did not fail as expected: ", exception==false);
    }
}