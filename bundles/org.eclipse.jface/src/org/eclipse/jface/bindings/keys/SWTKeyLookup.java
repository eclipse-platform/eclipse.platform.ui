/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 444070
 *******************************************************************************/
package org.eclipse.jface.bindings.keys;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;

/**
 * <p>
 * A look-up table for the formal grammar for keys, and the integer values they
 * represent. This look-up table is hard-coded to use SWT representations. By
 * replacing this class (and
 * {@link org.eclipse.jface.bindings.keys.SWTKeySupport}), you can remove the
 * dependency on SWT.
 * </p>
 *
 * @since 3.1
 * @see org.eclipse.jface.bindings.keys.KeyLookupFactory
 */
public final class SWTKeyLookup implements IKeyLookup {

	/**
	 * The look-up table for modifier keys. This is a map of formal name (<code>String</code>)
	 * to integer value (<code>Integer</code>).
	 */
	private final Map<String, Integer> modifierKeyTable = new HashMap<>();

	/**
	 * The look-up table for formal names. This is a map of integer value (<code>Integer</code>)
	 * to formal name (<code>String</code>).
	 */
	private final Map<Integer, String> nameTable = new HashMap<>();

	/**
	 * The look-up table for natural keys. This is a map of formal name (<code>String</code>)
	 * to integer value (<code>Integer</code>).
	 */
	private final Map<String, Integer> naturalKeyTable = new HashMap<>();

