/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.*;

/**
 * Utility class for exporting and importing the complete set of user-defined
 * preferences for all plugins in the platform.  The global preference file
 * is written in the following format:
 * 
 * <plugin-id1>=<plugin-version1>
 * <plugin-id1>/<property-key1>=<property-value1>
 * <plugin-id1>/<property-key2>=<property-value2>
 * ...
 * <plugin-id2>=<plugin-version2>
 * <plugin-id2>/<property-key1>=<property-value1>
 * <plugin-id2>/<property-key2>=<property-value2>
 * ...
 */
public class PreferenceExporter {
	/**
	 * The character that separates the plug-in id from the property key.
	 */
	private static final char PLUGIN_SEPARATOR = '/';

	/**
	 * @see Preferences#exportPreferences
	 */
	public static void exportPreferences(IPath file) {
		//collect all plugin preferences into a single global preference object
		Preferences globalPreferences = new Preferences();
		IPluginDescriptor[] descriptors = Platform.getPluginRegistry().getPluginDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			IPluginDescriptor descriptor = descriptors[i];
			//save current preferences to ensure most recent values are exported
			if (descriptor.isPluginActivated()) {
				try {
					descriptor.getPlugin().savePluginPreferences();
				} catch (CoreException e) {
					e.printStackTrace();
					continue;
				}
			}
			//now merge the plugin preferences into the global preference object
			if (mergePluginPreferences(descriptor, globalPreferences)) {
				//write the property that indicates the plugin version
				globalPreferences.setValue(descriptor.getUniqueIdentifier(), descriptor.getVersionIdentifier().toString());
			}

		}
		storePreferences(file, globalPreferences);
	}
	/**
	 * Returns the plugin preference file for the given plugin descriptor.
	 */
	private static IPath getPluginPreferenceFile(IPluginDescriptor descriptor) {
		IPath location = InternalPlatform.getMetaArea().getPluginStateLocation(descriptor);
		return location.append(Plugin.PREFERENCES_FILE_NAME);
	}
	/**
	 * @see Preferences#importPreferences
	 */
	public static void importPreferences(IPath file) {
		Map idsToPreferences = splitPreferences(file);
		IPluginDescriptor[] descriptors = Platform.getPluginRegistry().getPluginDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			setPluginPreferences(descriptors[i],
				(Preferences) idsToPreferences.get(descriptors[i].getUniqueIdentifier()));
		}
	}
	/**
	 * Loads preferences from the given file into the provided preferences instance.
	 * Returns the preferences instance.
	 */
	private static Preferences loadPreferences(IPath properties, Preferences preferences) {
		File propertiesFile = properties.toFile();
		if (propertiesFile.exists()) {
			InputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(propertiesFile));
				preferences.load(in);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return preferences;
	}
	/**
	 * Adds all preferences for the given plugin descriptor to the provided
	 * preferences instance.  Returns true if any properties were added,
	 * and false otherwise.
	 */
	private static boolean mergePluginPreferences(IPluginDescriptor descriptor, Preferences preferences) {
		boolean found = false;
		IPath propertiesFile = getPluginPreferenceFile(descriptor);
		if (propertiesFile.toFile().exists()) {
			Preferences pluginPreferences = loadPreferences(propertiesFile, new Preferences());
			String pluginId = descriptor.getUniqueIdentifier();
			String[] keys = pluginPreferences.propertyNames();
			found = keys.length > 0;
			for (int i = 0; i < keys.length; i++) {
				String longKey = pluginId + PLUGIN_SEPARATOR + keys[i];
				preferences.setValue(longKey, pluginPreferences.getString(keys[i]));
			}
		}
		return found;
	}

	/**
	 * Sets the given preferences as the preferences for the plugin with the
	 * given descriptor, overwriting any previously defined preferences.  If
	 * the given preferences is null, all values are returned to their default value.
	 */
	private static void setPluginPreferences(IPluginDescriptor descriptor, Preferences newPreferences) {
		if (descriptor.isPluginActivated()) {
			Plugin plugin = null;
			try {
				plugin = descriptor.getPlugin();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			if (plugin != null) {
				Preferences oldPreferences = plugin.getPluginPreferences();
				//remove all old values
				String[] keys = oldPreferences.propertyNames();
				for (int i = 0; i < keys.length; i++) {
					oldPreferences.setToDefault(keys[i]);
				}
				//add new values
				if (newPreferences != null) {
					keys = newPreferences.propertyNames();
					for (int i = 0; i < keys.length; i++) {
						oldPreferences.setValue(keys[i], newPreferences.getString(keys[i]));
					}
				}
				//save the preferences file
				storePreferences(getPluginPreferenceFile(descriptor), oldPreferences);
			}
		} else {
			//if the plugin isn't loaded, just save the preferences file directly
			storePreferences(getPluginPreferenceFile(descriptor), newPreferences);
		}
	}
	/**
	 * Splits up a global preference file into preferences for each plugin
	 * descriptor.  Returns a map of plugin IDs to Preferences objects
	 * for that plugin.
	 */
	private static Map splitPreferences(IPath file) {
		Preferences globalPreferences = loadPreferences(file, new Preferences());
		Map idsToPreferences = new HashMap();
		String[] keys = globalPreferences.propertyNames();
		for (int i = 0; i < keys.length; i++) {
			String longKey = keys[i];
			int index = longKey.indexOf(PLUGIN_SEPARATOR);
			if (index >= 0) {
				String pluginId = longKey.substring(0, index);
				String key = longKey.substring(index + 1);
				Preferences preferences = (Preferences) idsToPreferences.get(pluginId);
				if (preferences == null) {
					preferences = new Preferences();
					idsToPreferences.put(pluginId, preferences);
				}
				preferences.setValue(key, globalPreferences.getString(longKey));
			}
		}
		return idsToPreferences;
	}

	/**
	 * Writes the given preferences to the given file.  If the preferences are
	 * null or empty, the file is deleted.
	 */
	private static void storePreferences(IPath destination, Preferences preferences) {
		File destinationFile = destination.toFile();
		if (preferences == null || preferences.propertyNames().length == 0) {
			destinationFile.delete();
			return;
		}
		File parent = destinationFile.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(destinationFile));
			preferences.store(out, null);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Compares two plugin version identifiers to see if their preferences
	 * are compatible.  If they are not compatible, a warning message is 
	 * added to the given multistatus, according to the following rules:
	 * 
	 * - plugins that differ in service number: no status
	 * - plugins that differ in minor version: WARNING status
	 * - plugins that differ in major version:
	 * 	- where installed plugin is newer: WARNING status
	 * 	- where installed plugin is older: ERROR status
	 * @param pref The version identifer of the preferences to be loaded
	 * @param installed The version identifier of the installed plugin
	 */
	private static void validatePluginVersions(
		String pluginId,
		PluginVersionIdentifier pref,
		PluginVersionIdentifier installed,
		MultiStatus result) {

		if (installed.getMajorComponent() == pref.getMajorComponent() &&
			installed.getMinorComponent() == pref.getMinorComponent()) {
			return;
		}
		int severity;
		if (installed.getMajorComponent() < pref.getMajorComponent()) {
			severity = IStatus.ERROR;
		} else {
			severity = IStatus.WARNING;
		}
		String msg = Policy.bind("preferences.incompatible", new String[] { //$NON-NLS-1$
			pref.toString(), pluginId, installed.toString()});
		result.add(new Status(severity, Platform.PI_RUNTIME, 1, msg, null));
	}
	/**
	 * Validates that the preference versions in the given file match
	 * the currently installed plugins.  Returns an OK status if
	 * all preferences match the currently installed plugins, otherwise a MultiStatus
	 * describing what preferences don't match.
	 */
	public static IStatus validatePreferenceVersions(IPath file) {
		String msg = Policy.bind("preferences.validate"); //$NON-NLS-1$
		MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, 1, msg, null);
		Preferences globalPreferences = loadPreferences(file, new Preferences());
		IPluginRegistry registry = Platform.getPluginRegistry();
		String[] keys = globalPreferences.propertyNames();
		for (int i = 0; i < keys.length; i++) {
			String pluginId = keys[i];
			//if the key does not contain the separator character, it is a plugin version property
			if (pluginId.indexOf(PLUGIN_SEPARATOR) < 0) {
				IPluginDescriptor descriptor = registry.getPluginDescriptor(pluginId);
				if (descriptor != null) {
					String version = globalPreferences.getString(pluginId);
					PluginVersionIdentifier preferenceVersion = new PluginVersionIdentifier(version);
					PluginVersionIdentifier installedVersion = descriptor.getVersionIdentifier();
					validatePluginVersions(pluginId, preferenceVersion, installedVersion, result);
				}
			}
		}
		return result;
	}
}


