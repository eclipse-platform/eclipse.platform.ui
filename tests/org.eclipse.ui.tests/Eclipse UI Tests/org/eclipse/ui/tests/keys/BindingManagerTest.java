/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *******************************************************************************/
package org.eclipse.ui.tests.keys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
public final class BindingManagerTest {

	/**
	 * The binding manager to use in each test case. A new binding manager is
	 * created for each test case, and it is disposed when the test is over.
	 */
	private BindingManager bindingManager = null;

	/**
	 * The command manager for the currently running test. <code>null</code> if
	 * no test is running.
	 */
	private CommandManager commandManager = null;

	/**
	 * The context manager to use in each test case. A new context manager is
	 * created for each test case, and it is disposed when the test is over.
	 */
	private ContextManager contextManager = null;

	/**
	 * Creates a new context manager and a binding manager for use in the test
	 * cases.
	 */
	@BeforeEach
	public void doSetUp() {
		commandManager = new CommandManager();
		contextManager = new ContextManager();
		bindingManager = new BindingManager(contextManager, commandManager);
	}

	/**
	 * Releases the context manager and binding manager for garbage collection.
	 */
	@AfterEach
	public void doTearDown() {
		bindingManager = null;
		contextManager = null;
		commandManager = null;
	}

	/**
	 * Tests that the constructor disallows a null context manager.
	 */
	@Test
	public void testConstructor() {
		assertThrows(NullPointerException.class, () -> new BindingManager(null, null));
	}

	/**
	 * Tests that it is not possible to add a null binding. Tests that adding a
	 * binding forces a recomputation.
	 *
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	@Test
	public void testAddBinding() throws NotDefinedException {
		// Set up a state in which a binding may become active.
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// Try to add a null binding.
		assertThrows(NullPointerException.class, () -> bindingManager.addBinding(null));

		// Try to add a binding that should become active.
		final Binding binding = new TestBinding("conflict1", "na", "na", null,
				null, Binding.SYSTEM, null);
		bindingManager.addBinding(binding);
		assertSame(binding,
				bindingManager.getPerfectMatch(TestBinding.TRIGGER_SEQUENCE), "The binding should be active");
	}

	/**
	 * Tests that <code>getActiveBindingsDisregardingContext()</code> never
	 * returns <code>null</code>. The rest of the functionality is tested in
	 * <code>BindingInteractionsTest</code>.
	 *
	 * @see BindingInteractionsTest
	 */
	@Test
	public void testGetActiveBindingsDisregardingContext() {
		final Map<?, ?> activeBindings = bindingManager
				.getActiveBindingsDisregardingContext();
		assertNotNull(activeBindings, "The active bindings should never be null");
		assertTrue(activeBindings.isEmpty(), "The active bindings should start empty");
	}

	/**
	 * Tests that <code>getActiveBindingsDisregardingContextFlat()</code> never
	 * returns <code>null</code>. The rest of the functionality is tested in
	 * <code>BindingInteractionsTest</code>.
	 *
	 * @see BindingInteractionsTest
	 */
	@Test
	public void testGetActiveBindingsDisregardingContextFlat() {
		final Collection<?> activeBindings = bindingManager
				.getActiveBindingsDisregardingContextFlat();
		assertNotNull(activeBindings, "The active bindings should never be null");
		assertTrue(activeBindings.isEmpty(), "The active bindings should start empty");
	}

	/**
	 * Tests whether the method works with a null argument. Tests that it works
	 * in a simple case.
	 *
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	@Test
	public void testGetActiveBindingsFor() throws NotDefinedException {
		// Test with a null argument.
		final TriggerSequence[] activeBindingsForNull = bindingManager
				.getActiveBindingsFor((ParameterizedCommand) null);
		assertNotNull(activeBindingsForNull, "The active bindings for a command should never be null");
		assertTrue(
				activeBindingsForNull.length == 0, "The active binding for a null command should always be empty");

		// Test a simple case.
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final String commandId = "commandId";
		final Binding binding = new TestBinding(commandId, "na", "na", null,
				null, Binding.SYSTEM, null);
		bindingManager.addBinding(binding);

		final TriggerSequence[] bindings = bindingManager
				.getActiveBindingsFor(binding.getParameterizedCommand());
		assertEquals(1, bindings.length, "There should be one binding");
		assertSame(TestBinding.TRIGGER_SEQUENCE,
				bindings[0], "The binding should match");
	}

	/**
	 * Tests that the active scheme starts off <code>null</code>. The rest of
	 * the active scheme testing happens in <code>testSetActiveScheme()</code>.
	 *
	 * @see BindingManagerTest#testSetActiveScheme()
	 */
	@Test
	public void testGetActiveScheme() {
		assertNull(bindingManager.getActiveScheme(), "The active scheme should start null");
	}

