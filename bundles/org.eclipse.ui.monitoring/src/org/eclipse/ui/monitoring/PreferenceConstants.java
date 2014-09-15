/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Marcus Eng (Google) - initial API and implementation
 *	   Sergey Prigogin (Google)
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
	 * If true, enables the monitoring thread which logs when the UI thread becomes unresponsive.
	 */
	public static final String MONITORING_ENABLED = "monitoring_enabled"; //$NON-NLS-1$
	/** Sample interval to capture the traces of an unresponsive event. */
	public static final String DEADLOCK_REPORTING_THRESHOLD_MILLIS = "deadlock_reporting_threshold"; //$NON-NLS-1$
	/** Maximum number of traces to write out to the log. */
	public static final String MAX_STACK_SAMPLES = "max_stack_samples"; //$NON-NLS-1$
	/** Log events that took longer than the specified duration in milliseconds. */
	public static final String LONG_EVENT_THRESHOLD_MILLIS = "max_event_log_time"; //$NON-NLS-1$
	/**
	 * Start capturing traces if an event takes longer than the specified duration in
	 * milliseconds.
	 */
	public static final String INITIAL_SAMPLE_DELAY_MILLIS = "initial_sample_delay"; //$NON-NLS-1$
	/** Sample collection interval to capture the stack traces of an unresponsive event. */
	public static final String SAMPLE_INTERVAL_MILLIS = "sample_interval"; //$NON-NLS-1$
	/**
	 * If true, includes call stacks of all threads into the logged message. Otherwise, only
	 * the stack of the main thread is included. Disabled by default due to additional performance
	 * overhead.
	 */
	public static final String DUMP_ALL_THREADS = "dump_all_threads"; //$NON-NLS-1$
	/**
	 * If true, log freeze events to the Eclipse error log.
	 */
	public static final String LOG_TO_ERROR_LOG = "log_to_error_log"; //$NON-NLS-1$
	/**
	 * Comma separated fully qualified method names of stack frames to filter out.
	 * Long events containing these methods at the top of the stack will not be logged.
	 */
	public static final String FILTER_TRACES = "filter_traces"; //$NON-NLS-1$

	private PreferenceConstants() {}
}
