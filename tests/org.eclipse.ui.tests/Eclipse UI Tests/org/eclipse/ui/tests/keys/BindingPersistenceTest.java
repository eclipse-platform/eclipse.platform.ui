/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.keys;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test cases covering the various interaction between bindings. Bindings that
 * have been removed. Bindings that have been added. Inheritance of various
 * properties.
 * 
 * @since 3.1
 */
public final class BindingPersistenceTest extends UITestCase {

	/**
	 * Constructor for <code>BindingPersistenceTest</code>.
	 * 
	 * @param name
	 *            The name of the test
	 */
	public BindingPersistenceTest(final String name) {
		super(name);
	}

	/**
	 * <p>
	 * Tests whether the preference store will be read automatically when a
	 * change to the preference store is made.
	 * </p>
	 * 
	 * @throws ParseException
	 *             If "ALT+SHIFT+Q A" cannot be parsed by KeySequence.
	 */
	public final void testAutoLoad() throws ParseException {
		// Get the services.
		ICommandService commandService = (ICommandService) fWorkbench
				.getAdapter(ICommandService.class);
		IBindingService bindingService = (IBindingService) fWorkbench
				.getAdapter(IBindingService.class);
		bindingService.readRegistryAndPreferences(commandService);

		// Check the pre-conditions.
		final String emacsSchemeId = "org.eclipse.ui.emacsAcceleratorConfiguration";
		assertFalse("The active scheme should be Emacs yet", emacsSchemeId
				.equals(bindingService.getActiveScheme().getId()));
		final KeySequence formalKeySequence = KeySequence
				.getInstance("ALT+SHIFT+Q A");
		final String commandId = "org.eclipse.ui.views.showView";
		Binding[] bindings = bindingService.getBindings();
		int i;
		for (i = 0; i < bindings.length; i++) {
			final Binding binding = bindings[i];
			if ((binding.getType() == Binding.USER)
					&& (formalKeySequence.equals(binding.getTriggerSequence()))) {
				final ParameterizedCommand command = binding
						.getParameterizedCommand();
				final String actualCommandId = (command == null) ? null
						: command.getCommand().getId();
				assertFalse("The command should not yet be bound", commandId
						.equals(actualCommandId));
				break;
			}
		}
		assertEquals("There shouldn't be a matching command yet",
				bindings.length, i);

		// Modify the preference store.
		final IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		store
				.setValue(
						"org.eclipse.ui.commands",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?><org.eclipse.ui.commands><activeKeyConfiguration keyConfigurationId=\""
								+ emacsSchemeId
								+ "\"/><keyBinding commandId=\""
								+ commandId
								+ "\" contextId=\"org.eclipse.ui.contexts.window\" keyConfigurationId=\"org.eclipse.ui.defaultAcceleratorConfiguration\" keySequence=\""
								+ formalKeySequence
								+ "\"/></org.eclipse.ui.commands>");

		// Check that the values have changed.
		assertEquals("The active scheme should now be Emacs", emacsSchemeId,
				bindingService.getActiveScheme().getId());
		bindings = bindingService.getBindings();
		for (i = 0; i < bindings.length; i++) {
			final Binding binding = bindings[i];
			if ((binding.getType() == Binding.USER)
					&& (formalKeySequence.equals(binding.getTriggerSequence()))) {
				final ParameterizedCommand command = binding
						.getParameterizedCommand();
				final String actualCommandId = (command == null) ? null
						: command.getCommand().getId();
				assertEquals("The command should be bound to 'ALT+SHIFT+Q A'",
						commandId, actualCommandId);
				break;
			}
		}
		assertFalse("There should be a matching command now",
				(bindings.length == i));
	}

	public final void testSinglePlatform() throws Exception {
		// Get the services.
		ICommandService commandService = (ICommandService) fWorkbench
				.getAdapter(ICommandService.class);
		IBindingService bindingService = (IBindingService) fWorkbench
				.getAdapter(IBindingService.class);

		ParameterizedCommand about = new ParameterizedCommand(commandService
				.getCommand("org.eclipse.ui.help.aboutAction"), null);
		KeySequence m18A = KeySequence.getInstance("M1+8 A");
		KeySequence m18B = KeySequence.getInstance("M1+8 B");
		int numAboutBindings = 0;

		Binding[] bindings = bindingService.getBindings();
		for (int i = 0; i < bindings.length; i++) {
			final Binding binding = bindings[i];
			if (binding.getType() == Binding.SYSTEM) {
				String platform = binding.getPlatform();
				int idx = (platform == null ? -1 : platform.indexOf(','));
				assertEquals(binding.toString(), -1, idx);
				if (about.equals(binding.getParameterizedCommand())) {
					if (m18A.equals(binding.getTriggerSequence())) {
						numAboutBindings++;
						assertNull("M+8 A", binding.getPlatform());
					} else if (m18B.equals(binding.getTriggerSequence())) {
						numAboutBindings++;
						// assertEquals(Util.WS_CARBON, binding.getPlatform());
						// temp work around for honouring carbon bindings
						assertTrue("failure for platform: "
								+ binding.getPlatform(), Util.WS_CARBON
								.equals(binding.getPlatform())
								|| Util.WS_COCOA.equals(binding.getPlatform()));
					}
				}
			}
		}
		if (Util.WS_CARBON.equals(SWT.getPlatform())
				|| Util.WS_COCOA.equals(SWT.getPlatform())) {
			assertEquals(2, numAboutBindings);
		} else {
			assertEquals(1, numAboutBindings);
		}
	}

