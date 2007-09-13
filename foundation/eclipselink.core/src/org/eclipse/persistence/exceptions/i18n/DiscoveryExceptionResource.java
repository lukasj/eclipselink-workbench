/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.exceptions.i18n;

import java.util.ListResourceBundle;

/**
 * INTERNAL:
 * English ResourceBundle for ConcurrencyException messages.
 *
 * @author Steven Vo
 */
public class DiscoveryExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "22001", "Could not join multicast group" },
                                           { "22002", "Could not send service announcment" },
                                           { "22003", "Failed doing lookup of local host" },
                                           { "22004", "Failed trying to receive a service announcement from a remote service" }
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}