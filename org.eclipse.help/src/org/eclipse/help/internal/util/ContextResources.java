package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.help.internal.server.PluginURL;

/**
 * Uses a resource bundle to load images and strings from
 * a property file in a documentation plugin
 */
public class ContextResources extends DocResources{

	static HashMap resourceBundleTable = new HashMap();
	static ArrayList pluginsWithoutResources = new ArrayList();
	static HashMap propertiesTable = new HashMap();

	/**
	 * Resources constructort.
	 */
	public ContextResources() {
		super();
	}
	/**
	 * Returns a string from a context property file.
	 * It uses 'name' as a the key to retrieve from a context.properties file.
	 * this is used for translation of F1 manifest files
	 */
	public static String getPluginString(String pluginID, String name) {
	
		// check plugins without resources 
		if (pluginsWithoutResources.contains(pluginID))
			return name;
	
		// check cache
		Properties properties = (Properties) propertiesTable.get(pluginID);
	
		// load context.properties
		if (properties == null) {
			properties=loadProperties(pluginID,"/context.properties");
		}
		
		// load doc.properties, for compatibility with old specs
		if (properties == null) {
			properties=loadProperties(pluginID,"/doc.properties");
		}
		
		if(properties==null){
			// cache properties
			pluginsWithoutResources.add(pluginID);
			return name;
		}
		
		String value = properties.getProperty(name);
		if (value != null)
			return value;
	
		return name;
	}
	/**
	 * Loads properties file from a locally installed plugin, and adds to cache
	 * @param pluginID plugin ID
	 * @param propFile file path relative to plugin install directory
	 * @return property file or null if not exists
	 */
	private static Properties loadProperties(String pluginID, String propFile){
		try {
			PluginURL propertiesURL =
				new PluginURL(pluginID + propFile, "lang=" + Locale.getDefault().toString());
			Properties localProp = new Properties();
					
			localProp.load(propertiesURL.openStream()); //throws
			
			propertiesTable.put(pluginID, localProp);
			return localProp;
		} catch (Throwable ex) {
		}
		return null;
	}
}
