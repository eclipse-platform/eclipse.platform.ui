package org.eclipse.help.servlet;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Uses a resource bundle to load images and strings from
 * a property file in a documentation plugin
 */
public class WebappResources {

	// properties indexed by locale
	private static HashMap propertiesTable = new HashMap();
	private static EclipseConnector connector;

	/**
	 * Resources constructort.
	 */
	protected WebappResources(ServletContext context) {
		super();
		if (this.connector == null)
			this.connector = new EclipseConnector(context);
	}

	/**
	 * Returns a string from a property file.
	 * It uses 'name' as a the key to retrieve from the webapp.properties file.
	 */
	public static String getString(String name, HttpServletRequest request) {

		String locale = request.getLocale().toString();

		// check cache
		Properties properties = (Properties) propertiesTable.get(locale);

		// load context.properties
		if (properties == null)
			properties = loadProperties(request);

		if (properties == null)
			return name;

		String value = properties.getProperty(name);
		if (value != null)
			return value;
		else
			return name;
	}

	/**
	 * Loads properties file for a locale and adds to cache
	 * @param locale the input locale
	 * @return property file or null if not exists
	 */
	private static Properties loadProperties(HttpServletRequest request) {
		try {
			String propURL = "help:/org.eclipse.help.webapp/webapp.properties";
			InputStream propertiesStream = connector.openStream(propURL, request);
			Properties localProp = new Properties();
			localProp.load(propertiesStream);

			propertiesTable.put(request.getLocale().toString(), localProp);
			return localProp;
		} catch (Throwable ex) {
		}
		return null;
	}
}