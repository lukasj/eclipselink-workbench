/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.descriptors.invalidation;

import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.descriptors.invalidation.CacheInvalidationPolicy;

/**
 * PUBLIC:
 * A CacheInvalidationPolicy which allows objects to live for a specific amount of time
 * after they are read.  A TimeToLiveCacheInvalidationPolicy is instantiated with a specific
 * number of milliseconds.  This represents how long after an object is read it will expire.
 * @see CacheInvalidationPolicy
 */
public class TimeToLiveCacheInvalidationPolicy extends CacheInvalidationPolicy {
    protected long timeToLive = 0;

    /**
     * INTERNAL:
     * Default Constructor for Project XML
     * Unless the timeToLive is set by method, objects will expire immediately
     */
    public TimeToLiveCacheInvalidationPolicy() {
    }

    /**
     * PUBLIC:
     * Construct a TimeToLiveCacheInvalidationPolicy
     * @param timeToLive the number of milliseconds an object affected by this policy will live.
     */
    public TimeToLiveCacheInvalidationPolicy(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * INTERNAL:
     * return the next expiry time
     */
    public long getExpiryTimeInMillis(CacheKey key) {
        return key.getReadTime() + timeToLive;
    }

    /**
     * PUBLIC:
     * return the time-to-live specified for this policy
     */
    public long getTimeToLive() {
        return timeToLive;
    }

    /**
     * INTERNAL:
     * Return true if this object is set as invalid or has expired.
     */
    public boolean isInvalidated(CacheKey key, long currentTimeMillis) {
        if (key.getInvalidationState() == CacheKey.CACHE_KEY_INVALID) {
            return true;
        }
        return getExpiryTimeInMillis(key) <= currentTimeMillis;
    }

    /**
     * PUBLIC:
     * Set the time-to-live specified by this policy
     */
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }
}