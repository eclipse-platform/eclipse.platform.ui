/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.monitoring;

/**
 * Definitions of the preference constants.
 *
 * @since 1.0
 */
public class PreferenceConstants {
	public static final String PLUGIN_ID = "org.eclipse.ui.monitoring"; //$NON-NLS-1$
	/**
	 * If true, enables the monitoring thread which logs when the user sees a Blocked Jobs dialog or
	 * when the UI thread becomes unresponsive.
	 */
	public static final String MONITORING_ENABLED = "monitoring_enabled"; //$NON-NLS-1$
	/** Sample interval to capture the traces of an unresponsive event. */
	public static final String FORCE_DEADLOCK_LOG_TIME_MILLIS = "force_deadlock_log"; //$NON-NLS-1$
	/** Maximum number of traces to write out to the log. */
	public static final String MAX_LOG_TRACE_COUNT = "max_log_trace_count"; //$NON-NLS-1$
	/** Log events that took longer than the specified duration in milliseconds. */
	public static final String MAX_EVENT_LOG_TIME_MILLIS = "max_event_log_time"; //$NON-NLS-1$
	/**
	 * Start capturing traces if an event takes longer than the specified duration in
	 * milliseconds.
	 */
	public static final String MAX_EVENT_SAMPLE_TIME_MILLIS = "max_event_sample_time"; //$NON-NLS-1$
	/** Sample collection interval to capture the traces of an unresponsive event. */
	public static final String SAMPLE_INTERVAL_TIME_MILLIS = "sample_interval"; //$NON-NLS-1$
	/**
	 * If true, includes call stacks of all threads into the logged message. Otherwise, only the stack
	 * of the main thread is included. Disabled by default due to additional performance overhead.
	 */
	public static final String DUMP_ALL_THREADS = "dump_all_threads"; //$NON-NLS-1$
	/**
	 * If true, log freeze events to the Eclipse error log.
	 */
	public static final String LOG_TO_ERROR_LOG = "log_to_error_log"; //$NON-NLS-1$
	/** Stack traces to filter out. Any event with a filter matching any sample will be ignored. */
	public static final String FILTER_TRACES = "filter_traces"; //$NON-NLS-1$

	private PreferenceConstants() {}
}
