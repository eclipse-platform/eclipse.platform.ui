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

import static org.junit.Assert.fail;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.workbench.texteditor.tests.ScreenshotTest;

public final class FindReplaceTestUtil {

	private FindReplaceTestUtil() {
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
			fail("The find/replace UI did not receive focus. Screenshot: " + screenshotPath);
		}
	}

}
