/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.databinding.internal;

/**
 * Class Pair.  Represents a mathematical pair of objects (a, b).
 * @since 3.2
 */
public class Pair {

	/**
	 * a in the pair (a, b)
	 */
	public final Object a;

	/**
	 * b in the pair (a, b)
	 */
	public final Object b;

	/**
	 * Construct a Pair(a, b)
	 * 
	 * @param a a in the pair (a, b)
	 * @param b b in the pair (a, b)
	 */
	public Pair(Object a, Object b) {
		this.a = a;
		this.b = b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj.getClass() != Pair.class) {
			return false;
		}
		Pair other = (Pair) obj;
		return a.equals(other.a) && b.equals(other.b);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return a.hashCode() + b.hashCode();
	}
}