/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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

package org.eclipse.ui.tests.commands;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.junit.Test;

/**
 * Tests the new help context identifier support on commands and handlers.
 *
 * @since 3.2
 */
public final class HelpContextIdTest {

	private static final String COMMAND_HELP_ID = "org.eclipse.ui.tests.commandHelp";

	private static final String COMMAND_ID = "org.eclipse.ui.tests.helpContextId";

	private static final String HANDLER_HELP_ID = "org.eclipse.ui.tests.handlerHelp";

	private static final String NEW_HELP_ID = "new_help_id";

	/**
	 * Tests the reading of the help context identifier of the registry, as well
	 * as programmatic changes. It does not test whether there is notification.
	 *
	 * @throws NotDefinedException
	 *             If the command defined in the registry is somehow not
	 *             defined.
	 */
	@Test
	public final void testHelpContextId() throws NotDefinedException {
		final ICommandService commandService = PlatformUI.getWorkbench()
				.getService(ICommandService.class);
		final IHandlerService handlerService = PlatformUI.getWorkbench()
				.getService(IHandlerService.class);
		String helpContextId = null;

		// At first, the help context id should be the handler.
		helpContextId = commandService.getHelpContextId(COMMAND_ID);
		assertEquals(
				"The initial help context id should be that of the handler",
				HANDLER_HELP_ID, helpContextId);

		// Retract the handler help context id by creating a more specific handler
		handlerService.activateHandler(COMMAND_ID, new AbstractHandler() {
			@Override
			public final Object execute(final ExecutionEvent event) {
				return null;
			}
		}, new Expression() {

			@Override
			public void collectExpressionInfo(ExpressionInfo info) {
				info.addVariableNameAccess(ISources.ACTIVE_CURRENT_SELECTION_NAME);
			}

			@Override
			public EvaluationResult evaluate(IEvaluationContext context) {
				return EvaluationResult.TRUE;
			}
		});
		helpContextId = commandService.getHelpContextId(COMMAND_ID);
		assertEquals("The help context id should now be that of the command",
				COMMAND_HELP_ID, helpContextId);

		// Now re-define the command with a different help context id.
		final Command command = commandService.getCommand(COMMAND_ID);
		command.undefine();
		command.define("New Name", null, commandService.getCategory(null),
				null, null, NEW_HELP_ID);
		helpContextId = commandService.getHelpContextId(COMMAND_ID);
		assertEquals("The help context id should now be the new id",
				NEW_HELP_ID, helpContextId);
	}
}

