package org.eclipse.help.internal.server;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.util.Logger;
import java.net.*;

import org.eclipse.help.internal.HelpSystem;

/**
 * URL for documentation coming from a plugin.
 */
public class PluginURL extends HelpURL {
	// Keep track of all the zip files so they won't be
	// opened again. We may need to close them all at
	// some point, or maintain a window of just a few
	// simultaneously opened zip files.
	private static Hashtable zips = new Hashtable(/*of ZipFile */);

	private final static String lang = "lang";

	/**
	 * FileURL constructor comment.
	 * @param url java.lang.String
	 */
	public PluginURL(String url, String query) {
		super(url, query);
	}
	public static synchronized void clear() {
		for (Enumeration it = zips.elements(); it.hasMoreElements();) {
			try {
				ZipFile z = (ZipFile) it.nextElement();
				z.close();
			} catch (IOException e) {
			}
		}
	}
	private String getFile() {
		// Strip the plugin id
		int start = url.indexOf("/") + 1;

		// Strip query string or anchor bookmark
		int end = url.indexOf("?");
		if (end == -1)
			end = url.indexOf("#");
		if (end == -1)
			end = url.length();

		return url.substring(start, end);
	}
	private String getLocation() {
		// Assume the url is pluginID/path_to_topic.html
		int i = url.indexOf('/');
		String plugin = i == -1 ? "" : url.substring(0, i);
		IPluginDescriptor descriptor =
			Platform.getPluginRegistry().getPluginDescriptor(plugin);
		if (descriptor != null)
			return descriptor.getInstallURL().getFile().replace(File.separatorChar, '/');
		else
			return null;
	}
	private IPluginDescriptor getPlugin() {
		// Assume the url is pluginID/path_to_topic.html
		int i = url.indexOf('/');
		String plugin = i == -1 ? "" : url.substring(0, i);
		return Platform.getPluginRegistry().getPluginDescriptor(plugin);
	}
	/** Returns the path prefix that identifies the URL. */
	public static String getPrefix() {
		return "";
	}
	public boolean isCacheable() {
		if (getValue("resultof") != null)
			return false;
		else
			return true;
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStream() {
		// if it is client install,
		// forward request to the remote server
		if (HelpSystem.isClient()) {
			try {
				URL forwardingURL;
				if (query != null && !"".equals(query))
					forwardingURL =
						new URL(
							HelpSystem.getRemoteHelpServerURL(),
							HelpSystem.getRemoteHelpServerPath() + "/" + url + "?" + query);
				else
					forwardingURL =
						new URL(
							HelpSystem.getRemoteHelpServerURL(),
							HelpSystem.getRemoteHelpServerPath() + "/" + url);
				return forwardingURL.openStream();
			} catch (MalformedURLException mue) {
				return null;
			} catch (IOException ioe) {
				return null;
			}
		}

		IPluginDescriptor plugin = getPlugin();
		if (plugin == null)
			return null;

		String fileWithoutLocalePath = getFile();
		String fileWithLocalePath = fileWithoutLocalePath;
		String localePath = null;
		InputStream inputStream = null;

		URL purl = plugin.getInstallURL();

		Locale locale = Locale.getDefault();
		String clientLocale = getValue(lang);

		// The clientLocale takes precedent over the Help Server locale.
		if (clientLocale != null) {
			if (clientLocale.indexOf("_") != -1) {
				locale = new Locale(clientLocale.substring(0, 2), clientLocale.substring(3, 5));
			} else {
				locale = new Locale(clientLocale.substring(0, 2), "_  ");
				// In case client locale only contains language info and no country info
			}
		}

		/*
		The code looks up the doc in the following fasion (using en_US as example):
		1.  It looks for the doc.zip file in "plugin directory/.nl/xx_XX" and unzip the file to retrieve the .html doc file
		2.  Then, it looks for the doc.zip file in "plugin directory/.nl/xx" and unzip the file to retrieve the .html doc file
		3.  Then, it looks for the doc.zip file in the root directory of plugin directory and unzip the file to retrieve the default .html doc file
		4.  Then, it looks for the .html doc file in "plugin directory/.nl/xx_XX"
		5.  Then, it looks for the .html doc file in "plugin directory/.nl/xx"
		6.  Then, it looks for the default .html doc file in the root directory of plugin directory
		*/

		if (inputStream == null) {
			localePath = ".nl/" + locale.getLanguage() + "_" + locale.getCountry() + "/";
			inputStream =
				openStreamFromZip(locale, fileWithoutLocalePath, localePath, purl);
		}
		if (inputStream == null) {
			localePath = ".nl/" + locale.getLanguage() + "/";
			inputStream =
				openStreamFromZip(locale, fileWithoutLocalePath, localePath, purl);
		}
		if (inputStream == null) {
			localePath = "./";
			inputStream =
				openStreamFromZip(locale, fileWithoutLocalePath, localePath, purl);
		}

		if (inputStream == null) {
			fileWithLocalePath =
				".nl/"
					+ locale.getLanguage()
					+ "_"
					+ locale.getCountry()
					+ "/"
					+ fileWithoutLocalePath;
			inputStream = openStreamFromFile(locale, fileWithLocalePath, purl);
		}
		if (inputStream == null) {
			fileWithLocalePath =
				".nl/" + locale.getLanguage() + "/" + fileWithoutLocalePath;
			inputStream = openStreamFromFile(locale, fileWithLocalePath, purl);
		}
		if (inputStream == null) {
			fileWithLocalePath = "./";
			inputStream = openStreamFromFile(locale, fileWithoutLocalePath, purl);
		}

		return inputStream;

	}
	/**
	 * @return java.io.InputStream
	 * @param locale java.util.Locale
	 * @param file java.lang.String
	 * @param purl java.net.URL
	 */
	private InputStream openStreamFromFile(Locale locale, String file, URL purl) {
		InputStream inputStream = null;
		try {
			URL furl = new URL(purl, file);

			try {
				inputStream = furl.openStream();
			} catch (IOException e) {
				inputStream = null;
			}
		} catch (IOException e) {
		}
		return inputStream;
	}
	/**
	 * @return java.io.InputStream
	 * @param locale java.util.Locale
	 * @param file java.lang.String
	 * @param purl java.net.URL
	 */
	private InputStream openStreamFromZip(
		Locale locale,
		String file,
		String localePath,
		URL purl) {
		InputStream inputStream = null;
		try {
			URL jurl =
				new URL("jar", "", purl.toExternalForm() + localePath + "doc.zip!/" + file);

			try {
				inputStream = jurl.openStream();
			} catch (IOException e) {
				inputStream = null;
			}
		} catch (IOException e) {
		}
		return inputStream;
	}
}
