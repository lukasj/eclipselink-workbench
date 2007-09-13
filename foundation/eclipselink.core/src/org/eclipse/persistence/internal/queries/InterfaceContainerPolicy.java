/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.queries;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.lang.reflect.*;
import org.eclipse.persistence.exceptions.*;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.internal.security.PrivilegedMethodInvoker;
import org.eclipse.persistence.internal.security.PrivilegedClassForName;
import org.eclipse.persistence.internal.security.PrivilegedGetMethod;
import org.eclipse.persistence.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: The abstract class for ContainerPolicy's whose container class implements
 * a container interface.
 * <p>
 *
 * @see CollectionContainerPolicy
 * @see MapContainerPolicy
 */
public abstract class InterfaceContainerPolicy extends ContainerPolicy {

    /** The concrete container class. */
    protected Class containerClass;
    protected String containerClassName;

    /** The method which will return a clone of an instance of the containerClass. */
    protected transient Method cloneMethod;

    /**
     * INTERNAL:
     * Construct a new policy.
     */
    public InterfaceContainerPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class.
     */
    public InterfaceContainerPolicy(Class containerClass) {
        setContainerClass(containerClass);
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class name.
     */
    public InterfaceContainerPolicy(String containerClassName) {
        setContainerClassName(containerClassName);
    }
    
    /**
     * INTERNAL:
     * Return if the policy is equal to the other.
     * By default if they are the same class, they are considered equal.
     * This is used for query parse caching.
     */
    public boolean equals(Object object) {
        return super.equals(object) && getContainerClass().equals(((InterfaceContainerPolicy)object).getContainerClass());
    }

    /**
     * INTERNAL:
     * Return a clone of the specified container.
     */
    public Object cloneFor(Object container) {
        if (container == null) {
            return null;
        }

        try {
            return invokeCloneMethodOn(getCloneMethod(), container);
        } catch (IllegalArgumentException ex) {
            // container may be a superclass of the concrete container class
            // so we have to use the right clone method...
            return invokeCloneMethodOn(getCloneMethod(container.getClass()), container);
        }
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this ContainerPolicy to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);
        if (containerClassName == null){
            return;
        }
        Class containerClass = null;
        try{
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    containerClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(containerClassName, true, classLoader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.classNotFoundWhileConvertingClassNames(containerClassName, exception.getException());
                }
            } else {
                containerClass = org.eclipse.persistence.internal.security.PrivilegedAccessHelper.getClassForName(containerClassName, true, classLoader);
            }
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(containerClassName, exc);
        }
        setContainerClass(containerClass);
    };

    /**
     * INTERNAL:
     * Return the 'clone()' Method for the container class.
     * Lazy initialization is used, so we can serialize these things.
     */
    public Method getCloneMethod() {
        if (cloneMethod == null) {
            setCloneMethod(getCloneMethod(getContainerClass()));
        }
        return cloneMethod;
    }

    /**
     * INTERNAL:
     * Return the 'clone()' Method for the specified class.
     * Return null if the method does not exist anywhere in the hierarchy
     */
    protected Method getCloneMethod(Class javaClass) {
        try {
            // This must not be set "accessible" - clone() must be public, and some JVM's do not allow access to JDK classes.
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return (Method)AccessController.doPrivileged(new PrivilegedGetMethod(javaClass, "clone", (Class[])null, false));
                } catch (PrivilegedActionException exception) {
                    throw QueryException.methodDoesNotExistInContainerClass("clone", javaClass);
                }
            } else {
                return PrivilegedAccessHelper.getMethod(javaClass, "clone", (Class[])null, false);
            }
        } catch (NoSuchMethodException ex) {
            throw QueryException.methodDoesNotExistInContainerClass("clone", javaClass);
        }
    }

    /**
     * INTERNAL:
     * Returns the container class to be used with this policy.
     */
    public Class getContainerClass() {
        return containerClass;
    }

    public String getContainerClassName() {
        if ((containerClassName == null) && (containerClass != null)) {
            containerClassName = containerClass.getName();
        }
        return containerClassName;
    }

    public abstract Class getInterfaceType();

    /**
     * INTERNAL:
     * Return whether the iterator has more objects,
     */
    public boolean hasNext(Object iterator) {
        return ((Iterator)iterator).hasNext();
    }

    /**
     * INTERNAL:
     * Invoke the specified clone method on the container,
     * handling the necessary exceptions.
     */
    protected Object invokeCloneMethodOn(Method method, Object container) {
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return AccessController.doPrivileged(new PrivilegedMethodInvoker(method, container, (Object[])null));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw QueryException.cannotAccessMethodOnObject(method, container);
                    } else {
                        throw QueryException.methodInvocationFailed(method, container, throwableException);
                    }
                }
            } else {
                return PrivilegedAccessHelper.invokeMethod(method, container, (Object[])null);
            }
        } catch (IllegalAccessException ex1) {
            throw QueryException.cannotAccessMethodOnObject(method, container);
        } catch (InvocationTargetException ex2) {
            throw QueryException.methodInvocationFailed(method, container, ex2);
        }
    }

    /**
     * INTERNAL:
     * Validate the container type.
     */
    public boolean isValidContainerType(Class containerType) {
        return org.eclipse.persistence.internal.helper.Helper.classImplementsInterface(containerType, getInterfaceType());
    }

    /**
     * INTERNAL:
     * Return the next object on the queue.
     * Valid for some subclasses only.
     */
    protected Object next(Object iterator) {
        return ((Iterator)iterator).next();
    }

    /**
     * INTERNAL:
     * Set the Method that will return a clone of an instance of the containerClass.
     */
    public void setCloneMethod(Method cloneMethod) {
        this.cloneMethod = cloneMethod;
    }

    /**
     * INTERNAL:
     * Set the class to use as the container.
     */
    public void setContainerClass(Class containerClass) {
        this.containerClass = containerClass;
        initializeConstructor();
    }

    public void setContainerClassName(String containerClassName) {
        this.containerClassName = containerClassName;
    }
    
    /**
     * INTERNAL:
     * Return a container populated with the contents of the specified Vector.
     */
    public Object buildContainerFromVector(Vector vector, AbstractSession session) {
        // PERF: If a Vector policy just return the original.
        if (this.containerClass == ClassConstants.Vector_class) {
            return vector;
        }
        return super.buildContainerFromVector(vector, session);
    }

    protected Object toStringInfo() {
        return this.getContainerClass();
    }
}