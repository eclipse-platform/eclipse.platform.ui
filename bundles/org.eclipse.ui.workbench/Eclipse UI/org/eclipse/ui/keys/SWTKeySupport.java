/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.keys;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.keys.NativeKeyFormatter;

/**
 * A utility class for converting SWT events into key strokes.
 * 
 * @deprecated Please use org.eclipse.jface.bindings.keys.SWTKeySupport
 * @since 3.0
 */
public final class SWTKeySupport {

    /**
     * Given an SWT accelerator value, provide the corresponding key stroke.
     * 
     * @param accelerator
     *            The accelerator to convert; should be a valid SWT accelerator
     *            value.
     * @return The equivalent key stroke; never <code>null</code>.
     */
    public static KeyStroke convertAcceleratorToKeyStroke(int accelerator) {
        final SortedSet modifierKeys = new TreeSet();
        NaturalKey naturalKey = null;

        if ((accelerator & SWT.ALT) != 0) {
			modifierKeys.add(ModifierKey.ALT);
		}

        if ((accelerator & SWT.COMMAND) != 0) {
			modifierKeys.add(ModifierKey.COMMAND);
		}

        if ((accelerator & SWT.CTRL) != 0) {
			modifierKeys.add(ModifierKey.CTRL);
		}

        if ((accelerator & SWT.SHIFT) != 0) {
			modifierKeys.add(ModifierKey.SHIFT);
		}

        if (((accelerator & SWT.KEY_MASK) == 0) && (accelerator != 0)) {
            // There were only accelerators
            naturalKey = null;
        } else {
            // There were other keys.
            accelerator &= SWT.KEY_MASK;

            switch (accelerator) {
            case SWT.ARROW_DOWN:
                naturalKey = SpecialKey.ARROW_DOWN;
                break;
            case SWT.ARROW_LEFT:
                naturalKey = SpecialKey.ARROW_LEFT;
                break;
            case SWT.ARROW_RIGHT:
                naturalKey = SpecialKey.ARROW_RIGHT;
                break;
            case SWT.ARROW_UP:
                naturalKey = SpecialKey.ARROW_UP;
                break;
            case SWT.BREAK:
                naturalKey = SpecialKey.BREAK;
                break;
            case SWT.CAPS_LOCK:
                naturalKey = SpecialKey.CAPS_LOCK;
                break;
            case SWT.END:
                naturalKey = SpecialKey.END;
                break;
            case SWT.F1:
                naturalKey = SpecialKey.F1;
                break;
            case SWT.F10:
                naturalKey = SpecialKey.F10;
                break;
            case SWT.F11:
                naturalKey = SpecialKey.F11;
                break;
            case SWT.F12:
                naturalKey = SpecialKey.F12;
                break;
            case SWT.F2:
                naturalKey = SpecialKey.F2;
                break;
            case SWT.F3:
                naturalKey = SpecialKey.F3;
                break;
            case SWT.F4:
                naturalKey = SpecialKey.F4;
                break;
            case SWT.F5:
                naturalKey = SpecialKey.F5;
                break;
            case SWT.F6:
                naturalKey = SpecialKey.F6;
                break;
            case SWT.F7:
                naturalKey = SpecialKey.F7;
                break;
            case SWT.F8:
                naturalKey = SpecialKey.F8;
                break;
            case SWT.F9:
                naturalKey = SpecialKey.F9;
                break;
            case SWT.HOME:
                naturalKey = SpecialKey.HOME;
                break;
            case SWT.INSERT:
                naturalKey = SpecialKey.INSERT;
                break;
            case SWT.KEYPAD_0:
                naturalKey = SpecialKey.NUMPAD_0;
                break;
            case SWT.KEYPAD_1:
                naturalKey = SpecialKey.NUMPAD_1;
                break;
            case SWT.KEYPAD_2:
                naturalKey = SpecialKey.NUMPAD_2;
                break;
            case SWT.KEYPAD_3:
                naturalKey = SpecialKey.NUMPAD_3;
                break;
            case SWT.KEYPAD_4:
                naturalKey = SpecialKey.NUMPAD_4;
                break;
            case SWT.KEYPAD_5:
                naturalKey = SpecialKey.NUMPAD_5;
                break;
            case SWT.KEYPAD_6:
                naturalKey = SpecialKey.NUMPAD_6;
                break;
            case SWT.KEYPAD_7:
                naturalKey = SpecialKey.NUMPAD_7;
                break;
            case SWT.KEYPAD_8:
                naturalKey = SpecialKey.NUMPAD_8;
                break;
            case SWT.KEYPAD_9:
                naturalKey = SpecialKey.NUMPAD_9;
                break;
            case SWT.KEYPAD_ADD:
                naturalKey = SpecialKey.NUMPAD_ADD;
                break;
            case SWT.KEYPAD_CR:
                naturalKey = SpecialKey.NUMPAD_ENTER;
                break;
            case SWT.KEYPAD_DECIMAL:
                naturalKey = SpecialKey.NUMPAD_DECIMAL;
                break;
            case SWT.KEYPAD_DIVIDE:
                naturalKey = SpecialKey.NUMPAD_DIVIDE;
                break;
            case SWT.KEYPAD_EQUAL:
                naturalKey = SpecialKey.NUMPAD_EQUAL;
                break;
            case SWT.KEYPAD_MULTIPLY:
                naturalKey = SpecialKey.NUMPAD_MULTIPLY;
                break;
            case SWT.KEYPAD_SUBTRACT:
                naturalKey = SpecialKey.NUMPAD_SUBTRACT;
                break;
            case SWT.NUM_LOCK:
                naturalKey = SpecialKey.NUM_LOCK;
                break;
            case SWT.PAGE_DOWN:
                naturalKey = SpecialKey.PAGE_DOWN;
                break;
            case SWT.PAGE_UP:
                naturalKey = SpecialKey.PAGE_UP;
                break;
            case SWT.PAUSE:
                naturalKey = SpecialKey.PAUSE;
                break;
            case SWT.PRINT_SCREEN:
                naturalKey = SpecialKey.PRINT_SCREEN;
                break;
            case SWT.SCROLL_LOCK:
                naturalKey = SpecialKey.SCROLL_LOCK;
                break;
            default:
                naturalKey = CharacterKey
                        .getInstance((char) (accelerator & 0xFFFF));
            }
        }

        return KeyStroke.getInstance(modifierKeys, naturalKey);
    }

