/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 431667, 440893
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 450209
 *******************************************************************************/
package org.eclipse.e4.core.commands.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.FrameworkUtil;

public class DefineCommandsTest {

	private static final String TEST_ID2 = "test.id2";
	private static final String TEST_ID1 = "test.id1";
	private static final String TEST_ID1_WITH_PARAMETERS = "test.id1.with.parameters";
	private static final String TEST_CAT1 = "test.cat1";

	private IEclipseContext workbenchContext;

	@BeforeEach
	public void setUp() {
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(this.getClass()).getBundleContext());
		IEclipseContext globalContext = serviceContext.createChild();
		workbenchContext = globalContext.createChild("workbenchContext");
		ContextInjectionFactory.make(CommandServiceAddon.class, workbenchContext);
	}

	@AfterEach
	public void tearDown() {
		workbenchContext.dispose();
	}

	@Test
	public void testCreateCommands() throws NotDefinedException {
		ECommandService cs = workbenchContext.get(ECommandService.class);
		assertNotNull(cs);
		assertNotNull(cs.defineCategory(TEST_CAT1, "CAT1", null));
		Category category = cs.getCategory(TEST_CAT1);
		assertNotNull(category, "need category");
		assertNotNull(cs.defineCommand(TEST_ID1, "ID1", null, category, null), "command1");
		assertNotNull(cs.defineCommand(TEST_ID2, "ID2", null, category, null), "command2");
		Command cmd1 = cs.getCommand(TEST_ID1);
		assertNotNull(cmd1, "get command1");
		assertEquals("ID1", cmd1.getName());
		assertNotNull(cs.getCommand(TEST_ID2), "get command2");
		assertNotNull(cs.createCommand(TEST_ID1, null), "parameterized command");
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
		assertNotNull(cmd1, "get command1");
	}




	@Test
	public void testCreateWithSecondContexts() throws NotDefinedException {
		IEclipseContext localContext = workbenchContext.createChild();
		ECommandService cs = localContext.get(ECommandService.class);
		assertNotNull(cs);
		assertNotNull(cs.defineCategory(TEST_CAT1, "CAT1", null));
		Category category = cs.getCategory(TEST_CAT1);
		assertNotNull(category, "need category");
		assertNotNull(cs.defineCommand(TEST_ID1, "ID1", null, category, null), "command1");
		assertNotNull(cs.defineCommand(TEST_ID2, "ID2", null, category, null), "command2");

		Command cmd1 = cs.getCommand(TEST_ID1);
		assertNotNull(cmd1, "get command1");
		assertEquals("ID1", cmd1.getName());
		assertNotNull(cs.getCommand(TEST_ID2), "get command2");
	}

	@Test
	public void testCreateWithTwoContexts() throws NotDefinedException {
		IEclipseContext localContext = workbenchContext.createChild("Level1");
		ECommandService cs = localContext.get(ECommandService.class);
		assertNotNull(cs);
		assertNotNull(cs.defineCategory(TEST_CAT1, "CAT1", null));
		Category category = cs.getCategory(TEST_CAT1);
		assertNotNull(category, "need category");
		assertNotNull(cs.defineCommand(TEST_ID1, "ID1", null, category, null), "command1");
		assertNotNull(cs.defineCommand(TEST_ID2, "ID2", null, category, null), "command2");

		cs = workbenchContext.get(ECommandService.class);
		Command cmd1 = cs.getCommand(TEST_ID1);
		assertNotNull(cmd1, "get command1");
		assertEquals("ID1", cmd1.getName());
		assertNotNull(cs.getCommand(TEST_ID2), "get command2");
	}
}
