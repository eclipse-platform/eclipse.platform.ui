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
package org.eclipse.core.internal.events;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;
import org.eclipse.core.internal.plugins.PluginClassLoader;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * A placeholder for statistics about the runtime behaviour
 * of a particular plugin.  PluginStats objects are used internally
 * by the CoreStats mechanism.
 */
public class EventStats {
	protected String id;
	protected IProject project;
	protected IPluginDescriptor plugin;
	protected long notificationRunningTime = 0;
	protected long buildRunningTime = 0;
	protected int notificationCount = 0;
	protected int buildCount = 0;
	protected Vector exceptions = new Vector();

	/**
	 * If this flag is true, print debug info to the console.  If false, it
	 * fails silently.
	 */
	public static boolean DEBUG = false;

	/**
	 * The start time of the current build or notification
	 */
	private static long currentStart;

	/**
	 * The stat object tracking the current event
	 */
	private static EventStats currentStats;

	/**
	 * Plugin statistics
	 */
	private static int snapshotCount = 0;
	private static long snapshotTime = 0;

	private static Map notificationStats = new HashMap(20);
	private static QualifiedName STATS_PROPERTY = new QualifiedName(ResourcesPlugin.PI_RESOURCES, "buildstats"); //$NON-NLS-1$

	/** private constructor to prevent instantiation */
	private EventStats(String id, IProject project) {
		this.id = id;
		this.project = project;
	}

	public static void buildException(Exception e) {
		if (currentStats == null) {
			if (DEBUG)
				System.out.println(Policy.bind("utils.buildException")); //$NON-NLS-1$
		} else
			currentStats.addException(e);
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

		EventStats[] allStats = getAllStats();
		for (int i = 0; i < allStats.length; i++) {
			EventStats stats = allStats[i];
			totalBuildTime += stats.getBuildRunningTime();
			totalNotifyTime += stats.getNotifyRunningTime();
			totalExceptions += stats.getExceptionCount();
			totalBuilds += stats.getBuildCount();
			totalNotifications += stats.getNotifyCount();
		}
		//dump stats
		out.println("---------------------------------------------------------------"); //$NON-NLS-1$
		out.println(Policy.bind("utils.snapshots", Integer.toString(snapshotCount), Long.toString(snapshotTime))); //$NON-NLS-1$
		for (int i = 0; i < allStats.length; i++) {
			EventStats stats = allStats[i];
			out.println(Policy.bind("utils.stats", stats.getName())); //$NON-NLS-1$

			int notifyCount = stats.getNotifyCount();
			if (notifyCount > 0)
				out.println(Policy.bind("utils.notifications", Integer.toString(notifyCount), Integer.toString((int) (notifyCount * 100.0 / totalNotifications)))); //$NON-NLS-1$

			long notifyTime = stats.getNotifyRunningTime();
			if (notifyTime > 0)
				out.println(Policy.bind("utils.notifyTime", Long.toString(notifyTime), Integer.toString((int) (notifyTime * 100.0 / totalNotifyTime)))); //$NON-NLS-1$

			int buildCount = stats.getBuildCount();
			if (buildCount > 0)
				out.println(Policy.bind("utils.builds", Integer.toString(buildCount), Integer.toString((int) (buildCount * 100.0 / totalBuilds)))); //$NON-NLS-1$

			long buildTime = stats.getBuildRunningTime();
			if (buildTime > 0)
				out.println(Policy.bind("utils.buildTime", Long.toString(buildTime), Integer.toString((int) (buildTime * 100.0 / totalBuildTime)))); //$NON-NLS-1$

			int exceptions = stats.getExceptionCount();
			if (exceptions > 0)
				out.println(Policy.bind("utils.exceptions", Integer.toString(exceptions), Integer.toString((int) (exceptions * 100.0 / totalExceptions)))); //$NON-NLS-1$

			out.println(""); //$NON-NLS-1$
		}
	}

	public static void endBuild() {
		long end = System.currentTimeMillis();
		if (currentStats == null || currentStart == -1) {
			if (DEBUG)
				System.err.println(Policy.bind("utils.endBuild")); //$NON-NLS-1$
			return;
		}
		currentStats.addBuild(end - currentStart);
		currentStats = null;
		currentStart = -1;
	}

	public static void endNotify() {
		long end = System.currentTimeMillis();
		if (currentStats == null || currentStart == -1) {
			if (DEBUG)
				System.err.println(Policy.bind("utils.endNotify")); //$NON-NLS-1$
			return;
		}
		currentStats.addNotify(end - currentStart);
		currentStart = -1;
	}

	public static void endSnapshot() {
		snapshotTime += System.currentTimeMillis() - currentStart;
		snapshotCount++;
		currentStart = -1;
	}

	public static int getSnapCount() {
		return snapshotCount;
	}

	private static IPluginDescriptor getPluginFor(Object target) {
		ClassLoader loader = target.getClass().getClassLoader();
		if (loader instanceof PluginClassLoader)
			return ((PluginClassLoader) loader).getPluginDescriptor();
		return null;
	}

