package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;

public class ResourceLocator {

	/**
	 * Opens an input stream to a file contained in a zip in a plugin.
	 * This includes NL lookup.
	 */
	public static InputStream openFromZip(
		String pluginId,
		String zip,
		String file,
		String locale) {
		IPluginDescriptor pluginDesc =
			Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		return openFromZip(pluginDesc, zip, file, locale);
	}

	/**
	 * Opens an input stream to a file contained in a plugin.
	 * This includes NL lookup.
	 */
	public static InputStream openFromPlugin(
		String pluginId,
		String file,
		String locale) {
		IPluginDescriptor pluginDesc =
			Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		return openFromPlugin(pluginDesc, file, locale);
	}

	/**
	 * Opens an input stream to a file contained in a zip in a plugin.
	 * This includes NL lookup.
	 */
	public static InputStream openFromZip(
		IPluginDescriptor pluginDesc,
		String zip,
		String file,
		String locale) {
		// First try the NL lookup
		InputStream is = doOpenFromZip(pluginDesc, "$nl$/" + zip, file, locale);
		if (is == null)
			// Default location <plugin>/doc.zip
			is = doOpenFromZip(pluginDesc, zip, file, locale);
		return is;
	}

	/**
	 * Opens an input stream to a file contained in a plugin.
	 * This includes NL lookup.
	 */
	public static InputStream openFromPlugin(
		IPluginDescriptor pluginDesc,
		String file,
		String locale) {
		InputStream is = doOpenFromPlugin(pluginDesc, "$nl$/" + file, locale);
		if (is == null)
			// Default location
			is = doOpenFromPlugin(pluginDesc, file, locale);
		return is;
	}

	/**
	 * Opens an input stream to a file contained in doc.zip in a plugin
	 */
	private static InputStream doOpenFromZip(
		IPluginDescriptor pluginDesc,
		String zip,
		String file,
		String locale) {
		IPath zipFilePath = new Path(zip);
		Map override = new HashMap(1);
		override.put("$nl$", locale);
		try {
			URL zipFileURL = pluginDesc.getPlugin().find(zipFilePath, override);
			if (zipFileURL != null) {
				try {
					URL realZipURL = Platform.resolve(zipFileURL);
					if (realZipURL == null)
						return null;
					URL jurl = new URL("jar", "", realZipURL.toExternalForm() + "!/" + file);

					URLConnection jconnection = jurl.openConnection();
					jconnection.setDefaultUseCaches(false);
					jconnection.setUseCaches(false);
					return jconnection.getInputStream();

				} catch (IOException ioe) {
					return null;
				}
			}
		} catch (CoreException ce) {
			return null;
		}
		return null;
	}

	/**
	 * Opens an input stream to a file contained in a plugin
	 */
	private static InputStream doOpenFromPlugin(
		IPluginDescriptor pluginDesc,
		String file,
		String locale) {
		IPath flatFilePath = new Path(file);
		Map override = new HashMap(1);
		override.put("$nl$", locale);
		try {
			URL flatFileURL = pluginDesc.getPlugin().find(flatFilePath, override);
			if (flatFileURL != null)
				try {
					return flatFileURL.openStream();
				} catch (IOException e) {
					return null;
				}

		} catch (CoreException ce) {
			return null;
		}
		return null;
	}

}