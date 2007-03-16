/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.ui.keys.ModifierKey;

/**
 * A comparator that sorts the modifier keys based on the native environment.
 * Currently, this is only the windowing toolkit, but in the future it might
 * expand to include the window manager.
 * 
 * @since 3.0
 */
class NativeModifierKeyComparator implements Comparator {

    /**
     * The sort order value to use when the modifier key is not recognized.
     */
    private final static int UNKNOWN_KEY = Integer.MAX_VALUE;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compare(Object left, Object right) {
        ModifierKey modifierKeyLeft = (ModifierKey) left;
        ModifierKey modifierKeyRight = (ModifierKey) right;
        int modifierKeyLeftRank = rank(modifierKeyLeft);
        int modifierKeyRightRank = rank(modifierKeyRight);

        if (modifierKeyLeftRank != modifierKeyRightRank) {
            return modifierKeyLeftRank - modifierKeyRightRank;
        } else {
            return modifierKeyLeft.compareTo(modifierKeyRight);
        }
    }

    /**
     * Calculates a rank for a given modifier key.
     * 
     * @param modifierKey
     *            The modifier key to rank; may be <code>null</code>.
     * @return The rank of this modifier key. This is a non-negative number
     *         where a lower number suggests a higher rank.
     */
    private int rank(ModifierKey modifierKey) {
        String platform = SWT.getPlatform();

        if ("win32".equals(platform)) { //$NON-NLS-1$
            return rankWindows(modifierKey);
        }

        if ("gtk".equals(platform)) { //$NON-NLS-1$
            // TODO Do a look-up on window manager.
            return rankGNOME(modifierKey);
        }

        if ("carbon".equals(platform)) { //$NON-NLS-1$
            return rankMacOSX(modifierKey);
        }

        if ("motif".equals(platform)) { //$NON-NLS-1$
            // TODO Do a look-up on window manager.
            return rankGNOME(modifierKey);
        }

        return UNKNOWN_KEY;
    }

    /**
     * Provides a ranking for the modifier key based on the modifier key
     * ordering used in the GNOME window manager.
     * 
     * @param modifierKey
     *            The modifier key to rank; may be <code>null</code>.
     * @return The rank of this modifier key. This is a non-negative number
     *         where a lower number suggests a higher rank.
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
     * Provides a ranking for the modifier key based on the modifier key
     * ordering used in the KDE window manager.
     * 
     * @param modifierKey
     *            The modifier key to rank; may be <code>null</code>.
     * @return The rank of this modifier key. This is a non-negative number
     *         where a lower number suggests a higher rank.
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
     * Provides a ranking for the modifier key based on the modifier key
     * ordering used in the MacOS X operating system.
     * 
     * @param modifierKey
     *            The modifier key to rank; may be <code>null</code>.
     * @return The rank of this modifier key. This is a non-negative number
     *         where a lower number suggests a higher rank.
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
     * Provides a ranking for the modifier key based on the modifier key
     * ordering used in the Windows operating system.
     * 
     * @param modifierKey
     *            The modifier key to rank; may be <code>null</code>.
     * @return The rank of this modifier key. This is a non-negative number
     *         where a lower number suggests a higher rank.
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