	/**
	 * Tests that <code>getBindings</code> first returns <code>null</code>. It
	 * then verifies that an added binding is return from this method.
	 */
	@Test
	public void testGetBindings() {
		// Check the starting condition.
		assertNull(bindingManager.getBindings(), "The bindings should start off null");

		// Check that an added binding is included.
		final Binding binding = new TestBinding(null, "schemeId", "contextId",
				null, null, Binding.SYSTEM, null);
		bindingManager.addBinding(binding);
		final Binding[] bindings = bindingManager.getBindings();
		assertEquals(1, bindings.length, "There should be one binding");
		assertSame(binding, bindings[0], "The binding should be the same");

		/*
		 * Check that modifying this set does not modify the internal data
		 * structures.
		 */
		bindings[0] = null;
		assertNotNull(bindingManager.getBindings()[0], "There should be no change");
	}

	/**
	 * Tests that the list of defined schemes stays up-to-date
	 */
	@Test
	public void testGetDefinedSchemeIds() {
		// Starting condition.
		assertTrue(bindingManager.getDefinedSchemes().length == 0, "The set of defined schemes should start empty");

		// Retrieving a scheme shouldn't change anything.
		final Scheme scheme = bindingManager.getScheme("schemeId");
		assertTrue(
				bindingManager.getDefinedSchemes().length == 0, "The set of defined schemes should still be empty after a get");

		// Defining the scheme should change things.
		scheme.define("name", "description", null);
		Scheme[] definedSchemes = bindingManager.getDefinedSchemes();
		assertEquals(1,
				definedSchemes.length, "There should be one defined scheme id");
		assertSame(scheme,
				definedSchemes[0], "The defined scheme id should match");

		definedSchemes[0] = null;
		definedSchemes = bindingManager.getDefinedSchemes();
		assertSame(scheme,
				definedSchemes[0], "The API should not expose internal collections");

		// Undefining the scheme should also change things.
		scheme.undefine();
		assertTrue(
				bindingManager.getDefinedSchemes().length == 0, "The set of defined schemes should be empty after an undefine");
	}

