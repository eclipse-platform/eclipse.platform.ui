/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.PrintWriter;
import java.util.ArrayList;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.EventStats.IEventListener;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Processes, records, and performs notification of performance events
 * that occur in the system.
 */
public class EventStatsProcessor extends Job {
	private static final EventStatsProcessor instance = new EventStatsProcessor();

	private static final long SCHEDULE_DELAY = 2000;

	/**
	 * Events that have occurred but have not yet been broadcast.
	 */
	private final ArrayList changes = new ArrayList();

	/**
	 * Event listeners.
	 */
	private final ListenerList listeners = new ListenerList();

	/*
	 * @see EventStats#addEventListener
	 */
	public static void addEventListener(IEventListener listener) {
		instance.listeners.add(listener);
	}

	/**
	 * Records the fact that an event occurred.
	 * 
	 * @param stats The event that occurred
	 */
	public static void changed(EventStats stats) {
		synchronized (instance.changes) {
			instance.changes.add(stats);
		}
		instance.schedule(SCHEDULE_DELAY);
	}

	/*
	 * @see EventStats#printStats(PrintWriter)
	 */
	public static void printStats(PrintWriter out) {
		/* gather totals */
		long totalTime = 0;
		int totalCount = 0;
		EventStats[] allStats = EventStats.getAllStats();
		for (int i = 0; i < allStats.length; i++) {
			EventStats stats = allStats[i];
			totalTime += stats.getRunningTime();
			totalCount += stats.getRunCount();
		}
		//dump stats
		out.println("---------------------------------------------------------------"); //$NON-NLS-1$
		for (int i = 0; i < allStats.length; i++) {
			EventStats stats = allStats[i];
			out.print("Event: "); //$NON-NLS-1$
			out.print(stats.getEvent());
			out.print(" Blame: "); //$NON-NLS-1$
			out.print(stats.getBlame());
			if (stats.getContext() != null) {
				out.print(" Context: "); //$NON-NLS-1$
				out.print(stats.getContext());
			}
			out.println();

			int runCount = stats.getRunCount();
			if (runCount > 0) {
				out.print("Run count: "); //$NON-NLS-1$
				out.print(Integer.toString(runCount));
				out.print(" ("); //$NON-NLS-1$
				out.print(Integer.toString((int) (runCount * 100.0 / totalCount)));
				out.println(" % of total)"); //$NON-NLS-1$
			}

			long runTime = stats.getRunningTime();
			if (runTime > 0) {
				out.print("Duration (ms): "); //$NON-NLS-1$
				out.print(Long.toString(runTime));
				out.print(" ("); //$NON-NLS-1$
				out.print(Integer.toString((int) (runTime * 100.0 / totalTime)));
				out.println(" % of total)"); //$NON-NLS-1$
			}
			out.println(""); //$NON-NLS-1$
		}
	}

	/*
	 * @see EventStats#removeEventListener
	 */
	public static void removeEventListener(IEventListener listener) {
		instance.listeners.remove(listener);
	}

	private EventStatsProcessor() {
		super("Event Stats"); //$NON-NLS-1$
		setSystem(true);
		setPriority(DECORATE);
	}

	/*
	 * @see Job#run(IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		EventStats[] events;
		synchronized (changes) {
			events = (EventStats[]) changes.toArray(new EventStats[changes.size()]);
			changes.clear();
		}
		Object[] toNotify = listeners.getListeners();
		for (int i = 0; i < toNotify.length; i++) {
			((EventStats.IEventListener) toNotify[i]).eventsOccurred(events);
		}
		schedule(SCHEDULE_DELAY);
		return Status.OK_STATUS;
	}

	/*
	 * @see Job#shouldRun()
	 */
	public boolean shouldRun() {
		return !changes.isEmpty();
	}
}