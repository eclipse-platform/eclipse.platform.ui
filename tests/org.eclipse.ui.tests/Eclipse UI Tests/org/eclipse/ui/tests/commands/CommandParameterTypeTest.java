/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.tests.util.UITestCase;

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
	
	public void testSubtract() throws CommandException {
		testSubtract(8, 5, 3);
		testSubtract(-4, 12, -16);
	}
	
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
	
	public void testSubtract(int minuend, int subtrahend, int difference) throws CommandException {
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
	
	private ICommandService getCommandService() {
		Object serviceObject = getWorkbench().getAdapter(ICommandService.class);
		if (serviceObject != null) {
			ICommandService service = (ICommandService) serviceObject;
			return service;
		}
		return null;
	}
}
