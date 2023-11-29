/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Rule;
import org.junit.Test;

/**
 * A test for whether part identifiers work properly for HandlerSubmissions.
 *
 * @since 3.1
 */
public final class Bug74990Test {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	/**
	 * Tests whether a part-specific handler -- submitted via Java code -- is
	 * matched properly. This is only using the part id. The test verifies that it
	 * is active when the part is active, and not active when the part is not
	 * active.
	 *
	 * @throws PartInitException If something goes wrong creating the part to which
	 *                           this handler is tied.
	 */
	@Test
	public final void testPartIdSubmission() throws PartInitException {
		// Define a command.
		final String testCommandId = "org.eclipse.ui.tests.commands.Bug74990";
		IWorkbench fWorkbench = PlatformUI.getWorkbench();
		final IWorkbenchCommandSupport commandSupport = fWorkbench.getCommandSupport();
		final ICommand testCommand = commandSupport.getCommandManager().getCommand(testCommandId);

		// Create a handler submission.
		final IHandler handler = new AbstractHandler() {
			@Override
			public final Object execute(final Map parameterValuesByName) {
				// Do nothing.
				return null;
			}
		};
		final HandlerSubmission testSubmission = new HandlerSubmission("org.eclipse.ui.tests.api.MockViewPart", null,
				null, testCommandId, handler, Priority.MEDIUM);
		commandSupport.addHandlerSubmission(testSubmission);

		try {
			// Test to make sure the command is not currently handled.
			assertTrue("The MockViewPart command should not be handled", !testCommand.isHandled());

			/*
			 * Open a window with the MockViewPart, and make sure it is now handled.
			 */
			final IWorkbenchPage page = UITestCase.openTestWindow().getActivePage();
			final IViewPart openedView = page.showView("org.eclipse.ui.tests.api.MockViewPart");
			page.activate(openedView);
			while (fWorkbench.getDisplay().readAndDispatch()) {
				((Workbench) fWorkbench).getContext().processWaiting();
			}

			assertTrue("The MockViewPart command should be handled", testCommand.isHandled());

			// Hide the view, and test that is becomes unhandled again.
			page.hideView(openedView);
			while (fWorkbench.getDisplay().readAndDispatch()) {
				// Read the event queue
			}
			assertTrue("The MockViewPart command should not be handled", !testCommand.isHandled());

		} finally {
			commandSupport.removeHandlerSubmission(testSubmission);
		}

	}
}
