/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.ide.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Internal class to store preferences.
 *
 */
public class Preferences extends AbstractPreferenceInitializer {

	/**
	 * Preference store key to indicate showing tips at startup.
	 */
	public static final String PREF_RUN_AT_STARTUP = "activate_at_startup";

	/**
	 * Preference store key to indicate serving tips that the user as already seen.
	 */
	public static final String PREF_SERVE_READ_TIPS = "serve_read_tips";

	public Preferences() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = getPreferences();
		node.putBoolean(PREF_RUN_AT_STARTUP, true);
		node.putBoolean(PREF_SERVE_READ_TIPS, false);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public static IEclipsePreferences getPreferences() {
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode(Constants.BUNDLE_ID);
		return node;
	}

	public static boolean isRunAtStartup() {
		return getPreferences().getBoolean(PREF_RUN_AT_STARTUP, true);
	}

	public static boolean isServeReadTips() {
		return getPreferences().getBoolean(PREF_SERVE_READ_TIPS, false);
	}

	public static void setRunAtStartup(boolean runAtStartup) {
		IEclipsePreferences node = getPreferences();
		node.putBoolean(PREF_RUN_AT_STARTUP, runAtStartup);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setServeReadTips(boolean serveReadTips) {
		IEclipsePreferences node = getPreferences();
		node.putBoolean(PREF_SERVE_READ_TIPS, serveReadTips);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}
}