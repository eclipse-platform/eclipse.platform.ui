/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.commands.ICommandService;
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
}
