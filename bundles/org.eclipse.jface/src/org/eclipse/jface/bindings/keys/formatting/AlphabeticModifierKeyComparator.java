/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings.keys.formatting;

import java.util.Comparator;

import org.eclipse.jface.bindings.keys.ModifierKey;


/**
 * <p>
 * Compares modifier keys lexicographically by the name of the key.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public class AlphabeticModifierKeyComparator implements Comparator {

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compare(Object left, Object right) {
        ModifierKey modifierKeyLeft = (ModifierKey) left;
        ModifierKey modifierKeyRight = (ModifierKey) right;
        return modifierKeyLeft.toString()
                .compareTo(modifierKeyRight.toString());
    }
}
