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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
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

	/**
	 * Loads the read tips from disk.
	 * 
	 * @return a map that stores the read tip hashes per provider.
	 */
	public static Map<String, List<Integer>> getReadState() {
		HashMap<String, List<Integer>> result = new HashMap<>();
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode("org.eclipse.tips.ide.read");
		try {
			for (String key : node.childrenNames()) {
				ArrayList<Integer> tips = new ArrayList<>();
				org.osgi.service.prefs.Preferences tipsNode = node.node(key);
				for (String tipKey : tipsNode.keys()) {
					tips.add(Integer.valueOf(tipsNode.getInt(tipKey, 0)));
				}
				result.put(key, tips);
			}
		} catch (BackingStoreException e) {
			Status status = new Status(IStatus.ERROR, "org.eclipse.tips.ide", e.getMessage(), e);
			log(status);
		}
		return result;
	}

	/**
	 * Saves the list with read tips to disk.
	 * 
	 * @param pReadTips the list with read tips
	 * @return the status of the call
	 */
	public static IStatus saveReadState(Map<String, List<Integer>> pReadTips) {
		try {
			IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode("org.eclipse.tips.ide.read");
			for (String child : pReadTips.keySet()) {
				if (node.nodeExists(child)) {
					node.node(child).removeNode();
				}
			}
			node.clear();
			pReadTips.forEach(
					(key, tips) -> tips.forEach(value -> node.node(key).putInt(value.toString(), value.intValue())));
			node.flush();
			return Status.OK_STATUS;
		} catch (BackingStoreException e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "org.eclipse.tips.ide", e.getMessage(), e);
		}
	}

	public static IEclipsePreferences getPreferences() {
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode(Constants.BUNDLE_ID);
		return node;
	}

	private static void log(IStatus status) {
		if (status.matches(IStatus.ERROR | IStatus.WARNING)) {
			Bundle bundle = FrameworkUtil.getBundle(Preferences.class);
			Platform.getLog(bundle).log(status);
		}
		if (System.getProperty("org.eclipse.tips.consolelog") != null) {
			System.out.println(status.toString());
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
}