/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp;

import java.text.*;
import java.util.*;

import org.eclipse.core.runtime.*;

/**
 * Uses a resource bundle to load images and strings from a property file in a
 * documentation plugin
 */
public class WebappResources {

	// resource bundles indexed by locale
	private static HashMap resourceBundleTable = new HashMap();

	/**
	 * Returns a string from a property file. It uses 'name' as a the key to
	 * retrieve from the webapp.properties file.
	 */
	public static String getString(String name, Locale locale) {

		// get bundle
		ResourceBundle bundle = getBundle(locale);
		if (bundle == null) {
			return name;
		}

		// get value
		try {
			return bundle.getString(name);
		} catch (MissingResourceException mre) {
			return name;
		}
	}

	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, Locale locale, String replace0) {

		// get bundle
		ResourceBundle bundle = getBundle(locale);
		if (bundle == null) {
			return name;
		}

		// get value
		try {
			String stringFromPropertiesFile = bundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Obtains resource bundle for specified locale. Loads bundle if necessary
	 * 
	 * @param locale
	 *            Locale or null to use default locale
	 * @return ResourceBundle or null if not found
	 */
	private static ResourceBundle getBundle(Locale locale) {
		if (locale == null)
			locale = getDefaultLocale();

		// check cache
		ResourceBundle bundle = (ResourceBundle) resourceBundleTable
				.get(locale);

		// load bundle
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(WebappResources.class.getName(),
					locale);
			if (bundle != null) {
				resourceBundleTable.put(locale, bundle);
			}
		}
		return bundle;
	}
	private static Locale getDefaultLocale() {
		String nl = Platform.getNL();
		// sanity test
		if (nl == null)
			return Locale.getDefault();

		// break the string into tokens to get the Locale object
		StringTokenizer locales = new StringTokenizer(nl, "_"); //$NON-NLS-1$
		if (locales.countTokens() == 1)
			return new Locale(locales.nextToken(), ""); //$NON-NLS-1$
		else if (locales.countTokens() == 2)
			return new Locale(locales.nextToken(), locales.nextToken());
		else if (locales.countTokens() == 3)
			return new Locale(locales.nextToken(), locales.nextToken(), locales
					.nextToken());
		else
			return Locale.getDefault();
	}
}