    /**
     * <p>
     * Converts the given event into an SWT accelerator value -- considering the
     * modified character with the shift modifier. This is the third accelerator
     * value that should be checked.
     * </p>
     * <p>
     * For example, on a standard US keyboard, "Ctrl+Shift+5" would be viewed as
     * "Ctrl+Shift+%".
     * </p>
     * 
     * @param event
     *            The event to be converted; must not be <code>null</code>.
     * @return The combination of the state mask and the unmodified character.
     */
    public static int convertEventToModifiedAccelerator(Event event) {
        int modifiers = event.stateMask & SWT.MODIFIER_MASK;
        char character = topKey(event);
        return modifiers + toUpperCase(character);
    }

    /**
     * <p>
     * Converts the given event into an SWT accelerator value -- considering the
     * unmodified character with all modifier keys. This is the first
     * accelerator value that should be checked. However, all alphabetic
     * characters are considered as their uppercase equivalents.
     * </p>
     * <p>
     * For example, on a standard US keyboard, "Ctrl+Shift+5" would be viewed as
     * "Ctrl+Shift+5".
     * </p>
     * 
     * @param event
     *            The event to be converted; must not be <code>null</code>.
     * @return The combination of the state mask and the unmodified character.
     */
    public static int convertEventToUnmodifiedAccelerator(Event event) {
        return convertEventToUnmodifiedAccelerator(event.stateMask,
                event.keyCode);
    }

