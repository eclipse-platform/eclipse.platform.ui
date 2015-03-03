/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

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

/**
 * Tests whether the "org.eclipse.ui.commands" extension point can be added and
 * removed dynamically.
 *
 * @since 3.1.1
 */
public final class CommandsExtensionDynamicTest extends DynamicTestCase {

	/**
	 * Constructs a new instance of <code>CommandsExtensionDynamicTest</code>.
	 *
	 * @param testName
	 *            The name of the test; may be <code>null</code>.
	 */
	public CommandsExtensionDynamicTest(final String testName) {
		super(testName);
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
	 * removed dynamically. It tests that the data doesn't exist, and then loads
	 * the extension. It tests that the data then exists, and unloads the
	 * extension. It tests that the data then doesn't exist.
	 *
	 * @throws ParseException
	 *             If "M1+W" can't be parsed by the extension point.
	 */
	public final void testCommands() throws ParseException {
		final IBindingService bindingService = getWorkbench().getAdapter(IBindingService.class);
		final ICommandService commandService = getWorkbench().getAdapter(ICommandService.class);
		final IContextService contextService = getWorkbench().getAdapter(IContextService.class);
		final TriggerSequence triggerSequence = KeySequence.getInstance("M1+W");
		NamedHandleObject namedHandleObject;
		Binding[] bindings;
		Command command;
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
		namedHandleObject = bindingService.getScheme("monkey");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
		namedHandleObject = commandService.getCategory("monkey");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
		command = commandService.getCommand("monkey");
		try {
			command.execute(new ExecutionEvent());
			fail();
		} catch (final ExecutionException e) {
			fail();
		} catch (final NotHandledException e) {
			assertTrue(true);
		}
		try {
			command.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
		namedHandleObject = contextService.getContext("context");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
		namedHandleObject = contextService.getContext("scope");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}

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
		namedHandleObject = bindingService.getScheme("monkey");
		try {
			assertTrue("Monkey".equals(namedHandleObject.getName()));
		} catch (final NotDefinedException e) {
			fail();
		}
		command = commandService.getCommand("monkey");
		try {
			command.execute(new ExecutionEvent());
		} catch (final ExecutionException e) {
			fail();
		} catch (final NotHandledException e) {
			fail();
		}
		try {
			assertEquals("Monkey", command.getName());
		} catch (final NotDefinedException e) {
			fail();
		}
		namedHandleObject = commandService.getCommand("monkey");
		try {
			assertTrue("Monkey".equals(namedHandleObject.getName()));
		} catch (final NotDefinedException e) {
			fail();
		}
		namedHandleObject = contextService.getContext("context");
		try {
			assertTrue("Monkey".equals(namedHandleObject.getName()));
		} catch (final NotDefinedException e) {
			fail();
		}
		namedHandleObject = contextService.getContext("scope");
		try {
			assertTrue("Monkey".equals(namedHandleObject.getName()));
		} catch (final NotDefinedException e) {
			fail();
		}

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
		namedHandleObject = bindingService.getScheme("monkey");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
		namedHandleObject = commandService.getCategory("monkey");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
		command = commandService.getCommand("monkey");
		try {
			command.execute(new ExecutionEvent());
			fail();
		} catch (final ExecutionException e) {
			fail();
		} catch (final NotHandledException e) {
			assertTrue(true);
		}
		try {
			command.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
		namedHandleObject = contextService.getContext("context");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
		namedHandleObject = contextService.getContext("scope");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
	}

	public void testNonExistingHandler() {
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		getBundle();

		// till the handler is loaded, we assume it could be handled
		// when its time to execute, we load the handler and throw the
		// ExecutionException
		try {
			handlerService.executeCommand(
					"org.eclipse.ui.tests.command.handlerLoadException", null);
			fail("An exception should be thrown for this handler");
		} catch (Exception e) {
			if (!(e instanceof ExecutionException)) {
				fail("Unexpected exception while executing command", e);
			}
		}

		// afterwards, we know that the handler couldn't be loaded, so it can't
		// be handled
		// from now we always throw NotHandledException
		try {
			handlerService.executeCommand(
					"org.eclipse.ui.tests.command.handlerLoadException", null);
			fail("An exception should be thrown for this handler");
		} catch (Exception e) {
			if (!(e instanceof NotHandledException)) {
				fail("Unexpected exception while executing command", e);
			}
		}

		removeBundle();
	}

}
