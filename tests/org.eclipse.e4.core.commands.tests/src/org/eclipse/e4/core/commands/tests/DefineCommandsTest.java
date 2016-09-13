/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 431667, 440893
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 450209
 *******************************************************************************/
package org.eclipse.e4.core.commands.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefineCommandsTest {

	private static final String TEST_ID2 = "test.id2";
	private static final String TEST_ID1 = "test.id1";
	private static final String TEST_ID1_WITH_PARAMETERS = "test.id1.with.parameters";
	private static final String TEST_CAT1 = "test.cat1";

	private IEclipseContext workbenchContext;

	@Before
	public void setUp() {
		IEclipseContext globalContext = TestActivator.getDefault().getGlobalContext();
		workbenchContext = globalContext.createChild("workbenchContext");
		ContextInjectionFactory.make(CommandServiceAddon.class, workbenchContext);
	}

	@After
	public void tearDown() {
		workbenchContext.dispose();
	}

	@Test
	public void testCreateCommands() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		assertNotNull(cs);
		assertNotNull(cs.defineCategory(TEST_CAT1, "CAT1", null));
		Category category = cs.getCategory(TEST_CAT1);
		assertNotNull("need category", category);
		assertNotNull("command1", cs.defineCommand(TEST_ID1, "ID1", null, category, null));
		assertNotNull("command2", cs.defineCommand(TEST_ID2, "ID2", null, category, null));
		Command cmd1 = cs.getCommand(TEST_ID1);
		assertNotNull("get command1", cmd1);
		try {
			assertEquals("ID1", cmd1.getName());
		} catch (NotDefinedException e) {
			fail(e.getMessage());
		}
		assertNotNull("get command2", cs.getCommand(TEST_ID2));
		assertNotNull("parameterized command", cs.createCommand(TEST_ID1, null));
	}

	@Test
	public void testParamizedCommandsSimple() {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		IParameter[] parms = new IParameter[1];
		 parms[0] = new IParameter() {
			@Override
			public String getId() {
				return "viewId";
			}

			@Override
			public String getName() {
				return "View Id";
			}

			@Override
			public IParameterValues getValues() {
				return null;
			}

			@Override
			public boolean isOptional() {
				return false;
			}
		};
		// command needs to be defined
		Category defineCategory = cs.defineCategory(TEST_CAT1, "CAT1", null);
		Command command = cs.defineCommand(TEST_ID1_WITH_PARAMETERS, "TEST_ID1_WITH_PARAMETERS", null, defineCategory, parms);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("viewId", "Testing");
		// afterwards it is possible to create a ParameterizedCommand
		ParameterizedCommand createdParamizedCommand = cs.createCommand(TEST_ID1_WITH_PARAMETERS, parameters);
		assertNotNull(command);
		assertNotNull(createdParamizedCommand);
		Command cmd1 = cs.getCommand(TEST_ID1_WITH_PARAMETERS);
		assertNotNull("get command1", cmd1);
	}




	@Test
	public void testCreateWithSecondContexts() {
		IEclipseContext localContext = workbenchContext.createChild();
		ECommandService cs = localContext.get(ECommandService.class);
		assertNotNull(cs);
		assertNotNull(cs.defineCategory(TEST_CAT1, "CAT1", null));
		Category category = cs.getCategory(TEST_CAT1);
		assertNotNull("need category", category);
		assertNotNull("command1", cs.defineCommand(TEST_ID1, "ID1", null, category, null));
		assertNotNull("command2", cs.defineCommand(TEST_ID2, "ID2", null, category, null));

		Command cmd1 = cs.getCommand(TEST_ID1);
		assertNotNull("get command1", cmd1);
		try {
			assertEquals("ID1", cmd1.getName());
		} catch (NotDefinedException e) {
			fail(e.getMessage());
		}
		assertNotNull("get command2", cs.getCommand(TEST_ID2));
	}

	@Test
	public void testCreateWithTwoContexts() {
		IEclipseContext localContext = workbenchContext.createChild("Level1");
		ECommandService cs = localContext.get(ECommandService.class);
		assertNotNull(cs);
		assertNotNull(cs.defineCategory(TEST_CAT1, "CAT1", null));
		Category category = cs.getCategory(TEST_CAT1);
		assertNotNull("need category", category);
		assertNotNull("command1", cs.defineCommand(TEST_ID1, "ID1", null, category, null));
		assertNotNull("command2", cs.defineCommand(TEST_ID2, "ID2", null, category, null));

		cs = workbenchContext.get(ECommandService.class);
		Command cmd1 = cs.getCommand(TEST_ID1);
		assertNotNull("get command1", cmd1);
		try {
			assertEquals("ID1", cmd1.getName());
		} catch (NotDefinedException e) {
			fail(e.getMessage());
		}
		assertNotNull("get command2", cs.getCommand(TEST_ID2));
	}
}
