package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.debug.core.DebugPlugin;

/**
 * Utility methods for the debug core plugin.
 */
public class DebugCoreUtils {

	private static ResourceBundle fgResourceBundle;

	/**
	 * Utility method
	 */
	public static String getResourceString(String key) {
		if (fgResourceBundle == null) {
			fgResourceBundle= getResourceBundle();
		}
		if (fgResourceBundle != null) {
			return fgResourceBundle.getString(key);
		} else {
			return "!" + key + "!";
		}
	}

	/**
	 * Returns the resource bundle used by the core debug plugin.
	 */
	public static ResourceBundle getResourceBundle() {
		try {
			return ResourceBundle.getBundle("org.eclipse.debug.internal.core.DebugCoreResources");
		} catch (MissingResourceException e) {
			logError(e);
		}
		return null;
	}

	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		if (DebugPlugin.getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			System.out.println("Internal error logged from debug core: ");
			e.printStackTrace();
			System.out.println();
		}
	}

	/**
	 * Plug in the single argument to the resource String for the key to get a formatted resource String
	 */
	public static String getFormattedString(String key, String arg) {
		String string= getResourceString(key);
		return MessageFormat.format(string, new String[] { arg });
	}
	
	/**
	 * Plug in the arguments to the resource String for the key to get a formatted resource String
	 */
	public static String getFormattedString(String key, String[] args) {
		String string= getResourceString(key);
		return MessageFormat.format(string, args);
	}
}
