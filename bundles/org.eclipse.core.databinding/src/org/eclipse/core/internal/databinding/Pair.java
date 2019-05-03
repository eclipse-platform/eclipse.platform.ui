/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.databinding;

import java.util.Objects;

/**
 * Class Pair.  Represents a mathematical pair of objects (a, b).
 * @since 1.0
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(a);
		result = prime * result + Objects.hashCode(b);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		return Objects.equals(this.a, other.a) && Objects.equals(this.b, other.b);
	}

}
