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
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
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
		public void bindingManagerChanged(BindingManagerEvent e) {
			this.event = e;
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
		bindingManager.setActiveScheme(scheme);
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
		assertSame("The binding should be active", binding.getCommandId(),
				bindingManager.getPerfectMatch(TestBinding.TRIGGER_SEQUENCE));
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

		bindingManager.setActiveScheme(scheme);
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

		/*
		 * Check that modifying this set does not modify the internal data
		 * structures.
		 */
		bindings.clear();
		assertEquals("There should still be one binding", 1, bindingManager
				.getBindings().size());
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

	/**
	 * Tests that this method returns the expected list of sequences for a
	 * couple of scenarios. In the first scenario, there is one perfect match
	 * bindings and a partial match binding. In the second scenario, there are
	 * two partial match bindings. In the third scenario, we are checking that
	 * all bindings match an empty trigger sequence.
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 * @throws ParseException
	 *             If the hard-coded strings aren't constructed properly.
	 */
	public final void testGetPartialMatches() throws NotDefinedException,
			ParseException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SCENARIO 1
		final KeySequence perfectMatch = KeySequence.getInstance("CTRL+F");
		final Binding perfectMatchBinding = new KeyBinding(perfectMatch,
				"perfect", "na", "na", null, null, null, Binding.SYSTEM);
		final KeySequence partialMatch1 = KeySequence
				.getInstance("CTRL+F CTRL+F");
		final Binding partialMatchBinding1 = new KeyBinding(partialMatch1,
				"partial1", "na", "na", null, null, null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(perfectMatchBinding);
		bindings.add(partialMatchBinding1);
		bindingManager.setBindings(bindings);
		Map partialMatches = bindingManager.getPartialMatches(perfectMatch);
		assertTrue("A partial match should override a perfect match",
				!partialMatches.isEmpty());
		assertTrue("A partial match should override a perfect match",
				partialMatches.containsKey(partialMatch1));

		// SCENARIO 2
		final KeySequence partialMatch2 = KeySequence
				.getInstance("CTRL+F CTRL+F CTRL+F");
		final Binding partialMatchBinding2 = new KeyBinding(partialMatch2,
				"partial2", "na", "na", null, null, null, Binding.SYSTEM);
		bindings.clear();
		bindings.add(partialMatchBinding1);
		bindings.add(partialMatchBinding2);
		bindingManager.setBindings(bindings);
		partialMatches = bindingManager.getPartialMatches(perfectMatch);
		assertEquals("There should be two partial matches", 2, partialMatches
				.size());
		assertSame("The partial match should be the one defined",
				partialMatchBinding1.getCommandId(), partialMatches
						.get(partialMatch1));
		assertSame("The partial match should be the one defined",
				partialMatchBinding2.getCommandId(), partialMatches
						.get(partialMatch2));

		// SCENARIO 3
		bindings.add(perfectMatchBinding);
		bindingManager.setBindings(bindings);
		partialMatches = bindingManager.getPartialMatches(KeySequence
				.getInstance());
		assertEquals("There should be three partial matches", 3, partialMatches
				.size());
		assertSame("The partial match should be the one defined",
				perfectMatchBinding.getCommandId(), partialMatches
						.get(perfectMatch));
		assertSame("The partial match should be the one defined",
				partialMatchBinding1.getCommandId(), partialMatches
						.get(partialMatch1));
		assertSame("The partial match should be the one defined",
				partialMatchBinding2.getCommandId(), partialMatches
						.get(partialMatch2));
	}

	/**
	 * Tests that this method returns the expected command identifier. In the
	 * first scenario, there is one perfect match bindings and a partial match
	 * binding. In the second scenario, there are two partial match bindings. In
	 * the third scenario, we are checking that nothing matches an empty
	 * sequence.
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 * @throws ParseException
	 *             If the hard-coded strings aren't constructed properly.
	 */
	public final void testGetPerfectMatch() throws NotDefinedException,
			ParseException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SCENARIO 1
		final KeySequence perfectMatch = KeySequence.getInstance("CTRL+F");
		final Binding perfectMatchBinding = new KeyBinding(perfectMatch,
				"perfect", "na", "na", null, null, null, Binding.SYSTEM);
		final KeySequence partialMatch1 = KeySequence
				.getInstance("CTRL+F CTRL+F");
		final Binding partialMatchBinding1 = new KeyBinding(partialMatch1,
				"partial1", "na", "na", null, null, null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(perfectMatchBinding);
		bindings.add(partialMatchBinding1);
		bindingManager.setBindings(bindings);
		String actualCommandId = bindingManager.getPerfectMatch(perfectMatch);
		assertSame("This should be a perfect match", perfectMatchBinding
				.getCommandId(), actualCommandId);

		// SCENARIO 2
		final KeySequence partialMatch2 = KeySequence
				.getInstance("CTRL+F CTRL+F CTRL+F");
		final Binding partialMatchBinding2 = new KeyBinding(partialMatch2,
				"partial2", "na", "na", null, null, null, Binding.SYSTEM);
		bindings.clear();
		bindings.add(partialMatchBinding1);
		bindings.add(partialMatchBinding2);
		bindingManager.setBindings(bindings);
		actualCommandId = bindingManager.getPerfectMatch(perfectMatch);
		assertNull("This should be no perfect matches", actualCommandId);

		// SCENARIO 3
		bindings.add(perfectMatchBinding);
		bindingManager.setBindings(bindings);
		actualCommandId = bindingManager.getPerfectMatch(KeySequence
				.getInstance());
		assertNull("This should be no perfect matches for an empty sequence",
				actualCommandId);
	}

	/**
	 * Tests that the platform is never <code>null</code>.
	 */
	public final void testGetPlatform() {
		assertNotNull("The platform can never be null", bindingManager
				.getPlatform());
	}

	/**
	 * Tests that when a scheme is first retrieved, it is undefined. Tests that
	 * a second access to a scheme returns the same scheme.
	 */
	public final void testGetScheme() {
		final String schemeId = "schemeId";
		final Scheme firstScheme = bindingManager.getScheme(schemeId);
		assertTrue("A scheme should start undefined", !firstScheme.isDefined());
		final Scheme secondScheme = bindingManager.getScheme(schemeId);
		assertSame("The two scheme should be the same", firstScheme,
				secondScheme);
	}

	/**
	 * Tests that this method returns <code>true</code> when expected. In the
	 * first scenario, there is one perfect match bindings and a partial match
	 * binding. In the second scenario, there are two partial match bindings. In
	 * the third scenario, we are checking that all bindings match an empty
	 * trigger sequence.
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 * @throws ParseException
	 *             If the hard-coded strings aren't constructed properly.
	 */
	public final void testIsPartialMatch() throws NotDefinedException,
			ParseException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SCENARIO 1
		final KeySequence perfectMatch = KeySequence.getInstance("CTRL+F");
		final Binding perfectMatchBinding = new KeyBinding(perfectMatch,
				"perfect", "na", "na", null, null, null, Binding.SYSTEM);
		final KeySequence partialMatch1 = KeySequence
				.getInstance("CTRL+F CTRL+F");
		final Binding partialMatchBinding1 = new KeyBinding(partialMatch1,
				"partial1", "na", "na", null, null, null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(perfectMatchBinding);
		bindings.add(partialMatchBinding1);
		bindingManager.setBindings(bindings);
		assertTrue("A perfect match should be overridden by a partial",
				bindingManager.isPartialMatch(perfectMatch));

		// SCENARIO 2
		final KeySequence partialMatch2 = KeySequence
				.getInstance("CTRL+F CTRL+F CTRL+F");
		final Binding partialMatchBinding2 = new KeyBinding(partialMatch2,
				"partial2", "na", "na", null, null, null, Binding.SYSTEM);
		bindings.clear();
		bindings.add(partialMatchBinding1);
		bindings.add(partialMatchBinding2);
		bindingManager.setBindings(bindings);
		assertTrue("Two partial matches should count as a partial",
				bindingManager.isPartialMatch(perfectMatch));

		// SCENARIO 3
		bindings.add(perfectMatchBinding);
		bindingManager.setBindings(bindings);
		assertTrue("An empty sequence matches everything partially",
				bindingManager.isPartialMatch(KeySequence.getInstance()));
	}

	/**
	 * Tests that this method returns <code>true</code> when expected. In the
	 * first scenario, there is one perfect match bindings and a partial match
	 * binding. In the second scenario, there are two partial match bindings. In
	 * the third scenario, we are checking that nothing matches an empty
	 * sequence.
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 * @throws ParseException
	 *             If the hard-coded strings aren't constructed properly.
	 */
	public final void testIsPerfectMatch() throws NotDefinedException,
			ParseException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SCENARIO 1
		final KeySequence perfectMatch = KeySequence.getInstance("CTRL+F");
		final Binding perfectMatchBinding = new KeyBinding(perfectMatch,
				"perfect", "na", "na", null, null, null, Binding.SYSTEM);
		final KeySequence partialMatch1 = KeySequence
				.getInstance("CTRL+F CTRL+F");
		final Binding partialMatchBinding1 = new KeyBinding(partialMatch1,
				"partial1", "na", "na", null, null, null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(perfectMatchBinding);
		bindings.add(partialMatchBinding1);
		bindingManager.setBindings(bindings);
		assertTrue("This should be a perfect match", bindingManager
				.isPerfectMatch(perfectMatch));

		// SCENARIO 2
		final KeySequence partialMatch2 = KeySequence
				.getInstance("CTRL+F CTRL+F CTRL+F");
		final Binding partialMatchBinding2 = new KeyBinding(partialMatch2,
				"partial2", "na", "na", null, null, null, Binding.SYSTEM);
		bindings.clear();
		bindings.add(partialMatchBinding1);
		bindings.add(partialMatchBinding2);
		bindingManager.setBindings(bindings);
		assertTrue("This should be no perfect matches", !bindingManager
				.isPerfectMatch(perfectMatch));

		// SCENARIO 3
		bindings.add(perfectMatchBinding);
		bindingManager.setBindings(bindings);
		assertTrue("This should be no perfect matches", !bindingManager
				.isPerfectMatch(KeySequence.getInstance()));
	}

	/**
	 * Tests that you can remove binding, and that it will change the active
	 * bindings as well.
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public final void testRemoveBindings() throws NotDefinedException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// ADD SOME BINDINGS
		final Binding binding1 = new TestBinding("command1", "na", "na", null,
				null, Binding.SYSTEM);
		bindingManager.addBinding(binding1);
		final Binding binding2 = new TestBinding("command2", "na", "na", "zh",
				null, Binding.SYSTEM);
		bindingManager.addBinding(binding2);
		final Binding binding3 = new TestBinding("command3", "na", "na", null,
				"gtk", Binding.SYSTEM);
		bindingManager.addBinding(binding3);
		final Binding binding4 = new TestBinding("command4", "na", "na", null,
				"gtk", Binding.USER);
		bindingManager.addBinding(binding4);
		final Binding binding5 = new TestBinding("command5", "na", "na", "zh",
				"gtk", Binding.USER);
		bindingManager.addBinding(binding5);
		assertNotNull("There should be three active bindings", bindingManager
				.getActiveBindingsFor("command1"));
		assertNotNull("There should be three active bindings", bindingManager
				.getActiveBindingsFor("command2"));
		assertNotNull("There should be three active bindings", bindingManager
				.getActiveBindingsFor("command4"));

		// REMOVE SOME BINDINGS
		bindingManager.removeBindings(TestBinding.TRIGGER_SEQUENCE, "na", "na",
				"zh", "gtk", null, Binding.USER);
		assertEquals("There should be four bindings left", 4, bindingManager
				.getBindings().size());
		assertNotNull("There should be four active bindings", bindingManager
				.getActiveBindingsFor("command1"));
		assertNotNull("There should be four active bindings", bindingManager
				.getActiveBindingsFor("command2"));
		assertNotNull("There should be four active bindings", bindingManager
				.getActiveBindingsFor("command3"));
		assertNotNull("There should be four active bindings", bindingManager
				.getActiveBindingsFor("command4"));
	}

	/**
	 * Verifies that selecting an undefimned scheme doesn't work. Verifies that
	 * selecting a scheme works. Verifies that undefining scheme removes it as
	 * the active scheme.
	 */
	public final void testSetActiveScheme() {
		// SELECT UNDEFINED
		final String schemeId = "schemeId";
		final Scheme scheme = bindingManager.getScheme(schemeId);
		try {
			bindingManager.setActiveScheme(scheme);
			fail("Cannot activate an undefined scheme");
		} catch (final NotDefinedException e) {
			// Success
		}

		// SELECT DEFINED
		scheme.define("name", "description", null);
		try {
			bindingManager.setActiveScheme(scheme);
			assertSame("The schemes should match", scheme, bindingManager
					.getActiveScheme());
		} catch (final NotDefinedException e) {
			fail("Should be able to activate a scheme");
		}

		// UNDEFINE SELECTED
		scheme.undefine();
		assertNull("The scheme should have become unselected", bindingManager
				.getActiveScheme());
	}

	/**
	 * Verifies that you can set the bindings to null. Verifies that setting the
	 * bindings clears the cache.
	 * 
	 * @throws NotDefinedException
	 *             If this test doesn't properly define a scheme.
	 */
	public final void testSetBindings() throws NotDefinedException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SET NULL
		final String commandId = "commandId";
		bindingManager.setBindings(null);
		assertTrue("There should be no active bindings", bindingManager
				.getActiveBindingsFor(commandId).isEmpty());

		// ADD BINDING
		final Binding binding = new TestBinding(commandId, "na", "na", null,
				null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding);
		bindingManager.setBindings(bindings);
		final Collection activeBindings = bindingManager
				.getActiveBindingsFor(commandId);
		assertEquals("There should be one active binding", 1, activeBindings
				.size());
		assertSame("The binding should be the one we set",
				TestBinding.TRIGGER_SEQUENCE, activeBindings.iterator().next());
	}

	/**
	 * Verifies that it cannot be set to <code>null</code>. Verifies that it
	 * clears the cache.
	 * 
	 * @throws NotDefinedException
	 *             If this test doesn't properly define a scheme.
	 */
	public final void testSetLocale() throws NotDefinedException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SET TO NULL
		try {
			bindingManager.setLocale(null);
			fail("Cannot set the locale to null");
		} catch (final NullPointerException e) {
			// Success
		}

		// SET TO SOMETHING
		final String commandId = "commandId";
		final Binding binding = new TestBinding(commandId, "na", "na", "xx",
				null, Binding.SYSTEM);
		bindingManager.addBinding(binding);
		assertTrue("The binding shouldn't be active", bindingManager
				.getActiveBindingsFor(commandId).isEmpty());
		bindingManager.setLocale("xx_XX");
		final Collection activeBindings = bindingManager
				.getActiveBindingsFor(commandId);
		assertEquals("The binding should become active", 1, activeBindings
				.size());
		assertSame("The binding should be the same",
				TestBinding.TRIGGER_SEQUENCE, activeBindings.iterator().next());
	}

	/**
	 * Verifies that it cannot be set to <code>null</code>. Verifies that it
	 * clears the cache.
	 * 
	 * @throws NotDefinedException
	 *             If this test doesn't properly define a scheme.
	 */
	public final void testSetPlatform() throws NotDefinedException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SET TO NULL
		try {
			bindingManager.setPlatform(null);
			fail("Cannot set the platform to null");
		} catch (final NullPointerException e) {
			// Success
		}

		// SET TO SOMETHING
		final String commandId = "commandId";
		final Binding binding = new TestBinding(commandId, "na", "na", null,
				"atari", Binding.SYSTEM);
		bindingManager.addBinding(binding);
		assertTrue("The binding shouldn't be active", bindingManager
				.getActiveBindingsFor(commandId).isEmpty());
		bindingManager.setPlatform("atari");
		final Collection activeBindings = bindingManager
				.getActiveBindingsFor(commandId);
		assertEquals("The binding should become active", 1, activeBindings
				.size());
		assertSame("The binding should be the same",
				TestBinding.TRIGGER_SEQUENCE, activeBindings.iterator().next());
	}
}
