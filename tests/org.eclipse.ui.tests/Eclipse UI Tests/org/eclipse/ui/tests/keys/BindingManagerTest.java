/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.keys;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.BindingManagerEvent;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * <p>
 * This test case covers the general functionality of the binding manager's API
 * methods. This is not intended to test the interactions between bindings
 * themselves (e.g., solving a binding set). For tests dealing with
 * interactions, please look at <code>BindingInteractionsTest</code>.
 * </p>
 * <p>
 * The listener code is tested throughout the various tests. There is no
 * individual test method for the listener code.
 * </p>
 * 
 * @see org.eclipse.ui.tests.keys.BindingInteractionsTest
 * @since 3.1
 */
public final class BindingManagerTest extends UITestCase {

	/**
	 * A test listener that should be attached to the binding manager. The
	 * listener records the last fired event.
	 * 
	 * @since 3.1
	 */
	private static final class TestListener implements IBindingManagerListener {

		/**
		 * The last event that this listener saw. <code>null</code> if none.
		 */
		private BindingManagerEvent event = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.bindings.IBindingManagerListener#bindingManagerChanged(org.eclipse.jface.bindings.BindingManagerEvent)
		 */
		public void bindingManagerChanged(BindingManagerEvent event) {
			this.event = event;
		}

		/**
		 * Returns the last event.
		 * 
		 * @return The last event; may be <code>null</code> if none.
		 */
		public final BindingManagerEvent getLastEvent() {
			return event;
		}
	}

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
	 * The listener attached to the binding manager. This listener is attached
	 * at the beginning of each test case, and it is disposed when the test is
	 * over.
	 */
	private TestListener listener = null;

	/**
	 * Constructor for <code>BindingInteractionsTest</code>.
	 * 
	 * @param name
	 *            The name of the test
	 */
	public BindingManagerTest(final String name) {
		super(name);
	}

	/**
	 * Creates a new context manager and a binding manager for use in the test
	 * cases.
	 */
	protected final void doSetUp() {
		contextManager = new ContextManager();
		bindingManager = new BindingManager(contextManager);
		listener = new TestListener();
		bindingManager.addBindingManagerListener(listener);
	}

	/**
	 * Releases the context manager and binding manager for garbage collection.
	 */
	protected final void doTearDown() {
		bindingManager.removeBindingManagerListener(listener);
		listener = null;
		bindingManager = null;
		contextManager = null;
	}

	/**
	 * Tests that the constructor disallows a null context manager.
	 */
	public final void testConstructor() {
		try {
			new BindingManager(null);
			fail("A binding manager cannot be constructed with a null context manager");
		} catch (final NullPointerException e) {
			// Success
		}
	}

	/**
	 * Tests that it is not possible to add a null binding. Tests that adding a
	 * binding forces a recomputation.
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public final void testAddBinding() throws NotDefinedException {
		// Set up a state in which a binding may become active.
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme("na");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// Try to add a null binding.
		try {
			bindingManager.addBinding(null);
			fail("It should not be possible to add a null binding");
		} catch (final NullPointerException e) {
			// Success.
		}

		// Try to add a binding that should become active.
		final Binding binding = new TestBinding("conflict1", "na", "na", null,
				null, Binding.SYSTEM);
		bindingManager.addBinding(binding);
		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);
		assertEquals("The binding should be active", binding.getCommandId(),
				activeCommandId);
	}

	/**
	 * Tests that <code>getActiveBindings()</code> first returns
	 * <code>null</code>. The rest of the functionality is tested in
	 * <code>BindingInteractionsTest</code>.
	 * 
	 * @see BindingInteractionsTest
	 */
	public final void testGetActiveBindings() {
		assertNull("The active bindings should be null to start",
				bindingManager.getActiveBindings());
	}

	/**
	 * Tests that <code>getActiveBindingsDisregardingContext()</code> never
	 * returns <code>null</code>. The rest of the functionality is tested in
	 * <code>BindingInteractionsTest</code>.
	 * 
	 * @see BindingInteractionsTest
	 */
	public final void testGetActiveBindingsDisregardingContext() {
		final Map activeBindings = bindingManager
				.getActiveBindingsDisregardingContext();
		assertNotNull("The active bindings should never be null",
				activeBindings);
		assertTrue("The active bindings should start empty", activeBindings
				.isEmpty());
	}

