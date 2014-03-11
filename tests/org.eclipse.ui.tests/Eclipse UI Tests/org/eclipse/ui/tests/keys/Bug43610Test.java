/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.keys;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.tests.harness.util.AutomationUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test for Bug 43610.
 * 
 * @since 3.0
 */
public class Bug43610Test extends UITestCase {

	/**
	 * Constructs a new instance of this test case.
	 * 
	 * @param testName
	 *            The name of the test
	 */
	public Bug43610Test(String testName) {
		super(testName);
	}

	/**
	 * Tests that if "Shift+Alt+" is pressed, then the key code should
	 * represent the "Alt+" key press.
	 */
	public void testShiftAlt() {
		// Close Welcome: workaround for https://bugs.eclipse.org/429592 / https://bugs.eclipse.org/366608#c12
		IIntroManager introManager= PlatformUI.getWorkbench().getIntroManager();
		introManager.closeIntro(introManager.getIntro());

		// Set up a working environment.
		Display display = Display.getCurrent();
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				if (event.stateMask == SWT.SHIFT) {
					assertEquals(
							"Incorrect key code for 'Shift+Alt+'", SWT.ALT, event.keyCode); //$NON-NLS-1$
				}
			}
		};
		display.addFilter(SWT.KeyDown, listener);

		try {
			AutomationUtil.performKeyCodeEvent(display, SWT.KeyDown, SWT.SHIFT);
			AutomationUtil.performKeyCodeEvent(display, SWT.KeyDown, SWT.ALT);
			AutomationUtil.performKeyCodeEvent(display, SWT.KeyUp, SWT.ALT);
			AutomationUtil.performKeyCodeEvent(display, SWT.KeyUp, SWT.SHIFT);
			AutomationUtil.performKeyCodeEvent(display, SWT.KeyDown, SWT.ESC);
			AutomationUtil.performKeyCodeEvent(display, SWT.KeyUp, SWT.ESC);

			while (display.readAndDispatch())
				;

		} finally {
			// Clean up the working environment.
			display.removeFilter(SWT.KeyDown, listener);
		}
	}
}
