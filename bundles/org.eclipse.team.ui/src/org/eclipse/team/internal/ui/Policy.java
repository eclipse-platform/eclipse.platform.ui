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
package org.eclipse.team.internal.ui;


import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.core.*;

/**
 * Policy implements NLS convenience methods for the plugin and
 * makes progress monitor policy decisions
 */
public class Policy {
	private static String bundleName = "org.eclipse.team.internal.ui.messages"; //$NON-NLS-1$
	private static ResourceBundle bundle = null;
	
	//debug constants
	public static boolean DEBUG_SYNC_MODELS = false;

	static {
		//init debug options
		if (TeamUIPlugin.getPlugin().isDebugging()) {
			DEBUG_SYNC_MODELS = "true".equalsIgnoreCase(Platform.getDebugOption(TeamUIPlugin.ID + "/syncmodels"));//$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/*
	 * Returns a resource bundle, creating one if it none is available. 
	 */
	private static ResourceBundle getResourceBundle() {
		// thread safety
		ResourceBundle tmpBundle = bundle;
		if (tmpBundle != null)
			return tmpBundle;
		// always create a new classloader to be passed in 
		// in order to prevent ResourceBundle caching
		return bundle = ResourceBundle.getBundle(bundleName);
	}
	
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string.
	 * 
	 * @param id  the id to look up
	 * @param binding  the string to bind to the result
	 * @return the bound string
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] { binding });
	}
	
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 * 
	 * @param id  the id to look up
	 * @param binding1  the first string to bind to the result
	 * @param binding2  the second string to bind to the result
	 * @return the bound string
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] { binding1, binding2 });
	}
	
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 * 
	 * @param id  the id to look up
	 * @param binding1  the first string to bind to the result
	 * @param binding2  the second string to bind to the result
	 * @param binding3  the third string to bind to the result
	 * @return the bound string
	 */
	public static String bind(String id, String binding1, String binding2,String binding3) {
		return bind(id, new String[] { binding1, binding2, binding3 });
	}
	
	/**
	 * Gets a string from the resource bundle. We don't want to crash because of a missing String.
	 * Returns the key if not found.
	 * 
	 * @param key  the id to look up
	 * @return the string with the given key
	 */
	public static String bind(String key, ResourceBundle b) {
		try {
			return b.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Gets a string from the resource bundle. We don't want to crash because of a missing String.
	 * Returns the key if not found.
	 * 
	 * @param key  the id to look up
	 * @return the string with the given key
	 */
	public static String bind(String key) {
		try {
			return getResourceBundle().getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Gets a string from the resource bundle and binds it with the given arguments. If the key is 
	 * not found, return the key.
	 * 
	 * @param key  the id to look up
	 * @param args  the strings to bind to the result
	 * @return the bound string
	 */
	public static String bind(String key, Object[] args) {
		try {
			return MessageFormat.format(bind(key), args);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Checks if the progress monitor is canceled.
	 * 
	 * @param monitor  the onitor to check for cancellation
	 * @throws OperationCanceledException if the monitor is canceled
	 */
	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}
	/**
	 * Returns a monitor for the given monitor
	 * 
	 * @param monitor  the monitor to return a monitor for
	 * @return a monitor for the given monitor
	 */
	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null) {
			return new NullProgressMonitor();
		}
		return monitor;
	}	
	
	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}
	
	public static IProgressMonitor subInfiniteMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new InfiniteSubProgressMonitor(monitor, ticks);
	}
	
	public static IProgressMonitor subNullMonitorFor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new NullSubProgressMonitor(monitor);
	}
	
	public static String toTruncatedPath(IPath path, int split) {
		// Search backwards until split separators are found
		int count = 0;
		String stringPath = path.toString();
		int index = stringPath.length();
		while (count++ < split && index != -1) {
			index = stringPath.lastIndexOf(IPath.SEPARATOR, index - 1);
		}
		if (index == -1) {
			return stringPath;
		} else {
			return "..." + stringPath.substring(index); //$NON-NLS-1$
		}
	}

	public static ResourceBundle getBundle() {
		return getResourceBundle();
	}
}
