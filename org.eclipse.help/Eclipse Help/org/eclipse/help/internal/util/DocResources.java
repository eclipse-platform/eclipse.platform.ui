package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import java.io.*;
import org.eclipse.help.internal.util.*;


/**
 * Uses a resource bundle to load images and strings from
 * a property file in a documentation plugin
 */
public class DocResources {

	//static HashMap resourceBundleTable = new HashMap();
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
	 * this is used for translation of all manifest files (excluding F1)
	 */
	public static String getPluginString(String pluginID, String name) {
	
		// check plugins without resources 
		if (pluginsWithoutResources.contains(pluginID))
			return name;
	
		// check cache
		Properties properties = (Properties) propertiesTable.get(pluginID);
	
		// load doc.properties
		if (properties == null) {
			try {
				Properties localProp = new Properties();
				
				InputStream data = 
					ResourceLocator.openFromPlugin(pluginID, "doc.properties");		
				localProp.load(data);
				
				properties = localProp;
				propertiesTable.put(pluginID, properties);
			} catch (Throwable ex) {
			}
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
	
}
