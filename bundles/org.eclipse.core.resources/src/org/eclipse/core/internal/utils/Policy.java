/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.utils;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import java.text.MessageFormat;
import java.util.*;

public class Policy {
	private static String bundleName = "org.eclipse.core.internal.utils.messages";//$NON-NLS-1$
	private static ResourceBundle bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
	
	private static final int autoBuildOpWork = 70;
	private static final int autoBuildBuildWork = 30;
	private static final int manualBuildOpWork = 99;
	private static final int manualBuildBuildWork = 1;

	public static final boolean buildOnCancel = false;
	public static int opWork;
	public static int buildWork;
	public static int totalWork;

	// default workspace description values
	public static final boolean defaultAutoBuild = true;
	public static final boolean defaultSnapshots = true;
	public static final int defaultOperationsPerSnapshot = 100;
	public static final long defaultSnapshotInterval = 5 * 60 * 1000l;//5 minutes
	public static final long defaultDeltaExpiration = 30 * 24 * 3600 * 1000l; // 30 days
	public static final long defaultFileStateLongevity = 7 * 24 * 3600 * 1000l; // 7 days
	public static final long defaultMaxFileStateSize = 1024 * 1024l; // 1 Mb
	public static final int defaultMaxFileStates = 50;

	//debug constants
	public static boolean DEBUG_BUILD_FAILURE = false;
	public static boolean DEBUG_NEEDS_BUILD = false;
	public static boolean DEBUG_BUILD_INVOKING = false;
	public static boolean DEBUG_BUILD_DELTA = false;
	public static boolean DEBUG_NATURES = false;

	static {
		setupAutoBuildProgress(defaultAutoBuild);
		
		//init debug options
		if (ResourcesPlugin.getPlugin().isDebugging()) {
			DEBUG_BUILD_FAILURE = "true".equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/build/failure")); //$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_NEEDS_BUILD = "true".equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/build/needbuild")); //$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_BUILD_INVOKING = "true".equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/build/invoking")); //$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_BUILD_DELTA = "true".equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/build/delta")); //$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_NATURES = "true".equalsIgnoreCase(Platform.getDebugOption(ResourcesPlugin.PI_RESOURCES + "/natures")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
/**
 * Lookup the message with the given ID in this catalog 
 */
public static String bind(String id) {
	return bind(id, (String[])null);
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
	if (monitor == null)
		return new NullProgressMonitor();
	return monitor;
}
public static void setupAutoBuildProgress(boolean on) {
	opWork = on ? autoBuildOpWork : manualBuildOpWork;
	buildWork = on ? autoBuildBuildWork : manualBuildBuildWork;
	totalWork = opWork + buildWork;
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
}
