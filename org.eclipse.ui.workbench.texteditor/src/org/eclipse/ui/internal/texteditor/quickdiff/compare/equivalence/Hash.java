/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.equivalence;

/**
 * Value objects. Subclasses must override <code>equals</code> and are
 * typically <code>final</code>.
 *
 * @since 3.2
 */
public abstract class Hash implements Cloneable {

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			throw new AssertionError(x);
		}
	}

	/**
	 * Returns <code>true</code> if the two hashes are equal,
	 * <code>false</code> if not. Subclasses must override.
	 *
	 * @param obj {@inheritDoc}
	 * @return <code>true</code> if the receiver is equal to <code>obj</code>
	 */
	public abstract boolean equals(Object obj);
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @since 3.5
	 */
	public abstract int hashCode();

}