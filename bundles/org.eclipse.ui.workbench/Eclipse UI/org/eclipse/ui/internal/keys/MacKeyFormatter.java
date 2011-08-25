/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.CharacterKey;
import org.eclipse.ui.keys.Key;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.ModifierKey;
import org.eclipse.ui.keys.SpecialKey;

public final class MacKeyFormatter extends AbstractKeyFormatter {

    private final static class MacModifierKeyComparator extends
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

            if (ModifierKey.COMMAND.equals(modifierKey)) {
                return 3;
            }

            return Integer.MAX_VALUE;
        }
    }

    private final static HashMap KEY_LOOKUP = new HashMap();

    private final static Comparator MODIFIER_KEY_COMPARATOR = new MacModifierKeyComparator();

    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(MacKeyFormatter.class.getName());

    static {
        KEY_LOOKUP
                .put(CharacterKey.BS.toString(), "\u232B");  //$NON-NLS-1$
        KEY_LOOKUP
                .put(CharacterKey.CR.toString(), "\u21A9");  //$NON-NLS-1$
        KEY_LOOKUP.put(CharacterKey.DEL.toString(), "\u2326");  //$NON-NLS-1$
        KEY_LOOKUP.put(CharacterKey.SPACE.toString(), "\u2423");  //$NON-NLS-1$
        KEY_LOOKUP
                .put(ModifierKey.ALT.toString(), "\u2325");  //$NON-NLS-1$
        KEY_LOOKUP.put(ModifierKey.COMMAND.toString(), "\u2318");  //$NON-NLS-1$
        KEY_LOOKUP.put(ModifierKey.CTRL.toString(), "\u2303");  //$NON-NLS-1$
        KEY_LOOKUP.put(ModifierKey.SHIFT.toString(), "\u21E7");  //$NON-NLS-1$
        KEY_LOOKUP.put(SpecialKey.ARROW_DOWN.toString(), "\u2193");  //$NON-NLS-1$
        KEY_LOOKUP.put(SpecialKey.ARROW_LEFT.toString(), "\u2190");  //$NON-NLS-1$
        KEY_LOOKUP.put(SpecialKey.ARROW_RIGHT.toString(), "\u2192");  //$NON-NLS-1$
        KEY_LOOKUP.put(SpecialKey.ARROW_UP.toString(), "\u2191");  //$NON-NLS-1$
        KEY_LOOKUP.put(SpecialKey.END.toString(), "\u2198");  //$NON-NLS-1$
        KEY_LOOKUP.put(SpecialKey.NUMPAD_ENTER.toString(), "\u2324");  //$NON-NLS-1$
        KEY_LOOKUP
                .put(SpecialKey.HOME.toString(), "\u2196");  //$NON-NLS-1$
        KEY_LOOKUP.put(SpecialKey.PAGE_DOWN.toString(), "\u21DF");  //$NON-NLS-1$
        KEY_LOOKUP.put(SpecialKey.PAGE_UP.toString(), "\u21DE");  //$NON-NLS-1$
    }

    public String format(Key key) {
        String string = (String) KEY_LOOKUP.get(key.toString());
        return string != null ? string : super.format(key);
    }

    protected String getKeyDelimiter() {
        return Util.translateString(RESOURCE_BUNDLE, KEY_DELIMITER_KEY,
                Util.ZERO_LENGTH_STRING, false, false);
    }

    protected String getKeyStrokeDelimiter() {
        return Util.translateString(RESOURCE_BUNDLE, KEY_STROKE_DELIMITER_KEY,
                KeySequence.KEY_STROKE_DELIMITER, false, false);
    }

    protected Comparator getModifierKeyComparator() {
        return MODIFIER_KEY_COMPARATOR;
    }
}
