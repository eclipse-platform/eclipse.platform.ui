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
import java.util.HashMap;
import java.util.ResourceBundle;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.CharacterKey;
import org.eclipse.ui.keys.Key;
import org.eclipse.ui.keys.ModifierKey;
import org.eclipse.ui.keys.SpecialKey;

/**
 * Formats the key sequences and key strokes into the native human-readable
 * format. This is typically what you would see on the menus for the given
 * platform and locale.
 *
 * @since 3.0
 */
@SuppressWarnings("removal")
public class NativeKeyFormatter extends AbstractKeyFormatter {

	/**
	 * The key into the internationalization resource bundle for the delimiter to
	 * use between keys (on the Carbon platform).
	 */
	private static final String CARBON_KEY_DELIMITER_KEY = "CARBON_KEY_DELIMITER"; //$NON-NLS-1$

	/**
	 * A look-up table for the string representations of various carbon keys.
	 */
	private static final HashMap<String, String> CARBON_KEY_LOOK_UP = new HashMap<>();

	/**
	 * A comparator to sort modifier keys in the order that they would be displayed
	 * to a user. This comparator is platform-specific.
	 */
	private static final Comparator<ModifierKey> MODIFIER_KEY_COMPARATOR = new NativeModifierKeyComparator();

	/**
	 * The resource bundle used by <code>format()</code> to translate formal string
	 * representations by locale.
	 */
	private static final ResourceBundle RESOURCE_BUNDLE;

	/**
	 * The key into the internationalization resource bundle for the delimiter to
	 * use between key strokes (on the Win32 platform).
	 */
	private static final String WIN32_KEY_STROKE_DELIMITER_KEY = "WIN32_KEY_STROKE_DELIMITER"; //$NON-NLS-1$

	static {
		RESOURCE_BUNDLE = ResourceBundle.getBundle(NativeKeyFormatter.class.getName());

		CARBON_KEY_LOOK_UP.put(CharacterKey.BS.toString(), "\u232B"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(CharacterKey.CR.toString(), "\u21A9"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(CharacterKey.DEL.toString(), "\u2326"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(CharacterKey.SPACE.toString(), "\u2423"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(ModifierKey.ALT.toString(), "\u2325"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(ModifierKey.COMMAND.toString(), "\u2318"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(ModifierKey.CTRL.toString(), "\u2303"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(ModifierKey.SHIFT.toString(), "\u21E7"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(SpecialKey.ARROW_DOWN.toString(), "\u2193"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(SpecialKey.ARROW_LEFT.toString(), "\u2190"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(SpecialKey.ARROW_RIGHT.toString(), "\u2192"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(SpecialKey.ARROW_UP.toString(), "\u2191"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(SpecialKey.END.toString(), "\u2198"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(SpecialKey.NUMPAD_ENTER.toString(), "\u2324"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(SpecialKey.HOME.toString(), "\u2196"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(SpecialKey.PAGE_DOWN.toString(), "\u21DF"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(SpecialKey.PAGE_UP.toString(), "\u21DE"); //$NON-NLS-1$
	}

	/**
	 * Formats an individual key into a human readable format. This uses an
	 * internationalization resource bundle to look up the key. This does the
	 * platform-specific formatting for Carbon.
	 *
	 * @param key The key to format; must not be <code>null</code>.
	 * @return The key formatted as a string; should not be <code>null</code>.
	 */
	@Override
	public String format(Key key) {
		String name = key.toString();

		// TODO consider platform-specific resource bundles
		if (org.eclipse.jface.util.Util.isMac()) {
			String formattedName = CARBON_KEY_LOOK_UP.get(name);
			if (formattedName != null) {
				return formattedName;
			}
		}

		return super.format(key);
	}

	@Override
	protected String getKeyDelimiter() {
		// We must do the look up every time, as our locale might change.
		if (org.eclipse.jface.util.Util.isMac()) {
			return Util.translateString(RESOURCE_BUNDLE, CARBON_KEY_DELIMITER_KEY, Util.ZERO_LENGTH_STRING, false,
					false);
		}
		return Util.translateString(RESOURCE_BUNDLE, KEY_DELIMITER_KEY, KeyStroke.KEY_DELIMITER, false, false);
	}

	@Override
	protected String getKeyStrokeDelimiter() {
		// We must do the look up every time, as our locale might change.
		if (org.eclipse.jface.util.Util.isWindows()) {
			return Util.translateString(RESOURCE_BUNDLE, WIN32_KEY_STROKE_DELIMITER_KEY,
					KeySequence.KEY_STROKE_DELIMITER, false, false);
		}
		return Util.translateString(RESOURCE_BUNDLE, KEY_STROKE_DELIMITER_KEY, KeySequence.KEY_STROKE_DELIMITER, false,
				false);
	}

	@Override
	protected Comparator<ModifierKey> getModifierKeyComparator() {
		return MODIFIER_KEY_COMPARATOR;
	}
}
