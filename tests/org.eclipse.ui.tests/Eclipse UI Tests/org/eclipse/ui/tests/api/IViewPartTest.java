/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.tests.util.CallHistory;

/**
 * This is a test for IViewPart.  Since IViewPart is an
 * interface this test verifies the IViewPart lifecycle rather
 * than the implementation.
 */
public class IViewPartTest extends IWorkbenchPartTest {

    /**
     * Constructor for IEditorPartTest
     */
    public IViewPartTest(String testName) {
        super(testName);
    }

    /**
     * @see IWorkbenchPartTest#openPart(IWorkbenchPage)
     */
    protected MockPart openPart(IWorkbenchPage page) throws Throwable {
        return (MockWorkbenchPart) page.showView(MockViewPart.ID);
    }

    /**
     * @see IWorkbenchPartTest#closePart(IWorkbenchPage, MockWorkbenchPart)
     */
    protected void closePart(IWorkbenchPage page, MockPart part)
            throws Throwable {
        page.hideView((IViewPart) part);
    }
    
    /**
     * Tests that the view is closed without saving if isSaveOnCloseNeeded()
     * returns false.
     * @see ISaveable#isSaveOnCloseNeeded()
     */
    public void testOpenAndCloseSaveNotNeeded() throws Throwable {
        // Open a part.
        SaveableMockViewPart part = (SaveableMockViewPart) fPage.showView(SaveableMockViewPart.ID);
        part.setDirty(true);
        part.setSaveNeeded(false);
        closePart(fPage, part);
        
        CallHistory history = part.getCallHistory();
        assertTrue(history.verifyOrder(new String[] { "init",
                "createPartControl", "setFocus", "isSaveOnCloseNeeded", "dispose" }));
        assertFalse(history.contains("doSave"));
    }

}

