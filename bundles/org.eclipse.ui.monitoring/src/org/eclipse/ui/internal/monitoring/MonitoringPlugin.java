/*******************************************************************************
 * Copyright (C) 2014 , 2023 Google Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Steve Foreman (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *     Sergey Prigogin (Google)
 *     Christoph Läubrich - remove dependency to UI Activator
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * The activator class that controls the plug-in life cycle.
 */
public class MonitoringPlugin {

	private static ILog logger = ILog.of(MonitoringPlugin.class);

	public static void logError(String message, Throwable e) {
		logger.log(new Status(IStatus.ERROR, PreferenceConstants.PLUGIN_ID, message, e));
	}

	public static void logWarning(String message) {
		logger.log(new Status(IStatus.WARNING, PreferenceConstants.PLUGIN_ID, message));
	}

	/**
	 * The instance scope preferences of this bundle, the node preference changes are reported on.
	 */
	public static IEclipsePreferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(PreferenceConstants.PLUGIN_ID);
	}

	public static boolean getBooleanPreference(String key) {
		return Platform.getPreferencesService().getBoolean(PreferenceConstants.PLUGIN_ID, key, false, null);
	}

	public static int getIntPreference(String key) {
		return Platform.getPreferencesService().getInt(PreferenceConstants.PLUGIN_ID, key, 0, null);
	}

	public static String getStringPreference(String key) {
		return Platform.getPreferencesService().getString(PreferenceConstants.PLUGIN_ID, key, "", null); //$NON-NLS-1$
	}
}
