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
	 * If true, enables the monitoring thread which logs events which take long time to process.
	 */
	public static final String MONITORING_ENABLED = "monitoring_enabled"; //$NON-NLS-1$
	/**
	 * Events that took longer than the specified duration in milliseconds are logged as warnings.
	 */
	public static final String LONG_EVENT_WARNING_THRESHOLD_MILLIS = "long_event_warning_threshold"; //$NON-NLS-1$
	/**
	 * Events that took longer than the specified duration in milliseconds are logged as errors.
	 */
	public static final String LONG_EVENT_ERROR_THRESHOLD_MILLIS = "long_event_error_threshold"; //$NON-NLS-1$
	/**
	 * Events that took longer than the specified duration are reported as deadlocks without waiting
	 * for the event to finish.
	 */
	public static final String DEADLOCK_REPORTING_THRESHOLD_MILLIS = "deadlock_reporting_threshold"; //$NON-NLS-1$
	/**
	 * Maximum number of stack trace samples to write out to the log.
	 */
	public static final String MAX_STACK_SAMPLES = "max_stack_samples"; //$NON-NLS-1$
	/**
	 * If true, log freeze events to the Eclipse error log.
	 */
	public static final String LOG_TO_ERROR_LOG = "log_to_error_log"; //$NON-NLS-1$
	/**
	 * Comma separated fully qualified method names of stack frames. The names may contain
	 * '*' and '?' wildcard characters. A UI freeze is not logged if any of the stack traces
	 * of the UI thread contains at least one method matching the filter.
	 */
	public static final String UI_THREAD_FILTER = "ui_thread_filter"; //$NON-NLS-1$
	/**
	 * Comma separated fully qualified method names of stack frames. The names may contain
	 * '*' and '?' wildcard characters. A non-UI thread is not included in the logged UI freeze
	 * message if all stack frames of the thread match the filter.
	 */
	public static final String NONINTERESTING_THREAD_FILTER = "noninteresting_thread_filter"; //$NON-NLS-1$

	private PreferenceConstants() {}
}
