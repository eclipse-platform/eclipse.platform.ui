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

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.EventStats;

/**
 * An ResourceStats collects and aggregates timing data about an event such as
 * a builder running, an editor opening, etc.
 */
public class ResourceStats {
	/**
	 * The start time of the current build or notification
	 */
	private static long currentStart;

	/**
	 * The event that is currently occurring, maybe <code>null</code>
	 */
	private static EventStats currentStats;

	public static void endBuild() {
		long end = System.currentTimeMillis();
		if (currentStart > 0 && currentStats != null)
			currentStats.addRun(end - currentStart);
		currentStats = null;
		currentStart = -1;
	}

	public static void endNotify() {
		long end = System.currentTimeMillis();
		if (currentStart > 0 && currentStats != null)
			currentStats.addRun(end - currentStart);
		currentStats = null;
		currentStart = -1;
	}

	public static void endSnapshot() {
		long end = System.currentTimeMillis();
		if (currentStart > 0 && currentStats != null)
			currentStats.addRun(end - currentStart);
		currentStats = null;
		currentStart = -1;
	}

	/**
	 * Notifies the stats tool that a resource change listener has been added.
	 */
	public static void listenerAdded(IResourceChangeListener listener) {
		if (listener != null)
			EventStats.getStats(Policy.OPTION_TRACE_LISTENERS, listener.getClass().getName(), null);
	}

	/**
	 * Notifies the stats tool that a resource change listener has been removed.
	 */
	public static void listenerRemoved(IResourceChangeListener listener) {
		if (listener != null)
			EventStats.removeStats(Policy.OPTION_TRACE_LISTENERS, listener.getClass().getName(), null);
	}

	public static void startBuild(IncrementalProjectBuilder builder) {
		String key = ((InternalBuilder) builder).getLabel();
		currentStats = EventStats.getStats(Policy.OPTION_TRACE_BUILDERS, key, builder.getProject().getName());
		currentStart = System.currentTimeMillis();
	}

	public static void startNotify(IResourceChangeListener listener) {
		currentStats = EventStats.getStats(Policy.OPTION_TRACE_LISTENERS, listener.getClass().getName(), null);
		currentStart = System.currentTimeMillis();
	}

	public static void startSnapshot() {
		currentStats = EventStats.getStats(Policy.OPTION_TRACE_SNAPSHOT, ResourcesPlugin.PI_RESOURCES, null);
		currentStart = System.currentTimeMillis();
	}
}