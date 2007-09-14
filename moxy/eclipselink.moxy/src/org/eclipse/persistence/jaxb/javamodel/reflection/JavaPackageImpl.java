/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.jaxb.javamodel.reflection;

import org.eclipse.persistence.jaxb.javamodel.JavaAnnotation;
import org.eclipse.persistence.jaxb.javamodel.JavaClass;
import org.eclipse.persistence.jaxb.javamodel.JavaPackage;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A wrapper class for a JDK Package.  This implementation
 * of the TopLink JAXB 2.0 Java model simply makes reflective calls on the 
 * underlying JDK object. 
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide access to the underlying package's qualified name, annotations, etc.</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see org.eclipse.persistence.jaxb.javamodel.JavaPackage
 * @see java.lang.Package
 */
public class JavaPackageImpl implements JavaPackage {
    protected Package jPkg;
    
    public JavaPackageImpl(Package javaPackage) {
        jPkg = javaPackage;
    }
    
    /**
     * Assumes JavaType is a JavaClassImpl instance
     */
    public JavaAnnotation getAnnotation(JavaClass arg0) {
        if (arg0 != null) {
            Class annotationClass = ((JavaClassImpl) arg0).getJavaClass();
            if (jPkg.isAnnotationPresent(annotationClass)) {
               return new JavaAnnotationImpl(jPkg.getAnnotation(annotationClass));
            }
        }
        return null;
    }

    public Collection getAnnotations() {
        ArrayList<JavaAnnotation> annotationCollection = new ArrayList<JavaAnnotation>();
        Annotation[] annotations = jPkg.getAnnotations();
        for (Annotation annotation : annotations) {
            annotationCollection.add(new JavaAnnotationImpl(annotation));
        }
        return annotationCollection;
    }

    public String getName() {
        return jPkg.getName();
    }

    public String getQualifiedName() {
        return jPkg.getName();
    }

//  ---------------- unimplemented methods ----------------//
    public JavaAnnotation getDeclaredAnnotation(JavaClass arg0) {
        return null;
    }

    public Collection getDeclaredAnnotations() {
        return null;
    }
}