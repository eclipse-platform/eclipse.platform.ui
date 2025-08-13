/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
 *     Benjamin Muskalla - bug 222861 [Commands] ParameterizedCommand#equals broken
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ITypedParameter;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.junit.Test;

public class CommandParameterTypeTest {

	static final String SUBTRACT = "org.eclipse.ui.tests.commands.subtractInteger";
	static final String MINUEND = "minuend";
	static final String SUBTRAHEND = "subtrahend";

	static final String TYPE = "org.eclipse.ui.tests.commands.Integer";

	/**
	 * Tests invoking a command that subtracts one number from another. The
	 * handler for the subtract command will convert the string parameters to
	 * integers to perform the operation. This test drives much of the command
	 * parameter type infrastructure including obtaining parameter types and
	 * performing stringToObject and objectToString conversions.
	 */
	@Test
	public void testSubtract() throws CommandException {
		testSubtract(8, 5, 3);
		testSubtract(-4, 12, -16);
	}

	/**
	 * Test subtract again with invalid parameters and check for failure
	 * exception.
	 */
	@Test
	public void testSubtractTypeError() {
		// try to pass a Boolean instead of an Integer
		assertThrows(ParameterValueConversionException.class, () -> testSubtract(Integer.valueOf(3), Boolean.FALSE, 3));
	}

	/**
	 * Test the complete execution flow for the subtract command
	 */
	private void testSubtract(int minuend, int subtrahend, int difference) throws CommandException {
		testSubtract(Integer.valueOf(minuend), Integer.valueOf(subtrahend), difference);
	}

	/**
	 * Test the complete execution flow for the subtract command
	 */
	private void testSubtract(Object minuend, Object subtrahend, int difference) throws CommandException {
		ICommandService commandService = getCommandService();
		Command command = commandService.getCommand(SUBTRACT);

		// first convert the object params to strings
		String minuendStr = command.getParameterType(MINUEND)
				.getValueConverter().convertToString(minuend);
		String subtrahendStr = command.getParameterType(SUBTRAHEND)
				.getValueConverter().convertToString(subtrahend);

		// setup the parameterizations
		Parameterization minuendParam = new Parameterization(command
				.getParameter(MINUEND), minuendStr);
		Parameterization subtrahendParam = new Parameterization(command
				.getParameter(SUBTRAHEND), subtrahendStr);
		Parameterization[] parameterizations = new Parameterization[] {
				minuendParam, subtrahendParam };

		// execute the command and check the result
		ParameterizedCommand pCommand = new ParameterizedCommand(command,
				parameterizations);
		IHandlerService hs = PlatformUI.getWorkbench().getService(IHandlerService.class);
		Integer result = (Integer) pCommand.executeWithChecks(null, hs.getCurrentState());
		assertEquals(difference, result.intValue());
	}

	/**
	 * Tests AbstractParameterValueConverter.convertToObject for the Integer
	 * converter used in this test suite.
	 */
	@Test
	public void testConvertStringToInteger() throws CommandException {
		testConvertStringToInteger("33", 33, false);
		testConvertStringToInteger("-1", -1, false);
		testConvertStringToInteger("blah", 33, true);
		testConvertStringToInteger(null, 33, true);
	}

	private void testConvertStringToInteger(String value, int expected,
			boolean expectFail) throws CommandException {
		ICommandService commandService = getCommandService();
		ParameterType type = commandService.getParameterType(TYPE);

		AtomicReference<Object> converted = new AtomicReference<>();
		if (expectFail) {
			assertThrows(ParameterValueConversionException.class,
					() -> converted.set(type.getValueConverter().convertToObject(value)));
		} else {
			converted.set(type.getValueConverter().convertToObject(value));
			assertEquals(Integer.valueOf(expected), converted.get());
		}
	}

	/**
	 * Tests AbstractParameterValueConverter.convertToString for the Integer
	 * converter used in this test suite.
	 */
	@Test
	public void testConvertIntegerToString() throws CommandException {
		testConvertIntegerToString(Integer.valueOf(6), "6", false);
		testConvertIntegerToString(Integer.valueOf(0), "0", false);
		testConvertIntegerToString(Integer.valueOf(-32), "-32", false);
		testConvertIntegerToString(null, null, true);
		testConvertIntegerToString(Boolean.TRUE, null, true);
	}

