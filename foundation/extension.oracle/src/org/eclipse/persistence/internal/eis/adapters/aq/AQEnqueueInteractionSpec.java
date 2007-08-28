/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.eis.adapters.aq;

import oracle.AQ.*;

/**
 * Interaction spec for AQ JCA adapter.
 * Specifies an enqueue interaction for a queue.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class AQEnqueueInteractionSpec extends AQInteractionSpec {
    protected AQEnqueueOption options;

    /**
     * Default constructor.
     */
    public AQEnqueueInteractionSpec() {
    }

    /**
     * Return the AQ specific dequeue options.
     */
    public AQEnqueueOption getOptions() {
        return options;
    }

    /**
     * Set the AQ specific dequeue options.
     */
    public void setOptions(AQEnqueueOption options) {
        this.options = options;
    }
}