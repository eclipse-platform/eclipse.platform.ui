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

package org.eclipse.ui.tests.contexts;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.contexts.ContextManagerEvent;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * A test case covering the scenario described in Bug 84763. The problem was
 * that the context manager was exposing its internal data structures, and the
 * binding manager was mangling them. Debug then responded to bad information in
 * the <code>previouslyEnabledContextIds</code> property on the context event.
 *
 * @since 3.1
 */
public final class Bug84763Test extends UITestCase {

	/**
	 * The binding manager to use in each test case. A new binding manager is
	 * created for each test case, and it is disposed when the test is over.
	 */
	private BindingManager bindingManager = null;

	/**
	 * The context manager to use in each test case. A new context manager is
	 * created for each test case, and it is disposed when the test is over.
	 */
	private ContextManager contextManager = null;

	/**
	 * The context manager listener to use in each test case. A new context
	 * manager listener is created for each test case, and it is disposed when
	 * the test is over.
	 */
	private IContextManagerListener contextManagerListener = null;

	/**
	 * The set of the previous context identifiers returned by the last context
	 * manager event. This value is set to <code>null</code> at the end of
	 * each test.
	 */
	private Set previousContextIds = null;

	/**
	 * Constructor for <code>Bug84763Test</code>.
	 *
	 * @param name
	 *            The name of the test
	 */
	public Bug84763Test(final String name) {
		super(name);
	}

	/**
	 * Creates a new context manager and a binding manager for use in the test
	 * cases.
	 */
	@Override
	protected void doSetUp() {
		contextManager = new ContextManager();
		contextManagerListener = new IContextManagerListener() {

			@Override
			public void contextManagerChanged(
					ContextManagerEvent contextManagerEvent) {
				previousContextIds = contextManagerEvent
						.getPreviouslyActiveContextIds();
				if (previousContextIds != null) {
					previousContextIds = new HashSet(previousContextIds);
				}
			}

		};
		contextManager.addContextManagerListener(contextManagerListener);
		bindingManager = new BindingManager(contextManager,
				new CommandManager());
	}

	/**
	 * Releases the context manager and binding manager for garbage collection.
	 */
	@Override
	protected void doTearDown() {
		contextManager = null;
		contextManagerListener = null;
		previousContextIds = null;
		bindingManager = null;
	}

	/**
	 * <p>
	 * Testst whether the binding manager will overwrite information in the
	 * context manager. In particular, whether the list of previous enabled
	 * context identifiers will be changed.
	 * </p>
	 *
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 * @throws ParseException
	 *             If "CTRL+F" cannot be parsed for some reason.
	 */
	public void testWindowChildWhenDialog() throws NotDefinedException,
			ParseException {
		// Define the contexts to use.
		final Context dialogAndWindowsContext = contextManager
				.getContext(IContextIds.CONTEXT_ID_DIALOG_AND_WINDOW);
		dialogAndWindowsContext.define("In Dialogs and Windows", null, null);
		final Context dialogContext = contextManager
				.getContext(IContextIds.CONTEXT_ID_DIALOG);
		dialogContext.define("In Dialogs", null,
				IContextIds.CONTEXT_ID_DIALOG_AND_WINDOW);
		final Context windowContext = contextManager
				.getContext(IContextIds.CONTEXT_ID_WINDOW);
		windowContext.define("In Windows", null,
				IContextIds.CONTEXT_ID_DIALOG_AND_WINDOW);
		final Context windowChildContext = contextManager.getContext("sibling");
		windowChildContext.define("Sibling", null,
				IContextIds.CONTEXT_ID_WINDOW);

		// Force a binding computation.
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", null, null);
		bindingManager.setActiveScheme(scheme);
		final CommandManager commandManager = new CommandManager();
		final Command command = commandManager.getCommand("commandId");
		final ParameterizedCommand parameterizedCommand = new ParameterizedCommand(
				command, null);
		bindingManager.addBinding(new KeyBinding(KeySequence
				.getInstance("CTRL+F"), parameterizedCommand, scheme.getId(),
				windowChildContext.getId(), null, null, null, Binding.SYSTEM));
		bindingManager.getActiveBindingsFor((ParameterizedCommand) null);

		// Activate the dialog context and the sibling.
		final Set activeContextIds = new HashSet();
		activeContextIds.add(IContextIds.CONTEXT_ID_DIALOG);
		activeContextIds.add(IContextIds.CONTEXT_ID_DIALOG_AND_WINDOW);
		activeContextIds.add(windowChildContext.getId());
		contextManager.setActiveContextIds(activeContextIds);

		// Force a binding computation.
		bindingManager.getActiveBindingsFor((ParameterizedCommand) null);

		// Active the window context.
		activeContextIds.remove(IContextIds.CONTEXT_ID_DIALOG);
		activeContextIds.add(IContextIds.CONTEXT_ID_WINDOW);
		contextManager.setActiveContextIds(activeContextIds);

		// Force a binding computation.
		bindingManager.getActiveBindingsFor((ParameterizedCommand) null);

		/*
		 * Check to see what the listener got as the list of previously active
		 * context identifiers.
		 */
		assertEquals("There should have been 3 context ids active previously",
				3, previousContextIds.size());
		assertTrue("The previous contexts should include the dialog context",
				previousContextIds.contains(IContextIds.CONTEXT_ID_DIALOG));
		assertTrue("The previous contexts should include the dialog context",
				previousContextIds
						.contains(IContextIds.CONTEXT_ID_DIALOG_AND_WINDOW));
		assertTrue("The previous contexts should include the dialog context",
				previousContextIds.contains(windowChildContext.getId()));
		System.out.println("testSiblingContext");
	}
}
