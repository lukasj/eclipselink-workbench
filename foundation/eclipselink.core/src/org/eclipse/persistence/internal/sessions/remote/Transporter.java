/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.sessions.remote;

import java.io.*;
import org.eclipse.persistence.internal.helper.*;
import org.eclipse.persistence.queries.*;

/**
 * As the name signifies the object is responsible for carrying read objects from the server to the client.
 */
public class Transporter implements Serializable {

    /** Object(s) read from the server */
    public Object object;

    // Changed to public to allow access to TransporterHolder and TransporterHepler in org.eclipse.persistence.sessions.remote.corba.orbix.
    public boolean wasOperationSuccessful;

    // Changed to public to allow access to TransporterHolder and TransporterHepler in org.eclipse.persistence.sessions.remote.corba.orbix.
    protected IdentityHashtable objectDescriptors;
    protected DatabaseQuery query;

    public Transporter() {
        this.wasOperationSuccessful = true;
    }

    public Transporter(Object object) {
        this();
        this.object = object;
    }

    /**
     *  Return the exception which this Transporter is holding.  An exception will only be returned when the
     *  operation that returned this transporter was not successful.
     *  @return java.lang.RuntimeException
     */
    public RuntimeException getException() {
        if (wasOperationSuccessful()) {
            return null;
        }
        return (RuntimeException)getObject();
    }

    /**
     * Returns the read object(s) from the server side.
     */
    public Object getObject() {
        return object;
    }

    /**
     * Returns a hashtable of object descriptors.
     */
    public IdentityHashtable getObjectDescriptors() {
        return objectDescriptors;
    }

    /**
     *  Return the query associated with this transporter.
     *  @return org.eclipse.persistence.queries.DatabaseQuery
     */
    public DatabaseQuery getQuery() {
        return query;
    }

    /**
     *  Set the exception associated with this Transporter
     *  @param java.lang.RuntimeException
     */
    public void setException(RuntimeException exception) {
        setObject(exception);
        setWasOperationSuccessful(false);
    }

    /**
     * Set the read object(s) from the server side.
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * Get object to object descriptor
     */
    public void setObjectDescriptors(IdentityHashtable objectDescriptors) {
        this.objectDescriptors = objectDescriptors;
    }

    /**
     *  Set the query associated with this transporter
     *  @param org.eclipse.persistence.queries.DatabaseQuery
     */
    public void setQuery(DatabaseQuery query) {
        this.query = query;
    }

    /**
     *  Set whether the operation which caused this transporter to be returned was successful.
     *  @param boolean
     */
    public void setWasOperationSuccessful(boolean wasOperationSuccessful) {
        this.wasOperationSuccessful = wasOperationSuccessful;
    }

    public String toString() {
        return Helper.getShortClassName(getClass()) + "(" + getObject() + ")";
    }

    /**
     *  Return whether the operation which caused this transporter to be returned was successful.
     *  @return boolean
     */
    public boolean wasOperationSuccessful() {
        return wasOperationSuccessful;
    }
}