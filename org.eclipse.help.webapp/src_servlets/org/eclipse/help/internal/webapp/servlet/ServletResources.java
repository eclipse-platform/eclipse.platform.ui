package org.eclipse.help.internal.webapp.servlet;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 

import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.internal.webapp.WebappResources;


/**
 * Uses a resource bundle to load images and strings from
 * a property file in a documentation plugin
 */
public class ServletResources {

	/**
	 * Resources constructor.
	 */
	protected ServletResources() {
		super();
	}

	/**
	 * Returns a string from a property file.
	 * It uses 'name' as a the key to retrieve from the webapp.properties file.
	 * @param request HttpServletRequest or null; default locale will be used if null passed
	 */
	public static String getString(String name, HttpServletRequest request) {
		return WebappResources.getString(name, request.getLocale());
	}
}