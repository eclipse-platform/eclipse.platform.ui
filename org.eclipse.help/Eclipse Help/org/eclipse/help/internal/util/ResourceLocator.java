package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.net.*;
import org.eclipse.core.runtime.*;

public class ResourceLocator {
	
	/**
	 * Opens an input stream to a file contained in a zip in a plugin.
	 * This includes NL lookup.
	 */
	public static InputStream openFromZip(String pluginId, String zip, String file) {
		IPluginDescriptor pluginDesc = 
			Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		return openFromZip(pluginDesc, zip, file);
	}

	/**
	 * Opens an input stream to a file contained in a plugin.
	 * This includes NL lookup.
	 */
	public static InputStream openFromPlugin(String pluginId, String file) {
		IPluginDescriptor pluginDesc = 
			Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		return openFromPlugin(pluginDesc, file);
	}
	

	/**
	 * Opens an input stream to a file contained in a zip in a plugin.
	 * This includes NL lookup.
	 */
	public static InputStream openFromZip(IPluginDescriptor pluginDesc, String zip, String file) {
		// First try the NL lookup
		InputStream is = doOpenFromZip(pluginDesc, "$nl$/" + zip, file);
		if (is == null)
			// Default location <plugin>/doc.zip
			is = doOpenFromZip(pluginDesc, zip, file);
		return is;
	}

	/**
	 * Opens an input stream to a file contained in a plugin.
	 * This includes NL lookup.
	 */
	public static InputStream openFromPlugin(IPluginDescriptor pluginDesc, String file) {
		InputStream is = doOpenFromPlugin(pluginDesc, "$nl$/" + file);
		if (is == null)
			// Default location
			is = doOpenFromPlugin(pluginDesc, file);
		return is;
	}

	/**
	 * Opens an input stream to a file contained in doc.zip in a plugin
	 */
	private static InputStream doOpenFromZip(IPluginDescriptor pluginDesc, String zip, String file) {
		IPath zipFilePath = new Path(zip);
		try {
			URL zipFileURL = pluginDesc.getPlugin().find(zipFilePath);
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
	private static InputStream doOpenFromPlugin(IPluginDescriptor pluginDesc, String file) {
		IPath flatFilePath = new Path(file);
		try {
			URL flatFileURL = pluginDesc.getPlugin().find(flatFilePath);
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