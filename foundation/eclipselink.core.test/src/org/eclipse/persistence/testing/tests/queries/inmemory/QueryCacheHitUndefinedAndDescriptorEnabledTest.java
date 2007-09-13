/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.queries.inmemory;

//When query's shouldMaintainCache is undefined and descriptor's shouldDisableCacheHits is false,
//cache is checked and the same object is returned.
public class QueryCacheHitUndefinedAndDescriptorEnabledTest extends QueryAndDescriptorCacheHitTest {
    public QueryCacheHitUndefinedAndDescriptorEnabledTest() {
        setDescription("Test when cache hit is undefined in query and enabled in descriptor, cache is checked");
    }

    protected void setup() {
        super.setup();
        descriptor.setShouldDisableCacheHits(false);
    }
}