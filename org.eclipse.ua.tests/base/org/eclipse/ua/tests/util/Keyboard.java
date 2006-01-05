/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/*
 * Allows pressing of keys programmatically.
 */
public class Keyboard {

	/*
	 * Press and hold a key down until keyUp called.
	 */
	public static void keyDown(char c, int modifiers) {
		DisplayUtil.flush();
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = c;
		event.stateMask = modifiers;
		Display.getDefault().post(event);
		DisplayUtil.flush();
	}

	/*
	 * Press and hold a key down until keyUp called.
	 */
	public static void keyDown(int keyCode, int modifiers) {
		DisplayUtil.flush();
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = keyCode;
		event.stateMask = modifiers;
		Display.getDefault().post(event);
		DisplayUtil.flush();
	}
	
	/*
	 * Release a key that was previously called with keyDown.
	 */
	public static void keyUp(int keyCode, int modifiers) {
		DisplayUtil.flush();
		Event event = new Event();
		event.type = SWT.KeyUp;
		event.keyCode = keyCode;
		event.stateMask = modifiers;
		Display.getDefault().post(event);
		DisplayUtil.flush();
	}

	/*
	 * Release a key that was previously called with keyDown.
	 */
	public static void keyUp(char c, int modifiers) {
		DisplayUtil.flush();
		Event event = new Event();
		event.type = SWT.KeyUp;
		event.character = c;
		event.stateMask = modifiers;
		Display.getDefault().post(event);
		DisplayUtil.flush();
	}

	/*
	 * Press and release a key, programmatically.
	 */
	public static void press(int keyCode) {
		press(keyCode, SWT.NONE);
	}

	/*
	 * Press and release a key, programmatically.
	 */
	public static void press(char c) {
		press(c, SWT.NONE);
	}

	/*
	 * Press and release a key with the given modifiers (e.g. Alt, Ctrl).
	 */
	public static void press(int keyCode, int modifiers) {
		/*
		 * First press each modifier.
		 */
		int[] array = separateModifiers(modifiers);
		for (int i=0;i<array.length;++i) {
			keyDown(array[i], SWT.NONE);
		}

		/*
		 * Press and release the key.
		 */
		keyDown(keyCode, modifiers);
		keyUp(keyCode, modifiers);
		
		/*
		 * Release each modifier.
		 */
		for (int i=0;i<array.length;++i) {
			keyUp(array[i], SWT.NONE);
		}
	}

	/*
	 * Press and release a key with the given modifiers (e.g. Alt, Ctrl).
	 */
	public static void press(char c, int modifiers) {
		/*
		 * First press each modifier.
		 */
		int[] array = separateModifiers(modifiers);
		for (int i=0;i<array.length;++i) {
			keyDown(array[i], SWT.NONE);
		}

		/*
		 * Press and release the key.
		 */
		keyDown(c, modifiers);
		keyUp(c, modifiers);
		
		/*
		 * Release each modifier.
		 */
		for (int i=0;i<array.length;++i) {
			keyUp(array[i], SWT.NONE);
		}
	}
	
	/*
	 * Given the modifier flags, returns each modifier separately. For example, if you pass in
	 * (SWT.ALT | SWT.CTRL), it returns { SWT.ALT, SWT.CTRL }, in no particular order.
	 */
	private static int[] separateModifiers(int modifiers) {
		List list = new ArrayList();
		for (int i=0;i<32;++i) {
			int currentModifier = (1 << i);
			if ((modifiers & currentModifier) != 0) {
				list.add(new Integer(currentModifier));
			}
		}
		int[] array = new int[list.size()];
		for (int i=0;i<array.length;++i) {
			array[i] = ((Integer)list.get(i)).intValue();
		}
		return array;
	}
}
