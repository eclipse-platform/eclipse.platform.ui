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

package org.eclipse.jface.bindings.keys;

import java.util.SortedMap;
import java.util.TreeMap;

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
 * @since 3.1
 */
public class SpecialKey extends NaturalKey {

    /**
     * An internal map used to lookup instances of <code>SpecialKey</code>
     * given the formal string representation of a special key.
     */
    static final SortedMap specialKeysByName = new TreeMap();

    /**
     * The name of the 'Arrow Down' key.
     */
    private final static String ARROW_DOWN_NAME = "ARROW_DOWN"; //$NON-NLS-1$

    /**
     * The name of the 'Arrow Left' key.
     */
    private final static String ARROW_LEFT_NAME = "ARROW_LEFT"; //$NON-NLS-1$

    /**
     * The name of the 'Arrow Right' key.
     */
    private final static String ARROW_RIGHT_NAME = "ARROW_RIGHT"; //$NON-NLS-1$

    /**
     * The name of the 'Arrow Up' key.
     */
    private final static String ARROW_UP_NAME = "ARROW_UP"; //$NON-NLS-1$

    /**
     * The name for the 'Break' key.
     */
    private final static String BREAK_NAME = "BREAK"; //$NON-NLS-1$

    /**
     * The name for the 'Caps Lock' key.
     */
    private final static String CAPS_LOCK_NAME = "CAPS_LOCK"; //$NON-NLS-1$

    /**
     * The name of the 'End' key.
     */
    private final static String END_NAME = "END"; //$NON-NLS-1$

    /**
     * The name of the 'F1' key.
     */
    private final static String F1_NAME = "F1"; //$NON-NLS-1$

    /**
     * The name of the 'F10' key.
     */
    private final static String F10_NAME = "F10"; //$NON-NLS-1$

    /**
     * The name of the 'F11' key.
     */
    private final static String F11_NAME = "F11"; //$NON-NLS-1$

    /**
     * The name of the 'F12' key.
     */
    private final static String F12_NAME = "F12"; //$NON-NLS-1$

    /**
     * The name of the 'F13' key.
     */
    private final static String F13_NAME = "F13"; //$NON-NLS-1$

    /**
     * The name of the 'F14' key.
     */
    private final static String F14_NAME = "F14"; //$NON-NLS-1$

    /**
     * The name of the 'F15' key.
     */
    private final static String F15_NAME = "F15"; //$NON-NLS-1$

    /**
     * The name of the 'F2' key.
     */
    private final static String F2_NAME = "F2"; //$NON-NLS-1$

    /**
     * The name of the 'F3' key.
     */
    private final static String F3_NAME = "F3"; //$NON-NLS-1$

    /**
     * The name of the 'F4' key.
     */
    private final static String F4_NAME = "F4"; //$NON-NLS-1$

    /**
     * The name of the 'F5' key.
     */
    private final static String F5_NAME = "F5"; //$NON-NLS-1$

    /**
     * The name of the 'F6' key.
     */
    private final static String F6_NAME = "F6"; //$NON-NLS-1$

    /**
     * The name of the 'F7' key.
     */
    private final static String F7_NAME = "F7"; //$NON-NLS-1$

    /**
     * The name of the 'F8' key.
     */
    private final static String F8_NAME = "F8"; //$NON-NLS-1$

    /**
     * The name of the 'F9' key.
     */
    private final static String F9_NAME = "F9"; //$NON-NLS-1$

    /**
     * The name of the 'Home' key.
     */
    private final static String HOME_NAME = "HOME"; //$NON-NLS-1$

    /**
     * The name of the 'Insert' key.
     */
    private final static String INSERT_NAME = "INSERT"; //$NON-NLS-1$

    /**
     * The name of the 'NumLock' key.
     */
    private final static String NUM_LOCK_NAME = "NUM_LOCK"; //$NON-NLS-1$

    /**
     * The name of the '0' key on the numpad.
     */
    private final static String NUMPAD_0_NAME = "NUMPAD_0"; //$NON-NLS-1$

    /**
     * The name of the '1' key on the numpad.
     */
    private final static String NUMPAD_1_NAME = "NUMPAD_1"; //$NON-NLS-1$

    /**
     * The name of the '2' key on the numpad.
     */
    private final static String NUMPAD_2_NAME = "NUMPAD_2"; //$NON-NLS-1$

    /**
     * The name of the '3' key on the numpad.
     */
    private final static String NUMPAD_3_NAME = "NUMPAD_3"; //$NON-NLS-1$

