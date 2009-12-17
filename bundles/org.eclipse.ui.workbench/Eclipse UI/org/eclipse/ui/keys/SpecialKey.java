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

package org.eclipse.ui.keys;

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;

/**
 * <p>
 * Instances of <code>SpecialKey</code> represent the keys on keyboard
 * recognized as neither modifier keys nor character keys. These are special
 * control keys specific to computers (e.g., "left arrow", "page down", "F10",
 * etc.). They do not include keys representing letters, numbers or punctuation
 * from a natural language, nor do they include any key that can be represented
 * by a Unicode character (e.g., "backspace").
 * </p>
 * <p>
 * <code>SpecialKey</code> objects are immutable. Clients are not permitted to
 * extend this class.
 * </p>
 * 
 * @deprecated Please use org.eclipse.jface.bindings.keys.KeyStroke and
 *             org.eclipse.jface.bindings.keys.KeyLookupFactory
 * @since 3.0
 */
public final class SpecialKey extends NaturalKey {

	/**
	 * An internal map used to lookup instances of <code>SpecialKey</code>
	 * given the formal string representation of a special key.
	 */
	static SortedMap specialKeysByName = new TreeMap();

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Arrow Down' key.
	 */
	public final static SpecialKey ARROW_DOWN;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Arrow Left' key.
	 */
	public final static SpecialKey ARROW_LEFT;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Arrow Right' key.
	 */
	public final static SpecialKey ARROW_RIGHT;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Arrow Up' key.
	 */
	public final static SpecialKey ARROW_UP;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Break' key.
	 */
	public final static SpecialKey BREAK;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Caps Lock' key.
	 */
	public final static SpecialKey CAPS_LOCK;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'End' key.
	 */
	public final static SpecialKey END;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F1' key.
	 */
	public final static SpecialKey F1;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F10' key.
	 */
	public final static SpecialKey F10;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F11' key.
	 */
	public final static SpecialKey F11;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F12' key.
	 */
	public final static SpecialKey F12;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F13' key.
	 */
	public final static SpecialKey F13;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F14' key.
	 */
	public final static SpecialKey F14;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F15' key.
	 */
	public final static SpecialKey F15;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F2' key.
	 */
	public final static SpecialKey F2;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F3' key.
	 */
	public final static SpecialKey F3;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F4' key.
	 */
	public final static SpecialKey F4;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F5' key.
	 */
	public final static SpecialKey F5;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F6' key.
	 */
	public final static SpecialKey F6;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F7' key.
	 */
	public final static SpecialKey F7;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F8' key.
	 */
	public final static SpecialKey F8;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'F9' key.
	 */
	public final static SpecialKey F9;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Home' key.
	 */
	public final static SpecialKey HOME;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Insert' key.
	 */
	public final static SpecialKey INSERT;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'NumLock' key.
	 */
	public final static SpecialKey NUM_LOCK;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '0' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_0;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '1' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_1;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '2' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_2;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '3' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_3;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '4' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_4;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '5' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_5;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '6' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_6;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '7' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_7;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '8' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_8;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '9' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_9;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Add' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_ADD;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Decimal' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_DECIMAL;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Divide' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_DIVIDE;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Enter' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_ENTER;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the '=' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_EQUAL;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Multiply' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_MULTIPLY;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Subtract' key on the numpad.
	 */
	public final static SpecialKey NUMPAD_SUBTRACT;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Page Down' key.
	 */
	public final static SpecialKey PAGE_DOWN;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Page Up' key.
	 */
	public final static SpecialKey PAGE_UP;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Pause' key.
	 */
	public final static SpecialKey PAUSE;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Print Screen' key.
	 */
	public final static SpecialKey PRINT_SCREEN;

	/**
	 * The single static instance of <code>SpecialKey</code> which represents
	 * the 'Scroll Lock' key.
	 */
	public final static SpecialKey SCROLL_LOCK;

