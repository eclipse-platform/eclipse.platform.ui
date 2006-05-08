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

package org.eclipse.ui.tests.keys;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.keys.SWTKeySupport;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test for Bug 43800.
 * 
 * @since 3.0
 */
public class Bug43800Test extends UITestCase {

    /**
     * Constructs a new instance of this test case.
     * 
     * @param testName
     *           The name of the test
     */
    public Bug43800Test(String testName) {
        super(testName);
    }

    /**
     * Tests that key pressed with key codes greater than 16 bits are correctly
     * converted into accelerator values.
     */
    public void testTruncatingCast() {
        /*
         * Make an event representing a key stroke with a key code greater than
         * 16 bits.
         */
        Event event = new Event();
        event.keyCode = SWT.ARROW_LEFT;
        event.character = 0x00;
        event.stateMask = 0x00;

        // Convert the event, and test the resulting accelerator value.
        int accelerator = SWTKeySupport
                .convertEventToUnmodifiedAccelerator(event);
        assertEquals("Arrow_Left key truncated.", SWT.ARROW_LEFT, accelerator); //$NON-NLS-1$
    }
}
