/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.dynamicplugins;

import static org.junit.Assert.assertThrows;

import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests whether the "org.eclipse.ui.actionDefinitions" extension point can be
 * added and removed dynamically.
 *
 * @since 3.1.1
 */
@RunWith(JUnit4.class)
public final class ActionDefinitionsExtensionDynamicTest extends
		DynamicTestCase {

	/**
	 * Constructs a new instance of
	 * <code>ActionDefinitionsExtensionDynamicTest</code>.
	 */
	public ActionDefinitionsExtensionDynamicTest() {
		super(ActionDefinitionsExtensionDynamicTest.class.getSimpleName());
	}

	/**
	 * Returns the full-qualified identifier of the extension to be tested.
	 *
	 * @return The extension identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionId() {
		return "actionDefinitionsExtensionDynamicTest.testDynamicActionDefinitionAddition";
	}

	/**
	 * Returns the unqualified identifier of the extension point to be tested.
	 *
	 * @return The extension point identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_ACTION_DEFINITIONS;
	}

	/**
	 * Returns the relative location of the folder on disk containing the
	 * plugin.xml file.
	 *
	 * @return The relative install location; never <code>null</code>.
	 */
	@Override
	protected final String getInstallLocation() {
		return "data/org.eclipse.actionDefinitionsExtensionDynamicTest";
	}

	/**
	 * Tests whether the items defined in the extension point can be added and
	 * removed dynamically. It tests that the data doesn't exist, and then loads the
	 * extension. It tests that the data then exists, and unloads the extension. It
	 * tests that the data then doesn't exist.
	 *
	 * @throws NotDefinedException
	 */
	@Test
	public final void testActionDefinitions() throws NotDefinedException {
		final ICommandService service = getWorkbench().getAdapter(ICommandService.class);

		NamedHandleObject namedHandleObject1 = service.getCommand("monkey");
		assertThrows(NotDefinedException.class, () -> namedHandleObject1.getName());

		getBundle();

		NamedHandleObject namedHandleObject2 = service.getCommand("monkey");
		assertTrue("Monkey".equals(namedHandleObject2.getName()));

		removeBundle();

		NamedHandleObject namedHandleObject3 = service.getCommand("monkey");
		assertThrows(NotDefinedException.class, () -> namedHandleObject3.getName());
	}
}