    /**
     * The name of the '4' key on the numpad.
     */
    private final static String NUMPAD_4_NAME = "NUMPAD_4"; //$NON-NLS-1$

    /**
     * The name of the '5' key on the numpad.
     */
    private final static String NUMPAD_5_NAME = "NUMPAD_5"; //$NON-NLS-1$

    /**
     * The name of the '6' key on the numpad.
     */
    private final static String NUMPAD_6_NAME = "NUMPAD_6"; //$NON-NLS-1$

    /**
     * The name of the '7' key on the numpad.
     */
    private final static String NUMPAD_7_NAME = "NUMPAD_7"; //$NON-NLS-1$

    /**
     * The name of the '8' key on the numpad.
     */
    private final static String NUMPAD_8_NAME = "NUMPAD_8"; //$NON-NLS-1$

    /**
     * The name of the '9' key on the numpad.
     */
    private final static String NUMPAD_9_NAME = "NUMPAD_9"; //$NON-NLS-1$

    /**
     * The name of the 'Add' key on the numpad.
     */
    private final static String NUMPAD_ADD_NAME = "NUMPAD_ADD"; //$NON-NLS-1$

    /**
     * The name of the 'Decimal' key on the numpad.
     */
    private final static String NUMPAD_DECIMAL_NAME = "NUMPAD_DECIMAL"; //$NON-NLS-1$

    /**
     * The name of the 'Divide' key on the numpad.
     */
    private final static String NUMPAD_DIVIDE_NAME = "NUMPAD_DIVIDE"; //$NON-NLS-1$

    /**
     * The name of the 'Enter' key on the numpad.
     */
    private final static String NUMPAD_ENTER_NAME = "NUMPAD_ENTER"; //$NON-NLS-1$

    /**
     * The name of the '=' key on the numpad.
     */
    private final static String NUMPAD_EQUAL_NAME = "NUMPAD_EQUAL"; //$NON-NLS-1$

    /**
     * The name of the 'Multiply' key on the numpad.
     */
    private final static String NUMPAD_MULTIPLY_NAME = "NUMPAD_MULTIPLY"; //$NON-NLS-1$

    /**
     * The name of the 'Subtract' key on the numpad.
     */
    private final static String NUMPAD_SUBTRACT_NAME = "NUMPAD_SUBTRACT"; //$NON-NLS-1$

    /**
     * The name of the 'Page Down' key.
     */
    private final static String PAGE_DOWN_NAME = "PAGE_DOWN"; //$NON-NLS-1$

    /**
     * The name of the 'Page Up' key.
     */
    private final static String PAGE_UP_NAME = "PAGE_UP"; //$NON-NLS-1$

    /**
     * The name for the 'Pause' key.
     */
    private final static String PAUSE_NAME = "PAUSE"; //$NON-NLS-1$

    /**
     * The name for the 'Print Screen' key.
     */
    private final static String PRINT_SCREEN_NAME = "PRINT_SCREEN"; //$NON-NLS-1$