	/**
	 * Returns the stats object for the given ID.
	 */
	private static EventStats getStats(String id, IProject project, Object origin) {
		EventStats result = null;
		if (project == null) {
			result = (EventStats) notificationStats.get(id);
			if (result == null) {
				result = new EventStats(id, project);
				result.setPlugin(getPluginFor(origin));
				notificationStats.put(id, result);
			}
		} else {
			try {
				Map stats = (Map) project.getSessionProperty(STATS_PROPERTY);
				if (stats == null) {
					stats = new HashMap(5);
					project.setSessionProperty(STATS_PROPERTY, stats);
				}
				result = (EventStats) stats.get(id);
				if (result == null) {
					result = new EventStats(id, project);
					result.setPlugin(getPluginFor(origin));
					stats.put(id, result);
				}
			} catch (CoreException e) {
				//ignore if projects are not accessible
			}
		}
		return result;
	}

	/**
	 * Returns stats objects for all plugins that are registered
	 * for statistics (either notification listeners or builders).
	 */
	public static EventStats[] getAllStats() {
		ArrayList result = new ArrayList(notificationStats.values());
		try {
			IResource[] projects = ResourcesPlugin.getWorkspace().getRoot().members();
			for (int i = 0; i < projects.length; i++) {
				IProject project = (IProject) projects[i];
				try {
					Map stats = (Map) project.getSessionProperty(STATS_PROPERTY);
					if (stats != null)
						result.addAll(stats.values());
				} catch (CoreException e) {
					//ignore for non-existent projects
				}
			}
		} catch (CoreException e) {
			//ignore if projects are not accessible
		}
		return (EventStats[]) result.toArray(new EventStats[result.size()]);
	}

	/**
	 * Resets all known statistics.
	 */
	public static void resetStats() {
		for (Iterator iter = notificationStats.values().iterator(); iter.hasNext();)
			((EventStats) iter.next()).reset();
		try {
			IResource[] projects = ResourcesPlugin.getWorkspace().getRoot().members();
			for (int i = 0; i < projects.length; i++) {
				IProject project = (IProject) projects[i];
				try {
					Map stats = (Map) project.getSessionProperty(STATS_PROPERTY);
					if (stats != null)
						for (Iterator iter = stats.values().iterator(); iter.hasNext();)
							((EventStats) iter.next()).reset();
				} catch (CoreException e) {
					//ignore for non-existent projects
				}
			}
		} catch (CoreException e) {
			//ignore if projects are not accessible
		}
	}

	/**
	 * Notifies the stats tool that a resource change listener has been removed.
	 */
	public static void listenerRemoved(IResourceChangeListener listener) {
		if (listener != null)
			notificationStats.remove(listener.toString());
	}

	/**
	 * Notifies the stats tool that a resource change listener has been added.
	 */
	public static void listenerAdded(IResourceChangeListener listener) {
		if (listener != null)
			getStats(listener.toString(), null, listener);
	}

	public static void notifyException(Exception e) {
		if (currentStats == null) {
			if (DEBUG)
				System.out.println(Policy.bind("utils.buildException")); //$NON-NLS-1$
			return;
		}
		currentStats.addException(e);
	}

	public static void startBuild(IncrementalProjectBuilder builder) {
		String key = ((InternalBuilder) builder).getLabel();
		currentStats = getStats(key, builder.getProject(), builder);
		currentStart = System.currentTimeMillis();
	}

	public static void startNotify(IResourceChangeListener listener) {
		currentStats = getStats(listener.toString(), null, listener);
		currentStart = System.currentTimeMillis();
	}

	public static void startSnapshot() {
		currentStart = System.currentTimeMillis();
	}

	void addBuild(long elapsed) {
		buildCount++;
		buildRunningTime += elapsed;
	}

	void addException(Exception e) {
		exceptions.addElement(e);
	}

	void addNotify(long elapsed) {
		notificationCount++;
		notificationRunningTime += elapsed;
	}

	public int getBuildCount() {
		return buildCount;
	}

	public long getBuildRunningTime() {
		return buildRunningTime;
	}

	public Enumeration getCoreExceptions() {
		Vector runtime = new Vector();
		for (Enumeration e = exceptions.elements(); e.hasMoreElements();) {
			Exception next = (Exception) e.nextElement();
			if (next instanceof CoreException) {
				runtime.addElement(next);
			}
		}
		return runtime.elements();
	}

	public int getExceptionCount() {
		return exceptions.size();
	}

	public String getName() {
		return id;
	}

	public int getNotifyCount() {
		return notificationCount;
	}

	public long getNotifyRunningTime() {
		return notificationRunningTime;
	}

	public IPluginDescriptor getPlugin() {
		return plugin;
	}

	public IProject getProject() {
		return project;
	}

	public Enumeration getRuntimeExceptions() {
		Vector runtime = new Vector();
		for (Enumeration e = exceptions.elements(); e.hasMoreElements();) {
			Exception next = (Exception) e.nextElement();
			if (next instanceof RuntimeException) {
				runtime.addElement(next);
			}
		}
		return runtime.elements();
	}

	public long getTotalRunningTime() {
		return notificationRunningTime + buildRunningTime;
	}

	public void reset() {
		notificationRunningTime = 0;
		buildRunningTime = 0;
		notificationCount = 0;
		buildCount = 0;
		exceptions = new Vector();
	}

	public void setPlugin(IPluginDescriptor value) {
		plugin = value;
	}
}