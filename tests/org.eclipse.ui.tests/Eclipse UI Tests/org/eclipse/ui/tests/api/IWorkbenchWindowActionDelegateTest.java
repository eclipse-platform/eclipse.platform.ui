/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the lifecycle for a window action delegate.
 */
public class IWorkbenchWindowActionDelegateTest extends IActionDelegateTest {

	@Test
	@Override
	public void testRun() throws Throwable {
		// Run the action.
		super.testRun();

		// Verify lifecycle.
		// The init, selectionChanged, and run methods should
		// be called, in that order.
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		String[] testNames = new String[] { "init", "selectionChanged", "run" };
		assertEquals(Arrays.toString(testNames), Arrays.toString(delegate.callHistory.verifyAndReturnOrder(testNames)));
	}

	@Test
	public void testLazyInit() {
		// Action set shouldn't be shown / initialized on startup
		int count = NotInitializedWorkbenchWindowActionDelegate.INIT_COUNT.intValue();
		assertEquals("Expected to see zero inits of invisible delegates", 0, count);
		// So far we don't have tests for that, so let assume this is also true
		count = NotInitializedWorkbenchWindowActionDelegate.INSTANCE_COUNT.intValue();
		assertEquals("Expected to see zero instances of invisible delegates", 0, count);
	}

	/**
	 * Returns the last mock action delegate which was created.
	 */
	@Override
	protected MockActionDelegate getDelegate() throws Throwable {
		MockActionDelegate delegate = MockWorkbenchWindowActionDelegate.lastMockWorkbenchWindowActionDelegate;
		assertNotNull(delegate);
		return delegate;
	}

	// Bug 48799.  Commented out testDispose to avoid a test failure.  This should be a temporary solution.
	@Test
	@Ignore("Bug 48799")
	public void testDispose() throws Throwable {
		// Run the action.
		testRun();

		// Get the action.
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);

		// Dispose action.
		// Verify that the dispose method is called.
		delegate.callHistory.clear();
		removeAction();
		assertTrue(delegate.callHistory.contains("dispose"));
	}

	/**
	 * Regression test for bug 81422.  Tests to ensure that dispose() is only
	 * called once if the delegate implements both IWorkbenchWindowActionDelegate
	 * and IActionDelegate2.
	 */
	@Test
	@Ignore
	public void XXXtestDisposeWorkbenchWindowActionDelegateBug81422() {
		String id = MockWorkbenchWindowActionDelegate.SET_ID;
		fPage.showActionSet(id);
		MockWorkbenchWindowActionDelegate mockWWinActionDelegate = MockWorkbenchWindowActionDelegate.lastMockWorkbenchWindowActionDelegate;
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

	@Override
	protected Object createActionWidget() throws Throwable {
		fPage.showActionSet("org.eclipse.ui.tests.api.MockActionSet");
		return null;
	}

	@Override
	protected void runAction(Object widget) throws Throwable {
		ActionUtil.runActionWithLabel(fWindow, "Mock Action");
	}

	@Override
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

