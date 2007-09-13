/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.eis.adapters.xmlfile;

import java.util.Properties;
import javax.resource.cci.*;
import org.eclipse.persistence.eis.*;
import org.eclipse.persistence.internal.eis.adapters.xmlfile.*;
import org.eclipse.persistence.exceptions.*;

/**
 * Provides the behavoir of instantiating a XML file adapter ConnectionSpec.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class XMLFileEISConnectionSpec extends EISConnectionSpec {

    /** Connection spec properties. */
    public static String DIRECTORY = "directory";

    /**
     * PUBLIC:
     * Default constructor.
     */
    public XMLFileEISConnectionSpec() {
        super();
    }

    /**
     * Connect with the specified properties and return the Connection.
     */
    public Connection connectToDataSource(EISAccessor accessor, Properties properties) throws DatabaseException, ValidationException {
        setConnectionFactory(new XMLFileConnectionFactory());
        String directory = (String)properties.get(DIRECTORY);
        if (directory != null) {
            setConnectionSpec(new XMLFileConnectionSpec(directory));
        }

        return super.connectToDataSource(accessor, properties);
    }
}