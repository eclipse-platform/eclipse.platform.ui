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

	// resource bundles indexed by locale
	private static HashMap resourceBundleTable = new HashMap();
	/**
	 * Resources constructor.
	 */
	protected WebappResources(ServletContext context) {
		super();
	}

	/**
	 * Returns a string from a property file.
	 * It uses 'name' as a the key to retrieve from the webapp.properties file.
	 * @param request HttpServletRequest or null; default locale will be used if null passed
	 */
	public static String getString(String name, HttpServletRequest request) {
		Locale locale =
			request == null ? Locale.getDefault() : request.getLocale();

		// check cache
		ResourceBundle bundle =
			(ResourceBundle) resourceBundleTable.get(locale);

		// load bundle
		if (bundle == null) {
			bundle = ResourceBundle.getBundle("webapp", locale);
			if (bundle != null) {
				resourceBundleTable.put(locale, bundle);
			} else {
				return name;
			}
		}

		String value = bundle.getString(name);
		if (value != null)
			return value;
		else
			return name;
	}
}