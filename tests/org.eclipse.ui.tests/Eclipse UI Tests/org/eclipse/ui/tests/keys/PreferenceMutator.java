/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Preferences;

/**
 * A utility class for setting preferences related to key bindings. This class
 * currently uses the round-about procedure of manually writing a preferences
 * file, and then loading it back into the application. In the future, it might
 * use a direct API.
 * 
 * @since 3.0
 */
public abstract class PreferenceMutator {
    /**
     * Sets a key binding in the currently running Eclipse application. It
     * accomplishes this by writing out an exported preferences file by hand,
     * and then importing it back into the application.
     * 
     * @param commandId
     *           The command identifier to which the key binding should be
     *           associated; should not be <code>null</code>.
     * @param keySequenceText
     *           The text of the key sequence for this key binding; must not be
     *           <code>null</code>.
     * @throws CoreException
     *            If the exported preferences file is invalid for some reason.
     * @throws FileNotFoundException
     *            If the temporary file is removed before it can be read in.
     *            (Wow)
     * @throws IOException
     *            If the creation of or the writing to the temporary file fails
     *            for some reason.
     */
    static final void setKeyBinding(String commandId, String keySequenceText)
            throws CoreException, FileNotFoundException, IOException {
        // Set up the preferences.
        Properties preferences = new Properties();
        String key = "org.eclipse.ui.workbench/org.eclipse.ui.commands"; //$NON-NLS-1$
        String value = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<org.eclipse.ui.commands><activeKeyConfiguration/><keyBinding commandId=\"" + commandId + "\" keySequence=\"" + keySequenceText + "\"/></org.eclipse.ui.commands>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        preferences.put(key, value);
        
        String[] pluginIds = Platform.getExtensionRegistry().getNamespaces();
		for (int i = 0; i < pluginIds.length; i++) {
			preferences.put(pluginIds[i], new PluginVersionIdentifier(
					(String) Platform.getBundle(pluginIds[i]).getHeaders().get(
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
    }

}
