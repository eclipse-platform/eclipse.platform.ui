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
package org.eclipse.help.internal.webapp;

 
import java.util.*;

import org.eclipse.core.boot.*;

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
	protected WebappResources() {
		super();
	}

	/**
	 * Returns a string from a property file.
	 * It uses 'name' as a the key to retrieve from the webapp.properties file.
	 * @param request HttpServletRequest or null; default locale will be used if null passed
	 */
	public static String getString(String name, Locale locale) {
		if (locale == null)
			locale = getDefaultLocale();

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
		// get value
		try {
			return bundle.getString(name);
		} catch (MissingResourceException mre) {
			return name;
		}
	}
	
	private static Locale getDefaultLocale() {
		String nl = BootLoader.getNL();
		// sanity test
		if (nl == null)
			return Locale.getDefault();
		
		// break the string into tokens to get the Locale object
		StringTokenizer locales = new StringTokenizer(nl,"_");
		if (locales.countTokens() == 1)
			return new Locale(locales.nextToken(), "");
		else if (locales.countTokens() == 2)
			return new Locale(locales.nextToken(), locales.nextToken());
		else if (locales.countTokens() == 3)
			return new Locale(locales.nextToken(), locales.nextToken(), locales.nextToken());
		else
			return Locale.getDefault();
	}
}
