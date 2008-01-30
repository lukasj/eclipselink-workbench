/*******************************************************************************
* Copyright (c) 2007 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0, which accompanies this distribution and is available at
* http://www.eclipse.org/legal/epl-v10.html.
*
* Contributors:
*     Oracle - initial API and implementation
******************************************************************************/
package org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta;

import org.eclipse.persistence.tools.workbench.utility.string.StringTools;

/**
 * This is a hard-coded implementation of ExternalClass that can be used for
 * primitive types (int, float, void, etc.).
 */
final class PrimitiveExternalClass
	implements ExternalClass
{
	private final Class primitiveClass;

	private static final ExternalClassDescription[] EMPTY_EXTERNAL_CLASS_DESCRIPTIONS = new ExternalClassDescription[0];
	private static final ExternalField[] EMPTY_EXTERNAL_FIELDS = new ExternalField[0];
	private static final ExternalConstructor[] EMPTY_EXTERNAL_CONSTRUCTORS = new ExternalConstructor[0];
	private static final ExternalMethod[] EMPTY_EXTERNAL_METHODS = new ExternalMethod[0];


	PrimitiveExternalClass(Class primitiveClass) {
		super();
		this.primitiveClass = primitiveClass;
	}

	/**
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#getDeclaredClasses()
	 */
	public ExternalClassDescription[] getDeclaredClasses() {
		return EMPTY_EXTERNAL_CLASS_DESCRIPTIONS;
	}

	/**
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#getDeclaredConstructors()
	 */
	public ExternalConstructor[] getDeclaredConstructors() {
		return EMPTY_EXTERNAL_CONSTRUCTORS;
	}

	/**
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#getDeclaredFields()
	 */
	public ExternalField[] getDeclaredFields() {
		return EMPTY_EXTERNAL_FIELDS;
	}

	/**
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#getDeclaredMethods()
	 */
	public ExternalMethod[] getDeclaredMethods() {
		return EMPTY_EXTERNAL_METHODS;
	}

	/**
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#getDeclaringClass()
	 */
	public ExternalClassDescription getDeclaringClass() {
		return null;
	}

	/**
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#getInterfaces()
	 */
	public ExternalClassDescription[] getInterfaces() {
		return EMPTY_EXTERNAL_CLASS_DESCRIPTIONS;
	}

	/**
	 * public abstract final
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#getModifiers()
	 */
	public int getModifiers() {
		return this.primitiveClass.getModifiers();
	}

	/**
	 * "int", "float", "void", etc.
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#getName()
	 */
	public String getName() {
		return this.primitiveClass.getName();
	}

	/**
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#getSuperclass()
	 */
	public ExternalClassDescription getSuperclass() {
		return null;
	}

	/**
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#isInterface()
	 */
	public boolean isInterface() {
		return false;
	}

	/**
	 * @see org.eclipse.persistence.tools.workbench.mappingsmodel.spi.meta.ExternalClass#isPrimitive()
	 */
	public boolean isPrimitive() {
		return true;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return StringTools.buildToStringFor(this, this.getName());
	}

}