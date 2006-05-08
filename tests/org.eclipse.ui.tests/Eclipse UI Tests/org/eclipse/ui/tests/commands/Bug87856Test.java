/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.commands;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * This tests whether we are leaking handlers after their submission has been
 * removed.
 * 
 * @since 3.1
 */
public class Bug87856Test extends UITestCase {

	/**
	 * Constructs a new instance of <code>Bug87856Test</code>.
	 * 
	 * @param name
	 *            The name of the test
	 */
	public Bug87856Test(final String name) {
		super(name);
	}

	/**
	 * Tests whether the workbench command support (or its dependencies) will
	 * leak handlers when the process loop is run. Basically, we're checking to
	 * see that removing a handler submission really works.
	 */
	public final void testHandlerLeak() {
		final IWorkbenchCommandSupport commandSupport = fWorkbench
				.getCommandSupport();
		final ICommandService commandService = (ICommandService) fWorkbench
				.getAdapter(ICommandService.class);
		final String commandId = Bug87856Test.class.getName();
		final Command command = commandService.getCommand(commandId);

		// Submit a handler.
		IHandler handler = new AbstractHandler() {

			public Object execute(Map parameterValuesByName)
					throws org.eclipse.ui.commands.ExecutionException {
				// Do nothing
				return null;
			}

		};
		HandlerSubmission submission = new HandlerSubmission(null, null, null,
				command.getId(), handler, Priority.MEDIUM);
		commandSupport.addHandlerSubmission(submission);

		/*
		 * Remove the handler with no replacement, and hold on to the handler
		 * via a weak reference.
		 */
		commandSupport.removeHandlerSubmission(submission);
		submission = null;
		final WeakReference reference = new WeakReference(handler);
		handler = null;

		// Attempt to force garbage collection.
		System.gc();
		System.runFinalization();
		Thread.yield();
		System.gc();
		System.runFinalization();
		Thread.yield();
		System.gc();
		System.runFinalization();
		Thread.yield();

		// Check to see if the reference has been cleared.
		assertTrue(
				"We should not hold on to a handler after the submission has been removed.",
				reference.isEnqueued() || (reference.get() == null));
	}
}