    /**
     * The name for the 'Scroll Lock' key.
     */
    private final static String SCROLL_LOCK_NAME = "SCROLL_LOCK"; //$NON-NLS-1$		

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Arrow Down' key.
     */
    public final static SpecialKey ARROW_DOWN = new SpecialKey(ARROW_DOWN_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Arrow Left' key.
     */
    public final static SpecialKey ARROW_LEFT = new SpecialKey(ARROW_LEFT_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Arrow Right' key.
     */
    public final static SpecialKey ARROW_RIGHT = new SpecialKey(
            ARROW_RIGHT_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Arrow Up' key.
     */
    public final static SpecialKey ARROW_UP = new SpecialKey(ARROW_UP_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Break' key.
     */
    public final static SpecialKey BREAK = new SpecialKey(BREAK_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Caps Lock' key.
     */
    public final static SpecialKey CAPS_LOCK = new SpecialKey(CAPS_LOCK_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'End' key.
     */
    public final static SpecialKey END = new SpecialKey(END_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F1' key.
     */
    public final static SpecialKey F1 = new SpecialKey(F1_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F10' key.
     */
    public final static SpecialKey F10 = new SpecialKey(F10_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F11' key.
     */
    public final static SpecialKey F11 = new SpecialKey(F11_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F12' key.
     */
    public final static SpecialKey F12 = new SpecialKey(F12_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F13' key.
     */
    public final static SpecialKey F13 = new SpecialKey(F13_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F14' key.
     */
    public final static SpecialKey F14 = new SpecialKey(F14_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F15' key.
     */
    public final static SpecialKey F15 = new SpecialKey(F15_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F2' key.
     */
    public final static SpecialKey F2 = new SpecialKey(F2_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F3' key.
     */
    public final static SpecialKey F3 = new SpecialKey(F3_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F4' key.
     */
    public final static SpecialKey F4 = new SpecialKey(F4_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F5' key.
     */
    public final static SpecialKey F5 = new SpecialKey(F5_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F6' key.
     */
    public final static SpecialKey F6 = new SpecialKey(F6_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F7' key.
     */
    public final static SpecialKey F7 = new SpecialKey(F7_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F8' key.
     */
    public final static SpecialKey F8 = new SpecialKey(F8_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'F9' key.
     */
    public final static SpecialKey F9 = new SpecialKey(F9_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Home' key.
     */
    public final static SpecialKey HOME = new SpecialKey(HOME_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Insert' key.
     */
    public final static SpecialKey INSERT = new SpecialKey(INSERT_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'NumLock' key.
     */
    public final static SpecialKey NUM_LOCK = new SpecialKey(NUM_LOCK_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '0' key on the numpad.
     */
    public final static SpecialKey NUMPAD_0 = new SpecialKey(NUMPAD_0_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '1' key on the numpad.
     */
    public final static SpecialKey NUMPAD_1 = new SpecialKey(NUMPAD_1_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '2' key on the numpad.
     */
    public final static SpecialKey NUMPAD_2 = new SpecialKey(NUMPAD_2_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '3' key on the numpad.
     */
    public final static SpecialKey NUMPAD_3 = new SpecialKey(NUMPAD_3_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '4' key on the numpad.
     */
    public final static SpecialKey NUMPAD_4 = new SpecialKey(NUMPAD_4_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '5' key on the numpad.
     */
    public final static SpecialKey NUMPAD_5 = new SpecialKey(NUMPAD_5_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '6' key on the numpad.
     */
    public final static SpecialKey NUMPAD_6 = new SpecialKey(NUMPAD_6_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '7' key on the numpad.
     */
    public final static SpecialKey NUMPAD_7 = new SpecialKey(NUMPAD_7_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '8' key on the numpad.
     */
    public final static SpecialKey NUMPAD_8 = new SpecialKey(NUMPAD_8_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '9' key on the numpad.
     */
    public final static SpecialKey NUMPAD_9 = new SpecialKey(NUMPAD_9_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Add' key on the numpad.
     */
    public final static SpecialKey NUMPAD_ADD = new SpecialKey(NUMPAD_ADD_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Decimal' key on the numpad.
     */
    public final static SpecialKey NUMPAD_DECIMAL = new SpecialKey(
            NUMPAD_DECIMAL_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Divide' key on the numpad.
     */
    public final static SpecialKey NUMPAD_DIVIDE = new SpecialKey(
            NUMPAD_DIVIDE_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Enter' key on the numpad.
     */
    public final static SpecialKey NUMPAD_ENTER = new SpecialKey(
            NUMPAD_ENTER_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the '=' key on the numpad.
     */
    public final static SpecialKey NUMPAD_EQUAL = new SpecialKey(
            NUMPAD_EQUAL_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Multiply' key on the numpad.
     */
    public final static SpecialKey NUMPAD_MULTIPLY = new SpecialKey(
            NUMPAD_MULTIPLY_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Subtract' key on the numpad.
     */
    public final static SpecialKey NUMPAD_SUBTRACT = new SpecialKey(
            NUMPAD_SUBTRACT_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Page Down' key.
     */
    public final static SpecialKey PAGE_DOWN = new SpecialKey(PAGE_DOWN_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Page Up' key.
     */
    public final static SpecialKey PAGE_UP = new SpecialKey(PAGE_UP_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Pause' key.
     */
    public final static SpecialKey PAUSE = new SpecialKey(PAUSE_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Print Screen' key.
     */
    public final static SpecialKey PRINT_SCREEN = new SpecialKey(
            PRINT_SCREEN_NAME);

    /**
     * The single static instance of <code>SpecialKey</code> which represents
     * the 'Scroll Lock' key.
     */
    public final static SpecialKey SCROLL_LOCK = new SpecialKey(
            SCROLL_LOCK_NAME);

    static {
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
     * @param name
     *            The name of the key, must not be null.
     */
    protected SpecialKey(final String name) {
        super(name);
    }
}
