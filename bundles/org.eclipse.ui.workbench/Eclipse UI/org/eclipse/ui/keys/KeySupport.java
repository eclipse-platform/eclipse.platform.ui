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

package org.eclipse.ui.keys;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class KeySupport {

	/**
	 * JAVADOC
	 * 
	 * @param key
	 * @return
	 */
	public static KeyStroke convertFromSWT(int key) {
		SortedSet modifierKeys = new TreeSet();
		NaturalKey naturalKey = null;
		
		if ((key & SWT.ALT) != 0)
			modifierKeys.add(ModifierKey.ALT);

		if ((key & SWT.COMMAND) != 0)
			modifierKeys.add(ModifierKey.COMMAND);

		if ((key & SWT.CTRL) != 0)
			modifierKeys.add(ModifierKey.CTRL);

		if ((key & SWT.SHIFT) != 0)
			modifierKeys.add(ModifierKey.SHIFT);
		
		key &= SWT.KEY_MASK;
		
		switch (key) {
			case SWT.ARROW_DOWN:
				naturalKey = SpecialKey.ARROW_DOWN;
			case SWT.ARROW_LEFT:
				naturalKey = SpecialKey.ARROW_LEFT;
			case SWT.ARROW_RIGHT:
				naturalKey = SpecialKey.ARROW_RIGHT;
			case SWT.ARROW_UP:
				naturalKey = SpecialKey.ARROW_UP;
			case SWT.END:
				naturalKey = SpecialKey.END;
			case SWT.F1:
				naturalKey = SpecialKey.F1;
			case SWT.F10:
				naturalKey = SpecialKey.F10;
			case SWT.F11:
				naturalKey = SpecialKey.F11;
			case SWT.F12:
				naturalKey = SpecialKey.F12;
			case SWT.F2:
				naturalKey = SpecialKey.F2;
			case SWT.F3:
				naturalKey = SpecialKey.F3;				
			case SWT.F4:
				naturalKey = SpecialKey.F4;
			case SWT.F5:
				naturalKey = SpecialKey.F5;						
			case SWT.F6:
				naturalKey = SpecialKey.F6;
			case SWT.F7:
				naturalKey = SpecialKey.F7;
			case SWT.F8:
				naturalKey = SpecialKey.F8;
			case SWT.F9:
				naturalKey = SpecialKey.F9;		
			case SWT.HOME:
				naturalKey = SpecialKey.HOME;
			case SWT.INSERT:
				naturalKey = SpecialKey.INSERT;
			case SWT.PAGE_DOWN:
				naturalKey = SpecialKey.PAGE_DOWN;
			case SWT.PAGE_UP:
				naturalKey = SpecialKey.PAGE_UP;
			default:
				naturalKey = CharacterKey.getInstance((char) (key & 0xFFFF));		
		}

		return KeyStroke.getInstance(modifierKeys, naturalKey);
	}

	/**
	 * JAVADOC
	 * 
	 * @param keyStroke
	 * @return
	 */
	public static int convertToSWT(KeyStroke keyStroke) {
		if (keyStroke == null)
			throw new NullPointerException();
		
		int key = 0;
		// TODO
		return key;
	}

	private KeySupport() {
	}
}