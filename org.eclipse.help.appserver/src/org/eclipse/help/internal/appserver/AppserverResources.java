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
package org.eclipse.help.internal.appserver;

import java.text.*;
import java.util.*;

import org.eclipse.core.runtime.*;

/**
 * Uses a resource bundle to load strings from a property file.
 */
class AppserverResources {
	private static ResourceBundle resBundle;
	static {
		resBundle = ResourceBundle
				.getBundle(AppserverResources.class.getName());
	}
	/**
	 * Resources constructor.
	 */
	public AppserverResources() {
		super();
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
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0, String replace1) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0,
			String replace1, String replace2) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1,
							replace2});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0,
			String replace1, String replace2, String replace3) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1,
							replace2, replace3});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0,
			String replace1, String replace2, String replace3, String replace4) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1,
							replace2, replace3, replace4});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0,
			String replace1, String replace2, String replace3, String replace4,
			String replace5) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile = MessageFormat.format(
					stringFromPropertiesFile, new Object[]{replace0, replace1,
							replace2, replace3, replace4, replace5});
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

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
