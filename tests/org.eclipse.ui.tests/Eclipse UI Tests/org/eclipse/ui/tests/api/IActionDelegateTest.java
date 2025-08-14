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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test the lifecycle of an action delegate.
 */
public abstract class IActionDelegateTest {

	protected IWorkbenchWindow fWindow;

	protected IWorkbenchPage fPage;

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Before
	public void doSetUp() throws Exception {
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}

	@Test
	public void testRun() throws Throwable {
		// Create the action.
		Object obj = createActionWidget();

		// Run the action delegate.
		// The selectionChanged and run methods should be called, in that order.
		runAction(obj);
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		String[] testNames = new String[] { "selectionChanged", "run" };
		assertEquals(Arrays.toString(testNames),
				Arrays.toString(delegate.callHistory.verifyAndReturnOrder(testNames)));
	}

	@Test
	public void testSelectionChanged() throws Throwable {
		// Create the delegate by running it.
		Object obj = createActionWidget();
		runAction(obj);
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);

		// Fire a selection.
		// The selectionChanged method should be invoked.
		delegate.callHistory.clear();
		fireSelection(obj);
		assertTrue(delegate.callHistory.contains("selectionChanged"));
	}

	/**
	 * Returns the last mock action delegate which was created.
	 */
	protected MockActionDelegate getDelegate() throws Throwable {
		MockActionDelegate delegate = MockActionDelegate.lastMockActionDelegate;
		assertNotNull(delegate);
		return delegate;
	}

	/**
	 * Creates the widget for an action, and adds the action.
	 *
	 * @return an object which will be passed to runAction and
	 * fireSelection.
	 */
	protected abstract Object createActionWidget() throws Throwable;

	/**
	 * Adds and runs the action delegate.  Subclasses should override.
	 *
	 * @param widget the object returned from createActionWidget.
	 */
	protected abstract void runAction(Object widget) throws Throwable;

	/**
	 * Fires a selection from the source.  Subclasses should override.
	 *
	 * @param widget the object returned from createActionWidget.
	 */
	protected abstract void fireSelection(Object widget) throws Throwable;
}