	/**
	 * Constructs a new look-up class. This should only be done by the look-up
	 * factory.
	 *
	 * @see KeyLookupFactory
	 */
	SWTKeyLookup() {
		final Integer alt = Integer.valueOf(SWT.ALT);
		final Integer command = Integer.valueOf(SWT.COMMAND);
		final Integer ctrl = Integer.valueOf(SWT.CTRL);
		final Integer shift = Integer.valueOf(SWT.SHIFT);
		modifierKeyTable.put(ALT_NAME, alt);
		nameTable.put(alt, ALT_NAME);
		modifierKeyTable.put(COMMAND_NAME, command);
		nameTable.put(command, COMMAND_NAME);
		modifierKeyTable.put(CTRL_NAME, ctrl);
		nameTable.put(ctrl, CTRL_NAME);
		modifierKeyTable.put(SHIFT_NAME, shift);
		nameTable.put(shift, SHIFT_NAME);
		modifierKeyTable.put(M1_NAME,
				Util.isMac() ? command : ctrl);
		modifierKeyTable.put(M2_NAME, shift);
		modifierKeyTable.put(M3_NAME, alt);
		modifierKeyTable.put(M4_NAME, Util.isMac() ? ctrl
				: command);

		final Integer arrowDown = Integer.valueOf(SWT.ARROW_DOWN);
		naturalKeyTable.put(ARROW_DOWN_NAME, arrowDown);
		nameTable.put(arrowDown, ARROW_DOWN_NAME);
		final Integer arrowLeft = Integer.valueOf(SWT.ARROW_LEFT);
		naturalKeyTable.put(ARROW_LEFT_NAME, arrowLeft);
		nameTable.put(arrowLeft, ARROW_LEFT_NAME);
		final Integer arrowRight = Integer.valueOf(SWT.ARROW_RIGHT);
		naturalKeyTable.put(ARROW_RIGHT_NAME, arrowRight);
		nameTable.put(arrowRight, ARROW_RIGHT_NAME);
		final Integer arrowUp = Integer.valueOf(SWT.ARROW_UP);
		naturalKeyTable.put(ARROW_UP_NAME, arrowUp);
		nameTable.put(arrowUp, ARROW_UP_NAME);
		final Integer breakKey = Integer.valueOf(SWT.BREAK);
		naturalKeyTable.put(BREAK_NAME, breakKey);
		nameTable.put(breakKey, BREAK_NAME);
		final Integer bs = Integer.valueOf(SWT.BS);
		naturalKeyTable.put(BS_NAME, bs);
		nameTable.put(bs, BS_NAME);
		naturalKeyTable.put(BACKSPACE_NAME, bs);
		final Integer capsLock = Integer.valueOf(SWT.CAPS_LOCK);
		naturalKeyTable.put(CAPS_LOCK_NAME, capsLock);
		nameTable.put(capsLock, CAPS_LOCK_NAME);
		final Integer cr = Integer.valueOf(SWT.CR);
		naturalKeyTable.put(CR_NAME, cr);
		nameTable.put(cr, CR_NAME);
		naturalKeyTable.put(ENTER_NAME, cr);
		naturalKeyTable.put(RETURN_NAME, cr);
		final Integer del = Integer.valueOf(SWT.DEL);
		naturalKeyTable.put(DEL_NAME, del);
		nameTable.put(del, DEL_NAME);
		naturalKeyTable.put(DELETE_NAME, del);
		final Integer end = Integer.valueOf(SWT.END);
		naturalKeyTable.put(END_NAME, end);
		nameTable.put(end, END_NAME);
		final Integer esc = Integer.valueOf(SWT.ESC);
		naturalKeyTable.put(ESC_NAME, esc);
		nameTable.put(esc, ESC_NAME);
		naturalKeyTable.put(ESCAPE_NAME, esc);
		final Integer f1 = Integer.valueOf(SWT.F1);
		naturalKeyTable.put(F1_NAME, f1);
		nameTable.put(f1, F1_NAME);
		final Integer f2 = Integer.valueOf(SWT.F2);
		naturalKeyTable.put(F2_NAME, Integer.valueOf(SWT.F2));
		nameTable.put(f2, F2_NAME);
		final Integer f3 = Integer.valueOf(SWT.F3);
		naturalKeyTable.put(F3_NAME, Integer.valueOf(SWT.F3));
		nameTable.put(f3, F3_NAME);
		final Integer f4 = Integer.valueOf(SWT.F4);
		naturalKeyTable.put(F4_NAME, Integer.valueOf(SWT.F4));
		nameTable.put(f4, F4_NAME);
		final Integer f5 = Integer.valueOf(SWT.F5);
		naturalKeyTable.put(F5_NAME, Integer.valueOf(SWT.F5));
		nameTable.put(f5, F5_NAME);
		final Integer f6 = Integer.valueOf(SWT.F6);
		naturalKeyTable.put(F6_NAME, Integer.valueOf(SWT.F6));
		nameTable.put(f6, F6_NAME);
		final Integer f7 = Integer.valueOf(SWT.F7);
		naturalKeyTable.put(F7_NAME, Integer.valueOf(SWT.F7));
		nameTable.put(f7, F7_NAME);
		final Integer f8 = Integer.valueOf(SWT.F8);
		naturalKeyTable.put(F8_NAME, Integer.valueOf(SWT.F8));
		nameTable.put(f8, F8_NAME);
		final Integer f9 = Integer.valueOf(SWT.F9);
		naturalKeyTable.put(F9_NAME, Integer.valueOf(SWT.F9));
		nameTable.put(f9, F9_NAME);
		final Integer f10 = Integer.valueOf(SWT.F10);
		naturalKeyTable.put(F10_NAME, Integer.valueOf(SWT.F10));
		nameTable.put(f10, F10_NAME);
		final Integer f11 = Integer.valueOf(SWT.F11);
		naturalKeyTable.put(F11_NAME, Integer.valueOf(SWT.F11));
		nameTable.put(f11, F11_NAME);
		final Integer f12 = Integer.valueOf(SWT.F12);
		naturalKeyTable.put(F12_NAME, Integer.valueOf(SWT.F12));
		nameTable.put(f12, F12_NAME);
		final Integer f13 = Integer.valueOf(SWT.F13);
		naturalKeyTable.put(F13_NAME, Integer.valueOf(SWT.F13));
		nameTable.put(f13, F13_NAME);
		final Integer f14 = Integer.valueOf(SWT.F14);
		naturalKeyTable.put(F14_NAME, Integer.valueOf(SWT.F14));
		nameTable.put(f14, F14_NAME);
		final Integer f15 = Integer.valueOf(SWT.F15);
		naturalKeyTable.put(F15_NAME, Integer.valueOf(SWT.F15));
		nameTable.put(f15, F15_NAME);
		final Integer f16 = Integer.valueOf(SWT.F16);
		naturalKeyTable.put(F16_NAME, Integer.valueOf(SWT.F16));
		nameTable.put(f16, F16_NAME);
		final Integer f17 = Integer.valueOf(SWT.F17);
		naturalKeyTable.put(F17_NAME, Integer.valueOf(SWT.F17));
		nameTable.put(f17, F17_NAME);
		final Integer f18 = Integer.valueOf(SWT.F18);
		naturalKeyTable.put(F18_NAME, Integer.valueOf(SWT.F18));
		nameTable.put(f18, F18_NAME);
		final Integer f19 = Integer.valueOf(SWT.F19);
		naturalKeyTable.put(F19_NAME, Integer.valueOf(SWT.F19));
		nameTable.put(f19, F19_NAME);
		final Integer f20 = Integer.valueOf(SWT.F20);
		naturalKeyTable.put(F20_NAME, Integer.valueOf(SWT.F20));
		nameTable.put(f20, F20_NAME);
		final Integer ff = Integer.valueOf(12); // ASCII 0x0C
		naturalKeyTable.put(FF_NAME, ff);
		nameTable.put(ff, FF_NAME);
		final Integer home = Integer.valueOf(SWT.HOME);
		naturalKeyTable.put(HOME_NAME, home);
		nameTable.put(home, HOME_NAME);
		final Integer insert = Integer.valueOf(SWT.INSERT);
		naturalKeyTable.put(INSERT_NAME, insert);
		nameTable.put(insert, INSERT_NAME);
		final Integer lf = Integer.valueOf(SWT.LF);
		naturalKeyTable.put(LF_NAME, lf);
		nameTable.put(lf, LF_NAME);
		final Integer nul = Integer.valueOf(SWT.NULL);
		naturalKeyTable.put(NUL_NAME, nul);
		nameTable.put(nul, NUL_NAME);
		final Integer numLock = Integer.valueOf(SWT.NUM_LOCK);
		naturalKeyTable.put(NUM_LOCK_NAME, numLock);
		nameTable.put(numLock, NUM_LOCK_NAME);
		final Integer keypad0 = Integer.valueOf(SWT.KEYPAD_0);
		naturalKeyTable.put(NUMPAD_0_NAME, keypad0);
		nameTable.put(keypad0, NUMPAD_0_NAME);
		final Integer keypad1 = Integer.valueOf(SWT.KEYPAD_1);
		naturalKeyTable.put(NUMPAD_1_NAME, keypad1);
		nameTable.put(keypad1, NUMPAD_1_NAME);
		final Integer keypad2 = Integer.valueOf(SWT.KEYPAD_2);
		naturalKeyTable.put(NUMPAD_2_NAME, keypad2);
		nameTable.put(keypad2, NUMPAD_2_NAME);
		final Integer keypad3 = Integer.valueOf(SWT.KEYPAD_3);
		naturalKeyTable.put(NUMPAD_3_NAME, keypad3);
		nameTable.put(keypad3, NUMPAD_3_NAME);
		final Integer keypad4 = Integer.valueOf(SWT.KEYPAD_4);
		naturalKeyTable.put(NUMPAD_4_NAME, keypad4);
		nameTable.put(keypad4, NUMPAD_4_NAME);
		final Integer keypad5 = Integer.valueOf(SWT.KEYPAD_5);
		naturalKeyTable.put(NUMPAD_5_NAME, keypad5);
		nameTable.put(keypad5, NUMPAD_5_NAME);
		final Integer keypad6 = Integer.valueOf(SWT.KEYPAD_6);
		naturalKeyTable.put(NUMPAD_6_NAME, keypad6);
		nameTable.put(keypad6, NUMPAD_6_NAME);
		final Integer keypad7 = Integer.valueOf(SWT.KEYPAD_7);
		naturalKeyTable.put(NUMPAD_7_NAME, keypad7);
		nameTable.put(keypad7, NUMPAD_7_NAME);
		final Integer keypad8 = Integer.valueOf(SWT.KEYPAD_8);
		naturalKeyTable.put(NUMPAD_8_NAME, keypad8);
		nameTable.put(keypad8, NUMPAD_8_NAME);
		final Integer keypad9 = Integer.valueOf(SWT.KEYPAD_9);
		naturalKeyTable.put(NUMPAD_9_NAME, keypad9);
		nameTable.put(keypad9, NUMPAD_9_NAME);
		final Integer keypadAdd = Integer.valueOf(SWT.KEYPAD_ADD);
		naturalKeyTable.put(NUMPAD_ADD_NAME, keypadAdd);
		nameTable.put(keypadAdd, NUMPAD_ADD_NAME);
		final Integer keypadDecimal = Integer.valueOf(SWT.KEYPAD_DECIMAL);
		naturalKeyTable.put(NUMPAD_DECIMAL_NAME, keypadDecimal);
		nameTable.put(keypadDecimal, NUMPAD_DECIMAL_NAME);
		final Integer keypadDivide = Integer.valueOf(SWT.KEYPAD_DIVIDE);
		naturalKeyTable.put(NUMPAD_DIVIDE_NAME, keypadDivide);
		nameTable.put(keypadDivide, NUMPAD_DIVIDE_NAME);
		final Integer keypadCr = Integer.valueOf(SWT.KEYPAD_CR);
		naturalKeyTable.put(NUMPAD_ENTER_NAME, keypadCr);
		nameTable.put(keypadCr, NUMPAD_ENTER_NAME);
		final Integer keypadEqual = Integer.valueOf(SWT.KEYPAD_EQUAL);
		naturalKeyTable.put(NUMPAD_EQUAL_NAME, keypadEqual);
		nameTable.put(keypadEqual, NUMPAD_EQUAL_NAME);
		final Integer keypadMultiply = Integer.valueOf(SWT.KEYPAD_MULTIPLY);
		naturalKeyTable.put(NUMPAD_MULTIPLY_NAME, keypadMultiply);
		nameTable.put(keypadMultiply, NUMPAD_MULTIPLY_NAME);
		final Integer keypadSubtract = Integer.valueOf(SWT.KEYPAD_SUBTRACT);
		naturalKeyTable.put(NUMPAD_SUBTRACT_NAME, keypadSubtract);
		nameTable.put(keypadSubtract, NUMPAD_SUBTRACT_NAME);
		final Integer pageDown = Integer.valueOf(SWT.PAGE_DOWN);
		naturalKeyTable.put(PAGE_DOWN_NAME, pageDown);
		nameTable.put(pageDown, PAGE_DOWN_NAME);
		final Integer pageUp = Integer.valueOf(SWT.PAGE_UP);
		naturalKeyTable.put(PAGE_UP_NAME, pageUp);
		nameTable.put(pageUp, PAGE_UP_NAME);
		final Integer pause = Integer.valueOf(SWT.PAUSE);
		naturalKeyTable.put(PAUSE_NAME, pause);
		nameTable.put(pause, PAUSE_NAME);
		final Integer printScreen = Integer.valueOf(SWT.PRINT_SCREEN);
		naturalKeyTable.put(PRINT_SCREEN_NAME, printScreen);
		nameTable.put(printScreen, PRINT_SCREEN_NAME);
		final Integer scrollLock = Integer.valueOf(SWT.SCROLL_LOCK);
		naturalKeyTable.put(SCROLL_LOCK_NAME, scrollLock);
		nameTable.put(scrollLock, SCROLL_LOCK_NAME);
		final Integer space = Integer.valueOf(' ');
		naturalKeyTable.put(SPACE_NAME, space);
		nameTable.put(space, SPACE_NAME);
		final Integer tab = Integer.valueOf(SWT.TAB);
		naturalKeyTable.put(TAB_NAME, tab);
		nameTable.put(tab, TAB_NAME);
		final Integer vt = Integer.valueOf(11); // ASCII 0x0B
		naturalKeyTable.put(VT_NAME, vt);
		nameTable.put(vt, VT_NAME);
	}

