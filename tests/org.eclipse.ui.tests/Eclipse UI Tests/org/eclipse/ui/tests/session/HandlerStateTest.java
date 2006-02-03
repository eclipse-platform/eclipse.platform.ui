/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import junit.framework.TestCase;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * A test to verify the persistence of handler state between sessions.
 * 
 * @since 3.2
 */
public class HandlerStateTest extends TestCase {

	/**
	 * The identifier of the command with state that we wish to test.
	 */
	private static final String COMMAND_ID = "org.eclipse.ui.tests.commandWithState";

	/**
	 * The identifier of the state storing a simple object.
	 */
	private static final String STATE_ID = "STYLE";

	/**
	 * Constructs a new instance of <code>HandlerStateTest</code>.
	 * 
	 * @param testName
	 *            The name of the test; may be <code>null</code>.
	 */
	public HandlerStateTest(final String testName) {
		super(testName);
	}

	/**
	 * Verifies that the initial handler state is correct. After this, the state
	 * is changed.
	 */
	public final void testInitialHandlerState() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final ICommandService service = (ICommandService) workbench
				.getService(ICommandService.class);
		final Command command = service.getCommand(COMMAND_ID);
		final State state = command.getState(STATE_ID);
		final Boolean actual = (Boolean) state.getValue();
		assertTrue("The initial value should be true", actual.booleanValue());

		// Change the value for the next run.
		state.setValue(Boolean.FALSE);
	}

	/**
	 * Verifies that the handler state is persisted between sessions.
	 */
	public final void testModifiedHandlerState() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final ICommandService service = (ICommandService) workbench
				.getService(ICommandService.class);
		final Command command = service.getCommand(COMMAND_ID);
		final State state = command.getState(STATE_ID);
		final Boolean actual = (Boolean) state.getValue();
		assertTrue("The value should now be different", !actual.booleanValue());
	}
}
