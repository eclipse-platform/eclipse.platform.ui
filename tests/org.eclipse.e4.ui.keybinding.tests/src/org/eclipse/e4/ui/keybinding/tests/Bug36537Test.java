/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.keybinding.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.tests.harness.util.UITestCase;

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
	 *            The name of the test
	 */
	public Bug36537Test(String name) {
		super(name);
	}

	/**
	 * Tests that there are no redundant key bindings defined in the
	 * application.
	 */
	public void testForRedundantKeySequenceBindings() {
		final IWorkbenchWindow window = openTestWindow();
		final IWorkbench workbench = window.getWorkbench();
		final IBindingService bindingService = (IBindingService) workbench
				.getAdapter(IBindingService.class);
		final Binding[] bindings = bindingService.getBindings();
		final int bindingCount = bindings.length;
		Map keySequenceBindingsByKeySequence = new HashMap();

		for (int i = 0; i < bindingCount; i++) {
			// Retrieve the key binding.
			final Binding binding = bindings[i];

			// Find the point the bindings with matching key sequences.
			TriggerSequence triggerSequence = binding.getTriggerSequence();
			List matches = (List) keySequenceBindingsByKeySequence
					.get(triggerSequence);
			if (matches == null) {
				matches = new ArrayList();
				keySequenceBindingsByKeySequence.put(triggerSequence, matches);
			}

			// Check that we don't have any redundancy or other wackiness.
			Iterator matchItr = matches.iterator();
			while (matchItr.hasNext()) {
				final Binding matchedBinding = (Binding) matchItr.next();
				ParameterizedCommand commandA = binding
						.getParameterizedCommand();
				ParameterizedCommand commandB = matchedBinding
						.getParameterizedCommand();
				String contextA = binding.getContextId();
				String contextB = matchedBinding.getContextId();
				String keyConfA = binding.getSchemeId();
				String keyConfB = matchedBinding.getSchemeId();
				String localeA = binding.getLocale();
				String localeB = matchedBinding.getLocale();
				String platformA = binding.getPlatform();
				String platformB = matchedBinding.getPlatform();

				boolean same = true;
				int nullMatches = 0;
				same &= (commandA == null) ? (commandB == null) : (commandA
						.equals(commandB));
				same &= (contextA == null) || (contextB == null)
						|| (contextA.equals(contextB));
				if (((contextA == null) || (contextB == null))
						&& (contextA != contextB)) {
					nullMatches++;
				}
				same &= (keyConfA == null) || (keyConfB == null)
						|| (keyConfA.equals(keyConfB));
				if (((keyConfA == null) || (keyConfB == null))
						&& (keyConfA != keyConfB)) {
					nullMatches++;
				}
				same &= (localeA == null) || (localeB == null)
						|| (localeA.equals(localeB));
				if (((localeA == null) || (localeB == null))
						&& (localeA != localeB)) {
					nullMatches++;
				}
				same &= (platformA == null) || (platformB == null)
						|| (platformA.equals(platformB));
				if (((platformA == null) || (platformB == null))
						&& (platformA != platformB)) {
					nullMatches++;
				}

				assertFalse(
						"Redundant key bindings: " + binding + ", " + matchedBinding, same && (nullMatches < 1)); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Add the key binding.
			matches.add(binding);
		}
	}
}
