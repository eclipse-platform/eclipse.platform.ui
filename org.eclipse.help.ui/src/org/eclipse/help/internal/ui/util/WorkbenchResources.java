package org.eclipse.help.internal.ui.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.*;
import java.util.*;
import org.eclipse.help.internal.ui.WorkbenchHelpPlugin;
/**
 * Uses a resource bundle to load images and strings from
 * a property file.
 * This class needs to properly use the desired locale.
 */
public class WorkbenchResources {
	//*** NOTE: change this to properly load a resource bundle help.properties
	//***       for a desired locale....
	private static ResourceBundle resBundle;
	private static URL imageURL;
	static {
		resBundle = ResourceBundle.getBundle("helpworkbench", Locale.getDefault());
		try {
			imageURL =
				new URL(
					WorkbenchHelpPlugin.getDefault().getDescriptor().getInstallURL(),
					"icons/");
		} catch (MalformedURLException e) {
		}
	}
	/**
	 * WorkbenchResources constructor comment.
	 */
	public WorkbenchResources() {
		super();
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
		//return image;
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
	public static String getString(String name, String replace1) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%1", replace1);
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}
	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace1, String replace2) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%1", replace1);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%2", replace2);
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
		String replace1,
		String replace2,
		String replace3) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%1", replace1);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%2", replace2);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%3", replace3);
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
		String replace1,
		String replace2,
		String replace3,
		String replace4) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%1", replace1);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%2", replace2);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%3", replace3);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%4", replace4);
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
		String replace1,
		String replace2,
		String replace3,
		String replace4,
		String replace5) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%1", replace1);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%2", replace2);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%3", replace3);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%4", replace4);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%5", replace5);
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
		String replace1,
		String replace2,
		String replace3,
		String replace4,
		String replace5,
		String replace6) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%1", replace1);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%2", replace2);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%3", replace3);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%4", replace4);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%5", replace5);
			stringFromPropertiesFile =
				TString.change(stringFromPropertiesFile, "%6", replace6);
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}
	}
}