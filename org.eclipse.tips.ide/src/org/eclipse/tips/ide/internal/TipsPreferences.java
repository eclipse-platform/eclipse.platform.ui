/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.ide.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.tips.core.internal.TipManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Internal class to store preferences.
 *
 */
@SuppressWarnings("restriction")
public class TipsPreferences extends AbstractPreferenceInitializer {

	private static final String FALSE = "false"; //$NON-NLS-1$

	/**
	 * Preference store key to indicate showing tips at startup.
	 */
	public static final String PREF_STARTUP_BEHAVIOR = "activate_at_startup"; //$NON-NLS-1$

	/**
	 * Preference store key to indicate serving tips that the user as already seen.
	 */
	public static final String PREF_SERVE_READ_TIPS = "serve_read_tips"; //$NON-NLS-1$

	/**
	 * Preference store key to indicate providers which are disabled. I.e. no tips
	 * are shown from these providers. The providers are indicated by their
	 * identifiers, the identifiers are strings separated by commas.
	 */
	public static final String PREF_DISABLED_PROVIDERS = "disabled_providers"; //$NON-NLS-1$

	public TipsPreferences() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = getPreferences();
		node.putInt(PREF_STARTUP_BEHAVIOR, getDefaultStartupBehavior());
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
					if (!"provider".equals(tipKey)) { //$NON-NLS-1$
						tips.add(Integer.valueOf(store.getInt(tipKey)));
					}
				}
				result.put(store.getString("provider"), tips); //$NON-NLS-1$
			}
		} catch (Exception e) {
			Status status = new Status(IStatus.ERROR, "org.eclipse.tips.ide", e.getMessage(), e); //$NON-NLS-1$
			log(status);
		}
		return result;
	}

	private static FilenameFilter getStateFileNameFilter(File stateLocation) {
		return (pDir, pName) -> {
			if (pDir.equals(stateLocation) && pName.endsWith(".state")) { //$NON-NLS-1$
				return true;
			}
			return false;
		};
	}

	private static File getStateLocation() throws Exception {
		File file = new File(IDETipManager.getStateLocation(), "org.eclipse.tips.ide.state"); //$NON-NLS-1$
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
						new File(stateLocation, child.trim() + ".state").getAbsolutePath()); //$NON-NLS-1$
				pReadTips.get(child).forEach(value -> store.setValue(value.toString(), value.intValue()));
				store.setValue("provider", child); //$NON-NLS-1$
				store.save();
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, "org.eclipse.tips.ide", e.getMessage(), e); //$NON-NLS-1$
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
					String.format("%1$tR:%1$tS:%1$tN - %2$s", Calendar.getInstance().getTime(), format(status))); //$NON-NLS-1$
		}
	}

	/**
	 * Converts the passed status object to a readable string and tries to get the
	 * location where the log method was called.
	 *
	 * @param pStatus the status to format
	 * @return the formatted status to string.
	 */
	private static Object format(IStatus pStatus) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String origin = "<unknown>"; //$NON-NLS-1$
		if (stackTrace.length > 3) {
			StackTraceElement ste = stackTrace[4];
			String[] fqcn = ste.getClassName().split("\\.");
			String clazz = fqcn[fqcn.length - 1];
			origin = clazz + "#" + ste.getMethodName() + "(" + ste.getLineNumber() + ")";
		}

		String statusLine = pStatus.toString();
		if (statusLine.endsWith(" null")) {
			statusLine = statusLine.substring(0, statusLine.length() - " null".length());
		}
		return statusLine + " : " + origin;
	}

	public static int getStartupBehavior() {
		return getPreferences().getInt(PREF_STARTUP_BEHAVIOR, getDefaultStartupBehavior());
	}

	private static int getDefaultStartupBehavior() {
		String startupBehavior = System.getProperty("org.eclipse.tips.startup.default");
		if ("dialog".equals(startupBehavior)) {
			return TipManager.START_DIALOG;
		} else if ("background".equals(startupBehavior)) {
			return TipManager.START_BACKGROUND;
		} else if ("disable".equals(startupBehavior)) {
			return TipManager.START_DISABLE;
		}
		return TipManager.START_BACKGROUND;
	}

	public static boolean isServeReadTips() {
		return getPreferences().getBoolean(PREF_SERVE_READ_TIPS, false);
	}

	public static void setStartupBehavior(int startupBehavior) {
		IEclipsePreferences node = getPreferences();
		node.putInt(PREF_STARTUP_BEHAVIOR, startupBehavior);
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
		return !System.getProperty("org.eclipse.tips.debug", FALSE).equals(FALSE); //$NON-NLS-1$
	}

	/**
	 * @return true if console logging is required
	 */
	public static boolean isConsoleLog() {
		return !System.getProperty("org.eclipse.tips.consolelog", FALSE).equals(FALSE); //$NON-NLS-1$
	}

	/**
	 * @return A list of tip provider identifiers, which are disabled with a
	 *         preference on the configuration scope.
	 */
	public static List<String> getDisabledProviderIds() {
		IScopeContext[] scopes = { ConfigurationScope.INSTANCE, DefaultScope.INSTANCE };
		IPreferencesService preferencesService = Platform.getPreferencesService();
		String defaultValue = "";
		String disabledProviderIdsPreference = preferencesService.getString(Constants.BUNDLE_ID,
				PREF_DISABLED_PROVIDERS, defaultValue, scopes);
		String[] disabledProviderIds = disabledProviderIdsPreference.split(",");
		return Arrays.asList(disabledProviderIds);
	}
}