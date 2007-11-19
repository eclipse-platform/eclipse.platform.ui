/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests the new help context identifier support on commands and handlers.
 * 
 * @since 3.2
 */
public final class HelpContextIdTest extends UITestCase {

	private static final String COMMAND_HELP_ID = "org.eclipse.ui.tests.commandHelp";

	private static final String COMMAND_ID = "org.eclipse.ui.tests.helpContextId";

	private static final String HANDLER_HELP_ID = "org.eclipse.ui.tests.handlerHelp";

	private static final String NEW_HELP_ID = "new_help_id";

	/**
	 * Constructs a new instance of <code>HelpContextIdTest</code>
	 * 
	 * @param testName
	 *            The name of the test; may be <code>null</code>.
	 */
	public HelpContextIdTest(final String testName) {
		super(testName);
	}

	/**
	 * Tests the reading of the help context identifier of the registry, as well
	 * as programmatic changes. It does not test whether there is notification.
	 * 
	 * @throws NotDefinedException
	 *             If the command defined in the registry is somehow not
	 *             defined.
	 */
	public final void testHelpContextId() throws NotDefinedException {
		final ICommandService commandService = (ICommandService) fWorkbench
				.getService(ICommandService.class);
		final IHandlerService handlerService = (IHandlerService) fWorkbench
				.getService(IHandlerService.class);
		String helpContextId = null;

		// At first, the help context id should be the handler.
		helpContextId = commandService.getHelpContextId(COMMAND_ID);
		assertEquals(
				"The initial help context id should be that of the handler",
				HANDLER_HELP_ID, helpContextId);

		// Retract the handler help context id by creating a handler conflict.
		handlerService.activateHandler(COMMAND_ID, new AbstractHandler() {
			public final Object execute(final ExecutionEvent event) {
				return null;
			}
		});
		helpContextId = commandService.getHelpContextId(COMMAND_ID);
		assertEquals("The help context id should now be that of the command",
				COMMAND_HELP_ID, helpContextId);

		// Now undefine the command and check that an exception is thrown.
		final Command command = commandService.getCommand(COMMAND_ID);
		command.undefine();
		try {
			helpContextId = commandService.getHelpContextId(COMMAND_ID);
			fail("A NotDefinedException should have been thrown");
		} catch (final NotDefinedException e) {
			// success
		}

		// Now define the command with a different help context id.
		command.define("New Name", null, commandService.getCategory(null),
				null, null, NEW_HELP_ID);
		helpContextId = commandService.getHelpContextId(COMMAND_ID);
		assertEquals("The help context id should now be the new id",
				NEW_HELP_ID, helpContextId);
	}
}

