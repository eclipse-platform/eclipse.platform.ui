package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
}
