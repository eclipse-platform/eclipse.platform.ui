/**********************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.debug.internal.core;

import org.eclipse.osgi.util.NLS;

public class DebugCoreMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.debug.internal.core.DebugCoreMessages";//$NON-NLS-1$

	public static String Breakpoint_no_associated_marker;

	public static String BreakpointManager_Missing_breakpoint_definition;
	public static String BreakpointManager_Missing_model_identifier;
	public static String BreakpointManager_Breakpoint_extension__0__missing_required_attribute__markerType_1;
	public static String BreakpointManager_Breakpoint_extension__0__missing_required_attribute__class_2;
	public static String BreakpointManager_Class__0__specified_by_breakpoint_extension__1__does_not_implement_required_interface_IBreakpoint__3;
	public static String BreakpointManager_An_exception_occurred_during_breakpoint_change_notification__4;
	public static String BreakpointManager_An_exception_occurred_during_breakpoint_change_notification__5;

	public static String DebugEvent_illegal_detail;
	public static String DebugEvent_illegal_kind;

	public static String DebugPlugin_Invalid_status_handler_extension___0__2;
	public static String DebugPlugin_Debug_async_queue_1;
	public static String DebugPlugin_Exception_occurred_executing_command_line__1;
	public static String DebugPlugin_Eclipse_runtime_does_not_support_working_directory_2;
	public static String DebugPlugin_Registered_status_handler__0__does_not_implement_required_interface_IStatusHandler__1;
	public static String DebugPlugin_An_exception_occurred_while_dispatching_debug_events__2;
	public static String DebugPlugin_An_exception_occurred_while_filtering_debug_events__3;
	public static String DebugPlugin_31;
	public static String DebugPlugin_0;
	public static String DebugPlugin_1;

	public static String EnvironmentVariableResolver_0;
	
	public static String SystemPropertyResolver_0;	

	public static String InputStreamMonitor_label;

	public static String Launch_terminate_failed;

	public static String LaunchConfiguration_Exception_occurred_creating_launch_configuration_memento_9;
	public static String LaunchConfiguration_Exception_occurred_parsing_memento_5;
	public static String LaunchConfiguration_Failed_to_delete_launch_configuration__1;
	public static String LaunchConfiguration_Invalid_launch_configuration_memento__missing_path_attribute_3;
	public static String LaunchConfiguration_Invalid_launch_configuration_memento__missing_local_attribute_4;
	public static String LaunchConfiguration_Unable_to_restore_location_for_launch_configuration_from_memento___0__1;
	public static String LaunchConfiguration_Unable_to_generate_memento_for__0___shared_file_does_not_exist__1;
	public static String LaunchConfiguration_13;
	public static String LaunchConfigurationDelegate_6;
	public static String LaunchConfigurationDelegate_7;

	public static String LaunchConfigurationInfo_Attribute__0__is_not_of_type_boolean__3;
	public static String LaunchConfigurationInfo_Attribute__0__is_not_of_type_int__2;
	public static String LaunchConfigurationInfo_Attribute__0__is_not_of_type_java_lang_String__1;
	public static String LaunchConfigurationInfo_Attribute__0__is_not_of_type_java_util_List__1;
	public static String LaunchConfigurationInfo_Attribute__0__is_not_of_type_java_util_Map__1;
	public static String LaunchConfigurationInfo_Invalid_launch_configuration_XML__10;
	public static String LaunchConfigurationInfo_missing_type;
	public static String LaunchConfigurationInfo_36;

	public static String LaunchConfigurationWorkingCopy__0__occurred_generating_launch_configuration_XML__1;
	public static String LaunchConfigurationWorkingCopy_Specified_container_for_launch_configuration_does_not_exist_2;
	public static String LaunchConfigurationWorkingCopy_5;

	public static String LaunchManager__0__occurred_while_reading_launch_configuration_file__1___1;
	public static String LaunchManager_Invalid_launch_configuration_index__18;
	public static String LaunchManager_Invalid_source_locator_extentsion_defined_by_plug_in____0_______id___not_specified_12;
	public static String LaunchManager_does_not_exist;
	public static String LaunchManager_Source_locator_does_not_exist___0__13;
	public static String LaunchManager_Invalid_launch_configuration_comparator_extension_defined_by_plug_in__0____attribute_not_specified_1;
	public static String LaunchManager_An_exception_occurred_during_launch_change_notification__1;
	public static String LaunchManager_An_exception_occurred_during_launch_configuration_change_notification__3;
	public static String LaunchManager_30;

	public static String LaunchMode_1;

	public static String LogicalStructureType_7;
	public static String LogicalStructureType_0;
	public static String LogicalStructureProvider_0;
	public static String LogicalStructureProvider_1;

	public static String OutputStreamMonitor_label;

	public static String ProcessMonitorJob_0;

	public static String RuntimeProcess_terminate_failed;
	public static String RuntimeProcess_Exit_value_not_available_until_process_terminates__1;

	public static String ExpressionManager_An_exception_occurred_during_expression_change_notification__1;

	public static String LaunchConfigurationType_Launch_delegate_for__0__does_not_implement_required_interface_ILaunchConfigurationDelegate__1;
	public static String LaunchConfigurationType_9;
	public static String LaunchConfigurationType_10;

	public static String MemoryRenderingManager_ErrorMsg;
	public static String WatchExpression_0;
	public static String NullStreamsProxy_0;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, DebugCoreMessages.class);
	}
}