	/**
	 * Tests that the active locale is never <code>null</code>.
	 */
	@Test
	public void testGetLocale() {
		assertNotNull(bindingManager.getLocale(), "The locale should never be null");
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
	@Test
	public void testGetPartialMatches() throws NotDefinedException,
			ParseException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SCENARIO 1
		final KeySequence perfectMatch = KeySequence.getInstance("CTRL+F");
		final Command perfectCommand = commandManager.getCommand("perfect");
		final ParameterizedCommand perfectParameterizedCommand = new ParameterizedCommand(
				perfectCommand, null);
		final Binding perfectMatchBinding = new KeyBinding(perfectMatch,
				perfectParameterizedCommand, "na", "na", null, null, null,
				Binding.SYSTEM);
		final KeySequence partialMatch1 = KeySequence
				.getInstance("CTRL+F CTRL+F");
		final Command partialCommand1 = commandManager.getCommand("partial1");
		final ParameterizedCommand partialParameterizedCommand1 = new ParameterizedCommand(
				partialCommand1, null);
		final Binding partialMatchBinding1 = new KeyBinding(partialMatch1,
				partialParameterizedCommand1, "na", "na", null, null, null,
				Binding.SYSTEM);
		final Binding[] bindings = new Binding[2];
		bindings[0] = perfectMatchBinding;
		bindings[1] = partialMatchBinding1;
		bindingManager.setBindings(bindings);
		Map<?, ?> partialMatches = bindingManager.getPartialMatches(perfectMatch);
		assertTrue(!partialMatches.isEmpty(), "A partial match should override a perfect match");
		assertTrue(partialMatches.containsKey(partialMatch1), "A partial match should override a perfect match");

		// SCENARIO 2
		final KeySequence partialMatch2 = KeySequence
				.getInstance("CTRL+F CTRL+F CTRL+F");
		final Command partialCommand2 = commandManager.getCommand("partial2");
		final ParameterizedCommand partialParameterizedCommand2 = new ParameterizedCommand(
				partialCommand2, null);
		final Binding partialMatchBinding2 = new KeyBinding(partialMatch2,
				partialParameterizedCommand2, "na", "na", null, null, null,
				Binding.SYSTEM);
		bindings[0] = partialMatchBinding1;
		bindings[1] = partialMatchBinding2;
		bindingManager.setBindings(bindings);
		partialMatches = bindingManager.getPartialMatches(perfectMatch);
		assertEquals(2,
				partialMatches.size(), "There should be two partial matches");
		assertSame(partialMatchBinding1, partialMatches.get(partialMatch1), "The partial match should be the one defined");
		assertSame(partialMatchBinding2, partialMatches.get(partialMatch2), "The partial match should be the one defined");

		// SCENARIO 3
		bindingManager.addBinding(perfectMatchBinding);
		partialMatches = bindingManager.getPartialMatches(KeySequence
				.getInstance());
		assertEquals(3,
				partialMatches.size(), "There should be three partial matches");
		assertSame(perfectMatchBinding, partialMatches.get(perfectMatch), "The partial match should be the one defined");
		assertSame(partialMatchBinding1, partialMatches.get(partialMatch1), "The partial match should be the one defined");
		assertSame(partialMatchBinding2, partialMatches.get(partialMatch2), "The partial match should be the one defined");
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
	@Test
	public void testGetPerfectMatch() throws NotDefinedException,
			ParseException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SCENARIO 1
		final KeySequence perfectMatch = KeySequence.getInstance("CTRL+F");
		final Command perfectCommand = commandManager.getCommand("perfect");
		final ParameterizedCommand perfectParameterizedCommand = new ParameterizedCommand(
				perfectCommand, null);
		final Binding perfectMatchBinding = new KeyBinding(perfectMatch,
				perfectParameterizedCommand, "na", "na", null, null, null,
				Binding.SYSTEM);
		final KeySequence partialMatch1 = KeySequence
				.getInstance("CTRL+F CTRL+F");
		final Command partialCommand1 = commandManager.getCommand("partial1");
		final ParameterizedCommand partialParameterizedCommand1 = new ParameterizedCommand(
				partialCommand1, null);
		final Binding partialMatchBinding1 = new KeyBinding(partialMatch1,
				partialParameterizedCommand1, "na", "na", null, null, null,
				Binding.SYSTEM);
		final Binding[] bindings = new Binding[2];
		bindings[0] = perfectMatchBinding;
		bindings[1] = partialMatchBinding1;
		bindingManager.setBindings(bindings);
		Binding actualBinding = bindingManager.getPerfectMatch(perfectMatch);
		assertSame(perfectMatchBinding,
				actualBinding, "This should be a perfect match");

		// SCENARIO 2
		final KeySequence partialMatch2 = KeySequence
				.getInstance("CTRL+F CTRL+F CTRL+F");
		final Command partialCommand2 = commandManager.getCommand("partial2");
		final ParameterizedCommand partialParameterizedCommand2 = new ParameterizedCommand(
				partialCommand2, null);
		final Binding partialMatchBinding2 = new KeyBinding(partialMatch2,
				partialParameterizedCommand2, "na", "na", null, null, null,
				Binding.SYSTEM);
		bindings[0] = partialMatchBinding1;
		bindings[1] = partialMatchBinding2;
		bindingManager.setBindings(bindings);
		actualBinding = bindingManager.getPerfectMatch(perfectMatch);
		assertNull(actualBinding, "There should be no perfect matches");

		// SCENARIO 3
		bindingManager.addBinding(perfectMatchBinding);
		actualBinding = bindingManager.getPerfectMatch(KeySequence
				.getInstance());
		assertNull(actualBinding, "This should be no perfect matches for an empty sequence");
	}

