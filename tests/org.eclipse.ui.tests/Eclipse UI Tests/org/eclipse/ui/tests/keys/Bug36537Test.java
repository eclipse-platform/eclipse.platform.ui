/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.keys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.commands.KeySequenceBindingDefinition;
import org.eclipse.ui.internal.commands.MutableCommandManager;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests Bug 36537
 * 
 * @since 3.0
 */
public class Bug36537Test extends UITestCase {

	/**
	 * Constructor for Bug36537Test.
	 * 
	 * @param name
	 *           The name of the test
	 */
	public Bug36537Test(String name) {
		super(name);
	}

	/**
	 * Tests that there are no redundant key bindings defined in the
	 * application.
	 */
	public void testForRedundantKeySequenceBindings() {
		IWorkbenchWindow window = openTestWindow();
		Workbench workbench = (Workbench) window.getWorkbench();
		// TODO this is a bad downcast and will fail in the future.
		MutableCommandManager mutableCommandManager = (MutableCommandManager) workbench.getCommandSupport().getCommandManager();
		List keySequenceBindings = mutableCommandManager.getCommandRegistry().getKeySequenceBindingDefinitions();
		Iterator keySequenceBindingItr = keySequenceBindings.iterator();
		Map keySequenceBindingsByKeySequence = new HashMap();

		while (keySequenceBindingItr.hasNext()) {
			// Retrieve the key binding.
			KeySequenceBindingDefinition keySequenceBinding = (KeySequenceBindingDefinition) keySequenceBindingItr.next();

			// Find the point the bindings with matching key sequences.
			KeySequence keySequence = keySequenceBinding.getKeySequence();
			List matches = (List) keySequenceBindingsByKeySequence.get(keySequence);
			if (matches == null) {
				matches = new ArrayList();
				keySequenceBindingsByKeySequence.put(keySequence, matches);
			}

			// Check that we don't have any redundancy or other wackiness.
			Iterator matchItr = matches.iterator();
			while (matchItr.hasNext()) {
				KeySequenceBindingDefinition definition = (KeySequenceBindingDefinition) matchItr.next();
				String commandA = keySequenceBinding.getCommandId();
				String commandB = definition.getCommandId();
				String contextA = keySequenceBinding.getContextId();
				String contextB = definition.getContextId();
				String keyConfA = keySequenceBinding.getKeyConfigurationId();
				String keyConfB = definition.getKeyConfigurationId();
				String localeA = keySequenceBinding.getLocale();
				String localeB = definition.getLocale();
				String platformA = keySequenceBinding.getPlatform();
				String platformB = definition.getPlatform();

				boolean same = true;
				int nullMatches = 0;
				same &= (commandA == null) ? (commandB == null) : (commandA.equals(commandB));
				same &= (contextA == null) || (contextB == null) || (contextA.equals(contextB));
				if (((contextA == null) || (contextB == null)) && (contextA != contextB)) {
					nullMatches++;
				}
				same &= (keyConfA == null) || (keyConfB == null) || (keyConfA.equals(keyConfB));
				if (((keyConfA == null) || (keyConfB == null)) && (keyConfA != keyConfB)) {
					nullMatches++;
				}
				same &= (localeA == null) || (localeB == null) || (localeA.equals(localeB));
				if (((localeA == null) || (localeB == null)) && (localeA != localeB)) {
					nullMatches++;
				}
				same &= (platformA == null) || (platformB == null) || (platformA.equals(platformB));
				if (((platformA == null) || (platformB == null)) && (platformA != platformB)) {
					nullMatches++;
				}

				assertFalse("Redundant key bindings: " + keySequenceBinding + ", " + definition, same && (nullMatches < 1)); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Add the key binding.
			matches.add(keySequenceBinding);
		}
	}
}
