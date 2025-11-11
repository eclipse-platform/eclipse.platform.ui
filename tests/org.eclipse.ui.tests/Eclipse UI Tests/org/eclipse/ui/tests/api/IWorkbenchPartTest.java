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
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.CallHistory;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * This is a test for IWorkbenchPart.  Since IWorkbenchPart is an
 * interface this test verifies the IWorkbenchPart lifecycle rather
 * than the implementation.
 */
@ExtendWith(CloseTestWindowsExtension.class)
public abstract class IWorkbenchPartTest {

	

	protected IWorkbenchWindow fWindow;

	protected IWorkbenchPage fPage;

	@BeforeEach
	public final void setUp() throws Exception {
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}

	@Test
	public void testOpenAndClose() throws Throwable {
		// Open a part.
		MockPart part = openPart(fPage);
		assertTrue(part.isSiteInitialized());
		CallHistory history = part.getCallHistory();
		assertTrue(history.verifyOrder(new String[] { "setInitializationData", "init",
				"createPartControl", "setFocus" }));

		// Close the part.
		closePart(fPage, part);
		assertTrue(history.verifyOrder(new String[] { "setInitializationData", "init",
				"createPartControl", "setFocus", "widgetDisposed", "dispose" }));
	}

	/**
	 * Opens a part.  Subclasses should override
	 */
	protected abstract MockPart openPart(IWorkbenchPage page) throws Throwable;

	/**
	 * Closes a part.  Subclasses should override
	 */
	protected abstract void closePart(IWorkbenchPage page, MockPart part)
			throws Throwable;
}

