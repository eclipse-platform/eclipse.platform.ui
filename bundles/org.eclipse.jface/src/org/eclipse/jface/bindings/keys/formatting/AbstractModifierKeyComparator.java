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


abstract class AbstractModifierKeyComparator implements Comparator {

    public int compare(Object left, Object right) {
        ModifierKey modifierKeyLeft = (ModifierKey) left;
        ModifierKey modifierKeyRight = (ModifierKey) right;
        int modifierKeyLeftRank = rank(modifierKeyLeft);
        int modifierKeyRightRank = rank(modifierKeyRight);

        if (modifierKeyLeftRank != modifierKeyRightRank)
            return modifierKeyLeftRank - modifierKeyRightRank;

        return modifierKeyLeft.compareTo(modifierKeyRight);
    }

    protected abstract int rank(ModifierKey modifierKey);
}