    /**
     * <p>
     * Converts the given state mask and key code into an SWT accelerator value --
     * considering the unmodified character with all modifier keys. All
     * alphabetic characters are considered as their uppercase equivalents.
     * </p>
     * <p>
     * For example, on a standard US keyboard, "Ctrl+Shift+5" would be viewed as
     * "Ctrl+Shift+5".
     * </p>
     * 
     * @param stateMask
     *            The integer mask of modifiers keys depressed when this was
     *            pressed.
     * @param keyCode
     *            The key that was pressed, before being modified.
     * @return The combination of the state mask and the unmodified character.
     */
    private static int convertEventToUnmodifiedAccelerator(int stateMask,
            int keyCode) {
        int modifiers = stateMask & SWT.MODIFIER_MASK;
        int character = keyCode;
        return modifiers + toUpperCase(character);
    }

    /**
     * <p>
     * Converts the given event into an SWT accelerator value -- considering the
     * unmodified character with all modifier keys. This is the first
     * accelerator value that should be checked. However, all alphabetic
     * characters are considered as their uppercase equivalents.
     * </p>
     * <p>
     * For example, on a standard US keyboard, "Ctrl+Shift+5" would be viewed as
     * "Ctrl+%".
     * </p>
     * 
     * @param event
     *            The event to be converted; must not be <code>null</code>.
     * @return The combination of the state mask and the unmodified character.
     */
    public static int convertEventToUnmodifiedAccelerator(KeyEvent event) {
        return convertEventToUnmodifiedAccelerator(event.stateMask,
                event.keyCode);
    }

    /**
     * Converts the given event into an SWT accelerator value -- considering
     * the modified character without the shift modifier. This is the second
     * accelerator value that should be checked. Key strokes with alphabetic
     * natural keys are run through <code>convertEventToUnmodifiedAccelerator</code>
     * 
     * @param event
     *            The event to be converted; must not be <code>null</code>.
     * @return The combination of the state mask without shift, and the
     *         modified character.
     */
    public static int convertEventToUnshiftedModifiedAccelerator(Event event) {
        // Disregard alphabetic key strokes.
        if (Character.isLetter((char) event.keyCode)) {
            return convertEventToUnmodifiedAccelerator(event);
        }

        int modifiers = event.stateMask & (SWT.MODIFIER_MASK ^ SWT.SHIFT);
        char character = topKey(event);
        return modifiers + toUpperCase(character);
    }