	/**
	 * Tests that the platform is never <code>null</code>.
	 */
	@Test
	public void testGetPlatform() {
		assertNotNull(bindingManager.getPlatform(), "The platform can never be null");
	}

	/**
	 * Tests that when a scheme is first retrieved, it is undefined. Tests that
	 * a second access to a scheme returns the same scheme.
	 */
	@Test
	public void testGetScheme() {
		final String schemeId = "schemeId";
		final Scheme firstScheme = bindingManager.getScheme(schemeId);
		assertTrue(!firstScheme.isDefined(), "A scheme should start undefined");
		final Scheme secondScheme = bindingManager.getScheme(schemeId);
		assertSame(firstScheme,
				secondScheme, "The two scheme should be the same");
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
	@Test
	public void testIsPartialMatch() throws NotDefinedException,
			ParseException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SCENARIO 1
		final KeySequence perfectMatch = KeySequence.getInstance("CTRL+F");
		final Command perfectCommand = commandManager.getCommand("perfect");
		final ParameterizedCommand perfectParameterizedCommand = new ParameterizedCommand(
				perfectCommand, null);
		final Binding perfectMatchBinding = new KeyBinding(perfectMatch,
				perfectParameterizedCommand, "na", "na", null, null, null,
				Binding.SYSTEM);
		final KeySequence partialMatch1 = KeySequence
				.getInstance("CTRL+F CTRL+F");
		final Command partialCommand1 = commandManager.getCommand("partial1");
		final ParameterizedCommand partialParameterizedCommand1 = new ParameterizedCommand(
				partialCommand1, null);
		final Binding partialMatchBinding1 = new KeyBinding(partialMatch1,
				partialParameterizedCommand1, "na", "na", null, null, null,
				Binding.SYSTEM);
		final Binding[] bindings = new Binding[2];
		bindings[0] = perfectMatchBinding;
		bindings[1] = partialMatchBinding1;
		bindingManager.setBindings(bindings);
		assertTrue(bindingManager.isPartialMatch(perfectMatch), "A perfect match should be overridden by a partial");

		// SCENARIO 2
		final KeySequence partialMatch2 = KeySequence
				.getInstance("CTRL+F CTRL+F CTRL+F");
		final Command partialCommand2 = commandManager.getCommand("partial2");
		final ParameterizedCommand partialParameterizedCommand2 = new ParameterizedCommand(
				partialCommand2, null);
		final Binding partialMatchBinding2 = new KeyBinding(partialMatch2,
				partialParameterizedCommand2, "na", "na", null, null, null,
				Binding.SYSTEM);
		bindings[0] = partialMatchBinding1;
		bindings[1] = partialMatchBinding2;
		bindingManager.setBindings(bindings);
		assertTrue(bindingManager.isPartialMatch(perfectMatch), "Two partial matches should count as a partial");

		// SCENARIO 3
		bindingManager.addBinding(perfectMatchBinding);
		bindingManager.setBindings(bindings);
		assertTrue(bindingManager.isPartialMatch(KeySequence.getInstance()), "An empty sequence matches everything partially");
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
	@Test
	public void testIsPerfectMatch() throws NotDefinedException,
			ParseException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SCENARIO 1
		final KeySequence perfectMatch = KeySequence.getInstance("CTRL+F");
		final Command perfectCommand = commandManager.getCommand("perfect");
		final ParameterizedCommand perfectParameterizedCommand = new ParameterizedCommand(
				perfectCommand, null);
		final Binding perfectMatchBinding = new KeyBinding(perfectMatch,
				perfectParameterizedCommand, "na", "na", null, null, null,
				Binding.SYSTEM);
		final KeySequence partialMatch1 = KeySequence
				.getInstance("CTRL+F CTRL+F");
		final Command partialCommand1 = commandManager.getCommand("partial1");
		final ParameterizedCommand partialParameterizedCommand1 = new ParameterizedCommand(
				partialCommand1, null);
		final Binding partialMatchBinding1 = new KeyBinding(partialMatch1,
				partialParameterizedCommand1, "na", "na", null, null, null,
				Binding.SYSTEM);
		final Binding[] bindings = new Binding[2];
		bindings[0] = perfectMatchBinding;
		bindings[1] = partialMatchBinding1;
		bindingManager.setBindings(bindings);
		assertTrue(bindingManager.isPerfectMatch(perfectMatch), "This should be a perfect match");

		// SCENARIO 2
		final KeySequence partialMatch2 = KeySequence
				.getInstance("CTRL+F CTRL+F CTRL+F");
		final Command partialCommand2 = commandManager.getCommand("perfect");
		final ParameterizedCommand partialParameterizedCommand2 = new ParameterizedCommand(
				partialCommand2, null);
		final Binding partialMatchBinding2 = new KeyBinding(partialMatch2,
				partialParameterizedCommand2, "na", "na", null, null, null,
				Binding.SYSTEM);
		bindings[0] = partialMatchBinding1;
		bindings[1] = partialMatchBinding2;
		bindingManager.setBindings(bindings);
		assertTrue(!bindingManager.isPerfectMatch(perfectMatch), "This should be no perfect matches");

		// SCENARIO 3
		bindingManager.addBinding(perfectMatchBinding);
		assertTrue(!bindingManager.isPerfectMatch(KeySequence.getInstance()), "This should be no perfect matches");
	}

