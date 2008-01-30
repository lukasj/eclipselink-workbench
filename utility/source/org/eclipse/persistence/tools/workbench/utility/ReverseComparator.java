/*******************************************************************************
* Copyright (c) 2007 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0, which accompanies this distribution and is available at
* http://www.eclipse.org/legal/epl-v10.html.
*
* Contributors:
*     Oracle - initial API and implementation
******************************************************************************/
package org.eclipse.persistence.tools.workbench.utility;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This comparator will reverse the order of the specified comparator.
 * If the comparator is null, the natural ordering of the objects will be used.
 */
public class ReverseComparator
	implements Comparator, Serializable
{
	private final Comparator comparator;

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public ReverseComparator() {
		this(null);
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public ReverseComparator(Comparator comparator) {
		super();
		this.comparator = comparator;
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		if (this.comparator == null) {
			return ((Comparable) o2).compareTo(o1);
		}
		return this.comparator.compare(o2, o1);
	}

}