package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	private final static String lang = "lang";
	private final static String INI = ".ini";
	private final static String PROPERTIES = ".properties";

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
		
		// Optimization:
		// Properties files and .ini files are likely to
		// be found on the disk first.
		// The other files (documentation) are more likely
		// to first be found in the doc.zip files
		if (url.endsWith(PROPERTIES)  || url.endsWith(INI))
		{
			// first try finding the file in the plugin
			// and if not found, in the doc.zip
			inputStream = openFileFromPlugin();
			if (inputStream == null)
				inputStream = openFileFromZip();
		}
		else
		{
			// first try finding the file inside nl tree in doc.zip,
			// and then, in the file system
			inputStream = openFileFromZip();
			if (inputStream == null)
				inputStream = openFileFromPlugin();
		}
		return inputStream;
	}
	
	/**
	 * Opens an input stream to a file contained in doc.zip in a plugin.
	 * This includes NL lookup.
	 */
	public InputStream openFileFromZip()
	{
		// First try the NL lookup
		InputStream is = doOpenFileFromZip("$nl$/doc.zip", getFile());
		if (is == null)
			// Default location <plugin>/doc.zip
			is = doOpenFileFromZip("doc.zip", getFile());
		return is;
	}

	/**
	 * Opens an input stream to a file contained in a plugin.
	 * This includes NL lookup.
	 */
	public InputStream openFileFromPlugin()
	{
		InputStream is = doOpenFileFromPlugin("$nl$/" + getFile());
		if (is == null)
			// Default location
			is = doOpenFileFromPlugin(getFile());
		return is;
	}
	
	/**
	 * Opens an input stream to a file contained in doc.zip in a plugin
	 */
	private InputStream doOpenFileFromZip(String zip, String file)
	{
		IPath zipFilePath = new Path(zip);
		try{
			URL zipFileURL = getPlugin().getPlugin().find(zipFilePath);
			if(zipFileURL!=null){
				try{
					URL realZipURL = Platform.resolve(zipFileURL);
					if (realZipURL == null) 
						return null;
					URL jurl =	new URL("jar", "", realZipURL.toExternalForm()+"!/"+file);
					
					URLConnection jconnection = jurl.openConnection();
					jconnection.setDefaultUseCaches(false);
					jconnection.setUseCaches(false);
					return jconnection.getInputStream();
					
					//return jurl.openStream();
				}catch (IOException ioe){
					return null;
				}
			}
		}catch(CoreException ce){
			return null;
		}
		return null;
	}
	
	/**
	 * Opens an input stream to a file contained in a plugin
	 */
	private InputStream doOpenFileFromPlugin(String file)
	{
		IPath flatFilePath = new Path(file); 
		try{
			URL flatFileURL = getPlugin().getPlugin().find(flatFilePath);
			if(flatFileURL!=null)
				try{
					return flatFileURL.openStream();
				} catch (IOException e){
					return null;
				}

		}catch(CoreException ce){
			return null;
		}
		return null;
	}
	
}
