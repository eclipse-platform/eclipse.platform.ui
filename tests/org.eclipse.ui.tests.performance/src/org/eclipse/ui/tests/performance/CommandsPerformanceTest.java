/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * Responsible for testing the commands, contexts and bindings architecture.
 * This test does not rely on the existence of the workbench; it operates purely
 * on JFace code and lower. See the method comments for descriptions of the
 * currently supported performance tests.
 * </p>
 * 
 * @since 3.1
 */
public final class CommandsPerformanceTest extends BasicPerformanceTest {

	/**
	 * <p>
	 * Constructs a branch of a context tree. This creates a branch of the given
	 * depth -- remembering the identifiers along the way. This method operates
	 * recursively.
	 * </p>
	 * <p>
	 * TODO This should add a bit of breadth to the tree.
	 * </p>
	 * 
	 * @param contextManager
	 *            The context manager in which the contexts should be defined;
	 *            must not be <code>null</code>.
	 * @param parent
	 *            The parent context identifier for the context to be created;
	 *            may be <code>null</code>.
	 * @param successors
	 *            The number of successors to create. The depth of the branch to
	 *            be created. If this number is zero, then a context is created,
	 *            but no recursive call is made.
	 * @param activeContextIds
	 *            The list of active context identifiers; must not be
	 *            <code>null</code>.
	 */
	private static final void createContext(
			final ContextManager contextManager, final String parent,
			final int successors, final List activeContextIds) {
		final int count = activeContextIds.size();
		final String contextString = "context" + count;
		final Context context = contextManager.getContext(contextString);
		context.define(contextString, contextString, parent);
		activeContextIds.add(contextString);

		if (successors == 0) {
			return;
		}

		createContext(contextManager, contextString, successors - 1,
				activeContextIds);
	}

	/**
	 * <p>
	 * Constructs a branch of a scheme tree. This creates a branch of the given
	 * depth -- remembering the schemes along the way. This method operates
	 * recursively.
	 * </p>
	 * <p>
	 * TODO This should add a bit of breadth to the tree.
	 * </p>
	 * 
	 * @param bindingManager
	 *            The binding manager in which the schemes should be defined;
	 *            must not be <code>null</code>.
	 * @param parent
	 *            The parent scheme identifier for the scheme to be created; may
	 *            be <code>null</code>.
	 * @param successors
	 *            The number of successors to create. The depth of the branch to
	 *            be created. If this number is zero, then a scheme is created,
	 *            but no recursive call is made.
	 * @param schemes
	 *            The list of created schemes; must not be <code>null</code>.
	 */
	private static final void createScheme(final BindingManager bindingManager,
			final String parent, final int successors, final List schemes) {
		final int count = schemes.size();
		final String schemeString = "scheme" + count;
		final Scheme scheme = bindingManager.getScheme(schemeString);
		scheme.define(schemeString, schemeString, parent);
		schemes.add(scheme);

		if (successors == 0) {
			return;
		}

		createScheme(bindingManager, schemeString, successors - 1, schemes);
	}

	/**
	 * The binding manager for the currently running test. <code>null</code>
	 * if no test is running.
	 */
	private BindingManager bindingManager = null;

	/**
	 * The command manager for the currently running test. <code>null</code>
	 * if no test is running.
	 */
	private CommandManager commandManager = null;

	/**
	 * The context manager for the currently running test. <code>null</code>
	 * if no test is running.
	 */
	private ContextManager contextManager = null;

	/**
	 * Constructs an instance of <code>CommandsPerformanceTest</code>.
	 * 
	 * @param testName
	 *            Test's name.
	 */
	public CommandsPerformanceTest(final String name) {
		super(name);
	}

