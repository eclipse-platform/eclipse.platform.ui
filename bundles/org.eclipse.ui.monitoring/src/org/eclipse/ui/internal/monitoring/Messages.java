/*******************************************************************************
 * Copyright (c) 2014, 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Marcus Eng (Google) - initial API and implementation
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	public static String DefaultUiFreezeEventLogger_holding_1;
	public static String DefaultUiFreezeEventLogger_sample_header_2;
	public static String DefaultUiFreezeEventLogger_stack_trace_header;
	public static String DefaultUiFreezeEventLogger_thread_details;
	public static String DefaultUiFreezeEventLogger_thread_header_2;
	public static String DefaultUiFreezeEventLogger_ui_freeze_finished_header_2;
	public static String DefaultUiFreezeEventLogger_ui_freeze_ongoing_header_2;
	public static String DefaultUiFreezeEventLogger_waiting_for_1;
	public static String DefaultUiFreezeEventLogger_waiting_for_with_lock_owner_3;
	public static String EventLoopMonitorThread_deadlock_error_1;
	public static String EventLoopMonitorThread_deadlock_threshold_too_low_error_2;
	public static String EventLoopMonitorThread_display_was_null;
	public static String EventLoopMonitorThread_error_threshold_too_low_error_2;
	public static String EventLoopMonitorThread_external_exception_error_1;
	public static String EventLoopMonitorThread_invalid_argument_error_1;
	public static String EventLoopMonitorThread_invalid_logger_type_error_4;
	public static String EventLoopMonitorThread_logging_disabled_error;
	public static String EventLoopMonitorThread_warning_threshold_error_1;
	public static String EventLoopMonitorThread_max_event_loop_depth_exceeded_1;
	public static String EventLoopMonitorThread_workbench_was_null;
	public static String FilterHandler_missing_thread_error;
	public static String MonitoringStartup_initialization_error;

	private Messages() {
		// Do not instantiate.
	}

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
