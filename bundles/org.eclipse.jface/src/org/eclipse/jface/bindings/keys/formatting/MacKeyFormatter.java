/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.bindings.keys.formatting;

import java.util.Comparator;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.jface.bindings.keys.CharacterKey;
import org.eclipse.jface.bindings.keys.Key;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ModifierKey;
import org.eclipse.jface.bindings.keys.SpecialKey;
import org.eclipse.jface.util.Util;

/**
 * Formats the key in a form appropriate for MacOS X.
 * 
 * @since 3.1
 */
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
                .put(CharacterKey.BS.toString(), Character.toString('\u232B'));
        KEY_LOOKUP
                .put(CharacterKey.CR.toString(), Character.toString('\u21A9'));
        KEY_LOOKUP.put(CharacterKey.DEL.toString(), Character
                .toString('\u2326'));
        KEY_LOOKUP.put(CharacterKey.SPACE.toString(), Character
                .toString('\u2423'));
        KEY_LOOKUP
                .put(ModifierKey.ALT.toString(), Character.toString('\u2325'));
        KEY_LOOKUP.put(ModifierKey.COMMAND.toString(), Character
                .toString('\u2318'));
        KEY_LOOKUP.put(ModifierKey.CTRL.toString(), Character
                .toString('\u2303'));
        KEY_LOOKUP.put(ModifierKey.SHIFT.toString(), Character
                .toString('\u21E7'));
        KEY_LOOKUP.put(SpecialKey.ARROW_DOWN.toString(), Character
                .toString('\u2193'));
        KEY_LOOKUP.put(SpecialKey.ARROW_LEFT.toString(), Character
                .toString('\u2190'));
        KEY_LOOKUP.put(SpecialKey.ARROW_RIGHT.toString(), Character
                .toString('\u2192'));
        KEY_LOOKUP.put(SpecialKey.ARROW_UP.toString(), Character
                .toString('\u2191'));
        KEY_LOOKUP.put(SpecialKey.END.toString(), Character.toString('\u2198'));
        KEY_LOOKUP.put(SpecialKey.NUMPAD_ENTER.toString(), Character
                .toString('\u2324'));
        KEY_LOOKUP
                .put(SpecialKey.HOME.toString(), Character.toString('\u2196'));
        KEY_LOOKUP.put(SpecialKey.PAGE_DOWN.toString(), Character
                .toString('\u21DF'));
        KEY_LOOKUP.put(SpecialKey.PAGE_UP.toString(), Character
                .toString('\u21DE'));
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
