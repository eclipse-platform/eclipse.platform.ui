/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.bindings.keys.formatting;

import java.util.Comparator;

import org.eclipse.jface.bindings.keys.ModifierKey;

/**
 * <p>
 * An abstract implementation of a comparator for modifier keys.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
abstract class AbstractModifierKeyComparator implements Comparator {

	/**
	 * Compares two modifier keys based on their rank. The rank is determined by
	 * concrete implementations of this class.
	 * 
	 * @param left
	 *            The first modifier key; must not be <code>null</code>.
	 * @param right
	 *            The right modifier key; must not be <code>null</code>.
	 * @return The result of the comparison.
	 */
	public final int compare(final Object left, final Object right) {
		ModifierKey modifierKeyLeft = (ModifierKey) left;
		ModifierKey modifierKeyRight = (ModifierKey) right;
		int modifierKeyLeftRank = rank(modifierKeyLeft);
		int modifierKeyRightRank = rank(modifierKeyRight);

		if (modifierKeyLeftRank != modifierKeyRightRank)
			return modifierKeyLeftRank - modifierKeyRightRank;

		return modifierKeyLeft.compareTo(modifierKeyRight);
	}

	/**
	 * Returns an integer representation of the modifier key.
	 * 
	 * @param modifierKey
	 *            The modifier key for which the rank should be retrieved; must
	 *            not be <code>null</code>.
	 * @return An integer representation -- intended for discrete ordering -- of
	 *         the given modifier key.
	 */
	protected abstract int rank(final ModifierKey modifierKey);
}
