/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
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

import org.eclipse.ui.keys.ModifierKey;

abstract class AbstractModifierKeyComparator implements Comparator {

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

    protected abstract int rank(ModifierKey modifierKey);
}
