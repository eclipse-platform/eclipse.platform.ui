package org.eclipse.help.ui.internal.browser.win32;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
/**
 * Uses a resource bundle to load strings from
 * a property file.  It does not support loading images,
 * as WorkbenchResources class does.
 * This class needs to properly use the desired locale.
 */
public class IEResources {
	private static ResourceBundle resBundle;
	private static URL imageURL;
	/**
	 * IEResources constructor.
	 */
	public IEResources(String installURL) {
		super();
		resBundle = ResourceBundle.getBundle("helpworkbench", Locale.getDefault());
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
			imagePathURL = new URL(imageURL, resBundle.getString(name));
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
				MessageFormat.format(stringFromPropertiesFile, new Object[] { replace0 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
					}