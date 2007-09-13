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

/**
 * PUBLIC:
 * A CacheInvalidationPolicy is used to set objects in TopLink's identity maps to be invalid
 * following given rules.  CacheInvalidationPolicy is the abstract superclass for all
 * policies used for cache invalidation.
 * By default in TopLink, objects do not expire in the cache.  Several different policies
 * are available to allow objects to expire.  These can be set on the ClassDescriptor.
 * @see org.eclipse.persistence.descriptors.ClassDescriptor
 * @see org.eclipse.persistence.descriptors.cacheinvalidation.NoExpiryCacheInvalidationPolicy
 * @see org.eclipse.persistence.descriptors.cacheinvalidation.DailyCacheInvalidationPolicy
 * @see org.eclipse.persistence.descriptors.cacheinvalidation.TimeToLiveCacheInvalidationPolicy
 */
public abstract class CacheInvalidationPolicy implements java.io.Serializable {
    public static final long NO_EXPIRY = -1;

    /** this will represent objects that do not expire */
    protected boolean shouldUpdateReadTimeOnUpdate = false;

    /**
       * INTERNAL:
       * Get the next time when this object will become invalid
       */
    public abstract long getExpiryTimeInMillis(CacheKey key);

    /**
     * INTERNAL:
     * Return the remaining life of this object
     */
    public long getRemainingValidTime(CacheKey key) {
        long expiryTime = getExpiryTimeInMillis(key);
        long remainingTime = expiryTime - System.currentTimeMillis();
        if (remainingTime > 0) {
            return remainingTime;
        }
        return 0;
    }

    /**
     * INTERNAL:
     * return true if this object is expire, false otherwise.
     */
    public boolean isInvalidated(CacheKey key) {
        return isInvalidated(key, System.currentTimeMillis());
    }
    
    /**
     * INTERNAL:
     * return true if this object is expire, false otherwise.
     */
    public abstract boolean isInvalidated(CacheKey key, long currentTimeMillis);

    /**
     * PUBLIC:
     * Set whether to update the stored time an object was read when an object is updated.
     * When the read time is updated, it indicates to TopLink that the data in the object
     * is up to date.  This means that cache invalidation checks will occur relative to the
     * new read time.
     * By default, the read time will not be updated when an object is updated.
     * Often it is possible to be confident that the object is up to date after an update
     * because otherwise the update will fail because of the locking policies in use.
     */
    public void setShouldUpdateReadTimeOnUpdate(boolean shouldUpdateReadTime) {
        shouldUpdateReadTimeOnUpdate = shouldUpdateReadTime;
    }

    /**
     * PUBLIC:
     * Return whether objects affected by this CacheInvalidationPolicy should have
     * the read time on their cache keys updated when an update occurs.
     */
    public boolean shouldUpdateReadTimeOnUpdate() {
        return shouldUpdateReadTimeOnUpdate;
    }
}