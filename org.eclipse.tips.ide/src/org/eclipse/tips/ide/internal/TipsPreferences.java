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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Internal class to store preferences.
 *
 */
public class TipsPreferences extends AbstractPreferenceInitializer {

	/**
	 * Preference store key to indicate showing tips at startup.
	 */
	public static final String PREF_RUN_AT_STARTUP = "activate_at_startup";

	/**
	 * Preference store key to indicate serving tips that the user as already seen.
	 */
	public static final String PREF_SERVE_READ_TIPS = "serve_read_tips";

	public TipsPreferences() {
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

	/**
	 * Loads the read tips from disk.
	 * 
	 * @return a map that stores the read tip hashes per provider.
	 */
	public static Map<String, List<Integer>> getReadState() {
		HashMap<String, List<Integer>> result = new HashMap<>();

		try {
			File stateLocation = getStateLocation();
			for (String key : stateLocation.list(getStateFileNameFilter(stateLocation))) {
				PreferenceStore store = new PreferenceStore(new File(stateLocation, key).getAbsolutePath());
				store.load();
				ArrayList<Integer> tips = new ArrayList<>();
				for (String tipKey : store.preferenceNames()) {
					if (!"provider".equals(tipKey)) {
						tips.add(Integer.valueOf(store.getInt(tipKey)));
					}
				}
				result.put(store.getString("provider"), tips);
			}
		} catch (Exception e) {
			Status status = new Status(IStatus.ERROR, "org.eclipse.tips.ide", e.getMessage(), e);
			log(status);
		}
		return result;
	}

	private static FilenameFilter getStateFileNameFilter(File stateLocation) {
		return new FilenameFilter() {
			@Override
			public boolean accept(File pDir, String pName) {
				if (pDir.equals(stateLocation) && pName.endsWith(".state")) {
					return true;
				}
				return false;
			}
		};
	}

	private static File getStateLocation() throws Exception {
		File file = new File(IDETipManager.getStateLocation(), "org.eclipse.tips.ide.state");
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	/**
	 * Saves the list with read tips to disk.
	 * 
	 * @param pReadTips the list with read tips
	 * @return the status of the call
	 */
	public static IStatus saveReadState(Map<String, List<Integer>> pReadTips) {
		try {
			File stateLocation = getStateLocation();
			for (String child : pReadTips.keySet()) {
				PreferenceStore store = new PreferenceStore(
						new File(stateLocation, child.trim() + ".state").getAbsolutePath());
				pReadTips.get(child).forEach(value -> store.setValue(value.toString(), value.intValue()));
				store.setValue("provider", child);
				store.save();
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "org.eclipse.tips.ide", e.getMessage(), e);
		}
	}

	public static IEclipsePreferences getPreferences() {
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode(Constants.BUNDLE_ID);
		return node;
	}

	public static void log(IStatus status) {
		if (status.matches(IStatus.ERROR | IStatus.WARNING) || isDebug()) {
			Bundle bundle = FrameworkUtil.getBundle(TipsPreferences.class);
			Platform.getLog(bundle).log(status);
		}
		if (isConsoleLog()) {
			System.out.println(
					String.format("%1$tR:%1$tS:%1$tN - %2$s", Calendar.getInstance().getTime(), status.toString()));
		}
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

	/**
	 * @return true if tips are in debug mode.
	 */
	public static boolean isDebug() {
		return !System.getProperty("org.eclipse.tips.debug", "false").equals("false");
	}

	/**
	 * @return true if console logging is required
	 */
	public static boolean isConsoleLog() {
		return !System.getProperty("org.eclipse.tips.consolelog", "false").equals("false");
	}
}