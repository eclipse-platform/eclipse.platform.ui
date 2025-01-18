/*******************************************************************************
 * Copyright (c) 2004, 2025 IBM Corporation and others.
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
package org.eclipse.ui.tests.dynamicplugins;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.tests.TestPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class DynamicUtils {

	public static final Bundle installPlugin(String pluginName)
			throws IOException, BundleException {
		// Programmatically install a new plugin
		TestPlugin plugin = TestPlugin.getDefault();
		if (plugin == null) {
			throw new IllegalStateException(
					"TestPlugin default reference is null");
		}
		String pluginLocation = null;
		URL dataURL = FileLocator.resolve(plugin.getBundle().getEntry(pluginName));
		pluginLocation = "reference:" + dataURL.toExternalForm();
		return TestInstallUtil.installBundle(pluginLocation);
	}

	public static void uninstallPlugin(Bundle bundle) throws BundleException {
		TestInstallUtil.uninstallBundle(bundle);
	}

}
