/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.Comparator;
import java.util.ResourceBundle;

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ModifierKey;

public final class GnomeKeyFormatter extends AbstractKeyFormatter {

    private final static class GnomeModifierKeyComparator extends
            AbstractModifierKeyComparator {

        protected int rank(ModifierKey modifierKey) {
            if (ModifierKey.SHIFT.equals(modifierKey)) {
                return 0;
            }

            if (ModifierKey.CTRL.equals(modifierKey)) {
                return 1;
            }

            if (ModifierKey.ALT.equals(modifierKey)) {
                return 2;
            }

            return Integer.MAX_VALUE;
        }
    }

    private final static Comparator MODIFIER_KEY_COMPARATOR = new GnomeModifierKeyComparator();

    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(GnomeKeyFormatter.class.getName());

    protected String getKeyDelimiter() {
        return Util.translateString(RESOURCE_BUNDLE, KEY_DELIMITER_KEY,
                KeyStroke.KEY_DELIMITER, false, false);
    }

    protected String getKeyStrokeDelimiter() {
        return Util.translateString(RESOURCE_BUNDLE, KEY_STROKE_DELIMITER_KEY,
                KeySequence.KEY_STROKE_DELIMITER, false, false);
    }

    protected Comparator getModifierKeyComparator() {
        return MODIFIER_KEY_COMPARATOR;
    }
}