	@Override
	public final int formalKeyLookup(final String name) {
		final Object value = naturalKeyTable.get(name);
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}

		if (name.length() > 0) {
			throw new IllegalArgumentException("Unrecognized formal key name: " //$NON-NLS-1$
					+ name);
		}

		return name.charAt(0);
	}

	@Override
	public final Integer formalKeyLookupInteger(final String name) {
		final Object value = naturalKeyTable.get(name);
		if (value instanceof Integer) {
			return (Integer) value;
		}

		return Integer.valueOf(name.charAt(0));
	}

	@Override
	public final int formalModifierLookup(final String name) {
		final Object value = modifierKeyTable.get(name);
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}

		return 0;
	}

	@Override
	public final String formalNameLookup(final int key) {
		final Integer keyObject = Integer.valueOf(key);
		final Object value = nameTable.get(keyObject);
		if (value instanceof String) {
			return (String) value;
		}

		return Util.ZERO_LENGTH_STRING + ((char) key);
	}

	@Override
	public final int getAlt() {
		return SWT.ALT;
	}

	@Override
	public final int getCommand() {
		return SWT.COMMAND;
	}

	@Override
	public final int getCtrl() {
		return SWT.CTRL;
	}

	@Override
	public final int getShift() {
		return SWT.SHIFT;
	}

	@Override
	public final boolean isModifierKey(final int key) {
		return ((key & SWT.MODIFIER_MASK) != 0);
	}
}
