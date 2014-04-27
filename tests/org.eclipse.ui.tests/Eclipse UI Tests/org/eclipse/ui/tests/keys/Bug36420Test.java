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

package org.eclipse.ui.tests.keys;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests Bug 36420
 * 
 * @since 3.0
 */
public class Bug36420Test extends UITestCase {

    /**
     * Constructor for Bug36420Test.
     * 
     * @param name
     *            The name of the test
     */
    public Bug36420Test(String name) {
        super(name);
    }

    /**
     * Tests that importing key preferences actually has an effect.
     * 
     * @throws CoreException
     *             If the preferences can't be imported.
     * @throws FileNotFoundException
     *             If the temporary file is removed after it is created, but
     *             before it is opened. (Wow)
     * @throws IOException
     *             If something fails during output of the preferences file.
     */
    public void testImportKeyPreferences() throws CoreException,
            FileNotFoundException, IOException {
        String commandId = "org.eclipse.ui.window.nextView"; //$NON-NLS-1$
        String keySequenceText = "F S C K"; //$NON-NLS-1$

        /*
         * DO NOT USE PreferenceMutator for this section. This test case must
         * use these exact steps, while PreferenceMutator might use something
         * else in the future.
         */
        // Set up the preferences.
        Properties preferences = new Properties();
        String key = "org.eclipse.ui.workbench/org.eclipse.ui.commands"; //$NON-NLS-1$
        String value = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<org.eclipse.ui.commands><activeKeyConfiguration keyConfigurationId=\"" + IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID + "\"></activeKeyConfiguration><keyBinding	keyConfigurationId=\"org.eclipse.ui.defaultAcceleratorConfiguration\" commandId=\"" + commandId + "\" keySequence=\"" + keySequenceText + "\"/></org.eclipse.ui.commands>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        preferences.put(key, value);
        
        // This is the first pass way to "walk" through the list
        // of bundles
        String[] pluginIds = Platform.getExtensionRegistry().getNamespaces();
		for (int i = 0; i < pluginIds.length; i++) {
			preferences.put(pluginIds[i], new PluginVersionIdentifier(
					Platform.getBundle(pluginIds[i]).getHeaders().get(
							org.osgi.framework.Constants.BUNDLE_VERSION)));
		}

        // Export the preferences.
        File file = File.createTempFile("preferences", ".txt"); //$NON-NLS-1$//$NON-NLS-2$
        file.deleteOnExit();
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(file));
        preferences.store(bos, null);
        bos.close();

        // Attempt to import the key binding.
        Preferences.importPreferences(new Path(file.getAbsolutePath()));
        /*
         * END SECTION
         */

        // Check to see that the key binding for the given command matches.
        ICommandManager manager = fWorkbench.getCommandSupport()
                .getCommandManager();
        List keyBindings = manager.getCommand(commandId)
                .getKeySequenceBindings();
        Iterator keyBindingItr = keyBindings.iterator();
        boolean found = false;
        while (keyBindingItr.hasNext()) {
            KeySequence keyBinding = (KeySequence) keyBindingItr.next();
            String currentText = keyBinding.toString();
            if (keySequenceText.equals(currentText)) {
                found = true;
            }
        }

        assertTrue("Key binding not imported.", found); //$NON-NLS-1$
    }
}
