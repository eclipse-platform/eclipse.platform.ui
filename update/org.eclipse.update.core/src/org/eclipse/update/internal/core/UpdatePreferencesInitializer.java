/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * Class to initialize the preferences for the Update core plugin.
 */
public class UpdatePreferencesInitializer extends AbstractPreferenceInitializer {

	/**
	 * Default constructor
	 */
	public UpdatePreferencesInitializer() {
		super();
	}

	/**
	 * Initializes the default preferences settings for this plug-in.
	 * <p>
	 * This method is called sometime after the preference store for this
	 * plug-in is created. Default values are never stored in preference
	 * stores; they must be filled in each time. This method provides the
	 * opportunity to initialize the default values.
	 * </p>
	 * <p>
	 * The default implementation of this method does nothing. A subclass that needs
	 * to set default values for its preferences must reimplement this method.
	 * Default values set at a later point will override any default override
	 * settings supplied from outside the plug-in (product configuration or
	 * platform start up).
	 * </p>
	 */
	public void initializeDefaultPreferences() {
		Plugin plugin = UpdateCore.getPlugin();
		plugin.getPluginPreferences().setDefault(UpdateCore.P_CHECK_SIGNATURE, true);
		plugin.getPluginPreferences().setDefault(UpdateCore.P_AUTOMATICALLY_CHOOSE_MIRROR, false);
		plugin.getPluginPreferences().setDefault(UpdateCore.P_HISTORY_SIZE, UpdateCore.DEFAULT_HISTORY);
		plugin.getPluginPreferences().setDefault(UpdateCore.P_UPDATE_VERSIONS, UpdateCore.EQUIVALENT_VALUE);
		
		// If proxy host and port are set as system properties, use them as defaults
		String proxyHost = System.getProperty("http.proxyHost"); //$NON-NLS-1$
		if (proxyHost != null && proxyHost.trim().length() > 0) {
			String proxyPort = System.getProperty("http.proxyPort"); //$NON-NLS-1$
			if (proxyPort == null || proxyPort.trim().length() == 0)
				proxyPort = "80"; //$NON-NLS-1$
			plugin.getPluginPreferences().setDefault(UpdateCore.HTTP_PROXY_ENABLE, true);
			plugin.getPluginPreferences().setDefault(UpdateCore.HTTP_PROXY_HOST, proxyHost);
			plugin.getPluginPreferences().setDefault(UpdateCore.HTTP_PROXY_PORT, proxyPort);
		}
	}
}
