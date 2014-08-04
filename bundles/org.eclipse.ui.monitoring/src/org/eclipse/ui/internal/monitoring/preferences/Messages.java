/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	public static String FilterInputDialog_filter_input_header;
	public static String FilterInputDialog_filter_input_message;
	public static String FilterInputDialog_filter_input_label;
	public static String ListFieldEditor_add_filter_button_label;
	public static String MonitoringPreferenceListener_preference_error_header;
	public static String MonitoringPreferenceListener_preference_error;
	public static String MonitoringPreferencePage_capture_threshold_error;
	public static String MonitoringPreferencePage_deadlock_label;
	public static String MonitoringPreferencePage_dump_all_threads_label;
	public static String MonitoringPreferencePage_enable_thread_label;
	public static String MonitoringPreferencePage_event_log_label;
	public static String MonitoringPreferencePage_filter_label;
	public static String MonitoringPreferencePage_first_stack_label;
	public static String MonitoringPreferencePage_invalid_number_error;
	public static String MonitoringPreferencePage_log_freeze_events_label;
	public static String MonitoringPreferencePage_log_threshold_error;
	public static String MonitoringPreferencePage_sample_interval_label;
	public static String MonitoringPreferencePage_stack_sample_label;

	private Messages() {
		// Do not instantiate.
	}

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
