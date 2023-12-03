/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
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

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HandlerTest {
	private static final String HELP_COMMAND_ID = "org.eclipse.ui.commands.help";
	private static final String HELP_COMMAND1_ID = HELP_COMMAND_ID + "1";
	private IEclipseContext appContext;

	public static class TestHandler {
		boolean ran = false;
		boolean canRun;
		String rc;

		public TestHandler(boolean c, String ret) {
			canRun = c;
			rc = ret;
		}

		@CanExecute
		public boolean canExecute() {
			return canRun;
		}

		@Execute
		public Object execute() {
			ran = true;
			return rc;
		}
	}

	@Before
	public void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
	}

	@After
	public void tearDown() throws Exception {
		appContext.dispose();
	}

	@Test
	public void testOneCommand() throws Exception {
		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);
		final ParameterizedCommand help1Command = getCommand(appContext,
				HELP_COMMAND1_ID);

		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		EHandlerService service = appContext.get(EHandlerService.class);
		service.activateHandler(HELP_COMMAND_ID, handler);

		ECommandService cmdService = appContext.get(ECommandService.class);
		Command command = cmdService.getCommand(HELP_COMMAND_ID);
		assertEquals(HELP_COMMAND_ID, command.getId());
		assertEquals(HELP_COMMAND_ID, service.executeHandler(helpCommand));
		assertTrue(handler.ran);
		assertNull(service.executeHandler(help1Command));
	}

	@Test
	public void testTwoCommands() throws Exception {
		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);
		final ParameterizedCommand help1Command = getCommand(appContext,
				HELP_COMMAND1_ID);

		EHandlerService service = appContext.get(EHandlerService.class);
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);
		TestHandler handler1 = new TestHandler(false, HELP_COMMAND1_ID);
		service.activateHandler(HELP_COMMAND1_ID, handler1);
		assertEquals(HELP_COMMAND_ID, service.executeHandler(helpCommand));
		assertNull(service.executeHandler(help1Command));
		assertFalse(handler1.ran);
		handler1.canRun = true;
		assertEquals(HELP_COMMAND1_ID, service.executeHandler(help1Command));
		assertTrue(handler1.ran);
	}

	@Test
	public void testTwoHandlers() throws Exception {
		defineCommands(appContext);

		ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);

		EHandlerService service = appContext.get(EHandlerService.class);
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);

		IEclipseContext window = appContext.createChild("windowContext");
		window.activate();
		EHandlerService windowService = window.get(EHandlerService.class);
		String windowRC = HELP_COMMAND_ID + ".window";
		TestHandler windowHandler = new TestHandler(false, windowRC);
		windowService.activateHandler(HELP_COMMAND_ID, windowHandler);
		assertNull(service.executeHandler(helpCommand));
		assertFalse(windowHandler.ran);
		assertFalse(handler.ran);

		windowHandler.canRun = true;
		assertEquals(windowRC, service.executeHandler(helpCommand));
		assertTrue(windowHandler.ran);
		assertFalse(handler.ran);
	}

	private ParameterizedCommand getCommand(IEclipseContext appContext,
			String commandId) {
		ECommandService cs = appContext.get(ECommandService.class);
		final Command cmd = cs.getCommand(commandId);
		return new ParameterizedCommand(cmd, null);
	}

	@Test
	public void testCanExecute() throws Exception {
		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);

		EHandlerService service = appContext.get(EHandlerService.class);
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);

		IEclipseContext window = appContext.createChild("windowContext");
		window.activate();
		EHandlerService windowService = window.get(EHandlerService.class);
		String windowRC = HELP_COMMAND_ID + ".window";
		TestHandler windowHandler = new TestHandler(false, windowRC);
		windowService.activateHandler(HELP_COMMAND_ID, windowHandler);

		assertFalse(windowService.canExecute(helpCommand));
		windowHandler.canRun = true;
		assertTrue(windowService.canExecute(helpCommand));
		windowHandler.canRun = false;
		assertFalse(windowService.canExecute(helpCommand));
		windowService.deactivateHandler(HELP_COMMAND_ID, windowHandler);
		assertTrue(windowService.canExecute(helpCommand));
	}

	@Test
	public void testThreeContexts() throws Exception {
		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);

		EHandlerService service = appContext.get(EHandlerService.class);
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);

		IEclipseContext window = appContext.createChild("windowContext");
		window.activate();
		EHandlerService windowService = window.get(EHandlerService.class);
		String windowRC = HELP_COMMAND_ID + ".window";
		TestHandler windowHandler = new TestHandler(true, windowRC);
		windowService.activateHandler(HELP_COMMAND_ID, windowHandler);
		assertEquals(windowRC, service.executeHandler(helpCommand));

		IEclipseContext dialog = appContext.createChild("dialogContext");
		dialog.activate();
		assertEquals(HELP_COMMAND_ID, service.executeHandler(helpCommand));

		window.activate();
		assertEquals(windowRC, service.executeHandler(helpCommand));
	}

	@Test
	public void testDifferentExecutionContexts() throws Exception {
		defineCommands(appContext);
		final ParameterizedCommand helpCommand = getCommand(appContext,
				HELP_COMMAND_ID);

		EHandlerService service = appContext.get(EHandlerService.class);
		TestHandler handler = new TestHandler(true, HELP_COMMAND_ID);
		service.activateHandler(HELP_COMMAND_ID, handler);

		IEclipseContext window = appContext.createChild("windowContext");
		window.activate();
		EHandlerService windowService = window.get(EHandlerService.class);
		String windowRC = HELP_COMMAND_ID + ".window";
		TestHandler windowHandler = new TestHandler(true, windowRC);
		windowService.activateHandler(HELP_COMMAND_ID, windowHandler);
		assertEquals(windowRC, service.executeHandler(helpCommand));
		assertEquals(windowRC, windowService.executeHandler(helpCommand));

		IEclipseContext dialog = appContext.createChild("dialogContext");
		EHandlerService dialogService = dialog.get(EHandlerService.class);
		assertEquals(HELP_COMMAND_ID, dialogService.executeHandler(helpCommand));
	}

	private void defineCommands(IEclipseContext appContext) {
		ECommandService cmdService = (ECommandService) appContext
				.get(ECommandService.class.getName());
		Category category = cmdService.defineCategory("cat." + HELP_COMMAND_ID,
				"Help Category", null);
		cmdService.defineCommand(HELP_COMMAND_ID, "Help Command", null,
				category, null);
		cmdService.defineCommand(HELP_COMMAND1_ID, "Help 1 Command", null,
				category, null);
	}

}
