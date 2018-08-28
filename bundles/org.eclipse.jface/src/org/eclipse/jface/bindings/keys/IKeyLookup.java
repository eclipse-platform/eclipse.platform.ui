/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
package org.eclipse.jface.bindings.keys;

/**
 * <p>
 * A facilitiy for converting the formal representation for key strokes
 * (i.e., used in persistence) into real key stroke instances.
 * </p>
 *
 * @since 3.1
 */
public interface IKeyLookup {
	/**
	 * The formal name of the 'Alt' key.
	 */
	String ALT_NAME = "ALT"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Arrow Down' key.
	 */
	String ARROW_DOWN_NAME = "ARROW_DOWN"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Arrow Left' key.
	 */
	String ARROW_LEFT_NAME = "ARROW_LEFT"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Arrow Right' key.
	 */
	String ARROW_RIGHT_NAME = "ARROW_RIGHT"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Arrow Up' key.
	 */
	String ARROW_UP_NAME = "ARROW_UP"; //$NON-NLS-1$

	/**
	 * An alternate name for the backspace key.
	 */
	String BACKSPACE_NAME = "BACKSPACE"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Break' key.
	 */
	String BREAK_NAME = "BREAK"; //$NON-NLS-1$

	/**
	 * The formal name of the backspace key.
	 */
	String BS_NAME = "BS"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Caps Lock' key.
	 */
	String CAPS_LOCK_NAME = "CAPS_LOCK"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Command' key.
	 */
	String COMMAND_NAME = "COMMAND"; //$NON-NLS-1$

	/**
	 * The formal name of the carriage return (U+000D)
	 */
	String CR_NAME = "CR"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Ctrl' key.
	 */
	String CTRL_NAME = "CTRL"; //$NON-NLS-1$

	/**
	 * The formal name of the delete (U+007F) key
	 */
	String DEL_NAME = "DEL"; //$NON-NLS-1$

	/**
	 * An alternative name for the delete key.
	 */
	String DELETE_NAME = "DELETE"; //$NON-NLS-1$

	/**
	 * The formal name of the 'End' key.
	 */
	String END_NAME = "END"; //$NON-NLS-1$

	/**
	 * An alternative name for the enter key.
	 */
	String ENTER_NAME = "ENTER"; //$NON-NLS-1$

	/**
	 * The formal name of the escape (U+001B) key.
	 */
	String ESC_NAME = "ESC"; //$NON-NLS-1$

	/**
	 * An alternative name for the escape key.
	 */
	String ESCAPE_NAME = "ESCAPE"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F1' key.
	 */
	String F1_NAME = "F1"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F10' key.
	 */
	String F10_NAME = "F10"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F11' key.
	 */
	String F11_NAME = "F11"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F12' key.
	 */
	String F12_NAME = "F12"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F13' key.
	 */
	String F13_NAME = "F13"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F14' key.
	 */
	String F14_NAME = "F14"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F15' key.
	 */
	String F15_NAME = "F15"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F16' key.
	 *
	 * @since 3.6
	 */
	String F16_NAME = "F16"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F17' key.
	 *
	 * @since 3.6
	 */
	String F17_NAME = "F17"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F18' key.
	 *
	 * @since 3.6
	 */
	String F18_NAME = "F18"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F19' key.
	 *
	 * @since 3.6
	 */
	String F19_NAME = "F19"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F20' key.
	 *
	 * @since 3.6
	 */
	String F20_NAME = "F20"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F2' key.
	 */
	String F2_NAME = "F2"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F3' key.
	 */
	String F3_NAME = "F3"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F4' key.
	 */
	String F4_NAME = "F4"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F5' key.
	 */
	String F5_NAME = "F5"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F6' key.
	 */
	String F6_NAME = "F6"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F7' key.
	 */
	String F7_NAME = "F7"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F8' key.
	 */
	String F8_NAME = "F8"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F9' key.
	 */
	String F9_NAME = "F9"; //$NON-NLS-1$

	/**
	 * The formal name of the form feed (U+000C) key.
	 */
	String FF_NAME = "FF"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Home' key.
	 */
	String HOME_NAME = "HOME"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Insert' key.
	 */
	String INSERT_NAME = "INSERT"; //$NON-NLS-1$

	/**
	 * The formal name of the line feed (U+000A) key.
	 */
	String LF_NAME = "LF"; //$NON-NLS-1$

	/**
	 * The formal name of the 'M1' key.
	 */
	String M1_NAME = "M1"; //$NON-NLS-1$

	/**
	 * The formal name of the 'M2' key.
	 */
	String M2_NAME = "M2"; //$NON-NLS-1$

	/**
	 * The formal name of the 'M3' key.
	 */
	String M3_NAME = "M3"; //$NON-NLS-1$

	/**
	 * The formal name of the 'M4' key.
	 */
	String M4_NAME = "M4"; //$NON-NLS-1$

	/**
	 * The formal name of the null (U+0000) key.
	 */
	String NUL_NAME = "NUL"; //$NON-NLS-1$

	/**
	 * The formal name of the 'NumLock' key.
	 */
	String NUM_LOCK_NAME = "NUM_LOCK"; //$NON-NLS-1$

	/**
	 * The formal name of the '0' key on the numpad.
	 */
	String NUMPAD_0_NAME = "NUMPAD_0"; //$NON-NLS-1$

	/**
	 * The formal name of the '1' key on the numpad.
	 */
	String NUMPAD_1_NAME = "NUMPAD_1"; //$NON-NLS-1$

	/**
	 * The formal name of the '2' key on the numpad.
	 */
	String NUMPAD_2_NAME = "NUMPAD_2"; //$NON-NLS-1$

