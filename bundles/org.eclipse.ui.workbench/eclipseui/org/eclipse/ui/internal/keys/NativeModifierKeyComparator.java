/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

package org.eclipse.ui.internal.keys;

import java.util.Comparator;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.keys.ModifierKey;

/**
 * A comparator that sorts the modifier keys based on the native environment.
 * Currently, this is only the windowing toolkit, but in the future it might
 * expand to include the window manager.
 *
 * @since 3.0
 */
class NativeModifierKeyComparator implements Comparator<ModifierKey> {

	/**
	 * The sort order value to use when the modifier key is not recognized.
	 */
	private static final int UNKNOWN_KEY = Integer.MAX_VALUE;

	@Override
	public int compare(ModifierKey left, ModifierKey right) {
		ModifierKey modifierKeyLeft = left;
		ModifierKey modifierKeyRight = right;
		int modifierKeyLeftRank = rank(modifierKeyLeft);
		int modifierKeyRightRank = rank(modifierKeyRight);

		if (modifierKeyLeftRank != modifierKeyRightRank) {
			return modifierKeyLeftRank - modifierKeyRightRank;
		}
		return modifierKeyLeft.compareTo(modifierKeyRight);
	}

	/**
	 * Calculates a rank for a given modifier key.
	 *
	 * @param modifierKey The modifier key to rank; may be <code>null</code>.
	 * @return The rank of this modifier key. This is a non-negative number where a
	 *         lower number suggests a higher rank.
	 */
	private int rank(ModifierKey modifierKey) {

		if (Util.isWindows()) {
			return rankWindows(modifierKey);
		}

		if (Util.isGtk()) {
			// TODO Do a look-up on window manager.
			return rankGNOME(modifierKey);
		}

		if (Util.isMac()) {
			return rankMacOSX(modifierKey);
		}

		return UNKNOWN_KEY;
	}

	/**
	 * Provides a ranking for the modifier key based on the modifier key ordering
	 * used in the GNOME window manager.
	 *
	 * @param modifierKey The modifier key to rank; may be <code>null</code>.
	 * @return The rank of this modifier key. This is a non-negative number where a
	 *         lower number suggests a higher rank.
	 */
	private final int rankGNOME(ModifierKey modifierKey) {
		if (ModifierKey.SHIFT.equals(modifierKey)) {
			return 0;
		}

		if (ModifierKey.CTRL.equals(modifierKey)) {
			return 1;
		}

		if (ModifierKey.ALT.equals(modifierKey)) {
			return 2;
		}

		return UNKNOWN_KEY;

	}

	/**
	 * Provides a ranking for the modifier key based on the modifier key ordering
	 * used in the KDE window manager.
	 *
	 * @param modifierKey The modifier key to rank; may be <code>null</code>.
	 * @return The rank of this modifier key. This is a non-negative number where a
	 *         lower number suggests a higher rank.
	 */
//    private final int rankKDE(ModifierKey modifierKey) {
//        if (ModifierKey.ALT.equals(modifierKey)) {
//            return 0;
//        }
//
//        if (ModifierKey.CTRL.equals(modifierKey)) {
//            return 1;
//        }
//
//        if (ModifierKey.SHIFT.equals(modifierKey)) {
//            return 2;
//        }
//
//        return UNKNOWN_KEY;
//    }

	/**
	 * Provides a ranking for the modifier key based on the modifier key ordering
	 * used in the MacOS X operating system.
	 *
	 * @param modifierKey The modifier key to rank; may be <code>null</code>.
	 * @return The rank of this modifier key. This is a non-negative number where a
	 *         lower number suggests a higher rank.
	 */
	private final int rankMacOSX(ModifierKey modifierKey) {
		if (ModifierKey.SHIFT.equals(modifierKey)) {
			return 0;
		}

		if (ModifierKey.CTRL.equals(modifierKey)) {
			return 1;
		}

		if (ModifierKey.ALT.equals(modifierKey)) {
			return 2;
		}

		if (ModifierKey.COMMAND.equals(modifierKey)) {
			return 3;
		}

		return UNKNOWN_KEY;
	}

	/**
	 * Provides a ranking for the modifier key based on the modifier key ordering
	 * used in the Windows operating system.
	 *
	 * @param modifierKey The modifier key to rank; may be <code>null</code>.
	 * @return The rank of this modifier key. This is a non-negative number where a
	 *         lower number suggests a higher rank.
	 */
	private final int rankWindows(ModifierKey modifierKey) {
		if (ModifierKey.CTRL.equals(modifierKey)) {
			return 0;
		}

		if (ModifierKey.ALT.equals(modifierKey)) {
			return 1;
		}

		if (ModifierKey.SHIFT.equals(modifierKey)) {
			return 2;
		}

		return UNKNOWN_KEY;
	}
}