	/**
	 * Tests that <code>getActiveBindingsDisregardingContextFlat()</code>
	 * never returns <code>null</code>. The rest of the functionality is
	 * tested in <code>BindingInteractionsTest</code>.
	 * 
	 * @see BindingInteractionsTest
	 */
	public final void testGetActiveBindingsDisregardingContextFlat() {
		final Collection activeBindings = bindingManager
				.getActiveBindingsDisregardingContextFlat();
		assertNotNull("The active bindings should never be null",
				activeBindings);
		assertTrue("The active bindings should start empty", activeBindings
				.isEmpty());
	}

	/**
	 * Tests whether the method works with a null argument. Tests that it works
	 * in a simple case.
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public final void testGetActiveBindingsFor() throws NotDefinedException {
		// Test with a null argument.
		final Collection activeBindingsForNull = bindingManager
				.getActiveBindingsFor(null);
		assertNotNull("The active bindings for a command should never be null",
				activeBindingsForNull);
		assertTrue(
				"The active binding for a null command should always be empty",
				activeBindingsForNull.isEmpty());

		// Test a simple case.
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final String commandId = "commandId";
		final Binding binding = new TestBinding(commandId, "na", "na", null,
				null, Binding.SYSTEM);
		bindingManager.addBinding(binding);

		final Collection bindings = bindingManager
				.getActiveBindingsFor(commandId);
		assertEquals("There should be one binding", 1, bindings.size());
		assertSame("The binding should match", TestBinding.TRIGGER_SEQUENCE,
				bindings.iterator().next());
	}

	/**
	 * Tests that the active scheme starts off <code>null</code>. The rest of
	 * the active scheme testing happens in <code>testSetActiveScheme()</code>.
	 * 
	 * @see BindingManagerTest#testSetActiveScheme()
	 */
	public final void testGetActiveScheme() {
		assertNull("The active scheme should start null", bindingManager
				.getActiveScheme());
	}

	/**
	 * Tests that <code>getBindings</code> first returns <code>null</code>.
	 * It then verifies that an added binding is return from this method.
	 */
	public final void testGetBindings() {
		// Check the starting condition.
		assertNull("The bindings should start off null", bindingManager
				.getBindings());

		// Check that an added binding is included.
		final Binding binding = new TestBinding(null, "schemeId", "contextId",
				null, null, Binding.SYSTEM);
		bindingManager.addBinding(binding);
		final Set bindings = bindingManager.getBindings();
		assertEquals("There should be one binding", 1, bindings.size());
		assertSame("The binding should be the same", binding, bindings
				.iterator().next());
		try {
			bindings.clear();
			fail("The API should not expose internal collections");
		} catch (final UnsupportedOperationException e) {
			// Success
		}
	}

	/**
	 * Tests that the list of defined schemes stays up-to-date
	 */
	public final void testGetDefinedSchemeIds() {
		// Starting condition.
		assertTrue("The set of defined scheme ids should start empty",
				bindingManager.getDefinedSchemeIds().isEmpty());

		// Retrieving a scheme shouldn't change anything.
		final String schemeId = "schemeId";
		final Scheme scheme = bindingManager.getScheme(schemeId);
		assertTrue(
				"The set of defined scheme ids should still be empty after a get",
				bindingManager.getDefinedSchemeIds().isEmpty());

		// Defining the scheme should change things.
		scheme.define("name", "description", null);
		final Set definedSchemes = bindingManager.getDefinedSchemeIds();
		assertEquals("There should be one defined scheme id", 1, definedSchemes
				.size());
		assertSame("The defined scheme id should match", schemeId,
				definedSchemes.iterator().next());
		try {
			definedSchemes.clear();
			fail("The API should not expose internal collections");
		} catch (final UnsupportedOperationException e) {
			// Success
		}

		// Undefining the scheme should also change things.
		scheme.undefine();
		assertTrue(
				"The set of define scheme ids should be empty after an undefine",
				bindingManager.getDefinedSchemeIds().isEmpty());
	}

	/**
	 * Tests that the active locale is never <code>null</code>.
	 */
	public final void testGetLocale() {
		assertNotNull("The locale should never be null", bindingManager
				.getLocale());
	}

	public final void testGetPartialMatches() {
		// TODO Implement this test case.
	}

	public final void testGetPerfectMatch() {
		// TODO Implement this test case.
	}

	public final void testGetPlatform() {
		// TODO Implement this test case.
	}

	public final void testGetScheme() {
		// TODO Implement this test case.
	}

	public final void testIsPartialMatch() {
		// TODO Implement this test case.
	}

	public final void testIsPerfectMatch() {
		// TODO Implement this test case.
	}

	public final void testRemoveBindings() {

	}

	public final void testSetActiveScheme() {

	}

	public final void testSetBindings() {

	}

	public final void testSetLocale() {

	}

	public final void testSetPlatform() {

	}
}
