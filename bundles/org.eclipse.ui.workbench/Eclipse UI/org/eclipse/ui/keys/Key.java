/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.keys;

/**
 * <p>
 * <code>Key</code> is the abstract base class for all objects representing
 * keys on the keyboard.
 * </p>
 * <p>
 * All <code>Key</code> objects have a formal string representation, called
 * the 'name' of the key, available via the <code>toString()</code> method.
 * </p>
 * <p>
 * All <code>Key</code> objects, via the <code>format()</code> method,
 * provide a version of their formal string representation translated by
 * platform and locale, suitable for display to a user.
 * </p>
 * <p>
 * <code>Key</code> objects are immutable. Clients are not permitted to extend
 * this class.
 * </p>
 * 
 * @deprecated Please use org.eclipse.jface.bindings.keys.Key
 * @since 3.0
 */
public abstract class Key implements Comparable {

	/**
	 * The key from which this key was constructed. This value will never be
	 * <code>null</code>.
	 */
	protected final org.eclipse.jface.bindings.keys.Key key;

	/**
	 * Constructs an instance of <code>Key</code> given its formal string
	 * representation.
	 * 
	 * @param name
	 *            the formal string representation of this key. Must not be
	 *            <code>null</code>.
	 */
	Key(final org.eclipse.jface.bindings.keys.Key key) {
		if (key == null) {
			throw new NullPointerException(
					"Cannot construct a key from a null key"); //$NON-NLS-1$
		}

		this.key = key;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public final int compareTo(final Object object) {
		return key.compareTo(((Key) object).key);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(final Object object) {
		if (!(object instanceof Key))
			return false;

		return key.equals(((Key) object).key);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		return key.hashCode();
	}

	/**
	 * Returns the formal string representation for this key.
	 * 
	 * @return The formal string representation for this key. Guaranteed not to
	 *         be <code>null</code>.
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		return key.toString();
	}
}