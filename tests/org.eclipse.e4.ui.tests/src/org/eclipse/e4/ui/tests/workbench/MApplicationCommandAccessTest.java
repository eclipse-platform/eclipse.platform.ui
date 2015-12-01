/*******************************************************************************
 * Copyright (c) 2009, 2015 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class MApplicationCommandAccessTest {
	/**
	 *
	 */
	private static final int NUMBER_OF_COMMANDS = 1000;
	protected IEclipseContext applicationContext;
	protected E4Workbench wb;

	@Before
	public void setUp() throws Exception {
		applicationContext = E4Application.createDefaultContext();

		ContextInjectionFactory.make(CommandServiceAddon.class, applicationContext);
	}

	@After
	public void tearDown() throws Exception {
		applicationContext.dispose();
	}

	@Test
	public void testCreateApplictionWithCommands() {
		final MApplication application = createAppWithCommands();
		// Ensure all commands are created
		assertEquals(NUMBER_OF_COMMANDS, application.getCommands().size());
	}

	@Test
	public void testRemovalOfCommands() {
		final MApplication application = createAppWithCommands();
		application.getCommands().remove(0);

		// Ensure one commands is removed
		assertEquals(NUMBER_OF_COMMANDS - 1, application.getCommands().size());
	}

	@Test
	public void testFindCorrectCommand()
	{
		final MApplication application = createAppWithCommands();

		boolean found = false;
		for (MCommand command : application.getCommands()) {
			if (command.getElementId().equals(String.valueOf(NUMBER_OF_COMMANDS - 1))) {
				found = true;
				break;
			}
		}
		assertTrue(found);
	}

	@Test
	public void testFindCorrectCommandDirectAccess() {
		final MApplication application = createAppWithCommands();

		MCommand command = application.getCommand(String.valueOf(NUMBER_OF_COMMANDS - 1));
		assertNotNull(command);
		MCommand commandShouldBeNull = application.getCommand("invalid");
		assertNull(commandShouldBeNull);
	}

	@Test
	public void testFindNullAfterCommandRemoval() {
		final MApplication application = createAppWithCommands();

		MCommand command = application.getCommand(String.valueOf(NUMBER_OF_COMMANDS - 1));
		assertNotNull(command);
		application.getCommands().remove(command);
		MCommand commandShouldBeNull = application.getCommand(String.valueOf(NUMBER_OF_COMMANDS - 1));
		assertNull(commandShouldBeNull);
	}

	@Test
	public void testCommandAfterAddingIt() {
		final MApplication application = createAppWithCommands();
		EModelService modelService = applicationContext.get(EModelService.class);

		MCommand commandShouldBeNull = application.getCommand("neucommand");
		assertNull(commandShouldBeNull);
		// Create and add new command to application
		MCommand mCommand = modelService.createModelElement(MCommand.class);
		mCommand.setElementId("neucommand");
		application.getCommands().add(mCommand);

		MCommand neuCommnad = application.getCommand("neucommand");
		assertNotNull(neuCommnad);
	}

	/**
	 * Create an application with lots of commands
	 *
	 * @return
	 */
	private MApplication createAppWithCommands() {
		EModelService modelService = applicationContext.get(EModelService.class);
		final MApplication application = modelService.createModelElement(MApplication.class);
		for (int i = 0; i < NUMBER_OF_COMMANDS; i++) {
			MCommand command = modelService.createModelElement(MCommand.class);
			command.setElementId(String.valueOf(i));
			application.getCommands().add(command);
		}
		return application;
	}

}
