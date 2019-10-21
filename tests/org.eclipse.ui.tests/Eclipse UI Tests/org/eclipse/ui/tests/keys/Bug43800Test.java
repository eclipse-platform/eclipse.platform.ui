/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.keys;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.keys.SWTKeySupport;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test for Bug 43800.
 *
 * @since 3.0
 */
@RunWith(JUnit4.class)
public class Bug43800Test extends UITestCase {

	/**
	 * Constructs a new instance of this test case.
	 */
	public Bug43800Test() {
		super(Bug43800Test.class.getSimpleName());
	}

	/**
	 * Tests that key pressed with key codes greater than 16 bits are correctly
	 * converted into accelerator values.
	 */
	@Test
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
