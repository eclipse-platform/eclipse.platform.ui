/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that a handler contributed to <code>org.eclipse.ui.handlers</code> is
 * dispatched through dependency injection when it does not implement
 * <code>IHandler</code>.
 */
public class PojoHandlerTest {

	private static final String COMMAND_ID = "org.eclipse.ui.tests.commands.pojoHandler";

	private IHandlerService handlerService;

	private ICommandService commandService;

	@BeforeEach
	public void setUp() {
		handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		PojoHandler.executed = false;
		PojoHandler.canExecute = true;
	}

	@AfterEach
	public void tearDown() {
		PojoHandler.executed = false;
		PojoHandler.canExecute = true;
	}

	@Test
	public void executeDispatchesToExecuteAnnotatedMethod() throws Exception {
		Object result = handlerService.executeCommand(COMMAND_ID, null);

		assertTrue(PojoHandler.executed, "@Execute method was not called");
		assertEquals("executed", result, "the return value of @Execute was not passed back");
	}

	@Test
	public void canExecuteControlsEnablement() throws Exception {
		PojoHandler.canExecute = false;

		assertThrows(NotEnabledException.class, () -> handlerService.executeCommand(COMMAND_ID, null));
		assertFalse(PojoHandler.executed, "@Execute was called although @CanExecute returned false");

		PojoHandler.canExecute = true;
		handlerService.executeCommand(COMMAND_ID, null);
		assertTrue(PojoHandler.executed, "@Execute was not called after @CanExecute turned true");
	}

	@Test
	public void handlerIsInjected() throws Exception {
		handlerService.executeCommand(COMMAND_ID, null);

		assertNotNull(PojoHandler.injectedContext, "the handler was not injected");
	}

	@Test
	public void commandIsHandled() throws Exception {
		Command command = commandService.getCommand(COMMAND_ID);

		assertTrue(command.isDefined(), "the test command is not defined");
		handlerService.executeCommand(COMMAND_ID, null);
		assertTrue(command.isHandled(), "the command is not handled by the POJO");
	}
}
