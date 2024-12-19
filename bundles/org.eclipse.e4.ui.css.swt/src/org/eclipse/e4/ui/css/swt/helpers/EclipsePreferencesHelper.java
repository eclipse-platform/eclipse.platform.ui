/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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
package org.eclipse.e4.ui.css.swt.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class EclipsePreferencesHelper {
	public static final String PROPS_OVERRIDDEN_BY_CSS_PROP = "overriddenByCSS";

	public static final String SEPARATOR = ",";

	private static final String MULTI_VALUE_FORMATTER = "%s%s" + SEPARATOR;

	private static IPreferenceChangeListener preferenceChangeListener;

	private static String previousThemeId;

	private static String currentThemeId;

	private static final String PROPS_DEFAULT_VALUE_BEFORE_OVERIDDEN_FROM_CSS = "defaultValueBeforeOverriddenFromCSS";

	public static void appendOverriddenPropertyName(
			IEclipsePreferences preferences, String name) {
		String value = preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, SEPARATOR);
		if (value.equals(SEPARATOR)) {
			preferences
			.addPreferenceChangeListener(getPreferenceChangeListener());
		}
		if (!isOverriddenByCSS(value, name)) {
			preferences.put(PROPS_OVERRIDDEN_BY_CSS_PROP,
					String.format(MULTI_VALUE_FORMATTER, value, name));
		}
	}

	public static IPreferenceChangeListener getPreferenceChangeListener() {
		if (preferenceChangeListener == null) {
			preferenceChangeListener = new PreferenceOverriddenByCssChangeListener();
		}
		return preferenceChangeListener;
	}

	private static boolean isOverriddenByCSS(String propertiesOverriddenByCSS,
			String property) {
		return propertiesOverriddenByCSS.contains(SEPARATOR + property
				+ SEPARATOR);
	}

	public static List<String> getOverriddenPropertyNames(
			IEclipsePreferences preferences) {
		String value = preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, null);
		if (value == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>();
		for (String name : value.split(SEPARATOR)) {
			if (name != null && !name.isEmpty()) {
				result.add(name);
			}
		}
		return result;
	}

	public static void removeOverriddenPropertyNames(
			IEclipsePreferences preferences) {
		preferences.remove(PROPS_OVERRIDDEN_BY_CSS_PROP);
		preferences
		.removePreferenceChangeListener(getPreferenceChangeListener());
	}

	public static void removeOverriddenByCssProperty(
			IEclipsePreferences preferences, String preferenceToRemove) {
		StringBuilder overriddenByCSS = new StringBuilder(SEPARATOR);
		for (String preference : getOverriddenPropertyNames(preferences)) {
			if (!preference.equals(preferenceToRemove)) {
				overriddenByCSS.append(preference).append(SEPARATOR);
			}
		}
		preferences.put(PROPS_OVERRIDDEN_BY_CSS_PROP,
				overriddenByCSS.toString());
	}

	public static void setPreviousThemeId(String themeId) {
		previousThemeId = themeId;
	}

	public static void setCurrentThemeId(String themeId) {
		currentThemeId = themeId;
	}

	public static boolean isThemeChanged() {
		return currentThemeId != null && !currentThemeId.equals(previousThemeId);
	}

	public static class PreferenceOverriddenByCssChangeListener implements
	IPreferenceChangeListener {
		@Override
		public void preferenceChange(PreferenceChangeEvent event) {
			if (isModified(event) && isRelatedToOverriddenByCss(event)) {
				removeOverriddenByCssProperty(event);
			}
		}

		private boolean isModified(PreferenceChangeEvent event) {
			return event.getOldValue() != null && event.getNewValue() != null;
		}

		private boolean isRelatedToOverriddenByCss(PreferenceChangeEvent event) {
			return isOverriddenByCSS(
					event.getNode()
					.get(PROPS_OVERRIDDEN_BY_CSS_PROP, SEPARATOR),
					event.getKey());
		}

		protected void removeOverriddenByCssProperty(PreferenceChangeEvent event) {
			Preferences preferences = event.getNode();
			if (preferences instanceof IEclipsePreferences) {
				EclipsePreferencesHelper.removeOverriddenByCssProperty(
						(IEclipsePreferences) preferences, event.getKey());
			}
		}
	}

	public static void overrideDefault(IEclipsePreferences preferences, String name, String value) {
		String prefName = preferences.name();
		if (prefName == null) {
			return;
		}
		IEclipsePreferences defaultPrefs = DefaultScope.INSTANCE.getNode(prefName);
		if (defaultPrefs == null) {
			return;
		}
		String existing = defaultPrefs.get(name, null);
		if (existing != null && value != null && existing.equals(value) == false) {
			defaultPrefs.put(name, value);
			preferences.put(name + SEPARATOR + PROPS_DEFAULT_VALUE_BEFORE_OVERIDDEN_FROM_CSS, existing);
		}
	}

	public static void resetOverriddenDefaults(IEclipsePreferences preferences) {
		try {
			String[] keys = preferences.keys();
			if (keys == null) {
				return;
			}
			for (String key : keys) {
				if (key != null && key.endsWith(SEPARATOR + PROPS_DEFAULT_VALUE_BEFORE_OVERIDDEN_FROM_CSS)) {
					String overriddenDefault = preferences.get(key, null);
					String originKey = key.substring(0,
							key.lastIndexOf(SEPARATOR + PROPS_DEFAULT_VALUE_BEFORE_OVERIDDEN_FROM_CSS));
					IEclipsePreferences defaultPrefs = DefaultScope.INSTANCE.getNode(preferences.name());
					if (defaultPrefs != null) {
						String currentDefault = defaultPrefs.get(originKey, null);
						if (overriddenDefault != null && currentDefault != null
								&& !currentDefault.equals(overriddenDefault)) {
							defaultPrefs.put(originKey, overriddenDefault);
						}
					}
					preferences.remove(key);
				}
			}
		} catch (BackingStoreException e) { // silently ignored
		}
	}
}
