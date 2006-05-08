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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.CallHistory;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * This is a test for IWorkbenchPart.  Since IWorkbenchPart is an
 * interface this test verifies the IWorkbenchPart lifecycle rather
 * than the implementation.
 */
public abstract class IWorkbenchPartTest extends UITestCase {

    protected IWorkbenchWindow fWindow;

    protected IWorkbenchPage fPage;

    /**
     * Constructor for IActionDelegateTest
     */
    public IWorkbenchPartTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fWindow = openTestWindow();
        fPage = fWindow.getActivePage();
    }

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

