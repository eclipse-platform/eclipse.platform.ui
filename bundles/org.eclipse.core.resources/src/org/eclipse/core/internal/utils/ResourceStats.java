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

import java.io.*;
import java.util.*;
import org.eclipse.core.resources.IResourceChangeListener;
/**
 * This class is used to gather stats on various aspects of
 * core and plugin behaviour.  The Policy class determines whether
 * stats are gathered or not, so that instrumentation can be turned
 * off during production builds.
 */
public class ResourceStats {
	/**
	 * If this flag is true, CoreStats prints debug info
	 * to the console.  If false, it fails silently.
	 */
	public static boolean DEBUG = false;
	/**
	 * Table mapping plugin identifiers (Strings) to PluginStats objects.
	 */
	private static Hashtable pluginTable = new Hashtable(10);

	/**
	 * The start time of the current build or notification
	 */
	private static long currentStart;

	/**
	 * ID of plugin current building or being notified
	 */
	private static String currentPlugin;

	/**
	 * Plugin statistics
	 */
	private static int snapshotCount = 0;
	private static long snapshotTime = 0;
/**
 * CoreStats cannot be instantiated.
 */
private ResourceStats() {
	super();
}
public static void buildException(Exception e) {
	if (currentPlugin == null) {
		if (DEBUG) {
			System.out.println(Policy.bind("utils.buildException")); //$NON-NLS-1$
		}
		return;
	}
	PluginStats stats = getStats(currentPlugin);
	if (stats == null) {
		if (DEBUG) {
			System.out.println(Policy.bind("utils.missing", currentPlugin)); //$NON-NLS-1$
		}
		return;
	}
	stats.addException(e);
}
public static void dumpStats() {
	dumpStats(System.out);
}
public static void dumpStats(PrintStream out) {
	PrintWriter writer = new PrintWriter(out);
	dumpStats(writer);
	writer.flush();
	writer.close();

}
public static void dumpStats(PrintWriter out) {
	/* gather totals */
	long totalBuildTime = 0;
	long totalNotifyTime = 0;
	int totalExceptions = 0;
	int totalBuilds = 0;
	int totalNotifications = 0;

	for (Enumeration e = pluginTable.elements(); e.hasMoreElements();) {
		PluginStats stats = (PluginStats)e.nextElement();
		totalBuildTime += stats.getBuildRunningTime();
		totalNotifyTime += stats.getNotifyRunningTime();
		totalExceptions += stats.getExceptionCount();
		totalBuilds += stats.getBuildCount();
		totalNotifications += stats.getNotifyCount();
	}
	//dump stats
	out.println("---------------------------------------------------------------"); //$NON-NLS-1$
	out.println(Policy.bind("utils.snapshots", Integer.toString(snapshotCount), Long.toString(snapshotTime))); //$NON-NLS-1$
	for (Enumeration e = pluginTable.elements(); e.hasMoreElements();) {
		PluginStats stats = (PluginStats)e.nextElement();
		out.println(Policy.bind("utils.stats", stats.getName())); //$NON-NLS-1$

		int notifyCount = stats.getNotifyCount();
		if (notifyCount > 0) {
			out.println(Policy.bind("utils.notifications", Integer.toString(notifyCount), Integer.toString((int)((float)notifyCount * 100.0 / (float)totalNotifications)))); //$NON-NLS-1$
		}

		long notifyTime = stats.getNotifyRunningTime();
		if (notifyTime > 0) {
			out.println(Policy.bind("utils.notifyTime", Long.toString(notifyTime), Integer.toString((int)((float)notifyTime * 100.0 / (float)totalNotifyTime)))); //$NON-NLS-1$
		}

		int buildCount = stats.getBuildCount();
		if (buildCount > 0) {
			out.println(Policy.bind("utils.builds", Integer.toString(buildCount), Integer.toString((int)((float)buildCount * 100.0 / (float)totalBuilds)))); //$NON-NLS-1$
		}

		long buildTime = stats.getBuildRunningTime();
		if (buildTime > 0) {
			out.println(Policy.bind("utils.buildTime", Long.toString(buildTime), Integer.toString((int)((float)buildTime * 100.0 / (float)totalBuildTime)))); //$NON-NLS-1$
		}

		int exceptions = stats.getExceptionCount();
		if (exceptions > 0) {
			out.println(Policy.bind("utils.exceptions", Integer.toString(exceptions), Integer.toString((int)((float)exceptions * 100.0 / (float)totalExceptions)))); //$NON-NLS-1$
		}
		out.println(""); //$NON-NLS-1$
	}
}
public static void endBuild() {
	long end = System.currentTimeMillis();
	if (currentPlugin == null || currentStart == -1) {
		if (DEBUG) {
			System.err.println(Policy.bind("utils.endBuild")); //$NON-NLS-1$
		}
		return;
	}
	PluginStats stats = getStats(currentPlugin);
	if (stats == null) {
		if (DEBUG) {
			System.out.println(Policy.bind("utils.missing", currentPlugin.toString())); //$NON-NLS-1$
		}
		return;
	}
	stats.addBuild(end - currentStart);
	currentStart = -1;
}
public static void endNotify() {
	long end = System.currentTimeMillis();
	if (currentPlugin == null || currentStart == -1) {
		if (DEBUG) {
			System.err.println(Policy.bind("utils.endNotify")); //$NON-NLS-1$
		}
		return;
	}
	PluginStats stats = getStats(currentPlugin);
	if (stats == null) {
		if (DEBUG) {
			System.out.println(Policy.bind("utils.missing", currentPlugin.toString())); //$NON-NLS-1$
		}
		return;
	}
	stats.addNotify(end - currentStart);
	currentStart = -1;
}
public static void endSnapshot() {
	snapshotTime += System.currentTimeMillis() - currentStart;
	snapshotCount++;
	currentStart = -1;
}
public static int getSnapcount() {
	return snapshotCount;
}
/**
 * Returns the stats object for the given plugin ID.
 */
public static PluginStats getStats(String pluginID) {
	PluginStats stats = (PluginStats) pluginTable.get(pluginID);
	if (stats == null) {
		stats = new PluginStats(pluginID);
		pluginTable.put(pluginID, stats);
	}
	return stats;
}
/**
 * Returns stats objects for all plugins that are registered
 * for statistics (either notification listeners or builders).
 */
public static PluginStats[] getAllStats() {
	Collection values = pluginTable.values();
	return (PluginStats[])values.toArray(new PluginStats[values.size()]);
}
/**
 * Notifies the stats tool that a resource change listener has been removed.
 */
public static void listenerRemoved(IResourceChangeListener listener) {
	if (listener != null && pluginTable != null)
		pluginTable.remove(listener.toString());
}
/**
 * Notifies the stats tool that a resource change listener has been added.
 */
public static void listenerAdded(IResourceChangeListener listener) {
	if (listener != null && pluginTable != null)
		getStats(listener.toString());
}
public static void notifyException(Exception e) {
	if (currentPlugin == null) {
		if (DEBUG) {
			System.out.println(Policy.bind("utils.buildException")); //$NON-NLS-1$
		}
		return;
	}
	PluginStats stats = getStats(currentPlugin);
	if (stats == null) {
		if (DEBUG) {
			System.out.println(Policy.bind("utils.missing", currentPlugin.toString())); //$NON-NLS-1$
		}
		return;
	}
	stats.addException(e);
}
public static void startBuild(String builderID) {
	currentPlugin = builderID;
	currentStart = System.currentTimeMillis();
}
public static void startNotify(String pluginID) {
	currentPlugin = pluginID;
	currentStart = System.currentTimeMillis();
}
public static void startSnapshot() {
	currentStart = System.currentTimeMillis();
}
}
