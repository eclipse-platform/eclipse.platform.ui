/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.util;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;

public class ResourceLocator {
	private static final Hashtable zipCache = new Hashtable();
	private static final Object zipNotFound = new Object();

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
		String realZipURL = findZip(pluginDesc, zip, locale);
		if (realZipURL == null) {
			return null;
		}
		try {
			URL jurl = new URL("jar", "", realZipURL + "!/" + file);

			URLConnection jconnection = jurl.openConnection();
			jconnection.setDefaultUseCaches(false);
			jconnection.setUseCaches(false);
			return jconnection.getInputStream();

		} catch (IOException ioe) {
			return null;
		}
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
			URL flatFileURL =
				pluginDesc.getPlugin().find(flatFilePath, override);
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
	/**
	 * @param pluginDesc
	 * @param zip zip file path as required by Plugin.find()
	 * @param locale
	 * @return String form of resolved URL of a zip or null
	 */
	private static String findZip(
		IPluginDescriptor pluginDesc,
		String zip,
		String locale) {
		String pluginID = pluginDesc.getUniqueIdentifier();
		// check cache
		Object cached = zipCache.get(pluginID + '/' + zip + '/' + locale);
		if (cached == null) {
			// not in cache find on filesystem
			IPath zipFilePath = new Path(zip);
			Map override = new HashMap(1);
			override.put("$nl$", locale);
			try {
				URL zipFileURL =
					pluginDesc.getPlugin().find(zipFilePath, override);
				if (zipFileURL != null) {
					URL realZipURL = Platform.resolve(zipFileURL);
					cached = realZipURL.toExternalForm();
				} else {
					cached = zipNotFound;
				}
			} catch (CoreException ce) {
				cached = zipNotFound;
			} catch (IOException ioe) {
				cached = zipNotFound;
			}
			// cache it
			zipCache.put(pluginID + '/' + zip + '/' + locale, cached);
		}
		if (cached == zipNotFound) {
			return null;
		}
		return (String) cached;
	}

}
