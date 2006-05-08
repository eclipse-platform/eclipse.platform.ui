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

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.tests.harness.util.CallHistory;

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
     * returns false. This also tests some disposal behaviors specific to 
     * views: namely, that the contribution items are disposed in the correct
     * order with respect to the disposal of the view. 
     * 
     * @see ISaveablePart#isSaveOnCloseNeeded()
     */
    public void testOpenAndCloseSaveNotNeeded() throws Throwable {
        // Open a part.
        SaveableMockViewPart part = (SaveableMockViewPart) fPage.showView(SaveableMockViewPart.ID);
        part.setDirty(true);
        part.setSaveNeeded(false);
        closePart(fPage, part);
        
        CallHistory history = part.getCallHistory();
        
        // TODO: This verifies the 3.0 disposal order. However, there may be a bug here.
        // That is, it may be necessary to change this and dispose the contribution items 
        // after the view's dispose method in order to ensure that the site is never returning
        // a disposed contribution item. See bug 94457 for details.
        assertTrue(history.verifyOrder(new String[] { "setInitializationData", "init",
                "createPartControl", "setFocus", "isSaveOnCloseNeeded", 
                "widgetDisposed", "toolbarContributionItemWidgetDisposed", 
                "toolbarContributionItemDisposed", "dispose" }));
        assertFalse(history.contains("doSave"));
    }
    
}

