package org.eclipse.core.internal.utils;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import java.io.*;
import java.util.*;
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
			System.out.println("CoreStats got build exception without start build");
		}
		return;
	}
	PluginStats stats = getStats(currentPlugin);
	if (stats == null) {
		if (DEBUG) {
			System.out.println("PluginStats is missing for plugin: " + currentPlugin);
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
	out.println("---------------------------------------------------------------");
	out.println(Integer.toString(snapshotCount) + " snapshots took: " + snapshotTime + "ms");
	for (Enumeration e = pluginTable.elements(); e.hasMoreElements();) {
		PluginStats stats = (PluginStats)e.nextElement();
		out.println("Stats for: " + stats.getName());

		int notifyCount = stats.getNotifyCount();
		if (notifyCount > 0) {
			out.println("  Notifications: " + notifyCount + " (" 
				+ (int)((float)notifyCount * 100.0 / (float)totalNotifications) + "% of total)");
		}

		long notifyTime = stats.getNotifyRunningTime();
		if (notifyTime > 0) {
			out.println("  Notification time (ms): " + notifyTime + " (" 
				+ (int)((float)notifyTime * 100.0 / (float)totalNotifyTime) + "% of total)");
		}

		int buildCount = stats.getBuildCount();
		if (buildCount > 0) {
			out.println("  Builds: " + buildCount + " (" 
				+ (int)((float)buildCount * 100.0 / (float)totalBuilds) + "% of total)");
		}

		long buildTime = stats.getBuildRunningTime();
		if (buildTime > 0) {
			out.println("  Build time (ms): " + buildTime + " (" 
				+ (int)((float)buildTime * 100.0 / (float)totalBuildTime) + "% of total)");
		}

		int exceptions = stats.getExceptionCount();
		if (exceptions > 0) {		
			out.println("  Exceptions: " + exceptions + " (" 
				+ (int)((float)exceptions * 100.0 / (float)totalExceptions) + "% of total)");
		}
		out.println("");
	}
}
public static void endBuild() {
	long end = System.currentTimeMillis();
	if (currentPlugin == null || currentStart == -1) {
		if (DEBUG) {
			System.err.println("CoreStats got endBuild without startBuild");
		}
		return;
	}
	PluginStats stats = getStats(currentPlugin);
	if (stats == null) {
		if (DEBUG) {
			System.out.println("PluginStats is missing for plugin: " + currentPlugin);
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
			System.err.println("CoreStats got endNotify without startNotify");
		}
		return;
	}
	PluginStats stats = getStats(currentPlugin);
	if (stats == null) {
		if (DEBUG) {
			System.out.println("PluginStats is missing for plugin: " + currentPlugin);
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
private static PluginStats getStats(String pluginID) {
	PluginStats stats = (PluginStats) pluginTable.get(currentPlugin);
	if (stats == null) {
		stats = new PluginStats(pluginID);
		pluginTable.put(currentPlugin, stats);
	}
	return stats;
}
public static void notifyException(Exception e) {
	if (currentPlugin == null) {
		if (DEBUG) {
			System.out.println("CoreStats got build exception without start build");
		}
		return;
	}
	PluginStats stats = getStats(currentPlugin);
	if (stats == null) {
		if (DEBUG) {
			System.out.println("PluginStats is missing for plugin: " + currentPlugin);
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
