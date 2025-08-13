/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.keys.IBindingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests whether the "org.eclipse.ui.commands" extension point can be added and
 * removed dynamically.
 *
 * @since 3.1.1
 */
@RunWith(JUnit4.class)
public final class CommandsExtensionDynamicTest extends DynamicTestCase {

	/**
	 * Constructs a new instance of <code>CommandsExtensionDynamicTest</code>.
	 */
	public CommandsExtensionDynamicTest() {
		super(CommandsExtensionDynamicTest.class.getSimpleName());
	}

	/**
	 * Returns the full-qualified identifier of the extension to be tested.
	 *
	 * @return The extension identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionId() {
		return "commandsExtensionDynamicTest.testDynamicCommandAddition";
	}

	/**
	 * Returns the unqualified identifier of the extension point to be tested.
	 *
	 * @return The extension point identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_COMMANDS;
	}

	/**
	 * Returns the relative location of the folder on disk containing the
	 * plugin.xml file.
	 *
	 * @return The relative install location; never <code>null</code>.
	 */
	@Override
	protected final String getInstallLocation() {
		return "data/org.eclipse.commandsExtensionDynamicTest";
	}

	/**
	 * Tests whether the items defined in the extension point can be added and
	 * removed dynamically. It tests that the data doesn't exist, and then loads the
	 * extension. It tests that the data then exists, and unloads the extension. It
	 * tests that the data then doesn't exist.
	 *
	 * @throws ParseException      If "M1+W" can't be parsed by the extension point.
	 * @throws NotDefinedException
	 * @throws NotHandledException
	 * @throws ExecutionException
	 */
	@Test
	public final void testCommands()
			throws ParseException, NotDefinedException, ExecutionException, NotHandledException {
		final IBindingService bindingService = getWorkbench().getAdapter(IBindingService.class);
		final ICommandService commandService = getWorkbench().getAdapter(ICommandService.class);
		final IContextService contextService = getWorkbench().getAdapter(IContextService.class);
		final TriggerSequence triggerSequence = KeySequence.getInstance("M1+W");
		Binding[] bindings;
		boolean found;

		assertTrue(!"monkey".equals(bindingService.getActiveScheme().getId()));
		found = false;
		bindings = bindingService.getBindings();
		if (bindings != null) {
			for (final Binding binding : bindings) {
				if ("monkey".equals(binding.getSchemeId())
						&& IContextIds.CONTEXT_ID_WINDOW.equals(binding
								.getContextId())
						&& "monkey".equals(binding.getParameterizedCommand()
								.getId()) && binding.getPlatform() == null
						&& binding.getLocale() == null
						&& binding.getType() == Binding.SYSTEM
						&& triggerSequence.equals(binding.getTriggerSequence())) {
					found = true;

				}
			}
		}
		assertTrue(!found);
		NamedHandleObject bindingMonkey1 = bindingService.getScheme("monkey");
		assertThrows(NotDefinedException.class, () -> bindingMonkey1.getName());

		NamedHandleObject categoryMonkey1 = commandService.getCategory("monkey");
		assertThrows(NotDefinedException.class, () -> categoryMonkey1.getName());

		Command command1 = commandService.getCommand("monkey");
		assertThrows(NotHandledException.class, () -> command1.execute(new ExecutionEvent()));
		assertThrows(NotDefinedException.class, () -> command1.getName());

		NamedHandleObject contextContext1 = contextService.getContext("context");
		assertThrows(NotDefinedException.class, () -> contextContext1.getName());
		NamedHandleObject contextScope1 = contextService.getContext("scope");
		assertThrows(NotDefinedException.class, () -> contextScope1.getName());

		getBundle();

		assertTrue("monkey".equals(bindingService.getActiveScheme().getId()));
		found = false;
		bindings = bindingService.getBindings();
		if (bindings != null) {
			for (final Binding binding : bindings) {
				if ("monkey".equals(binding.getSchemeId())
						&& IContextIds.CONTEXT_ID_WINDOW.equals(binding
								.getContextId())
						&& "monkey".equals(binding.getParameterizedCommand()
								.getId()) && binding.getPlatform() == null
						&& binding.getLocale() == null
						&& binding.getType() == Binding.SYSTEM
						&& triggerSequence.equals(binding.getTriggerSequence())) {
					found = true;

				}
			}
		}
		assertTrue(found);
		NamedHandleObject bindingMonkey2 = bindingService.getScheme("monkey");
		assertTrue("Monkey".equals(bindingMonkey2.getName()));
		Command command2 = commandService.getCommand("monkey");
		command2.execute(new ExecutionEvent());
		assertEquals("Monkey", command2.getName());
		NamedHandleObject commandMonkey2 = commandService.getCommand("monkey");
		assertTrue("Monkey".equals(commandMonkey2.getName()));
		NamedHandleObject contextContext2 = contextService.getContext("context");
		assertTrue("Monkey".equals(contextContext2.getName()));
		NamedHandleObject contextScope2 = contextService.getContext("scope");
		assertTrue("Monkey".equals(contextScope2.getName()));

		removeBundle();

		assertTrue(!"monkey".equals(bindingService.getActiveScheme().getId()));
		found = false;
		bindings = bindingService.getBindings();
		if (bindings != null) {
			for (final Binding binding : bindings) {
				if ("monkey".equals(binding.getSchemeId())
						&& IContextIds.CONTEXT_ID_WINDOW.equals(binding
								.getContextId())
						&& "monkey".equals(binding.getParameterizedCommand()
								.getId()) && binding.getPlatform() == null
						&& binding.getLocale() == null
						&& binding.getType() == Binding.SYSTEM
						&& triggerSequence.equals(binding.getTriggerSequence())) {
					found = true;

				}
			}
		}
		assertTrue(!found);
		NamedHandleObject bindingMonkey3 = bindingService.getScheme("monkey");
		assertThrows(NotDefinedException.class, () -> bindingMonkey3.getName());
		NamedHandleObject commandMonkey3 = commandService.getCommand("monkey");
		assertThrows(NotDefinedException.class, () -> commandMonkey3.getName());
		Command command3 = commandService.getCommand("monkey");
		assertThrows(NotHandledException.class, () -> command3.execute(new ExecutionEvent()));
		assertThrows(NotDefinedException.class, () -> command3.getName());
		NamedHandleObject contextContext3 = contextService.getContext("context");
		assertThrows(NotDefinedException.class, () -> contextContext3.getName());
		NamedHandleObject contextScope3 = contextService.getContext("scope");
		assertThrows(NotDefinedException.class, () -> contextScope3.getName());
	}

	@Test
	public void testNonExistingHandler() {
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		getBundle();

		// till the handler is loaded, we assume it could be handled
		// when its time to execute, we load the handler and throw the
		// ExecutionException
		assertThrows(ExecutionException.class,
				() -> handlerService.executeCommand("org.eclipse.ui.tests.command.handlerLoadException", null));

		// afterwards, we know that the handler couldn't be loaded, so it can't
		// be handled
		// from now we always throw NotHandledException
		assertThrows(NotHandledException.class,
				() -> handlerService.executeCommand("org.eclipse.ui.tests.command.handlerLoadException", null));

		removeBundle();
	}

}
