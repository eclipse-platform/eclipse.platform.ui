/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.internal.runtime.*;

/**
 * EventStats collects and aggregates timing data about events such as
 * a builder running, an editor opening, etc.  This data is collected for the 
 * purpose of performance analysis, and is not intended to be used as
 * a generic event notification system.
 * 
 * @since 3.1
 */
public class EventStats {
	/**
	 * An event listener is notified after events occur
	 */
	public interface IEventListener {
		/**
		 * Notifies that an event occurred.  Notification might not occur
		 * in the same thread or near the time of the actual event.
		 * 
		 * @param event The event that occurred
		 */
		public void eventsOccurred(EventStats[] event);
	}

	/**
	 * All known event statistics.
	 */
	private final static Map statMap = new HashMap();

	/**
	 * Maximum allowed durations for each event.
	 * Maps String (event name) -> Long (threshold)
	 */
	private final static Map thresholdMap= new HashMap();

	/**
	 * An identifier that can be used to figure out who caused the event. This is
	 * typically the name of the client class or plugin responsible for the event.
	 */
	private String blame;

	/**
	 * An optional context, such as the input of an editor, or the target project
	 * of a build event.
	 */
	private String context;

	/**
	 * The symbolic name of the event that occurred.
	 */
	private String event;

	/**
	 * The total number of times this event has exceeded its maximum duration.
	 */
	private int failureCount = 0;

	/**
	 * The total number of times this event has occurred.
	 */
	private int runCount = 0;

	/**
	 * The total time in milliseconds taken by all occurrences of this event.
	 */
	private long runningTime = 0;

	/**
	 * Adds a listener that is notified when events occur.
	 */
	public static void addEventListener(IEventListener listener) {
		EventStatsProcessor.addEventListener(listener);
	}

	/**
	 * Discards all known event statistics.
	 */
	public static void clear() {
		statMap.clear();
	}

	/**
	 * Returns all event statistics.
	 */
	public static EventStats[] getAllStats() {
		return (EventStats[]) statMap.values().toArray(new EventStats[statMap.values().size()]);
	}

	/**
	 * Returns the stats object for the given ID.  A stats object is created and
	 * added to the global list of events if it did not already exist.

	 * @param eventName The name of the event to return
	 * @param blameName The blame for the event to return
	 * @param contextName The context for the event to return, or <code>null</code>
	 */
	public static EventStats getStats(String eventName, String blameName, String contextName) {
		Assert.isNotNull(eventName);
		Assert.isNotNull(blameName);
		EventStats stats = new EventStats(eventName, blameName, contextName);
		if (InternalPlatform.DEBUG_TRACE) {
			//use existing stats object if available
			EventStats oldStats = (EventStats) statMap.get(stats);
			if (oldStats == null)
				statMap.put(stats, stats);
			else
				stats = oldStats;
		}
		return stats;
	}

	/**
	 * Prints all statistics to the standard output.
	 */
	public static void printStats() {
		PrintWriter writer = new PrintWriter(System.out);
		EventStatsProcessor.printStats(writer);
		writer.flush();
		writer.close();
	}

	/**
	 * Writes all statistics using the provided writer
	 * 
	 * @param out The writer to print stats to.
	 */
	public static void printStats(PrintWriter out) {
		EventStatsProcessor.printStats(out);
	}

	/**
	 * Removes an event listener
	 */
	public static void removeEventListener(IEventListener listener) {
		EventStatsProcessor.removeEventListener(listener);
	}

	/**
	 * Removes statistics for a given event and blame
	 * 
	 * @param eventName The name of the event to remove
	 * @param blameName The blame for the event to remove
	 * @param contextName The context for the event to remove, or <code>null</code>
	 */
	public static void removeStats(String eventName, String blameName, String contextName) {
		statMap.remove(new EventStats(eventName, blameName, contextName));
	}

	/** 
	 * Creates a new EventStats object.
	 */
	public EventStats(String event, String blame, String context) {
		this.event = event;
		this.blame = blame;
		this.context = context;
	}

	/**
	 * Adds an occurence of this event to the cumulative counters.
	 * 
	 * @param elapsed The elapsed time of the new occurrence in milliseconds
	 */
	public void addRun(long elapsed) {
		runCount++;
		runningTime += elapsed;
		if (elapsed > getThreshold(event)) {
			failureCount++;
			if (InternalPlatform.DEBUG_TRACE_LOG) {
				String msg = NLS.bind(Messages.perf_failure, new Object[] {event, blame, context, new Long(elapsed)});
				IStatus failure = new Status(IStatus.WARNING, Platform.PI_RUNTIME, 1, msg, new RuntimeException());
				InternalPlatform.getDefault().log(failure);
			}
		}
		EventStatsProcessor.changed(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals()
	 */
	public boolean equals(Object obj) {
		//count and time are not considered part of equality
		if (!(obj instanceof EventStats))
			return false;
		EventStats that = (EventStats) obj;
		if (!this.event.equals(that.event))
			return false;
		if (!this.blame.equals(that.blame))
			return false;
		return this.context == null ? that.context == null : this.context.equals(that.context);
	}

	/**
	 * Returns an identifier that can be used to figure out who caused the event. This is
	 * typically the name of the client class or plugin responsible for the event.
	 * 
	 * @return The blame for this event
	 */
	public String getBlame() {
		return blame;
	}

	/**
	 * Returns the optional event context, such as the input of an editor, or the target project
	 * 
	 * of a build event.
	 * @return The context, or <code>null</code> if there is none
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Returns the symbolic name of the event that occurred.
	 * 
	 * @return The name of the event.
	 */
	public String getEvent() {
		return event;
	}
	
	/**
	 * Returns the number of times this event has exceeded its
	 * maximum duration.
	 * 
	 * @return The number of times this event has exceeded its
	 * maximum duration.
	 */
	public int getFailureCount() {
		return failureCount;
	}

	/**
	 * Returns the total number of times this event has occurred.
	 * 
	 * @return The number of occurrences of this event.
	 */
	public int getRunCount() {
		return runCount;
	}

	/**
	 * Returns the total execution time in milliseconds for all occurrences
	 * of this event.
	 * 
	 * @return The total running time in milliseconds.
	 */
	public long getRunningTime() {
		return runningTime;
	}

	/**
	 * Returns the performance threshold for this event.
	 */
	private long getThreshold(String eventName) {
		Long value = (Long) thresholdMap.get(eventName);
		if (value == null) {
			String option = InternalPlatform.getDefault().getOption(eventName);
			if (option != null) {
				try {
					value = new Long(option);
				} catch (NumberFormatException e) {
					//invalid option, just ignore
				}
			}
			if (value == null) 
				value = new Long(Long.MAX_VALUE);
			thresholdMap.put(eventName, value);
		}
		return value.longValue();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		//count and time are not considered part of equality
		int hash = event.hashCode() * 37 + blame.hashCode();
		if (context != null)
			hash = hash * 37 + context.hashCode();
		return hash;
	}

	/**
	 * Resets count and running time for this particular stats event.
	 */
	public void reset() {
		runningTime = 0;
		runCount = 0;
	}
}