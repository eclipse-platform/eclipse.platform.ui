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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Test cases covering the various interaction between bindings. Bindings that
 * have been removed. Bindings that have been added. Inheritance of various
 * properties.
 * 
 * @since 3.1
 */
public final class BindingInteractionsTest extends UITestCase {

	/**
	 * A binding that can be used for testing purposes. This guarantees that the
	 * properties tested are inherent to all bindings, and not just a specific
	 * type of bindings.
	 */
	private static final class TestBinding extends Binding {

		/**
		 * A simple trigger sequence for this test.
		 */
		private static final class TestTriggerSequence extends TriggerSequence {

			/**
			 * Constructs a new instance of <code>TestTriggerSequence</code>.
			 * 
			 * @param myTriggers
			 *            The triggers to use in constructing this sequence;
			 *            must not be <code>null</code>.
			 */
			public TestTriggerSequence(final List myTriggers) {
				super(myTriggers);
			}

			public final String format() {
				return toString();
			}
		}

		/**
		 * A trigger sequence to be used by all test bindings. This value is
		 * never <code>null</code>.
		 */
		private static final TriggerSequence TRIGGER_SEQUENCE = new TestTriggerSequence(
				Collections.EMPTY_LIST);

		/**
		 * Constructs a new instance of <code>TestBinding</code>.
		 * 
		 * @param commandId
		 *            The command identifier triggered by this binding; must not
		 *            be <code>null</code>.
		 * @param schemeId
		 *            The scheme to which this binding belongs; must not be
		 *            <code>null</code>.
		 * @param contextId
		 *            The context to which this binding belongs; must not be
		 *            <code>null</code>.
		 * @param locale
		 *            The locale for which this binding applies; may be
		 *            <code>null</code>.
		 * @param platform
		 *            The platform for which this binding applies; may be
		 *            <code>null</code>.
		 * @param type
		 *            The type of binding.
		 */
		private TestBinding(final String commandId, final String schemeId,
				final String contextId, final String locale,
				final String platform, final int type) {
			super(commandId, schemeId, contextId, locale, platform, null, type);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.bindings.Binding#getTriggerSequence()
		 */
		public final TriggerSequence getTriggerSequence() {
			return TRIGGER_SEQUENCE;
		}

		public final String toString() {
			return Util.ZERO_LENGTH_STRING;
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
	 * Constructor for <code>BindingInteractionsTest</code>.
	 * 
	 * @param name
	 *            The name of the test
	 */
	public BindingInteractionsTest(final String name) {
		super(name);
	}

	/**
	 * Creates a new context manager and a binding manager for use in the test
	 * cases.
	 */
	protected void doSetUp() {
		contextManager = new ContextManager();
		bindingManager = new BindingManager(contextManager);
	}

	/**
	 * Releases the context manager and binding manager for garbage collection.
	 */
	protected void doTearDown() {
		contextManager = null;
		bindingManager = null;
	}

	/**
	 * <p>
	 * Tests whether two identical bindings lead to a conflict.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testConflict() throws NotDefinedException {
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final Binding binding1 = new TestBinding("conflict1", "na", "na", null,
				null, Binding.SYSTEM);
		final Binding binding2 = new TestBinding("conflict2", "na", "na", null,
				null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindingManager.setBindings(bindings);

		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals("Neither binding should be active", null, activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether a plug-in developer can override a binding in a child
	 * context.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testContextOverride() throws NotDefinedException {
		final Context parentContext = contextManager.getContext("parent");
		parentContext.define("parent", "parent context", null);

		final Context childContext = contextManager.getContext("child");
		childContext.define("child", "child context", "parent");

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");

		final Binding binding1 = new TestBinding("parent", "na", "parent",
				null, null, Binding.SYSTEM);
		final Binding binding2 = new TestBinding("child", "na", "child", null,
				null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindingManager.setBindings(bindings);

		// Only "parent"
		final Set activeContextIds = new HashSet();
		activeContextIds.add("parent");
		contextManager.setActiveContextIds(activeContextIds);

		Map activeBindings = bindingManager.getActiveBindings();
		Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals(
				"When only the parent context is active, only the parent binding is active.",
				binding1.getCommandId(), activeCommandId);

		// Only "child"
		activeContextIds.clear();
		activeContextIds.add("child");
		contextManager.setActiveContextIds(activeContextIds);

		activeBindings = bindingManager.getActiveBindings();
		activeCommandId = activeBindings.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals(
				"When only the child context is active, only the child binding is active.",
				binding2.getCommandId(), activeCommandId);

		// Both "parent" and "child"
		activeContextIds.add("parent");
		contextManager.setActiveContextIds(activeContextIds);

		activeBindings = bindingManager.getActiveBindings();
		activeCommandId = activeBindings.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals(
				"When both contexts are active, only the child binding is active.",
				binding2.getCommandId(), activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether a user-defined deletion actually works.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testDeletedBinding() throws NotDefinedException {
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final Binding binding1 = new TestBinding(null, "na", "na", null, null,
				Binding.USER);
		final Binding binding2 = new TestBinding("system", "na", "na", null,
				null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindingManager.setBindings(bindings);

		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals("The user should be able to remove bindings", null,
				activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether a system deletion for a different locale or platform
	 * actually works. It shouldn't. Deletions should only work if they specify
	 * a matching locale or platform.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testDeletedBindingUnnecessarily() throws NotDefinedException {
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final Binding binding1 = new TestBinding(null, "na", "na",
				"not the current locale", null, Binding.SYSTEM);
		final Binding binding2 = new TestBinding("system", "na", "na", null,
				null, Binding.SYSTEM);
		final Binding binding3 = new TestBinding(null, "na", "na", null,
				"not the current platform", Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindings.add(binding3);
		bindingManager.setBindings(bindings);

		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals(
				"A binding should not cause a deletion if its locale or platform doesn't match",
				binding2.getCommandId(), activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether a user can add a binding to the same conditions once
	 * they've deleted the system binding.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testDeletedBindingWithAddition() throws NotDefinedException {
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final Binding binding1 = new TestBinding(null, "na", "na", null, null,
				Binding.USER);
		final Binding binding2 = new TestBinding("system", "na", "na", null,
				null, Binding.SYSTEM);
		final Binding binding3 = new TestBinding("user", "na", "na", null,
				null, Binding.USER);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindings.add(binding3);
		bindingManager.setBindings(bindings);

		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals("The user redefine a particular binding", binding3
				.getCommandId(), activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether a plug-in developer can override a binding for a particular
	 * locale.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testLocaleOverride() throws NotDefinedException {
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final Binding binding1 = new TestBinding("base", "na", "na", null,
				null, Binding.SYSTEM);
		final Binding binding2 = new TestBinding(null, "na", "na", Locale
				.getDefault().toString(), null, Binding.SYSTEM);
		final Binding binding3 = new TestBinding("locale-specific", "na", "na",
				Locale.getDefault().toString(), null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindings.add(binding3);
		bindingManager.setBindings(bindings);

		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals(
				"A plug-in developer should be able to change a binding for a locale",
				binding3.getCommandId(), activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether a plug-in developer can override a binding for a particular
	 * platform.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testPlatformOverride() throws NotDefinedException {
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final Binding binding1 = new TestBinding("base", "na", "na", null,
				null, Binding.SYSTEM);
		final Binding binding2 = new TestBinding(null, "na", "na", null, SWT
				.getPlatform(), Binding.SYSTEM);
		final Binding binding3 = new TestBinding("platform-specific", "na",
				"na", null, SWT.getPlatform(), Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindings.add(binding3);
		bindingManager.setBindings(bindings);

		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals(
				"A plug-in developer should be able to change a binding for a platform",
				binding3.getCommandId(), activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether a binding in a child scheme will override a binding in a
	 * parent scheme. The test is set-up as follows:
	 * </p>
	 * <ul>
	 * <li>Binding1(commandId="child",schemeId="child",contextId="na",locale=null,platform=null,type=SYSTEM)</li>
	 * <li>Binding2(commandId="parent",schemeId="parent",contextId="na",locale=null,platform=null,type=SYSTEM)</li>
	 * </ul>
	 * <p>
	 * Binding1 should win.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testSchemeOverride() throws NotDefinedException {
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme parentScheme = bindingManager.getScheme("parent");
		parentScheme.define("parent", "parent scheme", null);

		final Scheme childScheme = bindingManager.getScheme("child");
		childScheme.define("child", "child scheme", "parent");

		bindingManager.setActiveScheme("child");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final Binding binding1 = new TestBinding("child", "child", "na", null,
				null, Binding.SYSTEM);
		final Binding binding2 = new TestBinding("parent", "parent", "na",
				null, null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindingManager.setBindings(bindings);

		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals("The binding from the child scheme should be active",
				binding1.getCommandId(), activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether a binding in a child scheme will override a binding in a
	 * parent scheme -- regardless of their type. The test is set-up as follows:
	 * </p>
	 * <ul>
	 * <li>Binding1(commandId="child",schemeId="child",contextId="na",locale=null,platform=null,type=SYSTEM)</li>
	 * <li>Binding2(commandId="parent",schemeId="parent",contextId="na",locale=null,platform=null,type=USER)</li>
	 * </ul>
	 * <p>
	 * Binding1 should win.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testSchemeOverrideDifferentTypes() throws NotDefinedException {
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme parentScheme = bindingManager.getScheme("parent");
		parentScheme.define("parent", "parent scheme", null);

		final Scheme childScheme = bindingManager.getScheme("child");
		childScheme.define("child", "child scheme", "parent");

		bindingManager.setActiveScheme("child");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final Binding binding1 = new TestBinding("child", "child", "na", null,
				null, Binding.SYSTEM);
		final Binding binding2 = new TestBinding("parent", "parent", "na",
				null, null, Binding.USER);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindingManager.setBindings(bindings);

		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals("The binding from the child scheme should be active",
				binding1.getCommandId(), activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether two bindings defined for sibling active contexts leads to a
	 * conflict.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testSiblingContextConflict() throws NotDefinedException {
		final Context context1 = contextManager.getContext("sibling1");
		context1.define("sibling1", "first sibling context", null);

		final Context context2 = contextManager.getContext("sibling2");
		context2.define("sibling2", "second sibling context", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");

		final Binding binding1 = new TestBinding("sibling1", "na", "sibling1",
				null, null, Binding.SYSTEM);
		final Binding binding2 = new TestBinding("sibling2", "na", "sibling2",
				null, null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindingManager.setBindings(bindings);

		// One sibling active
		final Set activeContextIds = new HashSet();
		activeContextIds.add("sibling1");
		contextManager.setActiveContextIds(activeContextIds);

		Map activeBindings = bindingManager.getActiveBindings();
		Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals(
				"When only the first sibling is active, only the first binding is active",
				binding1.getCommandId(), activeCommandId);

		// Other sibling active
		activeContextIds.clear();
		activeContextIds.add("sibling2");
		contextManager.setActiveContextIds(activeContextIds);

		activeBindings = bindingManager.getActiveBindings();
		activeCommandId = activeBindings.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals(
				"When only the second sibling is active, only the second binding is active",
				binding2.getCommandId(), activeCommandId);

		// Both siblings are active
		activeContextIds.add("sibling1");
		contextManager.setActiveContextIds(activeContextIds);

		activeBindings = bindingManager.getActiveBindings();
		activeCommandId = activeBindings.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals("When both contexts are active, a conflict should occur",
				null, activeCommandId);
	}

	/**
	 * <p>
	 * Tests whether a user-defined binding will override the exact same binding
	 * defined in the system.
	 * </p>
	 * <ul>
	 * <li>Binding1(commandId="user",schemeId="na",contextId="na",locale=null,platform=null,type=USER)</li>
	 * <li>Binding2(commandId="system",schemeId="na",contextId="na",locale=null,platform=null,type=SYSTEM)</li>
	 * </ul>
	 * <p>
	 * Binding1 should win.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If the scheme we try to activate is not defined.
	 */
	public void testUserOverride() throws NotDefinedException {
		final Context context = contextManager.getContext("na");
		context.define("name", "description", null);

		final Scheme scheme = bindingManager.getScheme("na");
		scheme.define("name", "description", null);

		bindingManager.setActiveScheme("na");
		final Set activeContextIds = new HashSet();
		activeContextIds.add("na");
		contextManager.setActiveContextIds(activeContextIds);

		final Binding binding1 = new TestBinding("user", "na", "na", null,
				null, Binding.USER);
		final Binding binding2 = new TestBinding("system", "na", "na", null,
				null, Binding.SYSTEM);
		final Set bindings = new HashSet();
		bindings.add(binding1);
		bindings.add(binding2);
		bindingManager.setBindings(bindings);

		final Map activeBindings = bindingManager.getActiveBindings();
		final Object activeCommandId = activeBindings
				.get(TestBinding.TRIGGER_SEQUENCE);

		assertEquals("The user-defined binding should be active", binding1
				.getCommandId(), activeCommandId);
	}
}
