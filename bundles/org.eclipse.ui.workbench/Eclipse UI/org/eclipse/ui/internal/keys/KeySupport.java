/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.keys.CharacterKey;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ModifierKey;
import org.eclipse.ui.keys.NaturalKey;
import org.eclipse.ui.keys.SpecialKey;

public final class KeySupport {

	public static int convertEventToAccelerator(Event event) {
		int key = event.character;

		if (key == 0)
			key = event.keyCode;
		else {
			if (0 <= key && key <= 0x1F) {
				/* Some special packing of information might have occurred.  If
				 * CTRL is down, and the key code equals the character, then the
				 * character should be interpreted literally.  Otherwise, the
				 * character needs to be reinterpreted with respect to the CTRL
				 * mask (i.e., by adding 0x40 to the value).
				 */
				if ((event.stateMask & SWT.CTRL) != 0 && event.keyCode != event.character)
					key += 0x40;
			} else {
				if ('a' <= key && key <= 'z')
					key -= 'a' - 'A';
			}
		}

		int modifiers = event.stateMask & SWT.MODIFIER_MASK;
		return modifiers + key;
	}

	public static KeyStroke convertAcceleratorToKeyStroke(int accelerator) {
		final SortedSet modifierKeys = new TreeSet();
		NaturalKey naturalKey = null;

		if ((accelerator & SWT.ALT) != 0)
			modifierKeys.add(ModifierKey.ALT);

		if ((accelerator & SWT.COMMAND) != 0)
			modifierKeys.add(ModifierKey.COMMAND);

		if ((accelerator & SWT.CTRL) != 0)
			modifierKeys.add(ModifierKey.CTRL);

		if ((accelerator & SWT.SHIFT) != 0)
			modifierKeys.add(ModifierKey.SHIFT);

		accelerator &= SWT.KEY_MASK;

		switch (accelerator) {
			case SWT.ARROW_DOWN :
				naturalKey = SpecialKey.ARROW_DOWN;
				break;
			case SWT.ARROW_LEFT :
				naturalKey = SpecialKey.ARROW_LEFT;
				break;
			case SWT.ARROW_RIGHT :
				naturalKey = SpecialKey.ARROW_RIGHT;
				break;
			case SWT.ARROW_UP :
				naturalKey = SpecialKey.ARROW_UP;
				break;
			case SWT.END :
				naturalKey = SpecialKey.END;
				break;
			case SWT.F1 :
				naturalKey = SpecialKey.F1;
				break;
			case SWT.F10 :
				naturalKey = SpecialKey.F10;
				break;
			case SWT.F11 :
				naturalKey = SpecialKey.F11;
				break;
			case SWT.F12 :
				naturalKey = SpecialKey.F12;
				break;
			case SWT.F2 :
				naturalKey = SpecialKey.F2;
				break;
			case SWT.F3 :
				naturalKey = SpecialKey.F3;
				break;
			case SWT.F4 :
				naturalKey = SpecialKey.F4;
				break;
			case SWT.F5 :
				naturalKey = SpecialKey.F5;
				break;
			case SWT.F6 :
				naturalKey = SpecialKey.F6;
				break;
			case SWT.F7 :
				naturalKey = SpecialKey.F7;
				break;
			case SWT.F8 :
				naturalKey = SpecialKey.F8;
				break;
			case SWT.F9 :
				naturalKey = SpecialKey.F9;
				break;
			case SWT.HOME :
				naturalKey = SpecialKey.HOME;
				break;
			case SWT.INSERT :
				naturalKey = SpecialKey.INSERT;
				break;
			case SWT.PAGE_DOWN :
				naturalKey = SpecialKey.PAGE_DOWN;
				break;
			case SWT.PAGE_UP :
				naturalKey = SpecialKey.PAGE_UP;
				break;
			default :
				naturalKey = CharacterKey.getInstance((char) (accelerator & 0xFFFF));
		}

		return KeyStroke.getInstance(modifierKeys, naturalKey);
	}