	/**
	 * Tests that you can remove binding, and that it will change the active
	 * bindings as well.
	 *
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	@Test
	public void testRemoveBindings() throws NotDefinedException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// ADD SOME BINDINGS
		final Binding binding1 = new TestBinding("command1", "na", "na", null,
				null, Binding.SYSTEM, null);
		bindingManager.addBinding(binding1);
		final Binding binding2 = new TestBinding("command2", "na", "na", "zh",
				null, Binding.SYSTEM, null);
		bindingManager.addBinding(binding2);
		final Binding binding3 = new TestBinding("command3", "na", "na", null,
				"gtk", Binding.SYSTEM, null);
		bindingManager.addBinding(binding3);
		final Binding binding4 = new TestBinding("command4", "na", "na", null,
				"gtk", Binding.USER, null);
		bindingManager.addBinding(binding4);
		final Binding binding5 = new TestBinding("command5", "na", "na", "zh",
				"gtk", Binding.USER, null);
		bindingManager.addBinding(binding5);
		assertNotNull(bindingManager.getActiveBindingsFor(binding1
						.getParameterizedCommand()), "There should be three active bindings");
		assertNotNull(bindingManager.getActiveBindingsFor(binding2
						.getParameterizedCommand()), "There should be three active bindings");
		assertNotNull(bindingManager.getActiveBindingsFor(binding4
						.getParameterizedCommand()), "There should be three active bindings");

		// REMOVE SOME BINDINGS
		bindingManager.removeBindings(TestBinding.TRIGGER_SEQUENCE, "na", "na",
				"zh", "gtk", null, Binding.USER);
		assertEquals(4,
				bindingManager.getBindings().length, "There should be four bindings left");
		assertNotNull(bindingManager.getActiveBindingsFor(binding1
						.getParameterizedCommand()), "There should be four active bindings");
		assertNotNull(bindingManager.getActiveBindingsFor(binding2
						.getParameterizedCommand()), "There should be four active bindings");
		assertNotNull(bindingManager.getActiveBindingsFor(binding3
						.getParameterizedCommand()), "There should be four active bindings");
		assertNotNull(bindingManager.getActiveBindingsFor(binding4
						.getParameterizedCommand()), "There should be four active bindings");
	}

	/**
	 * Verifies that selecting an undefimned scheme doesn't work. Verifies that
	 * selecting a scheme works. Verifies that undefining scheme removes it as the
	 * active scheme.
	 *
	 * @throws NotDefinedException
	 */
	@Test
	public void testSetActiveScheme() throws NotDefinedException {
		// SELECT UNDEFINED
		final String schemeId = "schemeId";
		final Scheme scheme = bindingManager.getScheme(schemeId);
		assertThrows(NotDefinedException.class, () -> bindingManager.setActiveScheme(scheme));

		// SELECT DEFINED
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		assertSame(scheme,
					bindingManager.getActiveScheme(), "The schemes should match");

		// UNDEFINE SELECTED
		scheme.undefine();
		assertNull(bindingManager.getActiveScheme(), "The scheme should have become unselected");
	}

