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

import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test serialization and deserialization of ParameterizedCommands. See <a
 * href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=120523">bug 120523</a>.
 * 
 * @since 3.2
 */
public class CommandSerializationTest extends UITestCase {

	/**
	 * Constructs a new instance of <code>CommandSerializationTest</code>.
	 * 
	 * @param name
	 *            The name of the test
	 */
	public CommandSerializationTest(String testName) {
		super(testName);
	}
	
	private final String showPerspectiveCommandId = "org.eclipse.ui.perspectives.showPerspective";
	
	/**
	 * Test a serialization of the show perspective command with no parameters.
	 * 
	 * @throws CommandException
	 */
	public void testSerializeShowPerspective() throws CommandException {

		testDeserializeAndSerialize(showPerspectiveCommandId,
				showPerspectiveCommandId, 0, null, null);

		// test with unnecessary (but valid) trailing "()" characters
		testDeserializeAndSerialize(showPerspectiveCommandId+"()",
				showPerspectiveCommandId, 0, null, null);
	}
	
	
	/**
	 * Test a serialization of the show perspective command with a parameter.
	 * 
	 * @throws CommandException
	 */
	public void testSerializeShowResourcePerspective() throws CommandException {
		
		final String serializedShowResourcePerspectiveCommand = "org.eclipse.ui.perspectives.showPerspective(org.eclipse.ui.perspectives.showPerspective.perspectiveId=org.eclipse.ui.resourcePerspective)";
		final String showPerspectiveParameterId = "org.eclipse.ui.perspectives.showPerspective.perspectiveId";
		final String resourcePerspectiveId = "org.eclipse.ui.resourcePerspective";

		testDeserializeAndSerialize(serializedShowResourcePerspectiveCommand,
				showPerspectiveCommandId, 1,
				new String[] { showPerspectiveParameterId },
				new String[] { resourcePerspectiveId });
		
	}
	
	/**
	 * Test serialization of a command with zero parameters.
	 * 
	 * @throws CommandException
	 */
	public void testZeroParameterCommand() throws CommandException {
		final String zeroParameterCommandId = "org.eclipse.ui.tests.commands.zeroParameterCommand";

		// basic test
		testDeserializeAndSerialize(zeroParameterCommandId,
				zeroParameterCommandId, 0, null, null);
		
		// test with a bogus parameter
		testDeserializeAndSerialize(zeroParameterCommandId
				+ "(bogus.param=hello)", zeroParameterCommandId, 1, null, null);
	}
	
	/**
	 * Test serialization of a command with one parameter.
	 * 
	 * @throws CommandException
	 */
	public void testOneParameterCommand() throws CommandException {
		final String oneParameterCommandId = "org.eclipse.ui.tests.commands.oneParameterCommand";
		final String paramId1 = "param1.1";

		// basic test
		testDeserializeAndSerialize(oneParameterCommandId + "(param1.1=hello)",
				oneParameterCommandId, 1, new String[] { paramId1 },
				new String[] { "hello" });

		// try it with null value param
		testDeserializeAndSerialize(oneParameterCommandId + "(param1.1)",
				oneParameterCommandId, 1, new String[] { paramId1 },
				new String[] { null });

		// try it without the param
		testDeserializeAndSerialize(oneParameterCommandId,
				oneParameterCommandId, 0, null, null);
		
		// test with a bogus parameter
		testDeserializeAndSerialize(oneParameterCommandId
				+ "(bogus.param=hello)", oneParameterCommandId, 1, null, null);
		
		// test with a bogus parameter and the real one
		testDeserializeAndSerialize(oneParameterCommandId
				+ "(bogus.param=hello,param1.1=foo)", oneParameterCommandId, 2,
				new String[] { paramId1 }, new String[] { "foo" });
	}
	
	/**
	 * Test serialization of a command with two parameters.
	 * 
	 * @throws CommandException
	 */
	public void testTwoParameterCommand() throws CommandException {
		final String twoParameterCommandId = "org.eclipse.ui.tests.commands.twoParameterCommand";
		final String paramId1 = "param2.1";
		final String paramId2 = "param2.2";

		// basic test
		testDeserializeAndSerialize(twoParameterCommandId
				+ "(param2.1=hello,param2.2=goodbye)", twoParameterCommandId, 2,
				new String[] { paramId1, paramId2 }, new String[] { "hello",
						"goodbye" });
		
		// re-order parameters
		testDeserializeAndSerialize(twoParameterCommandId
				+ "(param2.2=goodbye,param2.1=hello)", twoParameterCommandId, 2,
				new String[] { paramId1, paramId2 }, new String[] { "hello",
						"goodbye" });
		
		// parameter values that need escaping
		final String value1Escaped = "hello%(%)%%%=%,";
		final String value2Escaped = "%%%=%(%)%,world";
		testDeserializeAndSerialize(twoParameterCommandId + "(param2.1="
				+ value1Escaped + ",param2.2=" + value2Escaped + ")",
				twoParameterCommandId, 2, new String[] { paramId1, paramId2 },
				new String[] { "hello()%=,", "%=(),world" });
	}
	