	static {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		ARROW_DOWN = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.ARROW_DOWN_NAME));
		ARROW_LEFT = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.ARROW_LEFT_NAME));
		ARROW_RIGHT = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.ARROW_RIGHT_NAME));
		ARROW_UP = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.ARROW_UP_NAME));
		BREAK = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.BREAK_NAME));
		CAPS_LOCK = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.CAPS_LOCK_NAME));
		END = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.END_NAME));
		F1 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F1_NAME));
		F2 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F2_NAME));
		F3 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F3_NAME));
		F4 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F4_NAME));
		F5 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F5_NAME));
		F6 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F6_NAME));
		F7 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F7_NAME));
		F8 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F8_NAME));
		F9 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F9_NAME));
		F10 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F10_NAME));
		F11 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F11_NAME));
		F12 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F12_NAME));
		F13 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F13_NAME));
		F14 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F14_NAME));
		F15 = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.F15_NAME));
		HOME = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.HOME_NAME));
		INSERT = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.INSERT_NAME));
		NUM_LOCK = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUM_LOCK_NAME));
		NUMPAD_0 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_0_NAME));
		NUMPAD_1 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_1_NAME));
		NUMPAD_2 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_2_NAME));
		NUMPAD_3 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_3_NAME));
		NUMPAD_4 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_4_NAME));
		NUMPAD_5 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_5_NAME));
		NUMPAD_6 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_6_NAME));
		NUMPAD_7 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_7_NAME));
		NUMPAD_8 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_8_NAME));
		NUMPAD_9 = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_9_NAME));
		NUMPAD_ADD = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_ADD_NAME));
		NUMPAD_DECIMAL = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_DECIMAL_NAME));
		NUMPAD_DIVIDE = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_DIVIDE_NAME));
		NUMPAD_ENTER = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_ENTER_NAME));
		NUMPAD_EQUAL = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_EQUAL_NAME));
		NUMPAD_MULTIPLY = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_MULTIPLY_NAME));
		NUMPAD_SUBTRACT = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.NUMPAD_SUBTRACT_NAME));
		PAGE_DOWN = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.PAGE_DOWN_NAME));
		PAGE_UP = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.PAGE_UP_NAME));
		PAUSE = new SpecialKey(lookup.formalKeyLookup(IKeyLookup.PAUSE_NAME));
		PRINT_SCREEN = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.PRINT_SCREEN_NAME));
		SCROLL_LOCK = new SpecialKey(lookup
				.formalKeyLookup(IKeyLookup.SCROLL_LOCK_NAME));

		specialKeysByName.put(SpecialKey.ARROW_DOWN.toString(),
				SpecialKey.ARROW_DOWN);
		specialKeysByName.put(SpecialKey.ARROW_LEFT.toString(),
				SpecialKey.ARROW_LEFT);
		specialKeysByName.put(SpecialKey.ARROW_RIGHT.toString(),
				SpecialKey.ARROW_RIGHT);
		specialKeysByName.put(SpecialKey.ARROW_UP.toString(),
				SpecialKey.ARROW_UP);
		specialKeysByName.put(SpecialKey.BREAK.toString(), SpecialKey.BREAK);
		specialKeysByName.put(SpecialKey.CAPS_LOCK.toString(),
				SpecialKey.CAPS_LOCK);
		specialKeysByName.put(SpecialKey.END.toString(), SpecialKey.END);
		specialKeysByName.put(SpecialKey.F1.toString(), SpecialKey.F1);
		specialKeysByName.put(SpecialKey.F10.toString(), SpecialKey.F10);
		specialKeysByName.put(SpecialKey.F11.toString(), SpecialKey.F11);
		specialKeysByName.put(SpecialKey.F12.toString(), SpecialKey.F12);
		specialKeysByName.put(SpecialKey.F13.toString(), SpecialKey.F13);
		specialKeysByName.put(SpecialKey.F14.toString(), SpecialKey.F14);
		specialKeysByName.put(SpecialKey.F15.toString(), SpecialKey.F15);
		specialKeysByName.put(SpecialKey.F2.toString(), SpecialKey.F2);
		specialKeysByName.put(SpecialKey.F3.toString(), SpecialKey.F3);
		specialKeysByName.put(SpecialKey.F4.toString(), SpecialKey.F4);
		specialKeysByName.put(SpecialKey.F5.toString(), SpecialKey.F5);
		specialKeysByName.put(SpecialKey.F6.toString(), SpecialKey.F6);
		specialKeysByName.put(SpecialKey.F7.toString(), SpecialKey.F7);
		specialKeysByName.put(SpecialKey.F8.toString(), SpecialKey.F8);
		specialKeysByName.put(SpecialKey.F9.toString(), SpecialKey.F9);
		specialKeysByName.put(SpecialKey.NUM_LOCK.toString(),
				SpecialKey.NUM_LOCK);
		specialKeysByName.put(SpecialKey.NUMPAD_0.toString(),
				SpecialKey.NUMPAD_0);
		specialKeysByName.put(SpecialKey.NUMPAD_1.toString(),
				SpecialKey.NUMPAD_1);
		specialKeysByName.put(SpecialKey.NUMPAD_2.toString(),
				SpecialKey.NUMPAD_2);
		specialKeysByName.put(SpecialKey.NUMPAD_3.toString(),
				SpecialKey.NUMPAD_3);
		specialKeysByName.put(SpecialKey.NUMPAD_4.toString(),
				SpecialKey.NUMPAD_4);
		specialKeysByName.put(SpecialKey.NUMPAD_5.toString(),
				SpecialKey.NUMPAD_5);
		specialKeysByName.put(SpecialKey.NUMPAD_6.toString(),
				SpecialKey.NUMPAD_6);
		specialKeysByName.put(SpecialKey.NUMPAD_7.toString(),
				SpecialKey.NUMPAD_7);
		specialKeysByName.put(SpecialKey.NUMPAD_8.toString(),
				SpecialKey.NUMPAD_8);
		specialKeysByName.put(SpecialKey.NUMPAD_9.toString(),
				SpecialKey.NUMPAD_9);
		specialKeysByName.put(SpecialKey.NUMPAD_ADD.toString(),
				SpecialKey.NUMPAD_ADD);
		specialKeysByName.put(SpecialKey.NUMPAD_DECIMAL.toString(),
				SpecialKey.NUMPAD_DECIMAL);
		specialKeysByName.put(SpecialKey.NUMPAD_DIVIDE.toString(),
				SpecialKey.NUMPAD_DIVIDE);
		specialKeysByName.put(SpecialKey.NUMPAD_ENTER.toString(),
				SpecialKey.NUMPAD_ENTER);
		specialKeysByName.put(SpecialKey.NUMPAD_EQUAL.toString(),
				SpecialKey.NUMPAD_EQUAL);
		specialKeysByName.put(SpecialKey.NUMPAD_MULTIPLY.toString(),
				SpecialKey.NUMPAD_MULTIPLY);
		specialKeysByName.put(SpecialKey.NUMPAD_SUBTRACT.toString(),
				SpecialKey.NUMPAD_SUBTRACT);
		specialKeysByName.put(SpecialKey.HOME.toString(), SpecialKey.HOME);
		specialKeysByName.put(SpecialKey.INSERT.toString(), SpecialKey.INSERT);
		specialKeysByName.put(SpecialKey.PAGE_DOWN.toString(),
				SpecialKey.PAGE_DOWN);
		specialKeysByName
				.put(SpecialKey.PAGE_UP.toString(), SpecialKey.PAGE_UP);
		specialKeysByName.put(SpecialKey.PAUSE.toString(), SpecialKey.PAUSE);
		specialKeysByName.put(SpecialKey.PRINT_SCREEN.toString(),
				SpecialKey.PRINT_SCREEN);
		specialKeysByName.put(SpecialKey.SCROLL_LOCK.toString(),
				SpecialKey.SCROLL_LOCK);
	}

	/**
	 * Constructs an instance of <code>SpecialKey</code> given a name.
	 * 
	 * @param key
	 *            The key to be wrapped.
	 */
	private SpecialKey(final int key) {
		super(key);
	}
}
