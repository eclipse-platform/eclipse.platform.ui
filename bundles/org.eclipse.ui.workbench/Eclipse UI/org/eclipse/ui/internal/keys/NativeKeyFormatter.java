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
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.CharacterKey;
import org.eclipse.ui.keys.Key;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ModifierKey;
import org.eclipse.ui.keys.SpecialKey;

/**
 * Formats the key sequences and key strokes into the native human-readable
 * format. This is typically what you would see on the menus for the given
 * platform and locale.
 * 
 * @since 3.0
 */
public class NativeKeyFormatter extends AbstractKeyFormatter {

    /**
     * The key into the internationalization resource bundle for the delimiter
     * to use between keys (on the Carbon platform).
     */
    private final static String CARBON_KEY_DELIMITER_KEY = "CARBON_KEY_DELIMITER"; //$NON-NLS-1$

    /**
     * A look-up table for the string representations of various carbon keys.
     */
    private final static HashMap CARBON_KEY_LOOK_UP = new HashMap();

    /**
     * A comparator to sort modifier keys in the order that they would be
     * displayed to a user. This comparator is platform-specific.
     */
    private final static Comparator MODIFIER_KEY_COMPARATOR = new NativeModifierKeyComparator();

    /**
     * The resource bundle used by <code>format()</code> to translate formal
     * string representations by locale.
     */
    private final static ResourceBundle RESOURCE_BUNDLE;

    /**
     * The key into the internationalization resource bundle for the delimiter
     * to use between key strokes (on the Win32 platform).
     */
    private final static String WIN32_KEY_STROKE_DELIMITER_KEY = "WIN32_KEY_STROKE_DELIMITER"; //$NON-NLS-1$

    static {
        RESOURCE_BUNDLE = ResourceBundle.getBundle(NativeKeyFormatter.class
                .getName());

        CARBON_KEY_LOOK_UP.put(CharacterKey.BS.toString(), Character
                .toString('\u232B'));
        CARBON_KEY_LOOK_UP.put(CharacterKey.CR.toString(), Character
                .toString('\u21A9'));
        CARBON_KEY_LOOK_UP.put(CharacterKey.DEL.toString(), Character
                .toString('\u2326'));
        CARBON_KEY_LOOK_UP.put(CharacterKey.SPACE.toString(), Character
                .toString('\u2423'));
        CARBON_KEY_LOOK_UP.put(ModifierKey.ALT.toString(), Character
                .toString('\u2325'));
        CARBON_KEY_LOOK_UP.put(ModifierKey.COMMAND.toString(), Character
                .toString('\u2318'));
        CARBON_KEY_LOOK_UP.put(ModifierKey.CTRL.toString(), Character
                .toString('\u2303'));
        CARBON_KEY_LOOK_UP.put(ModifierKey.SHIFT.toString(), Character
                .toString('\u21E7'));
        CARBON_KEY_LOOK_UP.put(SpecialKey.ARROW_DOWN.toString(), Character
                .toString('\u2193'));
        CARBON_KEY_LOOK_UP.put(SpecialKey.ARROW_LEFT.toString(), Character
                .toString('\u2190'));
        CARBON_KEY_LOOK_UP.put(SpecialKey.ARROW_RIGHT.toString(), Character
                .toString('\u2192'));
        CARBON_KEY_LOOK_UP.put(SpecialKey.ARROW_UP.toString(), Character
                .toString('\u2191'));
        CARBON_KEY_LOOK_UP.put(SpecialKey.END.toString(), Character
                .toString('\u2198'));
        CARBON_KEY_LOOK_UP.put(SpecialKey.NUMPAD_ENTER.toString(), Character
                .toString('\u2324'));
        CARBON_KEY_LOOK_UP.put(SpecialKey.HOME.toString(), Character
                .toString('\u2196'));
        CARBON_KEY_LOOK_UP.put(SpecialKey.PAGE_DOWN.toString(), Character
                .toString('\u21DF'));
        CARBON_KEY_LOOK_UP.put(SpecialKey.PAGE_UP.toString(), Character
                .toString('\u21DE'));
    }

    /**
     * Formats an individual key into a human readable format. This uses an
     * internationalization resource bundle to look up the key. This does the
     * platform-specific formatting for Carbon.
     * 
     * @param key
     *            The key to format; must not be <code>null</code>.
     * @return The key formatted as a string; should not be <code>null</code>.
     */
    public String format(Key key) {
        String name = key.toString();

        // TODO consider platform-specific resource bundles
        if ("carbon".equals(SWT.getPlatform())) { //$NON-NLS-1$    	
            String formattedName = (String) CARBON_KEY_LOOK_UP.get(name);
            if (formattedName != null) {
                return formattedName;
            }
        }

        return super.format(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.keys.AbstractKeyFormatter#getKeyDelimiter()
     */
    protected String getKeyDelimiter() {
        // We must do the look up every time, as our locale might change.
        if ("carbon".equals(SWT.getPlatform())) { //$NON-NLS-1$
            return Util.translateString(RESOURCE_BUNDLE,
                    CARBON_KEY_DELIMITER_KEY, Util.ZERO_LENGTH_STRING, false,
                    false);
        } else {
            return Util.translateString(RESOURCE_BUNDLE, KEY_DELIMITER_KEY,
                    KeyStroke.KEY_DELIMITER, false, false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.keys.AbstractKeyFormatter#getKeyStrokeDelimiter()
     */
    protected String getKeyStrokeDelimiter() {
        // We must do the look up every time, as our locale might change.
        if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
            return Util.translateString(RESOURCE_BUNDLE,
                    WIN32_KEY_STROKE_DELIMITER_KEY,
                    KeySequence.KEY_STROKE_DELIMITER, false, false);
        } else {
            return Util.translateString(RESOURCE_BUNDLE,
                    KEY_STROKE_DELIMITER_KEY, KeySequence.KEY_STROKE_DELIMITER,
                    false, false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.keys.AbstractKeyFormatter#getModifierKeyComparator()
     */
    protected Comparator getModifierKeyComparator() {
        return MODIFIER_KEY_COMPARATOR;
    }
}