	@Test
	public void testGetCurrentConflicts() throws NotDefinedException,
			ParseException {

		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		contextManager.setActiveContextIds(null);

		Command command1 = commandManager.getCommand("conflictCommand1");
		ParameterizedCommand parameterizedCommand1 = ParameterizedCommand
				.generateCommand(command1, null);
		Command command2 = commandManager.getCommand("conflictCommand2");
		ParameterizedCommand parameterizedCommand2 = ParameterizedCommand
				.generateCommand(command2, null);
		Command command3 = commandManager.getCommand("conflictCommand3");
		ParameterizedCommand parameterizedCommand3 = ParameterizedCommand
				.generateCommand(command3, null);
		KeySequence conflict = KeySequence.getInstance("M1+M2+9");
		KeySequence noConflict = KeySequence.getInstance("M1+M2+8");
		Binding binding1 = new KeyBinding(conflict, parameterizedCommand1,
				"na", "na", null, null, null, Binding.SYSTEM);
		Binding binding2 = new KeyBinding(conflict, parameterizedCommand2,
				"na", "na", null, null, null, Binding.SYSTEM);
		Binding binding3 = new KeyBinding(noConflict, parameterizedCommand3,
				"na", "na", null, null, null, Binding.SYSTEM);
		final Binding[] bindings = new Binding[] { binding1, binding2, binding3 };
		bindingManager.setBindings(bindings);

		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		Map<?, ?> activeBindingsDisregardingContext = bindingManager
				.getActiveBindingsDisregardingContext();// force a recompute
		assertNotNull(activeBindingsDisregardingContext);

		Map<?, ?> currentConflicts = bindingManager.getCurrentConflicts();
		assertEquals(1, currentConflicts.size()); // we have only one conflict

		Collection<?> conflictsCollection = bindingManager
				.getConflictsFor(noConflict);
		assertNull(conflictsCollection); // no conflict for this keybinding

		conflictsCollection = bindingManager.getConflictsFor(conflict);
		assertNotNull(conflictsCollection); // this has one conflict with 2
											// commands
		assertEquals(2, conflictsCollection.size());

	}

	/**
	 * Verifies that you can set the bindings to null. Verifies that setting the
	 * bindings clears the cache.
	 *
	 * @throws NotDefinedException
	 *             If this test doesn't properly define a scheme.
	 */
	@Test
	public void testSetBindings() throws NotDefinedException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SET NULL
		bindingManager.setBindings(null);
		assertTrue(
				bindingManager
						.getActiveBindingsFor((ParameterizedCommand) null).length == 0, "There should be no active bindings");

