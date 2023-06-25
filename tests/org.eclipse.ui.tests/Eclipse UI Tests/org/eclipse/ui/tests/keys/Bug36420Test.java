/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.tests.keys;

import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.keys.KeySequence;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests Bug 36420
 *
 * @since 3.0
 */
@Ignore("This no longer works due to focus issues related to key bindings")
// See commit f4f9a6680173270f913891b1d2a8b5f05854b6f4
public class Bug36420Test {

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
	@Test
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
		for (String pluginId : pluginIds) {
			preferences.put(pluginId, new PluginVersionIdentifier(
					Platform.getBundle(pluginId).getHeaders().get(
							org.osgi.framework.Constants.BUNDLE_VERSION)));
		}

		// Export the preferences.
		File file = File.createTempFile("preferences", ".txt"); //$NON-NLS-1$//$NON-NLS-2$
		file.deleteOnExit();
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
			preferences.store(bos, null);
		}

		// Attempt to import the key binding.
		Preferences.importPreferences(IPath.fromOSString(file.getAbsolutePath()));
		/*
		 * END SECTION
		 */

		// Check to see that the key binding for the given command matches.
		ICommandManager manager = PlatformUI.getWorkbench().getCommandSupport()
				.getCommandManager();
		List<KeySequence> keyBindings = manager.getCommand(commandId)
				.getKeySequenceBindings();
		Iterator<KeySequence> keyBindingItr = keyBindings.iterator();
		boolean found = false;
		while (keyBindingItr.hasNext()) {
			KeySequence keyBinding = keyBindingItr.next();
			String currentText = keyBinding.toString();
			if (keySequenceText.equals(currentText)) {
				found = true;
			}
		}

		assertTrue("Key binding not imported.", found); //$NON-NLS-1$
	}
}
