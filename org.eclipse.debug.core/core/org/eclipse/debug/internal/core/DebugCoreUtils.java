package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;

/**
 * Utility methods for the debug core plugin.
 */
public class DebugCoreUtils {

	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		if (DebugPlugin.getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			System.out.println("Internal error logged from debug core: "); //$NON-NLS-1$
			e.printStackTrace();
			System.out.println();
		}
	}


}
