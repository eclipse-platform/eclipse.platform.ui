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

package org.eclipse.ui.tests.keys;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Test for Bug 43538.
 * 
 * @since 3.0
 */
public class Bug43538Test extends UITestCase {

    /**
     * Constructs a new instance of this test case.
     * 
     * @param testName
     *            The name of the test
     */
    public Bug43538Test(String testName) {
        super(testName);
    }

    /**
     * Tests that if "Shift+Alt+" is pressed, then the key code should
     * represent the "Alt+" key press.
     */
    public void testShiftAlt() throws AWTException {
        // Set up a working environment.
        Display display = Display.getCurrent();
        Listener listener = new Listener() {
            int count = 0;

            public void handleEvent(Event event) {
                if (event.stateMask == SWT.CTRL) {
                    assertEquals(
                            "Multiple key down events for 'Ctrl+Space'", 0, count++); //$NON-NLS-1$
                }
            }
        };
        display.addFilter(SWT.KeyDown, listener);

        // Test.
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.keyRelease(KeyEvent.VK_SPACE);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        while (display.readAndDispatch())
            ;

        // Clean up the working environment.
        display.removeFilter(SWT.KeyDown, listener);
    }
}