	private void testConvertIntegerToString(Object value, String expected,
			boolean expectFail) throws CommandException {
		ICommandService commandService = getCommandService();
		ParameterType type = commandService.getParameterType(TYPE);

		AtomicReference<Object> converted = new AtomicReference<>();
		if (expectFail) {
			assertThrows(ParameterValueConversionException.class,
					() -> converted.set(type.getValueConverter().convertToString(value)));
		} else {
			converted.set(type.getValueConverter().convertToString(value));
			assertEquals(expected, converted.get());
		}
	}

	/**
	 * Tests ParameterType.isCompatible for various values with the Integer
	 * parameter type used in this test suite.
	 */
	@Test
	public void testIsCompatible() throws CommandException {
		ICommandService commandService = getCommandService();
		ParameterType type = commandService.getParameterType(TYPE);

		assertTrue(type.isCompatible(Integer.valueOf(4)));
		assertTrue(type.isCompatible(Integer.valueOf(0)));
		assertTrue(type.isCompatible(Integer.valueOf(-434)));
		assertFalse(type.isCompatible(null));
		assertFalse(type.isCompatible("4"));
	}

	/**
	 * Try to find a command that takes integers as parameters. We might find
	 * multiple commands - just make sure we can at least find the subtract
	 * command used elsewhere in this test suite.
	 */
	@Test
	public void testFindIntegerParamCommand() throws CommandException {
		Integer value = Integer.valueOf(6);

		ICommandService commandService = getCommandService();
		Command[] commands = commandService.getDefinedCommands();

		boolean foundSubtract = false;

		for (Command command2 : commands) {
			Command command = command2;
			if (!command.isDefined()) {
				continue;
			}

			IParameter[] parameters = command.getParameters();
			if (parameters == null) {
				continue;
			}

			if (parameters.length == 0) {
				continue;
			}

			if (checkParamType1(command, parameters[0], value)
					&& checkParamType2(parameters[0], value)) {
				if (SUBTRACT.equals(command.getId())) {
					foundSubtract = true;
					break;
				}
			}
		}

		assertTrue(foundSubtract);
	}

	private boolean checkParamType1(Command command, IParameter parameter,
			Object value) throws CommandException {
		ParameterType type = command.getParameterType(parameter.getId());
		if (type == null) {
			return false;
		}
		return type.isCompatible(value);
	}

	private boolean checkParamType2(IParameter parameter, Object value)
			throws CommandException {
		if (!(parameter instanceof ITypedParameter typedParameter)) {
			return false;
		}
		ParameterType type = typedParameter.getParameterType();
		if (type == null) {
			return false;
		}
		return type.isCompatible(value);
	}


	/**
	 * Test {@link Command#getReturnType()}, making sure we can get the return
	 * type of the subtract command.
	 */
	@Test
	public void testGetReturnType() throws CommandException {
		ICommandService commandService = getCommandService();
		Command command = commandService.getCommand(SUBTRACT);

		ParameterType returnType = command.getReturnType();
		assertNotNull(returnType);
		assertEquals(TYPE, returnType.getId());
	}

	private ICommandService getCommandService() {
		ICommandService serviceObject = PlatformUI.getWorkbench().getAdapter(ICommandService.class);
		if (serviceObject != null) {
			return serviceObject;
		}
		return null;
	}

	/**
	 * Test {@link ParameterizedCommand}, making sure the order of
	 * the parameters is not important.
	 */
	@Test
	public void testUnrelevantOrder() throws NotDefinedException {
		ICommandService commandService = getCommandService();
		Command command = commandService.getCommand(SUBTRACT);

		IParameter sub = command.getParameter(SUBTRAHEND);
		IParameter min = command.getParameter(MINUEND);
		Parameterization param1 = new Parameterization(sub, "5");
		Parameterization param2 = new Parameterization(min, "3");

		Parameterization[] params = new Parameterization[2];
		params[0] = param1;
		params[1] = param2;

		Parameterization[] params1 = new Parameterization[2];
		params1[0] = param2;
		params1[1] = param1;

		ParameterizedCommand pCommand1 = new ParameterizedCommand(command, params);
		ParameterizedCommand pCommand2 = new ParameterizedCommand(command, params1);

		assertTrue(pCommand1.equals(pCommand2));
	}
}
