/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test the lifecycle of an action delegate.
 */
@RunWith(JUnit4.class)
public class IActionBarsTest extends UITestCase {

	protected IWorkbenchWindow fWindow;

	protected IWorkbenchPage fPage;

	private static class MockAction extends Action {
		public boolean hasRun = false;

		public MockAction() {
			super();
		}

		@Override
		public void run() {
			hasRun = true;
		}
	}

	/**
	 * Constructor for IActionDelegateTest
	 */
	public IActionBarsTest() {
		super(IActionBarsTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}

	@Test
	public void testGetMenuManager() throws Throwable {
		// From Javadoc: "Returns the menu manager."

		IViewPart part = fPage.showView(MockViewPart.ID);
		IActionBars bars = part.getViewSite().getActionBars();
		assertNotNull(bars);
		IMenuManager mgr = bars.getMenuManager();
		assertNotNull(mgr);
	}

	@Test
	public void testGetStatusLineManager() throws Throwable {
		// From Javadoc: "Returns the status line manager."

		IViewPart part = fPage.showView(MockViewPart.ID);
		IActionBars bars = part.getViewSite().getActionBars();
		assertNotNull(bars);
		IStatusLineManager mgr = bars.getStatusLineManager();
		assertNotNull(mgr);
	}

	@Test
	public void testGetToolBarManager() throws Throwable {
		// From Javadoc: "Returns the tool bar manager."

		IViewPart part = fPage.showView(MockViewPart.ID);
		IActionBars bars = part.getViewSite().getActionBars();
		assertNotNull(bars);
		IToolBarManager mgr = bars.getToolBarManager();
		assertNotNull(mgr);
	}

	@Test
	public void testGetGlobalActionHandler() throws Throwable {
		// From Javadoc: "Returns the global action handler for
		// the action with the given id.

		IViewPart part = fPage.showView(MockViewPart.ID);
		IActionBars bars = part.getViewSite().getActionBars();
		assertNotNull(bars);

		// Get actions.  They should all be null.
		assertNull(bars.getGlobalActionHandler(IWorkbenchActionConstants.CUT));
		assertNull(bars.getGlobalActionHandler(IWorkbenchActionConstants.COPY));
		assertNull(bars.getGlobalActionHandler(IWorkbenchActionConstants.UNDO));

		// Create actions.
		MockAction cut = new MockAction();
		MockAction copy = new MockAction();
		MockAction undo = new MockAction();

		// Set actions.
		bars.setGlobalActionHandler(IWorkbenchActionConstants.CUT, cut);
		bars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copy);
		bars.setGlobalActionHandler(IWorkbenchActionConstants.UNDO, undo);
		bars.updateActionBars();

		// Get actions.  They should not be null.
		assertEquals(cut, bars
				.getGlobalActionHandler(IWorkbenchActionConstants.CUT));
		assertEquals(copy, bars
				.getGlobalActionHandler(IWorkbenchActionConstants.COPY));
		assertEquals(undo, bars
				.getGlobalActionHandler(IWorkbenchActionConstants.UNDO));
	}

		@Test
		public void testSetGlobalActionHandler() throws Throwable {
			// From Javadoc: "Returns the global action handler for
			// the action with the given id.

			IViewPart part = fPage.showView(MockViewPart.ID);
			IActionBars bars = part.getViewSite().getActionBars();
			assertNotNull(bars);

			// Create actions.
			MockAction cut = new MockAction();
			MockAction copy = new MockAction();
			MockAction undo = new MockAction();

			// Set actions.
			bars.setGlobalActionHandler(IWorkbenchActionConstants.CUT, cut);
			bars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copy);
			bars.setGlobalActionHandler(IWorkbenchActionConstants.UNDO, undo);
			bars.updateActionBars();

			// Run the real workbench actions.
			// Verify the actions are invoked.
			cut.hasRun = copy.hasRun = undo.hasRun = false;

			// anything that has been converted from a RetargetAction in
			// WorkbenchActionBuilder must be run as a command
			runMatchingCommand(fWindow, ActionFactory.CUT.getId());

			ActionUtil.runActionUsingPath(this, fWindow,
					IWorkbenchActionConstants.M_EDIT + '/'
							+ IWorkbenchActionConstants.UNDO);
			assertTrue(cut.hasRun);
			assertTrue(!copy.hasRun);
			assertTrue(undo.hasRun);

			// Now create a second view and run the actions again.
			// Our global actions should not be invoked.
			fPage.showView(MockViewPart.ID2);
			cut.hasRun = copy.hasRun = undo.hasRun = false;
			runMatchingCommand(fWindow, ActionFactory.CUT.getId());
			ActionUtil.runActionUsingPath(this, fWindow,
					IWorkbenchActionConstants.M_EDIT + '/'
							+ IWorkbenchActionConstants.UNDO);
			assertTrue(!cut.hasRun);
			assertTrue(!copy.hasRun);
			assertTrue(!undo.hasRun);

			// Reactivate test view and run actions again.
			// This time our global actions should be invoked.
			fPage.activate(part);
			cut.hasRun = copy.hasRun = undo.hasRun = false;
			runMatchingCommand(fWindow, ActionFactory.CUT.getId());
			ActionUtil.runActionUsingPath(this, fWindow,
					IWorkbenchActionConstants.M_EDIT + '/'
							+ IWorkbenchActionConstants.UNDO);
			assertTrue(cut.hasRun);
			assertTrue(!copy.hasRun);
			assertTrue(undo.hasRun);
		}

	private void runMatchingCommand(IWorkbenchWindow window, String actionId) {
		IHandlerService hs = window.getService(IHandlerService.class);
		IActionCommandMappingService ms = window.getService(IActionCommandMappingService.class);
		String commandId = ms.getCommandId(actionId);
		assertNotNull(commandId);
		try {
			hs.executeCommand(commandId, null);
		} catch (NotHandledException | NotEnabledException e) {
			// this is not a failure, just a condition to be checked by
			// the test
		} catch (Exception e) {
			fail("Failed to run " + commandId, e);
		}
	}
}

