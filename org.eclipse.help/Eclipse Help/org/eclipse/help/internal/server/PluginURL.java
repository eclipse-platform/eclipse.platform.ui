package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.util.*;
import java.net.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;

/**
 * URL for documentation coming from a plugin.
 */
public class PluginURL extends HelpURL {
	private final static String lang = "lang";
	private final static String INI = ".ini";
	protected IPluginDescriptor plugin;
	protected String file;
	
	/**
	 * FileURL constructor comment.
	 * @param url java.lang.String
	 */
	public PluginURL(String url, String query) {
		super(url, query);
	}

	private String getFile() {
		if (file == null)
		{
			// Strip the plugin id
			int start = url.indexOf("/") + 1;

			// Strip query string or anchor bookmark
			int end = url.indexOf("?");
			if (end == -1)
				end = url.indexOf("#");
			if (end == -1)
				end = url.length();

			file = url.substring(start, end);
		}
		return file;

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
		if (plugin == null)
		{
			// Assume the url is pluginID/path_to_topic.html
			int i = url.indexOf('/');
			String pluginId = i == -1 ? "" : url.substring(0, i);
			plugin = Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		}
		return plugin;
	}

	private Locale getLocale()
	{	
		String clientLocale = getValue(lang);

		// The clientLocale takes precedence over the Help Server locale.
		if (clientLocale != null) {
			if (clientLocale.indexOf("_") != -1) {
				return new Locale(clientLocale.substring(0, 2), clientLocale.substring(3, 5));
			} else {
				return new Locale(clientLocale.substring(0, 2), "_  ");
				// In case client locale only contains language info and no country info
			}
		}
		else
			return Locale.getDefault();
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
		else
			return openStreamLocally();
	}

	/**
	 * Opens an input stream to the plugin url specified, assuming
	 * the plugin is obtained from the current eclipse session
	 */
	public InputStream openStreamLocally(){
		if (getPlugin() == null)
			return null;

		if (getFile() == null)
			return null;

		// When the platform supports find() with a locale specified, use this
		//Locale locale = getLocale();
					
		InputStream inputStream = null;
		
		// first try finding the file inside nl tree in doc.zip,
		// and then, in the file system
		inputStream = ResourceLocator.openFromZip(getPlugin(), "doc.zip", getFile());
		if (inputStream == null)
			inputStream = ResourceLocator.openFromPlugin(getPlugin(), getFile());
		
		return inputStream;
	}
	
	
	
}
