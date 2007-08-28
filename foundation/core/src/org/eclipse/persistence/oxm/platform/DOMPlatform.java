/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.oxm.platform;

import org.eclipse.persistence.internal.oxm.record.DOMUnmarshaller;
import org.eclipse.persistence.internal.oxm.record.PlatformUnmarshaller;
import org.eclipse.persistence.oxm.XMLContext;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>This class indicates that DOM parsing should be used when appropriate in an
 *  XML project to create XMLRecords.
 *  <b>Responsibilities:</b><ul>
 *  <li>Extend XMLPlatform</li>
 *  <li>Overrides newPlatformUnmarshaller to return an instance of DOMUnmarshaller</li>
 *  </ul>
 *  
 *  @author  mmacivor
 *  @see org.eclipse.persistence.internal.oxm.record.DOMUnmarshaller
 *  @see org.eclipse.persistence.oxm.record.DOMRecord
 */
public class DOMPlatform extends XMLPlatform {

    /**
     * INTERNAL:
     */
    public PlatformUnmarshaller newPlatformUnmarshaller(XMLContext xmlContext) {
        return new DOMUnmarshaller(xmlContext);
    }
}