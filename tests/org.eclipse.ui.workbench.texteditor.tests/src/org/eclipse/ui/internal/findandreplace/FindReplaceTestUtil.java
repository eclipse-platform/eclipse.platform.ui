/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
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
package org.eclipse.ui.internal.findandreplace;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.workbench.texteditor.tests.ScreenshotTest;

public final class FindReplaceTestUtil {

	private FindReplaceTestUtil() {
	}

	/**
	 * Carries out what is already pending and returns. Unlike {@link #runEventQueue()}
	 * it does not wait, so it covers what the code under test has scheduled by now but
	 * not what it schedules with a delay. Preferred where there is nothing to wait
	 * for, since waiting costs half a second every time.
	 */
	public static void processPendingEvents() {
		Display display= PlatformUI.getWorkbench().getDisplay();
		while (!display.isDisposed() && display.readAndDispatch()) {
			// do nothing
		}
	}

	public static void runEventQueue() {
		Display display= PlatformUI.getWorkbench().getDisplay();
		for (int i= 0; i < 10; i++) { // workaround for https://bugs.eclipse.org/323272
			while (display.readAndDispatch()) {
				// do nothing
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	/**
	 * Delivers a key stroke to the given control the way the workbench sees one,
	 * through the display filters the key binding dispatcher installs. Notifying the
	 * widget rather than posting a native event keeps this independent of the
	 * operating system, so it also works headless, and it is synchronous: the stroke
	 * has been carried out by the time this returns, and only what it triggers in turn
	 * may still be pending.
	 *
	 * @param target the control to deliver the stroke to
	 * @param stateMask the modifiers held down, {@link SWT#NONE} for none
	 * @param keyCode the key, either a character or one of the {@link SWT} key codes
	 */
	public static void notifyKeyDown(Control target, int stateMask, int keyCode) {
		Event keyEvent= new Event();
		keyEvent.stateMask= stateMask;
		keyEvent.keyCode= keyCode;
		keyEvent.character= characterFor(stateMask, keyCode);
		// The type and the widget are filled in by SWT while sending the event.
		target.notifyListeners(SWT.KeyDown, keyEvent);
	}

	/**
	 * The character a key stroke carries, which is not simply its key code. Keys that
	 * stand for no character, such as the arrow keys, have their own code range and
	 * carry none, whereas the Enter key of the keypad lives in that range and still
	 * carries a carriage return. Control, and only Control, turns a letter into a
	 * control character: on macOS {@link SWT#MOD1} is Command, which does not.
	 */
	private static char characterFor(int stateMask, int keyCode) {
		if (keyCode == SWT.CR || keyCode == SWT.KEYPAD_CR) {
			return '\r';
		}
		if ((keyCode & SWT.KEYCODE_BIT) != 0) {
			return '\0';
		}
		if ((stateMask & SWT.CTRL) != 0 && Character.isLetter(keyCode)) {
			return (char) (Character.toUpperCase(keyCode) - 64);
		}
		return (char) keyCode;
	}

	public static void waitForFocus(Supplier<Boolean> hasFocusValidator, String testName) {
		int focusAttempts= 0;
		while (!hasFocusValidator.get() && focusAttempts < 10) {
			focusAttempts++;
			PlatformUI.getWorkbench().getDisplay().readAndDispatch();
			if (!hasFocusValidator.get()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
		}
		if (!hasFocusValidator.get()) {
			String screenshotPath= ScreenshotTest.takeScreenshot(FindReplaceUITest.class, testName, System.out);
			Display display= PlatformUI.getWorkbench().getDisplay();
			// Where the focus ended up instead tells apart the usual causes: no focus
			// control at all means the window is not active, a control of the workbench
			// means something took the focus away, and an invisible widget tree means the
			// focus was never given away in the first place.
			fail("The find/replace UI did not receive focus. Focused control: " + display.getFocusControl()
					+ ", active shell: " + display.getActiveShell() + ". Screenshot: " + screenshotPath);
		}
	}

}