	/**
	 * The formal name of the '3' key on the numpad.
	 */
	String NUMPAD_3_NAME = "NUMPAD_3"; //$NON-NLS-1$

	/**
	 * The formal name of the '4' key on the numpad.
	 */
	String NUMPAD_4_NAME = "NUMPAD_4"; //$NON-NLS-1$

	/**
	 * The formal name of the '5' key on the numpad.
	 */
	String NUMPAD_5_NAME = "NUMPAD_5"; //$NON-NLS-1$

	/**
	 * The formal name of the '6' key on the numpad.
	 */
	String NUMPAD_6_NAME = "NUMPAD_6"; //$NON-NLS-1$

	/**
	 * The formal name of the '7' key on the numpad.
	 */
	String NUMPAD_7_NAME = "NUMPAD_7"; //$NON-NLS-1$

	/**
	 * The formal name of the '8' key on the numpad.
	 */
	String NUMPAD_8_NAME = "NUMPAD_8"; //$NON-NLS-1$

	/**
	 * The formal name of the '9' key on the numpad.
	 */
	String NUMPAD_9_NAME = "NUMPAD_9"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Add' key on the numpad.
	 */
	String NUMPAD_ADD_NAME = "NUMPAD_ADD"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Decimal' key on the numpad.
	 */
	String NUMPAD_DECIMAL_NAME = "NUMPAD_DECIMAL"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Divide' key on the numpad.
	 */
	String NUMPAD_DIVIDE_NAME = "NUMPAD_DIVIDE"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Enter' key on the numpad.
	 */
	String NUMPAD_ENTER_NAME = "NUMPAD_ENTER"; //$NON-NLS-1$

	/**
	 * The formal name of the '=' key on the numpad.
	 */
	String NUMPAD_EQUAL_NAME = "NUMPAD_EQUAL"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Multiply' key on the numpad.
	 */
	String NUMPAD_MULTIPLY_NAME = "NUMPAD_MULTIPLY"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Subtract' key on the numpad.
	 */
	String NUMPAD_SUBTRACT_NAME = "NUMPAD_SUBTRACT"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Page Down' key.
	 */
	String PAGE_DOWN_NAME = "PAGE_DOWN"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Page Up' key.
	 */
	String PAGE_UP_NAME = "PAGE_UP"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Pause' key.
	 */
	String PAUSE_NAME = "PAUSE"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Print Screen' key.
	 */
	String PRINT_SCREEN_NAME = "PRINT_SCREEN"; //$NON-NLS-1$

	/**
	 * An alternative name for the enter key.
	 */
	String RETURN_NAME = "RETURN"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Scroll Lock' key.
	 */
	String SCROLL_LOCK_NAME = "SCROLL_LOCK"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Shift' key.
	 */
	String SHIFT_NAME = "SHIFT"; //$NON-NLS-1$

	/**
	 * The formal name of the space (U+0020) key.
	 */
	String SPACE_NAME = "SPACE"; //$NON-NLS-1$

	/**
	 * The formal name of the tab (U+0009) key.
	 */
	String TAB_NAME = "TAB"; //$NON-NLS-1$

	/**
	 * The formal name of the vertical tab (U+000B) key.
	 */
	String VT_NAME = "VT"; //$NON-NLS-1$

	/**
	 * Looks up a single natural key by its formal name, and returns the integer
	 * representation for this natural key
	 *
	 * @param name
	 *            The formal name of the natural key to look-up; must not be
	 *            <code>null</code>.
	 * @return The integer representation of this key. If the natural key cannot
	 *         be found, then this method returns <code>0</code>.
	 */
	int formalKeyLookup(String name);

	/**
	 * Looks up a single natural key by its formal name, and returns the integer
	 * representation for this natural key
	 *
	 * @param name
	 *            The formal name of the natural key to look-up; must not be
	 *            <code>null</code>.
	 * @return The integer representation of this key. If the natural key cannot
	 *         be found, then this method returns <code>0</code>.
	 */
	Integer formalKeyLookupInteger(String name);

	/**
	 * Looks up a single modifier key by its formal name, and returns the integer
	 * representation for this modifier key
	 *
	 * @param name
	 *            The formal name of the modifier key to look-up; must not be
	 *            <code>null</code>.
	 * @return The integer representation of this key. If the modifier key
	 *         cannot be found, then this method returns <code>0</code>.
	 */
	int formalModifierLookup(String name);

	/**
	 * Looks up a key value, and returns the formal string representation for
	 * that key
	 *
	 * @param key
	 *            The key to look-up.
	 * @return The formal string representation of this key. If this key cannot
	 *         be found, then it is simply the character corresponding to that
	 *         integer value.
	 */
	String formalNameLookup(int key);

	/**
	 * Returns the integer representation of the ALT key.
	 *
	 * @return The ALT key
	 */
	int getAlt();

	/**
	 * Returns the integer representation of the COMMAND key.
	 *
	 * @return The COMMAND key
	 */
	int getCommand();

	/**
	 * Returns the integer representation of the CTRL key.
	 *
	 * @return The CTRL key
	 */
	int getCtrl();

	/**
	 * Returns the integer representation of the SHIFT key.
	 *
	 * @return The SHIFT key
	 */
	int getShift();

	/**
	 * Returns whether the given key is a modifier key.
	 *
	 * @param key
	 *            The integer value of the key to check.
	 * @return <code>true</code> if the key is one of the modifier keys;
	 *         <code>false</code> otherwise.
	 */
	boolean isModifierKey(int key);
}