    /**
     * Given a key stroke, this method provides the equivalent SWT accelerator
     * value. The functional inverse of <code>convertAcceleratorToKeyStroke</code>.
     * 
     * @param keyStroke
     *            The key stroke to convert; must not be <code>null</code>.
     * @return The SWT accelerator value
     */
    public static final int convertKeyStrokeToAccelerator(
            final KeyStroke keyStroke) {
        int accelerator = 0;
        final Iterator iterator = keyStroke.getModifierKeys().iterator();

        while (iterator.hasNext()) {
            final ModifierKey modifierKey = (ModifierKey) iterator.next();

            if (modifierKey == ModifierKey.ALT) {
				accelerator |= SWT.ALT;
			} else if (modifierKey == ModifierKey.COMMAND) {
				accelerator |= SWT.COMMAND;
			} else if (modifierKey == ModifierKey.CTRL) {
				accelerator |= SWT.CTRL;
			} else if (modifierKey == ModifierKey.SHIFT) {
				accelerator |= SWT.SHIFT;
			}
        }

        final NaturalKey naturalKey = keyStroke.getNaturalKey();

        if (naturalKey instanceof CharacterKey) {
			accelerator |= ((CharacterKey) naturalKey).getCharacter();
		} else if (naturalKey instanceof SpecialKey) {
            final SpecialKey specialKey = (SpecialKey) naturalKey;

            if (specialKey == SpecialKey.ARROW_DOWN) {
				accelerator |= SWT.ARROW_DOWN;
			} else if (specialKey == SpecialKey.ARROW_LEFT) {
				accelerator |= SWT.ARROW_LEFT;
			} else if (specialKey == SpecialKey.ARROW_RIGHT) {
				accelerator |= SWT.ARROW_RIGHT;
			} else if (specialKey == SpecialKey.ARROW_UP) {
				accelerator |= SWT.ARROW_UP;
			} else if (specialKey == SpecialKey.END) {
				accelerator |= SWT.END;
			} else if (specialKey == SpecialKey.F1) {
				accelerator |= SWT.F1;
			} else if (specialKey == SpecialKey.F10) {
				accelerator |= SWT.F10;
			} else if (specialKey == SpecialKey.F11) {
				accelerator |= SWT.F11;
			} else if (specialKey == SpecialKey.F12) {
				accelerator |= SWT.F12;
			} else if (specialKey == SpecialKey.F2) {
				accelerator |= SWT.F2;
			} else if (specialKey == SpecialKey.F3) {
				accelerator |= SWT.F3;
			} else if (specialKey == SpecialKey.F4) {
				accelerator |= SWT.F4;
			} else if (specialKey == SpecialKey.F5) {
				accelerator |= SWT.F5;
			} else if (specialKey == SpecialKey.F6) {
				accelerator |= SWT.F6;
			} else if (specialKey == SpecialKey.F7) {
				accelerator |= SWT.F7;
			} else if (specialKey == SpecialKey.F8) {
				accelerator |= SWT.F8;
			} else if (specialKey == SpecialKey.F9) {
				accelerator |= SWT.F9;
			} else if (specialKey == SpecialKey.HOME) {
				accelerator |= SWT.HOME;
			} else if (specialKey == SpecialKey.INSERT) {
				accelerator |= SWT.INSERT;
			} else if (specialKey == SpecialKey.PAGE_DOWN) {
				accelerator |= SWT.PAGE_DOWN;
			} else if (specialKey == SpecialKey.PAGE_UP) {
				accelerator |= SWT.PAGE_UP;
			}
        }

        return accelerator;
    }

    private static final IKeyFormatter NATIVE_FORMATTER = new NativeKeyFormatter();

    /**
     * Provides an instance of <code>IKeyFormatter</code> appropriate for the
     * current instance.
     * 
     * @return an instance of <code>IKeyFormatter</code> appropriate for the
     *         current instance; never <code>null</code>.
     */
    public static IKeyFormatter getKeyFormatterForPlatform() {
        return NATIVE_FORMATTER;
    }

    /**
     * Makes sure that a fully-modified character is converted to the normal
     * form. This means that "Ctrl+" key strokes must reverse the modification
     * caused by control-escaping. Also, all lower case letters are converted
     * to uppercase.
     * 
     * @param event
     *            The event from which the fully-modified character should be
     *            pulled.
     * @return The modified character, uppercase and without control-escaping.
     */
    private static char topKey(Event event) {
        char character = event.character;
        boolean ctrlDown = (event.stateMask & SWT.CTRL) != 0;

        if (ctrlDown && event.character != event.keyCode
                && event.character < 0x20) {
			character += 0x40;
		}

        return character;
    }

    /**
     * Makes the given character uppercase if it is a letter.
     * 
     * @param keyCode
     *            The character to convert.
     * @return The uppercase equivalent, if any; otherwise, the character
     *         itself.
     */
    private static int toUpperCase(int keyCode) {
        // Will this key code be truncated?
        if (keyCode > 0xFFFF) {
            return keyCode;
        }

        // Downcast in safety. Only make characters uppercase.
        char character = (char) keyCode;
        return Character.isLetter(character) ? Character.toUpperCase(character)
                : keyCode;
    }

    /**
     * This class should never be instantiated.
     */
    private SWTKeySupport() {
        // This class should never be instantiated.
    }
}
