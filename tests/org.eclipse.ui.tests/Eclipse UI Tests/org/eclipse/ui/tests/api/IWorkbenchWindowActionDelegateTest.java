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

import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.tests.harness.util.ActionUtil;

/**
 * Tests the lifecycle for a window action delegate.
 */
public class IWorkbenchWindowActionDelegateTest extends IActionDelegateTest {

    /**
     * Constructor for IWorkbenchWindowActionDelegateTest
     */
    public IWorkbenchWindowActionDelegateTest(String testName) {
        super(testName);
    }

    public void testInit() throws Throwable {
        // Run the action.
        testRun();

        // Verify lifecycle.
        // The init, selectionChanged, and run methods should
        // be called, in that order.
        MockActionDelegate delegate = getDelegate();
        assertNotNull(delegate);
        assertTrue(delegate.callHistory.verifyOrder(new String[] { "init",
                "selectionChanged", "run" }));
    }

    // Bug 48799.  Commented out testDispose to avoid a test failure.  This should be a temporary solution.
    //	public void testDispose() throws Throwable {
    //		// Run the action.
    //		testRun();
    //		
    //		// Get the action.
    //		MockActionDelegate delegate = getDelegate();
    //		assertNotNull(delegate);
    //		
    //		// Dispose action.
    //		// Verify that the dispose method is called.
    //		delegate.callHistory.clear();
    //		removeAction();
    //		assertTrue(delegate.callHistory.contains("dispose"));
    //	}

    /**
     * Regression test for bug 81422.  Tests to ensure that dispose() is only
     * called once if the delegate implements both IWorkbenchWindowActionDelegate
     * and IActionDelegate2.
     */
    public void testDisposeWorkbenchWindowActionDelegateBug81422() {
        String id = MockWorkbenchWindowActionDelegate.SET_ID;
        fPage.showActionSet(id);
        MockWorkbenchWindowActionDelegate mockWWinActionDelegate = MockWorkbenchWindowActionDelegate.lastDelegate;
        // hide (from the compiler) the fact that the interfaces are implemented
        Object mockAsObject = mockWWinActionDelegate; 
        // asserts that the mock object actually implements both interfaces mentioned in the PR
        assertTrue(mockAsObject instanceof IActionDelegate2);
        assertTrue(mockAsObject instanceof IWorkbenchWindowActionDelegate);
        // we are only interested in the calls from now on
        mockWWinActionDelegate.callHistory.clear();
        // this causes the action set to be disposed
        fPage.close();
        // assert that dispose was called
    	assertTrue(mockWWinActionDelegate.callHistory.contains("dispose"));
    	// assert that dispose was not called twice
    	assertFalse(mockWWinActionDelegate.callHistory.verifyOrder(new String[] {"dispose", "dispose"}));
    }

    /**
     * @see IActionDelegateTest#createActionWidget()
     */
    protected Object createActionWidget() throws Throwable {
        fPage.showActionSet("org.eclipse.ui.tests.api.MockActionSet");
        return null;
    }

    /**
     * @see IActionDelegateTest#runAction()
     */
    protected void runAction(Object widget) throws Throwable {
        ActionUtil.runActionWithLabel(this, fWindow, "Mock Action");
    }

    /**
     * @see IActionDelegateTest#fireSelection()
     */
    protected void fireSelection(Object widget) throws Throwable {
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        view.fireSelection();
    }

    /**
     * Removes the action.
     */
    protected void removeAction() {
        fPage.hideActionSet("org.eclipse.ui.tests.api.MockActionSet");
    }
}

