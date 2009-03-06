/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.5
 * @author Prakash G.R.
 * 
 */
public class RadioStateTest extends UITestCase {

	private ICommandService commandService;
	private IHandlerService handlerService;

	public RadioStateTest(String testName) {
		super(testName);
	}

	protected void doSetUp() throws Exception {
		super.doSetUp();
		commandService = (ICommandService) fWorkbench
				.getService(ICommandService.class);
		handlerService = (IHandlerService) fWorkbench
				.getService(IHandlerService.class);
	}

	public void testRadioValues() throws Exception {

		Command command1 = commandService
				.getCommand("org.eclipse.ui.tests.radioStateCommand1");

		// check the initial values
		assertState(command1, "value2");

		// execute with value1
		Parameterization radioParam = new Parameterization(command1
				.getParameter(RadioState.PARAMETER_ID), "value1");
		ParameterizedCommand parameterizedCommand = new ParameterizedCommand(
				command1, new Parameterization[] { radioParam });
		handlerService.executeCommand(parameterizedCommand, null);

		// check if updated
		assertState(command1, "value1");
		
		handlerService.executeCommand(parameterizedCommand, null);
		assertState(command1, "value1");
		
		Parameterization radioParam2 = new Parameterization(command1
				.getParameter(RadioState.PARAMETER_ID), "value2");
		ParameterizedCommand parameterizedCommand2 = new ParameterizedCommand(
				command1, new Parameterization[] { radioParam2 });
		handlerService.executeCommand(parameterizedCommand2, null);
		assertState(command1, "value2");
	}
	private void assertState(Command command, String expectedValue) {
		State state = command.getState(RadioState.STATE_ID);
		Object value = state.getValue();
		assertTrue(value instanceof String);
		assertEquals(expectedValue, value);
	}

}
