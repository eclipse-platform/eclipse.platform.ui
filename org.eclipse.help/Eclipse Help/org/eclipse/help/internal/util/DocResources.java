package org.eclipse.help.internal.util;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;
import java.io.File;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.HelpSystem;
import java.net.*;

/**
 * Uses a resource bundle to load images and strings from
 * a property file in a documentation plugin
 */
public class DocResources {

	static HashMap resourceBundleTable = new HashMap();
	static ArrayList pluginsWithoutResources = new ArrayList();
	static HashMap propertiesTable = new HashMap();

	/**
	 * Resources constructort.
	 */
	public DocResources() {
		super();
	}
	/**
	 * Returns a string from a property file.
	 * It uses 'name' as a the key to retrieve from a doc.properties file.
	 * this is used for translation of all manifest files (including F1)
	 */
	public static String getPluginString(String pluginID, String name) {

		// resource loader
		ClassLoader resourceLoader = null;
		// base url for the plugin
		URL pluginBaseURL = null;

		// Shortcuts
		try {
			// try plugins without resources 
			if (pluginsWithoutResources.contains(pluginID))
				return name;

			// try local plugins
			ResourceBundle resBundlePlugin =
				(ResourceBundle) resourceBundleTable.get(pluginID);
			if (resBundlePlugin != null) {
				String value = resBundlePlugin.getString(name);
				if (value == null)
					value = name;
				return value;
			}

			// try remote plugins
			Properties properties = (Properties) propertiesTable.get(pluginID);
			if (properties != null) {
				String value = properties.getProperty(name);
				if (value == null)
					value = name;
				return value;
			}

			// None of the resources is caches, so try to load it

			/*
			The sequence classpath that is specified in the ClassLoader is:
			1.  "plugin dir"/.nl/xx_XX
			2.  "plugin dir"/.nl/xx
			3.  "plugin dir"
			The file it looks for is doc.properties
			*/

			IPluginDescriptor pd =
				Platform.getPluginRegistry().getPluginDescriptor(pluginID);

			// If the plugin is not installed locally try to get it from
			// a remote installation
			if (pd == null)
				return getRemotePluginString(pluginID, name);

			pluginBaseURL = pd.getInstallURL();
			resourceLoader =
				new URLClassLoader(
					new URL[] {
						new URL(pluginBaseURL, ".nl/" + Locale.getDefault() + "/"),
						new URL(pluginBaseURL, ".nl/" + Locale.getDefault().getLanguage() + "/"),
						pluginBaseURL },
					null);

			resBundlePlugin =
				ResourceBundle.getBundle("doc", Locale.getDefault(), resourceLoader);

			// Get the string
			if (resBundlePlugin == null) {
				pluginsWithoutResources.add(pluginID);
				return name;
			} else {
				resourceBundleTable.put(pluginID, resBundlePlugin);
				String value = resBundlePlugin.getString(name);
				if (value == null)
					value = name;
				return value;
			}
		} catch (Throwable ex) {
			// could not create resource bundle
			Logger.logError(
				Resources.getString("E010", pluginID, Locale.getDefault().toString(), name),
				ex);
			return name;
		} finally {
			resourceLoader = null; // don't need it after we have the bundle
		}
	}
	/**
	 * Returns a string from a property file of a
	 * documentation plugin installed remotely
	 */
	private static String getRemotePluginString(String pluginID, String name) {
		// We don't do cache lookup here, as it 
		// was done in getPluginString() 
		Properties properties = null;
		URL propertiesURL = null;
		try {
			propertiesURL =
				new URL(
					HelpSystem.getRemoteHelpServerURL(),
					HelpSystem.getRemoteHelpServerPath()
						+ "/"
						+ pluginID
						+ "/doc.properties?lang="
						+ Locale.getDefault().toString());

			properties = new Properties();
			properties.load(propertiesURL.openStream());
			propertiesTable.put(pluginID, properties);
		} catch (MalformedURLException e) {
			// could not create resource bundle
			//propertiesTable.put(pluginID, properties);
		} catch (Throwable ex) {
			// could not create resource bundle
			//propertiesTable.put(pluginID, properties);
		}

		if (properties != null) {
			String value = properties.getProperty(name);
			if (value == null)
				value = name;
			return value;
		} else {
			pluginsWithoutResources.add(pluginID);
			return name;
		}
	}
}
