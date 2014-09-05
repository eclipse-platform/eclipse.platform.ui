/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.keys.IBindingService;

/**
 * Tests whether the "org.eclipse.ui.bindings" extension point can be added and
 * removed dynamically.
 * 
 * @since 3.1.1
 */
public final class BindingsExtensionDynamicTest extends DynamicTestCase {

	/**
	 * Constructs a new instance of <code>BindingsExtensionDynamicTest</code>.
	 * 
	 * @param testName
	 *            The name of the test; may be <code>null</code>.
	 */
	public BindingsExtensionDynamicTest(final String testName) {
		super(testName);
	}

	/**
	 * Returns the full-qualified identifier of the extension to be tested.
	 * 
	 * @return The extension identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionId() {
		return "bindingsExtensionDynamicTest.testDynamicBindingAddition";
	}

	/**
	 * Returns the unqualified identifier of the extension point to be tested.
	 * 
	 * @return The extension point identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_BINDINGS;
	}

	/**
	 * Returns the relative location of the folder on disk containing the
	 * plugin.xml file.
	 * 
	 * @return The relative install location; never <code>null</code>.
	 */
	@Override
	protected final String getInstallLocation() {
		return "data/org.eclipse.bindingsExtensionDynamicTest";
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
	public void testBindings() throws ParseException {
		final IBindingService bindingService = (IBindingService) getWorkbench()
				.getAdapter(IBindingService.class);
		final TriggerSequence triggerSequence = KeySequence.getInstance("M1+W");
		Binding[] bindings;
		Scheme scheme;
		boolean found;

		found = false;
		bindings = bindingService.getBindings();
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				final Binding binding = bindings[i];
				if ("monkey".equals(binding.getSchemeId())
						&& IContextIds.CONTEXT_ID_WINDOW.equals(binding
								.getContextId())
						&& "org.eclipse.ui.views.showView".equals(binding
								.getParameterizedCommand().getId())
						&& binding.getParameterizedCommand().getParameterMap()
								.containsKey(
										IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID)
						&& binding.getPlatform() == null
						&& binding.getLocale() == null
						&& binding.getType() == Binding.SYSTEM
						&& triggerSequence.equals(binding.getTriggerSequence())) {
					found = true;

				}
			}
		}
		assertTrue(!found);
		scheme = bindingService.getScheme("monkey");
		try {
			scheme.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}

		getBundle();

		found = false;
		bindings = bindingService.getBindings();
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				final Binding binding = bindings[i];
				if ("monkey".equals(binding.getSchemeId())
						&& IContextIds.CONTEXT_ID_WINDOW.equals(binding
								.getContextId())
						&& "org.eclipse.ui.views.showView".equals(binding
								.getParameterizedCommand().getId())
						&& binding.getParameterizedCommand().getParameterMap()
								.containsKey(
										IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID)
						&& binding.getPlatform() == null
						&& binding.getLocale() == null
						&& binding.getType() == Binding.SYSTEM
						&& triggerSequence.equals(binding.getTriggerSequence())) {
					found = true;

				}
			}
		}
		assertTrue(found);
		scheme = bindingService.getScheme("monkey");
		try {
			assertTrue("Monkey".equals(scheme.getName()));
		} catch (final NotDefinedException e) {
			fail();
		}

		removeBundle();

		found = false;
		bindings = bindingService.getBindings();
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				final Binding binding = bindings[i];
				if ("monkey".equals(binding.getSchemeId())
						&& IContextIds.CONTEXT_ID_WINDOW.equals(binding
								.getContextId())
						&& "org.eclipse.ui.views.showView".equals(binding
								.getParameterizedCommand().getId())
						&& binding.getParameterizedCommand().getParameterMap()
								.containsKey(
										IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID)
						&& binding.getPlatform() == null
						&& binding.getLocale() == null
						&& binding.getType() == Binding.SYSTEM
						&& triggerSequence.equals(binding.getTriggerSequence())) {
					found = true;

				}
			}
		}
		assertTrue(!found);
		scheme = bindingService.getScheme("monkey");
		try {
			scheme.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
	}
}
