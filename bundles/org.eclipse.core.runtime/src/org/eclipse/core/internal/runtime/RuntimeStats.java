package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;
/**
 * This class is used to gather stats on various aspects of
 * core and plugin behaviour.  The Policy class determines whether
 * stats are gathered or not, so that instrumentation can be turned
 * off during production builds.
 */
public class RuntimeStats {
	/**
	 * If this flag is true, CoreStats prints debug info
	 * to the console.  If false, it fails silently.
	 */
	public static boolean DEBUG = false;
	/** Table mapping plugin identifiers (Strings) to PluginStats objects.
	 */
	private static Hashtable pluginTable = new Hashtable(10);

	/** The start time of the current build or notification
	 */
	private static long currentStart;

	/** Plugin statistics
	 */
	private static int snapshotCount = 0;
	private static long snapshotTime = 0;
	
/**
 * CoreStats cannot be instantiated.
 */
private RuntimeStats() {
	super();
}
public static void dumpStats() {
	dumpStats(System.out);
}
public static void dumpStats(PrintStream out) {
	dumpStats(new PrintWriter(out));

}
public static void dumpStats(PrintWriter out) {
	/* gather totals */
	int totalExceptions = 0;

	for (Enumeration e = pluginTable.elements(); e.hasMoreElements();) {
		PluginStats stats = (PluginStats) e.nextElement();
		totalExceptions += stats.getExceptionCount();
	}
	//dump stats
	out.println(Integer.toString(snapshotCount) + " snapshots took: " + snapshotTime + "ms");
	for (Enumeration e = pluginTable.elements(); e.hasMoreElements();) {
		PluginStats stats = (PluginStats) e.nextElement();
		out.println("Stats for: " + stats.getName());

		int exceptions = stats.getExceptionCount();
		if (exceptions > 0) {
			out.println("  Exceptions: " + exceptions + " (" + (int) ((float) exceptions * 100.0 / (float) totalExceptions) + "% of total)");
		}
		out.println("");
	}
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
 * Returns the plug-in stats object for the given plug-in id.
 * If one does not currently exist, one is created, remembered and returned.
 */
private static PluginStats getStats(String pluginID) {
	PluginStats stats = (PluginStats) pluginTable.get(pluginID);
	if (stats == null) {
		stats = new PluginStats(pluginID);
		pluginTable.put(pluginID, stats);
	}
	return stats;
}
public static void startSnapshot() {
	currentStart = System.currentTimeMillis();
}
}
