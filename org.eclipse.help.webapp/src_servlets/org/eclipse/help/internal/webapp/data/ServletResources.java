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
package org.eclipse.help.internal.webapp.data;

 

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

	/**
	 * Returns a string from a property file.
	 * It uses 'name' as a the key to retrieve from the webapp.properties file.
	 * @param request HttpServletRequest or null; default locale will be used if null passed
	 */
	public static String getString(String name, String replace0, HttpServletRequest request) {
		return WebappResources.getString(name, request.getLocale(), replace0);
	}
}
