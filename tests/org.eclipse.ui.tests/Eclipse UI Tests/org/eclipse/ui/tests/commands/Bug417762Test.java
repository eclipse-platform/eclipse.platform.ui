/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class Bug417762Test extends UITestCase {

	/**
	 * @param testName
	 */
	public Bug417762Test(String testName) {
		super(testName);
	}

	@Test
	public void testAsReported() throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		IWorkbench workbench = getWorkbench();
		ICommandService commandService = workbench
				.getService(ICommandService.class);
		IHandlerService handlerService = workbench
				.getService(IHandlerService.class);
		Command showInCommand = commandService
				.getCommand(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN);
		Map<String, String> parameters = new HashMap<String, String>();
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
		IWorkbench workbench = getWorkbench();
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
		IWorkbench workbench = getWorkbench();
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
		IWorkbench workbench = getWorkbench();
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