		// ADD BINDING
		final String commandId = "commandId";
		final Binding binding = new TestBinding(commandId, "na", "na", null,
				null, Binding.SYSTEM, null);
		final Binding[] bindings = new Binding[1];
		bindings[0] = binding;
		bindingManager.setBindings(bindings);
		final TriggerSequence[] activeBindings = bindingManager
				.getActiveBindingsFor(binding.getParameterizedCommand());
		assertEquals(1,
				activeBindings.length, "There should be one active binding");
		assertSame(TestBinding.TRIGGER_SEQUENCE, activeBindings[0], "The binding should be the one we set");
	}

	/**
	 * Verifies that it cannot be set to <code>null</code>. Verifies that it
	 * clears the cache.
	 *
	 * @throws NotDefinedException
	 *             If this test doesn't properly define a scheme.
	 */
	@Test
	public void testSetLocale() throws NotDefinedException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SET TO NULL
		assertThrows(NullPointerException.class, () -> bindingManager.setLocale(null));

		// SET TO SOMETHING
		final String commandId = "commandId";
		final Binding binding = new TestBinding(commandId, "na", "na", "xx",
				null, Binding.SYSTEM, null);
		bindingManager.addBinding(binding);
		assertTrue(bindingManager.getActiveBindingsFor(binding
						.getParameterizedCommand()).length == 0, "The binding shouldn't be active");
		bindingManager.setLocale("xx_XX");
		final TriggerSequence[] activeBindings = bindingManager
				.getActiveBindingsFor(binding.getParameterizedCommand());
		assertEquals(1,
				activeBindings.length, "The binding should become active");
		assertSame(TestBinding.TRIGGER_SEQUENCE, activeBindings[0], "The binding should be the same");
	}

	/**
	 * Verifies that it cannot be set to <code>null</code>. Verifies that it
	 * clears the cache.
	 *
	 * @throws NotDefinedException
	 *             If this test doesn't properly define a scheme.
	 */
	@Test
	public void testSetPlatform() throws NotDefinedException {
		// GENERAL SET-UP
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);
		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);
		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		// SET TO NULL
		assertThrows(NullPointerException.class, () -> bindingManager.setPlatform(null));

		// SET TO SOMETHING
		final String commandId = "commandId";
		final Binding binding = new TestBinding(commandId, "na", "na", null,
				"atari", Binding.SYSTEM, null);
		bindingManager.addBinding(binding);
		assertTrue(bindingManager.getActiveBindingsFor(binding
						.getParameterizedCommand()).length == 0, "The binding shouldn't be active");
		bindingManager.setPlatform("atari");
		final TriggerSequence[] activeBindings = bindingManager
				.getActiveBindingsFor(binding.getParameterizedCommand());
		assertEquals(1,
				activeBindings.length, "The binding should become active");
		assertSame(TestBinding.TRIGGER_SEQUENCE, activeBindings[0], "The binding should be the same");
	}

	/**
	 * Tests whether the method works with a null argument. Tests that it works
	 * in a simple case.
	 *
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	@Test
	public void testGetBestActiveBindingFor() throws Exception {
		// Test with a null argument.
		final TriggerSequence[] activeBindingsForNull = bindingManager
				.getActiveBindingsFor((ParameterizedCommand) null);
		assertNotNull(activeBindingsForNull, "The active bindings for a command should never be null");
		assertTrue(
				activeBindingsForNull.length == 0, "The active binding for a null command should always be empty");

		// Test a simple case.
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme(scheme);
		final Set<String> activeContextIds = new HashSet<>();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final String commandId = "commandId";
		final String categoryId = "cat";
		Category cat = commandManager.getCategory(categoryId);
		cat.define("cat", "cat");
		Command cmd = commandManager.getCommand(commandId);
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
		cmd.define("na", "NA", cat, parms);
		Map<String, String> map = new HashMap<>();
		map.put("viewId", "outline");
		ParameterizedCommand outline = ParameterizedCommand.generateCommand(
				cmd, map);
		map = new HashMap<>();
		map.put("viewId", "console");
		ParameterizedCommand console = ParameterizedCommand.generateCommand(
				cmd, map);
		assertFalse(outline.equals(console));

		final Binding b2 = new KeyBinding(KeySequence.getInstance("M1+M2+V"),
				outline, "na", "na", null, null, null, Binding.SYSTEM);
		bindingManager.addBinding(b2);

		final Binding binding = new KeyBinding(KeySequence.getInstance("M1+V"),
				outline, "na", "na", null, null, null, Binding.SYSTEM);
		bindingManager.addBinding(binding);

		final Binding b3 = new KeyBinding(KeySequence.getInstance("M1+M2+C"),
				console, "na", "na", null, null, null, Binding.SYSTEM);
		bindingManager.addBinding(b3);

		// - above is all done as part of startup

		final TriggerSequence[] bindings = bindingManager
				.getActiveBindingsFor(binding.getParameterizedCommand());
		assertEquals(2, bindings.length);

		final TriggerSequence bestBinding = bindingManager
				.getBestActiveBindingFor(outline);
		assertEquals(binding.getTriggerSequence(), bestBinding);

		final TriggerSequence bestBinding2 = bindingManager
				.getBestActiveBindingFor(console);
		assertEquals(b3.getTriggerSequence(), bestBinding2);
	}
}
