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
package org.eclipse.core.internal.utils;

import java.text.MessageFormat;
import java.util.*;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;

public class Policy {
	public static final long MAX_BUILD_DELAY = 1000;
	public static final long MIN_BUILD_DELAY = 100;
	private static String bundleName = "org.eclipse.core.internal.utils.messages";//$NON-NLS-1$
	private static ResourceBundle bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());

	public static final boolean buildOnCancel = false;
	public static int opWork = 99;
	public static int endOpWork = 1;
	public static final int totalWork = 100;

	//debug constants
	public static boolean DEBUG_BUILD_FAILURE = false;
	public static boolean DEBUG_NEEDS_BUILD = false;
	public static boolean DEBUG_BUILD_INVOKING = false;
	public static boolean DEBUG_BUILD_DELTA = false;
	public static boolean DEBUG_NATURES = false;
	public static boolean DEBUG_HISTORY = false;
	public static boolean DEBUG_PREFERENCES = false;

	public static boolean MONITOR_BUILDERS = false;
	public static boolean MONITOR_LISTENERS = false;

	// Get timing information for restoring data
	public static boolean DEBUG_RESTORE = false;
	public static boolean DEBUG_RESTORE_MARKERS = false;
	public static boolean DEBUG_RESTORE_SYNCINFO = false;
	public static boolean DEBUG_RESTORE_TREE = false;
	public static boolean DEBUG_RESTORE_METAINFO = false;
	public static boolean DEBUG_RESTORE_SNAPSHOTS = false;
	public static boolean DEBUG_RESTORE_MASTERTABLE = false;

	// Get timing information for saving and snapshoting data
	public static boolean DEBUG_SAVE = false;
	public static boolean DEBUG_SAVE_MARKERS = false;
	public static boolean DEBUG_SAVE_SYNCINFO = false;
	public static boolean DEBUG_SAVE_TREE = false;
	public static boolean DEBUG_SAVE_METAINFO = false;
	public static boolean DEBUG_SAVE_MASTERTABLE = false;

	public static boolean DEBUG_AUTO_REFRESH = false;

	static {
		//init debug options
		if (ResourcesPlugin.getPlugin().isDebugging()) {
			String sTrue = Boolean.TRUE.toString();
			DEBUG_BUILD_FAILURE = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/build/failure")); //$NON-NLS-1$ 
			DEBUG_NEEDS_BUILD = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/build/needbuild")); //$NON-NLS-1$ 
			DEBUG_BUILD_INVOKING = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/build/invoking")); //$NON-NLS-1$ 
			DEBUG_BUILD_DELTA = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/build/delta")); //$NON-NLS-1$ 
			DEBUG_NATURES = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/natures")); //$NON-NLS-1$ 
			DEBUG_HISTORY = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/history")); //$NON-NLS-1$ 
			DEBUG_PREFERENCES = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/preferences")); //$NON-NLS-1$

			MONITOR_BUILDERS = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/monitor/builders")); //$NON-NLS-1$ 
			MONITOR_LISTENERS = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/monitor/listeners")); //$NON-NLS-1$ 

			DEBUG_RESTORE_MARKERS = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/restore/markers")); //$NON-NLS-1$ 
			DEBUG_RESTORE_SYNCINFO = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/restore/syncinfo")); //$NON-NLS-1$ 
			DEBUG_RESTORE_TREE = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/restore/tree")); //$NON-NLS-1$ 
			DEBUG_RESTORE_METAINFO = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/restore/metainfo")); //$NON-NLS-1$ 
			DEBUG_RESTORE_SNAPSHOTS = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/restore/snapshots")); //$NON-NLS-1$ 
			DEBUG_RESTORE_MASTERTABLE = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/restore/mastertable")); //$NON-NLS-1$ 
			DEBUG_RESTORE = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/restore")); //$NON-NLS-1$ 

			DEBUG_SAVE_MARKERS = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/save/markers")); //$NON-NLS-1$ 
			DEBUG_SAVE_SYNCINFO = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/save/syncinfo")); //$NON-NLS-1$ 
			DEBUG_SAVE_TREE = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/save/tree")); //$NON-NLS-1$ 
			DEBUG_SAVE_METAINFO = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/save/metainfo")); //$NON-NLS-1$ 
			DEBUG_SAVE_MASTERTABLE = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/save/mastertable")); //$NON-NLS-1$ 
			DEBUG_SAVE = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/save")); //$NON-NLS-1$ 

			DEBUG_AUTO_REFRESH = sTrue.equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/refresh")); //$NON-NLS-1$ 
		}
	}

	/**
	 * Lookup the message with the given ID in this catalog 
	 */
	public static String bind(String id) {
		return bind(id, (String[]) null);
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string.
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] {binding});
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] {binding1, binding2});
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string values.
	 */
	public static String bind(String id, String[] bindings) {
		if (id == null)
			return "No message available";//$NON-NLS-1$
		String message = null;
		try {
			message = bundle.getString(id);
		} catch (MissingResourceException e) {
			// If we got an exception looking for the message, fail gracefully by just returning
			// the id we were looking for.  In most cases this is semi-informative so is not too bad.
			return "Missing message: " + id + " in: " + bundleName;//$NON-NLS-1$ //$NON-NLS-2$
		}
		if (bindings == null)
			return message;
		return MessageFormat.format(message, bindings);
	}

	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}

	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		return monitor == null ? new NullProgressMonitor() : monitor;
	}

	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}

	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks, int style) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks, style);
	}

	/**
	 * Print a debug message to the console. 
	 * Pre-pend the message with the current date and the name of the current thread.
	 */
	public static void debug(String message) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(new Date(System.currentTimeMillis()));
		buffer.append(" - ["); //$NON-NLS-1$
		buffer.append(Thread.currentThread().getName());
		buffer.append("] "); //$NON-NLS-1$
		buffer.append(message);
		System.out.println(buffer.toString());
	}
}