	/**
	 * <p>
	 * Sets up a sufficiently complex set of bindings.
	 * </p>
	 * <p>
	 * At the time of writing, Eclipse's key binding set contains about five
	 * hundred bindings. Of these, 140 specify platform information, while only
	 * 5 specify locale information. About 40 are deletion markers. The deepest
	 * point in the context tree is four levels. There are two schemes.
	 * </p>
	 * <p>
	 * The test binding set contains five thousand bindings. About 1400 specify
	 * either locale or platform information. Five hundred are deletion markers.
	 * The deepest point in the context tree is 40 levels. There are twenty
	 * schemes.
	 * </p>
	 * <p>
	 * The depth of the locale and platform tree is the same in both real life
	 * and the test case. It is difficult to imagine why the locale list would
	 * ever be anything but four elements, or why the platform list would ever
	 * be anything but three elements.
	 * </p>
	 * 
	 * @throws NotDefinedException
	 *             If something went wrong initializing the active scheme.
	 */
	protected final void doSetUp() throws NotDefinedException, Exception {
		super.doSetUp();

		/*
		 * The constants to use in creating the various objects. The platform
		 * locale count must be greater than or equal to the number of deletion
		 * markers. Deletion markers are typically created based on the platform
		 * or locale.
		 */
		final int contextTreeDepth = 40;
		final int schemeDepth = 20;
		final int bindingCount = 5000;
		final int platformLocaleCount = 1400;
		final int deletionMarkers = 500;
		final String currentLocale = Locale.getDefault().toString();
		final String currentPlatform = Util.getWS();

		// Set-up a table of modifier keys.
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		final int modifierKeys0 = 0;
		final int modifierKeys1 = lookup.getAlt();
		final int modifierKeys2 = lookup.getCommand();
		final int modifierKeys3 = lookup.getCtrl();
		final int modifierKeys4 = lookup.getShift();
		final int modifierKeys5 = lookup.getAlt() | lookup.getCommand();
		final int modifierKeys6 = lookup.getAlt() | lookup.getCtrl();
		final int modifierKeys7 = lookup.getAlt() | lookup.getShift();
		final int modifierKeys8 = lookup.getCommand() | lookup.getCtrl();
		final int modifierKeys9 = lookup.getCommand() | lookup.getShift();
		final int modifierKeys10 = lookup.getCtrl() | lookup.getShift();
		final int modifierKeys11 = lookup.getAlt() | lookup.getCommand()
				| lookup.getCtrl();
		final int modifierKeys12 = lookup.getAlt() | lookup.getCommand()
				| lookup.getShift();
		final int modifierKeys13 = lookup.getAlt() | lookup.getCtrl()
				| lookup.getShift();
		final int modifierKeys14 = lookup.getCommand() | lookup.getCtrl()
				| lookup.getShift();
		final int modifierKeys15 = lookup.getAlt() | lookup.getCommand()
				| lookup.getCtrl() | lookup.getShift();
		final int[] modifierKeyTable = { modifierKeys0, modifierKeys1,
				modifierKeys2, modifierKeys3, modifierKeys4, modifierKeys5,
				modifierKeys6, modifierKeys7, modifierKeys8, modifierKeys9,
				modifierKeys10, modifierKeys11, modifierKeys12, modifierKeys13,
				modifierKeys14, modifierKeys15 };

		// Initialize the command manager.
		commandManager = new CommandManager();

		// Initialize the contexts.
		contextManager = new ContextManager();
		final List activeContextIds = new ArrayList();
		createContext(contextManager, null, contextTreeDepth, activeContextIds);
		contextManager.setActiveContextIds(new HashSet(activeContextIds));

		// Initialize the schemes.
		bindingManager = new BindingManager(contextManager, commandManager);
		final List schemes = new ArrayList();
		createScheme(bindingManager, null, schemeDepth, schemes);
		bindingManager
				.setActiveScheme((Scheme) schemes.get(schemes.size() - 1));

		// Create the deletion markers.
		final Binding[] bindings = new Binding[bindingCount];
		for (int i = 0; i < deletionMarkers; i++) {
			/*
			 * Set-up the locale and platform. These are based on the numbers
			 * given above.
			 */
			String locale = null;
			String platform = null;

			if (i < platformLocaleCount) {
				switch (i % 4) {
				case 0:
					locale = currentLocale;
					break;
				case 1:
					platform = currentPlatform;
					break;
				case 2:
					locale = "gibberish";
					break;
				case 3:
					platform = "gibberish";
					break;
				}
			}

			// Build a key sequence.
			final char character = (char) ('A' + (i % 26));
			final int modifierKeys = modifierKeyTable[(i / 26)
					% modifierKeyTable.length];
			final KeyStroke keyStroke = KeyStroke.getInstance(modifierKeys,
					character);
			final KeySequence keySequence = KeySequence.getInstance(keyStroke);

			// Build the other parameters.
			final String schemeId = ((Scheme) schemes.get(i % schemes.size()))
					.getId();
			final String contextId = (String) activeContextIds.get(i
					% activeContextIds.size());
			final int type = (i % 2);

			// Construct the binding.
			final Binding binding = new KeyBinding(keySequence, null, schemeId,
					contextId, locale, platform, null, type);
			bindings[i] = binding;
		}

		/*
		 * Now create the regular bindings. By using the same loop structure and
		 * resetting the index to zero, we ensure that the deletion markers will
		 * actually delete something.
		 */
		for (int i = 0; i < bindingCount - deletionMarkers; i++) {
			/*
			 * Set-up the locale and platform for those bindings that will not
			 * be used to match the above deletion markers. These are based on
			 * the numbers given above.
			 */
			String locale = null;
			String platform = null;

			if ((i > deletionMarkers) && (i < platformLocaleCount)) {
				switch (i % 4) {
				case 0:
					locale = currentLocale;
					break;
				case 1:
					platform = currentPlatform;
					break;
				case 2:
					locale = "gibberish";
					break;
				case 3:
					platform = "gibberish";
					break;
				}
			}

			// Build a key sequence.
			final char character = (char) ('A' + (i % 26));
			final int modifierKeys = modifierKeyTable[(i / 26)
					% modifierKeyTable.length];
			final KeyStroke keyStroke = KeyStroke.getInstance(modifierKeys,
					character);
			final KeySequence keySequence = KeySequence.getInstance(keyStroke);

			// Build the other parameters.
			final String commandId = "command" + i;
			final String schemeId = ((Scheme) schemes.get(i % schemes.size()))
					.getId();
			final String contextId = (String) activeContextIds.get(i
					% activeContextIds.size());
			final int type = (i % 2);

			// Construct the binding.
			final Command command = commandManager.getCommand(commandId);
			final ParameterizedCommand parameterizedCommand = new ParameterizedCommand(
					command, null);
			final Binding binding = new KeyBinding(keySequence,
					parameterizedCommand, schemeId, contextId, locale,
					platform, null, type);
			bindings[i + deletionMarkers] = binding;
		}
		bindingManager.setBindings(bindings);
	}

