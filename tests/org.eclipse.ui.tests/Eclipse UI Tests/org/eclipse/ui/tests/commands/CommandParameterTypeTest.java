/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ITypedParameter;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class CommandParameterTypeTest extends UITestCase {

	static final String SUBTRACT = "org.eclipse.ui.tests.commands.subtractInteger";
	static final String MINUEND = "minuend";
	static final String SUBTRAHEND = "subtrahend";
	
	static final String TYPE = "org.eclipse.ui.tests.commands.Integer";
	
	/**
	 * Constructs a new instance of <code>CommandParameterTypeTest</code>.
	 * 
	 * @param name
	 *            The name of the test
	 */
	public CommandParameterTypeTest(String testName) {
		super(testName);
	}
	
	/**
	 * Tests invoking a command that subtracts one number from another. The
	 * handler for the subtract command will convert the string parameters to
	 * integers to perform the operation. This test drives much of the command
	 * parameter type infrastructure including obtaining parameter types and
	 * performing stringToObject and objectToString conversions.
	 */
	public void testSubtract() throws CommandException {
		testSubtract(8, 5, 3);
		testSubtract(-4, 12, -16);
	}
	
	/**
	 * Test subtract again with invalid parameters and check for failure
	 * exception.
	 */
	public void testSubtractTypeError() {
		try {
			// try to pass a Boolean instead of an Integer
			testSubtract(new Integer(3), Boolean.FALSE, 3);
			fail("expected ParameterValueConversionException");
		}
		catch (ParameterValueConversionException ex) {
			// passed
		}
		catch (Exception ex) {
			fail("expected ParameterValueConversionException");
		}
	}
	
	/**
	 * Test the complete execution flow for the subtract command
	 */
	private void testSubtract(int minuend, int subtrahend, int difference) throws CommandException {
		testSubtract(new Integer(minuend), new Integer(subtrahend), difference);
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
		Integer result = (Integer) pCommand.executeWithChecks(null, null);
		assertEquals(difference, result.intValue());
	}
	
	/**
	 * Tests AbstractParameterValueConverter.convertToObject for the Integer
	 * converter used in this test suite.
	 */
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

		Object converted = null;
		if (expectFail) {
			try {
				converted = type.getValueConverter().convertToObject(value);
				fail("expected ParameterValueConversionException");
			} catch (ParameterValueConversionException ex) {
				// passed
				return;
			} catch (Exception ex) {
				fail("expected ParameterValueConversionException");
			}
		} else {
			converted = type.getValueConverter().convertToObject(value);
		}

		assertEquals(new Integer(expected), converted);
	}
	
	/**
	 * Tests AbstractParameterValueConverter.convertToString for the Integer
	 * converter used in this test suite.
	 */
	public void testConvertIntegerToString() throws CommandException {
		testConvertIntegerToString(new Integer(6), "6", false);
		testConvertIntegerToString(new Integer(0), "0", false);
		testConvertIntegerToString(new Integer(-32), "-32", false);
		testConvertIntegerToString(null, null, true);
		testConvertIntegerToString(Boolean.TRUE, null, true);
	}
	
	private void testConvertIntegerToString(Object value, String expected,
			boolean expectFail) throws CommandException {
		ICommandService commandService = getCommandService();
		ParameterType type = commandService.getParameterType(TYPE);

		String converted = null;
		if (expectFail) {
			try {
				converted = type.getValueConverter().convertToString(value);
				fail("expected ParameterValueConversionException");
			} catch (ParameterValueConversionException ex) {
				// passed
				return;
			} catch (Exception ex) {
				fail("expected ParameterValueConversionException");
			}
		} else {
			converted = type.getValueConverter().convertToString(value);
		}
		assertEquals(expected, converted);
	}
	
	/**
	 * Tests ParameterType.isCompatible for various values with the Integer
	 * parameter type used in this test suite.
	 */
	public void testIsCompatible() throws CommandException {
		ICommandService commandService = getCommandService();
		ParameterType type = commandService.getParameterType(TYPE);
		
		assertTrue(type.isCompatible(new Integer(4)));
		assertTrue(type.isCompatible(new Integer(0)));
		assertTrue(type.isCompatible(new Integer(-434)));
		assertFalse(type.isCompatible(null));
		assertFalse(type.isCompatible("4"));
	}
	
	/**
	 * Try to find a command that takes integers as parameters. We might find
	 * multiple commands - just make sure we can at least find the subtract
	 * command used elsewhere in this test suite.
	 */
	public void testFindIntegerParamCommand() throws CommandException {
		Integer value = new Integer(6);
		
		ICommandService commandService = getCommandService();
		Command[] commands = commandService.getDefinedCommands();
		
		boolean foundSubtract = false;
		
		for (int i = 0; i < commands.length; i++) {
			Command command = commands[i];
			if (!command.isDefined())
				continue;
			
			IParameter[] parameters = command.getParameters();
			if (parameters == null)
				continue;
			
			if (parameters.length == 0)
				continue;
			
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
		if (type == null)
			return false;
		return type.isCompatible(value);
	}
	
	private boolean checkParamType2(IParameter parameter, Object value)
			throws CommandException {
		if (!(parameter instanceof ITypedParameter))
			return false;
		ParameterType type = ((ITypedParameter) parameter).getParameterType();
		if (type == null)
			return false;
		return type.isCompatible(value);
	}
	
	
	/**
	 * Test {@link Command#getReturnType()}, making sure we can get the return
	 * type of the subtract command.
	 */
	public void testGetReturnType() throws CommandException {
		ICommandService commandService = getCommandService();
		Command command = commandService.getCommand(SUBTRACT);
		
		ParameterType returnType = command.getReturnType();
		assertNotNull(returnType);
		assertEquals(TYPE, returnType.getId());
	}
	
	private ICommandService getCommandService() {
		Object serviceObject = getWorkbench().getAdapter(ICommandService.class);
		if (serviceObject != null) {
			ICommandService service = (ICommandService) serviceObject;
			return service;
		}
		return null;
	}
}
