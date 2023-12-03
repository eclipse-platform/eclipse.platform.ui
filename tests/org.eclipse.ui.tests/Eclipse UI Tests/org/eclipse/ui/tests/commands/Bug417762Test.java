/*******************************************************************************
 * Copyright (c) 2013, 2017 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.junit.Test;

/**
 * @since 3.5
 */
public class Bug417762Test {

	@Test
	public void testAsReported() throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandService = workbench
				.getService(ICommandService.class);
		IHandlerService handlerService = workbench
				.getService(IHandlerService.class);
		Command showInCommand = commandService
				.getCommand(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN);
		Map<String, String> parameters = new HashMap<>();
		parameters.put(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET,
				"my.view.id");
		IEvaluationContext contextSnapshot = handlerService
				.createContextSnapshot(true);
		ExecutionEvent event = new ExecutionEvent(showInCommand, parameters,
				null, contextSnapshot);
		showInCommand.executeWithChecks(event);
	}

	@Test
	public void testSuggestionUseExecuteCommand() throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandService = workbench
				.getService(ICommandService.class);
		IHandlerService handlerService = workbench
				.getService(IHandlerService.class);
		Command showInCommand = commandService
				.getCommand(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN);
//		Map<String, String> parameters = new HashMap<String, String>();
//		parameters.put(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET,
//				"my.view.id");
//		IEvaluationContext contextSnapshot = handlerService
//				.createContextSnapshot(true);
//		ExecutionEvent event = new ExecutionEvent(showInCommand, parameters,
//				null, contextSnapshot);
//		showInCommand.executeWithChecks(event);

		handlerService
				.executeCommand(
						ParameterizedCommand.generateCommand(
								showInCommand,
								Collections
										.singletonMap(
												IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET,
												"my.view.id")), null);
	}

	@Test
	public void testSuggestionUseExecuteCommandInContext() throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandService = workbench
				.getService(ICommandService.class);
		IHandlerService handlerService = workbench
				.getService(IHandlerService.class);
		Command showInCommand = commandService
				.getCommand(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN);
//		Map<String, String> parameters = new HashMap<String, String>();
//		parameters.put(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET,
//				"my.view.id");
		IEvaluationContext contextSnapshot = handlerService
				.createContextSnapshot(true);
//		ExecutionEvent event = new ExecutionEvent(showInCommand, parameters,
//				null, contextSnapshot);
//		showInCommand.executeWithChecks(event);

		handlerService
				.executeCommandInContext(
						ParameterizedCommand.generateCommand(
								showInCommand,
								Collections
										.singletonMap(
												IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET,
												"my.view.id")), null, contextSnapshot);
	}

	@Test
	public void testSuggestionUseParameterizedCommandExecuteWithChecks() throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandService = workbench
				.getService(ICommandService.class);
		IHandlerService handlerService = workbench
				.getService(IHandlerService.class);
		Command showInCommand = commandService
				.getCommand(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN);
//		Map<String, String> parameters = new HashMap<String, String>();
//		parameters.put(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET,
//				"my.view.id");
		IEvaluationContext contextSnapshot = handlerService
				.createContextSnapshot(true);
//		ExecutionEvent event = new ExecutionEvent(showInCommand, parameters,
//				null, contextSnapshot);
//		showInCommand.executeWithChecks(event);

		final ParameterizedCommand pc = ParameterizedCommand.generateCommand(
				showInCommand,
				Collections
						.singletonMap(
								IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET,
								"my.view.id"));
		pc.executeWithChecks(null, contextSnapshot);
//		handlerService
//				.executeCommandInContext(
//						pc, null, contextSnapshot);
	}


}