	protected final void doTearDown() throws Exception {
		bindingManager = null;
		commandManager = null;
		contextManager = null;
		super.doTearDown();
	}

	/**
	 * <p>
	 * Tests how long it takes to access the cache if no conditions have
	 * changed. It measures how long it takes to look up the computation from
	 * the cache one million times.
	 * </p>
	 * 
	 * @throws ParseException
	 *             If "CTRL+F" can't be parsed for some strange reason.
	 */
	public final void testBindingCacheHitHard() throws ParseException {
		// Constants
		final int cacheHits = 1000000;
		final KeySequence keySequence = KeySequence.getInstance("CTRL+F");

		// Compute once.
		bindingManager.getPartialMatches(keySequence);

		// Time how long it takes to access the cache;
		startMeasuring();
		for (int i = 0; i < cacheHits; i++) {
			bindingManager.getPartialMatches(keySequence);
		}
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
	}

	/**
	 * <p>
	 * Tests how long it takes to access the cache if no conditions have
	 * changed. It measures how long it takes to look up the computation from
	 * the cache one million times. In this test, the look-up is done in reverse --
	 * from command identifier to trigger.
	 * </p>
	 * 
	 * @throws ParseException
	 *             If "CTRL+F" can't be parsed for some strange reason.
	 */
	public final void testBindingCacheHitHardReverse() throws ParseException {
		// Constants
		final int cacheHits = 1000000;
		final KeySequence keySequence = KeySequence.getInstance("CTRL+F");

		// Compute once.
		bindingManager.getPartialMatches(keySequence);

		// Time how long it takes to access the cache;
		startMeasuring();
		for (int i = 0; i < cacheHits; i++) {
			bindingManager.getActiveBindingsFor((ParameterizedCommand) null);
		}
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
	}

	/**
	 * <p>
	 * Tests how long it takes to access the cache if the conditions have
	 * changed, but the cache contains a matching entry. It measures how long it
	 * takes to look up the computation from the cache forty thousand times.
	 * </p>
	 * 
	 * @throws ParseException
	 *             If "CTRL+F" can't be parsed for some strange reason.
	 */
	public final void testBindingCacheHitSoft() throws ParseException {
		// Constants
		final int cacheHits = 10000;
		final KeySequence keySequence = KeySequence.getInstance("CTRL+F");

		// Compute once for each context set.
		final Set contextSet1 = contextManager.getActiveContextIds();
		bindingManager.getPartialMatches(keySequence);
		final List contextList = new ArrayList(contextSet1);
		contextList.remove(contextList.size() - 1);
		final Set contextSet2 = new HashSet(contextList);
		contextManager.setActiveContextIds(contextSet2);
		bindingManager.getPartialMatches(keySequence);

		// Time how long it takes to access the cache;
		startMeasuring();
		for (int i = 0; i < cacheHits; i++) {
			if ((i % 2) == 0) {
				contextManager.setActiveContextIds(contextSet1);
			} else {
				contextManager.setActiveContextIds(contextSet2);
			}
			bindingManager.getPartialMatches(keySequence);
		}
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
	}

	/**
	 * <p>
	 * Tests how long it takes to do a full computation (i.e., a cache miss) on
	 * an exceptionally large set of bindings. The binding set tries to mimick
	 * some of the same properties of a "real" binding set.
	 * </p>
	 * 
	 * @throws ParseException
	 *             If "CTRL+F" can't be parsed for some strange reason.
	 */
	public final void testBindingCacheMissLarge() throws ParseException {
		// Constants
		final KeySequence keySequence = KeySequence.getInstance("CTRL+F");

		// Time how long it takes to solve the binding set.
		startMeasuring();
		bindingManager.getPartialMatches(keySequence);
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
	}
}
