/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.contexts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.contexts.NotDefinedException;
import org.junit.Test;

/**
 * The test case for reading in elements from the extension point. This verifies
 * that contexts defined in this test plug-in's extensions are all properly read
 * in. This includes extensions read from deprecated locations, as well as the
 * currently preferred way of specifying contexts.
 *
 * @since 3.0
 */
public class ExtensionTestCase {

	/**
	 * Tests that the "org.eclipse.ui.acceleratorScopes" extension point can be
	 * read in by Eclipse. This extension point is currently deprecated.
	 *
	 * @throws NotDefinedException
	 *             This shouldn't really be possible, as the test should fail
	 *             gracefully before this could happen.
	 */
	@Test
	public final void testAcceleratorScopes() throws NotDefinedException {
		final IWorkbenchContextSupport contextSupport = PlatformUI.getWorkbench()
				.getContextSupport();
		final IContextManager contextManager = contextSupport
				.getContextManager();

		final IContext context1 = contextManager
				.getContext("org.eclipse.ui.tests.acceleratorScopes.test1");
		assertTrue(
				"Context contributed via 'org.eclipse.ui.acceleratorScopes' is not loaded properly.",
				context1.isDefined());
		assertEquals(
				"Context contributed via 'org.eclipse.ui.acceleratorScopes' does not get its name.",
				"Test Accelerator Scope 1", context1.getName());

		final IContext context2 = contextManager
				.getContext("org.eclipse.ui.tests.acceleratorScopes.test2");
		assertTrue(
				"Context contributed via 'org.eclipse.ui.acceleratorScopes' is not loaded properly.",
				context2.isDefined());
		assertEquals(
				"Context contributed via 'org.eclipse.ui.acceleratorScopes' does not get its name.",
				"Test Accelerator Scope 2", context2.getName());
		assertEquals(
				"Context contributed via 'org.eclipse.ui.acceleratorScopes' does not get its parent.",
				"org.eclipse.ui.tests.acceleratorScopes.test1", context2
						.getParentId());
	}

	/**
	 * Tests that the "scopes" element in the "org.eclipse.ui.commands"
	 * extension point can be read in as a context by Eclipse. This element is
	 * currently deprecated.
	 *
	 * @throws NotDefinedException
	 *             This shouldn't really be possible, as the test should fail
	 *             gracefully before this could happen.
	 */
	@Test
	public final void testCommandsScopes() throws NotDefinedException {
		final IWorkbenchContextSupport contextSupport = PlatformUI.getWorkbench()
				.getContextSupport();
		final IContextManager contextManager = contextSupport
				.getContextManager();

		final IContext context1 = contextManager
				.getContext("org.eclipse.ui.tests.commands.scope1");
		assertTrue(
				"Context contributed via 'org.eclipse.ui.commands' is not loaded properly.",
				context1.isDefined());
		assertEquals(
				"Context contributed via 'org.eclipse.ui.commands' does not get its name.",
				"Test Scope 1", context1.getName());

		final IContext context2 = contextManager
				.getContext("org.eclipse.ui.tests.commands.scope2");
		assertTrue(
				"Context contributed via 'org.eclipse.ui.commands' is not loaded properly.",
				context2.isDefined());
		assertEquals(
				"Context contributed via 'org.eclipse.ui.commands' does not get its name.",
				"Test Scope 2", context2.getName());
		assertEquals(
				"Context contributed via 'org.eclipse.ui.commands' does not get its parent.",
				"org.eclipse.ui.tests.commands.scope1", context2.getParentId());
	}

	/**
	 * Tests that the currently preferred way of specifiying contexts can be
	 * read in properly by Eclipse. This uses all of the non-deprecated
	 * attributes.
	 *
	 * @throws NotDefinedException
	 *             This shouldn't really be possible, as the test should fail
	 *             gracefully before this could happen.
	 */
	@Test
	public final void testContexts() throws NotDefinedException {
		final IWorkbenchContextSupport contextSupport = PlatformUI.getWorkbench()
				.getContextSupport();
		final IContextManager contextManager = contextSupport
				.getContextManager();

		final IContext context1 = contextManager
				.getContext("org.eclipse.ui.tests.contexts.context1");
		assertTrue(
				"Context contributed via 'org.eclipse.ui.contexts' is not loaded properly.",
				context1.isDefined());
		assertEquals(
				"Context contributed via 'org.eclipse.ui.contexts' does not get its name.",
				"Test Context 1", context1.getName());

		final IContext context2 = contextManager
				.getContext("org.eclipse.ui.tests.contexts.context2");
		assertTrue(
				"Context contributed via 'org.eclipse.ui.contexts' is not loaded properly.",
				context2.isDefined());
		assertEquals(
				"Context contributed via 'org.eclipse.ui.contexts' does not get its name.",
				"Test Context 2", context2.getName());
		assertEquals(
				"Context contributed via 'org.eclipse.ui.contexts' does not get its parent.",
				"org.eclipse.ui.tests.contexts.context1", context2
						.getParentId());
	}
}
