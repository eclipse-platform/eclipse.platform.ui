/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.keys;

import java.util.Objects;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * <code>Key</code> is the abstract base class for all objects representing keys
 * on the keyboard.
 * </p>
 * <p>
 * All <code>Key</code> objects have a formal string representation, called the
 * 'name' of the key, available via the <code>toString()</code> method.
 * </p>
 * <p>
 * All <code>Key</code> objects, via the <code>format()</code> method, provide a
 * version of their formal string representation translated by platform and
 * locale, suitable for display to a user.
 * </p>
 * <p>
 * <code>Key</code> objects are immutable. Clients are not permitted to extend
 * this class.
 * </p>
 *
 * @deprecated Please use org.eclipse.jface.bindings.keys.KeyStroke and
 *             org.eclipse.jface.bindings.keys.KeyLookupFactory
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
@Deprecated
public abstract class Key implements Comparable {

	/**
	 * The key from which this key was constructed. This value is defined by
	 * <code>KeyLookupFactory.getDefault()</code>.
	 */
	protected final int key;

	/**
	 * Constructs an instance of <code>Key</code> given its formal string
	 * representation.
	 *
	 * @param key the integer representation of this key, as defined by
	 *            <code>KeyLookupFactory.getDefault()</code>.
	 */
	Key(final int key) {
		this.key = key;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public final int compareTo(final Object object) {
		return Util.compare(key, ((Key) object).key);
	}

	@Override
	public final boolean equals(final Object object) {
		if (!(object instanceof Key)) {
			return false;
		}

		return key == ((Key) object).key;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(key);
	}

	/**
	 * Returns the formal string representation for this key.
	 *
	 * @return The formal string representation for this key. Guaranteed not to be
	 *         <code>null</code>.
	 */
	@Override
	public final String toString() {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		return lookup.formalNameLookup(key);
	}
}
