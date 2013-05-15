/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.commands.tests;

import junit.framework.TestCase;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

public class DefineCommandsTest extends TestCase {

	private static final String TEST_ID2 = "test.id2";
	private static final String TEST_ID1 = "test.id1";
	private static final String TEST_CAT1 = "test.cat1";

	public void testCreateCommands() throws Exception {
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		assertNotNull(cs);
		assertNotNull(cs.defineCategory(TEST_CAT1, "CAT1", null));
		Category category = cs.getCategory(TEST_CAT1);
		assertNotNull("need category", category);
		assertNotNull("command1", cs.defineCommand(TEST_ID1, "ID1", null,
				category, null));
		assertNotNull("command2", cs.defineCommand(TEST_ID2, "ID2", null,
				category, null));

		Command cmd1 = cs.getCommand(TEST_ID1);
		assertNotNull("get command1", cmd1);
		assertEquals("ID1", cmd1.getName());
		assertNotNull("get command2", cs.getCommand(TEST_ID2));
		
		assertNotNull("parameterized command", cs.createCommand(TEST_ID1, null));
	}

	public void testCreateWithSecondContexts() throws Exception {
		IEclipseContext localContext = workbenchContext.createChild();
		ECommandService cs = (ECommandService) localContext
				.get(ECommandService.class.getName());
		assertNotNull(cs);
		assertNotNull(cs.defineCategory(TEST_CAT1, "CAT1", null));
		Category category = cs.getCategory(TEST_CAT1);
		assertNotNull("need category", category);
		assertNotNull("command1", cs.defineCommand(TEST_ID1, "ID1", null,
				category, null));
		assertNotNull("command2", cs.defineCommand(TEST_ID2, "ID2", null,
				category, null));

		Command cmd1 = cs.getCommand(TEST_ID1);
		assertNotNull("get command1", cmd1);
		assertEquals("ID1", cmd1.getName());
		assertNotNull("get command2", cs.getCommand(TEST_ID2));
	}

	public void testCreateWithTwoContexts() throws Exception {
		IEclipseContext localContext = workbenchContext.createChild("Level1");
		ECommandService cs = (ECommandService) localContext
				.get(ECommandService.class.getName());
		assertNotNull(cs);
		assertNotNull(cs.defineCategory(TEST_CAT1, "CAT1", null));
		Category category = cs.getCategory(TEST_CAT1);
		assertNotNull("need category", category);
		assertNotNull("command1", cs.defineCommand(TEST_ID1, "ID1", null,
				category, null));
		assertNotNull("command2", cs.defineCommand(TEST_ID2, "ID2", null,
				category, null));

		cs = (ECommandService) workbenchContext.get(ECommandService.class
				.getName());
		Command cmd1 = cs.getCommand(TEST_ID1);
		assertNotNull("get command1", cmd1);
		assertEquals("ID1", cmd1.getName());
		assertNotNull("get command2", cs.getCommand(TEST_ID2));
	}

	private IEclipseContext workbenchContext;

	@Override
	protected void setUp() throws Exception {
		IEclipseContext globalContext = TestActivator.getDefault().getGlobalContext();
		workbenchContext = globalContext.createChild("workbenchContext");
		ContextInjectionFactory.make(CommandServiceAddon.class, workbenchContext);
	}

	@Override
	protected void tearDown() throws Exception {
		workbenchContext.dispose();
	}
}