	/**
	 * Test serialization of a command with three parameters.
	 * 
	 * @throws CommandException
	 */
	public void testThreeParameterCommand() throws CommandException {
		final String threeParameterCommandId = "org.eclipse.ui.tests.commands.threeParameterCommand";
		final String paramId1 = "param3.1";
		final String paramId2 = "param3.2";
		final String paramId3 = "param3.3";

		// basic test
		testDeserializeAndSerialize(threeParameterCommandId
				+ "(param3.1=foo,param3.2=bar,param3.3=baz)",
				threeParameterCommandId, 3, new String[] { paramId1, paramId2,
						paramId3 }, new String[] { "foo", "bar", "baz" });
		
		// test with a null parameter
		testDeserializeAndSerialize(threeParameterCommandId
				+ "(param3.1,param3.2=bar,param3.3=baz)",
				threeParameterCommandId, 3, new String[] { paramId1, paramId2,
						paramId3 }, new String[] { null, "bar", "baz" });
		
		// test with all null parameters
		testDeserializeAndSerialize(threeParameterCommandId
				+ "(param3.1,param3.2,param3.3)",
				threeParameterCommandId, 3, new String[] { paramId1, paramId2,
						paramId3 }, new String[] { null, null, null });
		
		// test with a missing parameter
		testDeserializeAndSerialize(threeParameterCommandId
				+ "(param3.1=foo,param3.3=baz)", threeParameterCommandId, 2,
				new String[] { paramId1, paramId3 }, new String[] { "foo",
						"baz" });
	}

	
	/**
	 * Test serialization of a command with names that need UTF-8 encoding.
	 * 
	 * @throws CommandException
	 */
	public void testFunnyNamesCommand() throws CommandException {
		final String funnyNamesCommandId = "org.eclipse.ui.tests.command.with.f=%)(,unny.name";
		final String funnyNamesCommandIdEncoded = "org.eclipse.ui.tests.command.with.f%=%%%)%(%,unny.name";

		final String funnyNamesParamId = "param.with.F({'><+|.)=,%.name";
		final String funnyNamesParamIdEncoded = "param.with.F%({'><+|.%)%=%,%%.name";

		final String funnyValue = "= %,.&\n\t\r?*[](){}";
		final String funnyValueEncoded = "%= %%%,.&\n\t\r?*[]%(%){}";

		final String serializedFunnyNamesCommand = funnyNamesCommandIdEncoded
				+ "(" + funnyNamesParamIdEncoded + "=" + funnyValueEncoded + ")";

		// basic test
		testDeserializeAndSerialize(serializedFunnyNamesCommand,
				funnyNamesCommandId, 1, new String[] { funnyNamesParamId },
				new String[] { funnyValue });
	}
	
	public void testMalformedSerializationStrings() {
		// try a missing closing ')'
		expectSerializationException(showPerspectiveCommandId + "(");
		
		// try a bad escape sequence
		expectSerializationException("some.command.foo%bar");
	}
	
	public void testUndefinedCommands() {
		expectNotDefinedException("this.command.ain't.defined(i.hope)");
	}
	
	/**
	 * Test deserializing a stored command and then serializing it back into a
	 * string. The <code>serializedParameterizedCommand</code> may contain
	 * some "bogus" parameters (ones not defined in the command registry for the
	 * command). The <code>paramIds</code> and <code>paramValues</code>
	 * arrays represent only the ids and values of the non-bogus serialized
	 * parameters.
	 * 
	 * @param serializedParameterizedCommand
	 *            a command serialization string
	 * @param commandId
	 *            id of the serialized command
	 * @param serializedParamCount
	 *            number of parameters in the serialization (some may be bogus)
	 * @param paramIds
	 *            parameter ids in the serialization that represent real
	 *            parameters in the command
	 * @param paramValues
	 *            parameter values in the serialization (same number and order
	 *            as paramIds)
	 * @throws CommandException
	 */
	private void testDeserializeAndSerialize(
			String serializedParameterizedCommand, String commandId,
			int serializedParamCount, String[] paramIds, String[] paramValues)
			throws CommandException {
		
		ICommandService commandService = getCommandService();
		
		int realParamCount = (paramIds == null) ? 0 : paramIds.length;

		// first convert the serialized string to a ParameterizedCommand and
		// check the parts
		ParameterizedCommand pCommand = commandService
				.deserialize(serializedParameterizedCommand);
		assertNotNull(pCommand);
		assertEquals(commandId, pCommand.getId());

		Map paramMap = pCommand.getParameterMap();
		assertEquals(realParamCount, paramMap.size());

		if (paramIds != null) {
			for (int i = 0; i < realParamCount; i++) {
				assertTrue(paramMap.containsKey(paramIds[i]));
				assertEquals(paramValues[i], paramMap.get(paramIds[i]));
			}
		}

		// now convert the ParameterizedCommand back to a serialized string
		String serialization = pCommand.serialize();
		
		if ((realParamCount == serializedParamCount) && (realParamCount < 2)) {
			if ((realParamCount == 0)
					&& (serializedParameterizedCommand.endsWith("()"))) {
				// empty "()" is ok, but the re-serialization won't have it
				// so add it back for comparison....
				assertEquals(serializedParameterizedCommand, serialization
						+ "()");
			} else {
				assertEquals(serializedParameterizedCommand, serialization);
			}
		} else {
			// params may have been re-ordered so we can't compare
		}
		
		// deserialize again and use .equals() on the ParameterizedCommands
		ParameterizedCommand pCommand2 = commandService.deserialize(serialization);
		assertEquals(pCommand, pCommand2);
	}
	
	private void expectSerializationException(String serializedParameterizedCommand) {
		ICommandService commandService = getCommandService();
		
		try {
			commandService.deserialize(serializedParameterizedCommand);
			fail("expected SerializationException");
		} catch (SerializationException ex) {
			// passed
		} catch (NotDefinedException ex) {
			fail("expected SerializationException");
		}
	}
	
	private void expectNotDefinedException(String serializedParameterizedCommand) {
		ICommandService commandService = getCommandService();
		
		try {
			commandService.deserialize(serializedParameterizedCommand);
			fail("expected NotDefinedException");
		} catch (SerializationException ex) {
			fail("expected NotDefinedException");
		} catch (NotDefinedException ex) {
			// passed
		}
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