	public final void testBindingTransform() throws Exception {
		ICommandService commandService = (ICommandService) fWorkbench
				.getAdapter(ICommandService.class);
		IBindingService bindingService = (IBindingService) fWorkbench
				.getAdapter(IBindingService.class);

		ParameterizedCommand addWS = new ParameterizedCommand(commandService
				.getCommand("org.eclipse.ui.navigate.addToWorkingSet"), null);
		KeySequence m18w = KeySequence.getInstance("M1+8 W");
		KeySequence m28w = KeySequence.getInstance("M2+8 W");
		boolean foundDeleteMarker = false;
		int numOfMarkers = 0;
		Binding[] bindings = bindingService.getBindings();
		for (int i = 0; i < bindings.length; i++) {
			final Binding binding = bindings[i];
			if (binding.getType() == Binding.SYSTEM) {
				String platform = binding.getPlatform();
				int idx = (platform == null ? -1 : platform.indexOf(','));
				assertEquals(binding.toString(), -1, idx);
				if (addWS.equals(binding.getParameterizedCommand())) {
					if (m18w.equals(binding.getTriggerSequence())) {
						numOfMarkers++;
						assertNull(m18w.format(), binding.getPlatform());
					} else if (m28w.equals(binding.getTriggerSequence())) {
						numOfMarkers++;
						assertTrue(platform, Util.WS_CARBON.equals(platform)
								|| Util.WS_COCOA.equals(platform)
								|| Util.WS_GTK.equals(platform)
								|| Util.WS_WIN32.equals(platform));
					}
				} else if (binding.getParameterizedCommand() == null
						&& m18w.equals(binding.getTriggerSequence())) {
					assertTrue(platform, Util.WS_CARBON.equals(platform)
							|| Util.WS_COCOA.equals(platform)
							|| Util.WS_GTK.equals(platform)
							|| Util.WS_WIN32.equals(platform));
					numOfMarkers++;
					foundDeleteMarker = true;
				}
			}
		}
		assertEquals(3, numOfMarkers);
		assertTrue("Unable to find delete marker", foundDeleteMarker);

		// make sure that the proper contexts are currently active
		IContextService contextService = (IContextService) fWorkbench
				.getService(IContextService.class);
		contextService
				.activateContext(IContextService.CONTEXT_ID_DIALOG_AND_WINDOW);
		contextService.activateContext(IContextService.CONTEXT_ID_WINDOW);
		TriggerSequence[] activeBindingsFor = bindingService
				.getActiveBindingsFor(addWS);
		assertEquals(1, activeBindingsFor.length);
	}

	public void testModifierWithPlatform() throws Exception {

		ICommandService commandService = (ICommandService) fWorkbench
				.getAdapter(ICommandService.class);
		IBindingService bindingService = (IBindingService) fWorkbench
				.getAdapter(IBindingService.class);
		ParameterizedCommand importCmd = new ParameterizedCommand(
				commandService.getCommand("org.eclipse.ui.file.import"), null);
		Binding[] bindings = bindingService.getBindings();
		int numOfMarkers = 0;
		for (int i = 0; i < bindings.length; i++) {
			final Binding binding = bindings[i];
			if (binding.getType() != Binding.SYSTEM)
				continue;

			if (importCmd.equals(binding.getParameterizedCommand())) {
				// make sure the modifier is applied
				assertEquals(KeySequence.getInstance("M2+8 I"), binding
						.getTriggerSequence());
				numOfMarkers++;
			}
		}

		// only one binding, if the platform matches
		assertEquals(numOfMarkers, 1);
	}

	public void testModifierNotApplied() throws Exception {

		ICommandService commandService = (ICommandService) fWorkbench
				.getAdapter(ICommandService.class);
		IBindingService bindingService = (IBindingService) fWorkbench
				.getAdapter(IBindingService.class);
		ParameterizedCommand exportCmd = new ParameterizedCommand(
				commandService.getCommand("org.eclipse.ui.file.export"), null);
		Binding[] bindings = bindingService.getBindings();
		for (int i = 0; i < bindings.length; i++) {
			final Binding binding = bindings[i];
			if (binding.getType() != Binding.SYSTEM)
				continue;

			if (exportCmd.equals(binding.getParameterizedCommand())) {
				// make sure the modifier is NOT applied
				assertEquals(KeySequence.getInstance("M1+8 E"), binding
						.getTriggerSequence());
				break;
			}
		}
	}
	
	public void testDifferentPlatform() throws Exception {

		ICommandService commandService = (ICommandService) fWorkbench
				.getAdapter(ICommandService.class);
		IBindingService bindingService = (IBindingService) fWorkbench
				.getAdapter(IBindingService.class);
		ParameterizedCommand backCmd = new ParameterizedCommand(
				commandService.getCommand("org.eclipse.ui.navigate.back"), null);
		Binding[] bindings = bindingService.getBindings();
		for (int i = 0; i < bindings.length; i++) {
			final Binding binding = bindings[i];
			if (binding.getType() != Binding.SYSTEM)
				continue;

			if (backCmd.equals(binding.getParameterizedCommand())) {
				// make sure the modifier is NOT applied
				// this will fail on Photon (but Paul thinks we'll never run the test suite on that platform :-)
				assertEquals(KeySequence.getInstance("M1+8 Q"), binding
						.getTriggerSequence());
				// and the platform should be null
				assertNull(binding.getPlatform());
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		final IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		store
				.setValue(
						"org.eclipse.ui.commands",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?><org.eclipse.ui.commands><activeKeyConfiguration keyConfigurationId=\""
								+ IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID
								+ "\"/></org.eclipse.ui.commands>");
		super.doTearDown();
	}
}
