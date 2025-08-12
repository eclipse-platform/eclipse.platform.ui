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

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests whether the "org.eclipse.ui.handlers" extension point can be added and
 * removed dynamically.
 *
 * @since 3.1.1
 */
@RunWith(JUnit4.class)
public final class HandlersExtensionDynamicTest extends DynamicTestCase {

	/**
	 * Constructs a new instance of <code>HandlersExtensionDynamicTest</code>.
	 */
	public HandlersExtensionDynamicTest() {
		super(HandlersExtensionDynamicTest.class.getSimpleName());
	}

	/**
	 * Returns the full-qualified identifier of the extension to be tested.
	 *
	 * @return The extension identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionId() {
		return "handlersExtensionDynamicTest.testDynamicHandlerAddition";
	}

	/**
	 * Returns the unqualified identifier of the extension point to be tested.
	 *
	 * @return The extension point identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_HANDLERS;
	}

	/**
	 * Returns the relative location of the folder on disk containing the
	 * plugin.xml file.
	 *
	 * @return The relative install location; never <code>null</code>.
	 */
	@Override
	protected final String getInstallLocation() {
		return "data/org.eclipse.handlersExtensionDynamicTest";
	}

	/**
	 * Tests whether the items defined in the extension point can be added and
	 * removed dynamically. It tests that the data doesn't exist, and then loads the
	 * extension. It tests that the data then exists, and unloads the extension. It
	 * tests that the data then doesn't exist.
	 *
	 * @throws NotHandledException
	 * @throws ExecutionException
	 */
	@Test
	public final void testHandlers() throws ExecutionException, NotHandledException {
		final ICommandService commandService = getWorkbench().getAdapter(ICommandService.class);

		Command command1 = commandService.getCommand("monkey");
		assertThrows(NotHandledException.class, () -> command1.execute(new ExecutionEvent()));

		getBundle();

		Command command2 = commandService.getCommand("monkey");
		command2.execute(new ExecutionEvent());

		removeBundle();

		Command command3 = commandService.getCommand("monkey");
		assertThrows(NotHandledException.class, () -> command3.execute(new ExecutionEvent()));
	}
}
