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
package org.eclipse.help.ui.internal.browser.embedded;
import java.net.*;
import java.text.*;
import java.util.*;
/**
 * Uses a resource bundle to load strings from
 * a property file.  It does not support loading images,
 * as WorkbenchResources class does.
 * This class needs to properly use the desired locale.
 */
public class EmbeddedBrowserResources {
	private static ResourceBundle resBundle;
	private static URL imageURL;
	/**
	 * IEResources constructor.
	 */
	public EmbeddedBrowserResources(String localeString, String installURL) {
		super();
		resBundle =
			ResourceBundle.getBundle("org.eclipse.help.ui.internal.HelpUIResources", getLocale(localeString));
		try {
			imageURL = new URL(new URL(installURL), "icons/");
		} catch (MalformedURLException e) {
		}
	}
	/**
	 * Returns a string from a property file
	 */
	public static URL getImagePath(String name) {
		URL imagePathURL = null;
		try {
			imagePathURL = new URL(imageURL, name);
			return imagePathURL;
		} catch (MalformedURLException e) {
		}
		return null;
	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name) {
		try {
			return resBundle.getString(name);
		} catch (Exception e) {
			return name;
		}
	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}

	private static Locale getLocale(String nl) {
		if (nl == null)
			return Locale.getDefault();

		// break the string into tokens to get the Locale object
		StringTokenizer locales = new StringTokenizer(nl, "_");
		if (locales.countTokens() == 1)
			return new Locale(locales.nextToken(), "");
		else if (locales.countTokens() == 2)
			return new Locale(locales.nextToken(), locales.nextToken());
		else if (locales.countTokens() == 3)
			return new Locale(
				locales.nextToken(),
				locales.nextToken(),
				locales.nextToken());
		else
			return Locale.getDefault();
	}
}
