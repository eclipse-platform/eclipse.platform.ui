package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.text.*;
import java.util.*;

import org.eclipse.core.boot.*;

/**
 * Uses a resource bundle to load images and strings from
 * a property file.
 */
public class Resources {
	private static ResourceBundle resBundle;
	static {
		resBundle = ResourceBundle.getBundle("help", getDefaultLocale());
	}
	/**
	 * Resources constructor.
	 */
	public Resources() {
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
			stringFromPropertiesFile =
				MessageFormat.format(stringFromPropertiesFile, new Object[] { replace0 });
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
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(
		String name,
		String replace0,
		String replace1,
		String replace2) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1, replace2 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(
		String name,
		String replace0,
		String replace1,
		String replace2,
		String replace3) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1, replace2, replace3 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(
		String name,
		String replace0,
		String replace1,
		String replace2,
		String replace3,
		String replace4) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1, replace2, replace3, replace4 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(
		String name,
		String replace0,
		String replace1,
		String replace2,
		String replace3,
		String replace4,
		String replace5) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1, replace2, replace3, replace4, replace5 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
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