	public static final int convertKeyStrokeToAccelerator(final KeyStroke keyStroke) {
		if (keyStroke == null)
			throw new NullPointerException();

		int accelerator = 0;
		final Iterator iterator = keyStroke.getModifierKeys().iterator();

		while (iterator.hasNext()) {
			final ModifierKey modifierKey = (ModifierKey) iterator.next();

			if (modifierKey == ModifierKey.ALT)
				accelerator |= SWT.ALT;
			else if (modifierKey == ModifierKey.COMMAND)
				accelerator |= SWT.COMMAND;
			else if (modifierKey == ModifierKey.CTRL)
				accelerator |= SWT.CTRL;
			else if (modifierKey == ModifierKey.SHIFT)
				accelerator |= SWT.SHIFT;
		}

		final NaturalKey naturalKey = keyStroke.getNaturalKey();

		if (naturalKey instanceof CharacterKey)
			accelerator |= ((CharacterKey) naturalKey).getCharacter();
		else if (naturalKey instanceof SpecialKey) {
			final SpecialKey specialKey = (SpecialKey) naturalKey;

			if (specialKey == SpecialKey.ARROW_DOWN)
				accelerator |= SWT.ARROW_DOWN;
			else if (specialKey == SpecialKey.ARROW_LEFT)
				accelerator |= SWT.ARROW_LEFT;
			else if (specialKey == SpecialKey.ARROW_RIGHT)
				accelerator |= SWT.ARROW_RIGHT;
			else if (specialKey == SpecialKey.ARROW_UP)
				accelerator |= SWT.ARROW_UP;
			else if (specialKey == SpecialKey.END)
				accelerator |= SWT.END;
			else if (specialKey == SpecialKey.F1)
				accelerator |= SWT.F1;
			else if (specialKey == SpecialKey.F10)
				accelerator |= SWT.F10;
			else if (specialKey == SpecialKey.F11)
				accelerator |= SWT.F11;
			else if (specialKey == SpecialKey.F12)
				accelerator |= SWT.F12;
			else if (specialKey == SpecialKey.F2)
				accelerator |= SWT.F2;
			else if (specialKey == SpecialKey.F3)
				accelerator |= SWT.F3;
			else if (specialKey == SpecialKey.F4)
				accelerator |= SWT.F4;
			else if (specialKey == SpecialKey.F5)
				accelerator |= SWT.F5;
			else if (specialKey == SpecialKey.F6)
				accelerator |= SWT.F6;
			else if (specialKey == SpecialKey.F7)
				accelerator |= SWT.F7;
			else if (specialKey == SpecialKey.F8)
				accelerator |= SWT.F8;
			else if (specialKey == SpecialKey.F9)
				accelerator |= SWT.F9;
			else if (specialKey == SpecialKey.HOME)
				accelerator |= SWT.HOME;
			else if (specialKey == SpecialKey.INSERT)
				accelerator |= SWT.INSERT;
			else if (specialKey == SpecialKey.PAGE_DOWN)
				accelerator |= SWT.PAGE_DOWN;
			else if (specialKey == SpecialKey.PAGE_UP)
				accelerator |= SWT.PAGE_UP;
		}

		return accelerator;
	}

	public static String formatCarbon(KeySequence keySequence) {
		StringBuffer stringBuffer = new StringBuffer();
		List keyStrokes = keySequence.getKeyStrokes();

		for (int i = 0; i < keyStrokes.size(); i++) {
			if (i >= 1)
				stringBuffer.append(' ');

			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			stringBuffer.append(formatCarbon(keyStroke));
		}

		return stringBuffer.toString();
	}

	public static String formatCarbon(KeyStroke keyStroke) {
		StringBuffer stringBuffer = new StringBuffer();
		Set modifierKeys = keyStroke.getModifierKeys();

		if (modifierKeys.contains(ModifierKey.SHIFT))
			stringBuffer.append('\u21E7');

		if (modifierKeys.contains(ModifierKey.CTRL))
			stringBuffer.append('\u2303');

		if (modifierKeys.contains(ModifierKey.ALT))
			stringBuffer.append('\u2325');

		if (modifierKeys.contains(ModifierKey.COMMAND))
			stringBuffer.append('\u2318');

		keyStroke = KeyStroke.getInstance(keyStroke.getNaturalKey());
		stringBuffer.append(keyStroke.format());
		return stringBuffer.toString();
	}

	private KeySupport() {
	}
}