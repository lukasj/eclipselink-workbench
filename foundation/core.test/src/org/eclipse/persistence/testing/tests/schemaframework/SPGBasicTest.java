/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.schemaframework;

import java.io.*;
import org.eclipse.persistence.tools.schemaframework.*;
import org.eclipse.persistence.testing.framework.*;

public class SPGBasicTest extends TestCase {
    private SchemaManager manager;
    private StoredProcedureGenerator generator;
    private Writer myWriter = null;

    public SPGBasicTest() {
    }

    public void setup() {
        setManager(new SchemaManager(getDatabaseSession()));
        setGenerator(new StoredProcedureGenerator(getManager()));
    }

    public void test() {
        getGenerator().generateStoredProcedures(getWriter());
    }

    public void verify() {
    }

    public void reset() {
        setManager(null);
        setGenerator(null);
        try {
            getWriter().close();
        } catch (Exception e) {
        }
    }

    // Accessors
    private StoredProcedureGenerator getGenerator() {
        return generator;
    }

    private SchemaManager getManager() {
        return manager;
    }

    private Writer getWriter() {
        if (myWriter == null) {
            try {
                setWriter(new FileWriter("GeneratedStoredProcedure.sql"));
            } catch (java.io.IOException e) {
            }
        }
        return myWriter;
    }

    private void setGenerator(StoredProcedureGenerator newGenerator) {
        generator = newGenerator;
    }

    private void setManager(SchemaManager newManager) {
        manager = newManager;
    }

    private void setWriter(Writer newWriter) {
        myWriter = newWriter;
    }
}