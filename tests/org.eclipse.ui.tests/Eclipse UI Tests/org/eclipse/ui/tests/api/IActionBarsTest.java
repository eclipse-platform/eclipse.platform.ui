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

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
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
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test the lifecycle of an action delegate.
 */
public class IActionBarsTest {

	private IWorkbenchWindow fWindow;

	private IWorkbenchPage fPage;

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

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


	@Before
	public void doSetUp() throws Exception {
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
			MockAction quit = new MockAction();

			// Set actions.
			bars.setGlobalActionHandler(IWorkbenchActionConstants.CUT, cut);
			bars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copy);
			bars.setGlobalActionHandler(IWorkbenchActionConstants.UNDO, undo);
			bars.setGlobalActionHandler(IWorkbenchActionConstants.QUIT, quit);
			bars.updateActionBars();

			// Run the real workbench actions.
			// Verify the actions are invoked.
			cut.hasRun = copy.hasRun = undo.hasRun = false;

			// anything that has been converted from a RetargetAction in
			// WorkbenchActionBuilder must be run as a command
			runMatchingCommand(fWindow, ActionFactory.CUT.getId());
			runMatchingCommand(fWindow, ActionFactory.UNDO.getId());

			ActionUtil.runActionUsingPath(fWindow,
					IWorkbenchActionConstants.M_FILE + '/' + IWorkbenchActionConstants.QUIT);
			assertTrue(cut.hasRun);
			assertTrue(!copy.hasRun);
			assertTrue(undo.hasRun);
			assertTrue(quit.hasRun);

			// Now create a second view and run the actions again.
			// Our global actions should not be invoked.
			IViewPart part2 = fPage.showView(MockViewPart.ID2);

			// Set again a different MockAction to prevent the actual closing of the IDE
			// This is necessary as most of available the RetargetActions are converted
			part2.getViewSite().getActionBars().setGlobalActionHandler(IWorkbenchActionConstants.QUIT,
					new MockAction());

			cut.hasRun = copy.hasRun = undo.hasRun = quit.hasRun = false;
			runMatchingCommand(fWindow, ActionFactory.CUT.getId());
			runMatchingCommand(fWindow, ActionFactory.UNDO.getId());
			ActionUtil.runActionUsingPath(fWindow,
					IWorkbenchActionConstants.M_FILE + '/' + IWorkbenchActionConstants.QUIT);
			assertTrue(!cut.hasRun);
			assertTrue(!copy.hasRun);
			assertTrue(!undo.hasRun);
			assertTrue(!quit.hasRun);

			// Reactivate test view and run actions again.
			// This time our global actions should be invoked.
			fPage.activate(part);
			cut.hasRun = copy.hasRun = undo.hasRun = quit.hasRun = false;
			runMatchingCommand(fWindow, ActionFactory.CUT.getId());
			runMatchingCommand(fWindow, ActionFactory.UNDO.getId());
			ActionUtil.runActionUsingPath(fWindow,
					IWorkbenchActionConstants.M_FILE + '/' + IWorkbenchActionConstants.QUIT);
			assertTrue(cut.hasRun);
			assertTrue(!copy.hasRun);
			assertTrue(undo.hasRun);
			assertTrue(quit.hasRun);
		}

		private void runMatchingCommand(IWorkbenchWindow window, String actionId)
				throws ExecutionException, NotDefinedException {
		IHandlerService hs = window.getService(IHandlerService.class);
		IActionCommandMappingService ms = window.getService(IActionCommandMappingService.class);
		String commandId = ms.getCommandId(actionId);
		assertNotNull(commandId);
		try {
			hs.executeCommand(commandId, null);
		} catch (NotHandledException | NotEnabledException e) {
			// this is not a failure, just a condition to be checked by
			// the test
		}
	}
}

