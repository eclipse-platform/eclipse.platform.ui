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
package org.eclipse.team.internal.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.*;

public class Policy {
	private static String bundleName = "org.eclipse.team.internal.core.messages"; //$NON-NLS-1$
	private static ResourceBundle bundle = null;

	//debug constants
	public static boolean DEBUG_STREAMS = false;
	public static boolean DEBUG_REFRESH_JOB = true;
	public static boolean DEBUG_BACKGROUND_EVENTS = false;
	public static boolean DEBUG_THREADING = false;

	static {
		//init debug options
		if (TeamPlugin.getPlugin().isDebugging()) {
			DEBUG_STREAMS = "true".equalsIgnoreCase(Platform.getDebugOption(TeamPlugin.ID + "/streams"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_REFRESH_JOB = "true".equalsIgnoreCase(Platform.getDebugOption(TeamPlugin.ID + "/refreshjob"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_BACKGROUND_EVENTS = "true".equalsIgnoreCase(Platform.getDebugOption(TeamPlugin.ID + "/backgroundevents"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_THREADING = "true".equalsIgnoreCase(Platform.getDebugOption(TeamPlugin.ID + "/threading"));//$NON-NLS-1$ //$NON-NLS-2$
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
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] { binding });
	}
	
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] { binding1, binding2 });
	}
	
	/**
	 * Gets a string from the resource bundle. We don't want to crash because of a missing String.
	 * Returns the key if not found.
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
	 * Progress monitor helpers
	 */
	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}
	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();
		return monitor;
	}
	
	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}
	
	public static IProgressMonitor infiniteSubMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new InfiniteSubProgressMonitor(monitor, ticks);
	}
}
