package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Help context ids for the debug ui.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
public interface IDebugHelpContextIds {
	public static final String PREFIX = IDebugUIConstants.PLUGIN_ID + "."; //$NON-NLS-1$
	
	// Actions
	public static final String DEBUG_ACTION = PREFIX + "debug_action_context"; //$NON-NLS-1$
	public static final String RUN_ACTION = PREFIX + "run_action_context"; //$NON-NLS-1$
	public static final String INSPECT_ACTION = PREFIX + "inspect_action_context"; //$NON-NLS-1$
	public static final String CHANGE_VALUE_ACTION = PREFIX + "change_value_action_context"; //$NON-NLS-1$
	public static final String CLEAR_CONSOLE_ACTION = PREFIX + "clear_console_action_context"; //$NON-NLS-1$
	public static final String COPY_TO_CLIPBOARD_ACTION = PREFIX + "copy_to_clipboard_action_context"; //$NON-NLS-1$
	public static final String COPY_VARIABLES_TO_CLIPBOARD_ACTION = PREFIX + "copy_variables_to_clipboard_action_context"; //$NON-NLS-1$
	public static final String DISCONNECT_ACTION = PREFIX + "disconnect_action_context"; //$NON-NLS-1$
	public static final String ENABLE_DISABLE_BREAKPOINT_ACTION = PREFIX + "enable_disable_breakpoint_action_context"; //$NON-NLS-1$
	public static final String LAUNCH_SELECTION_ACTION = PREFIX + "launch_selection_action_context"; //$NON-NLS-1$
	public static final String OPEN_BREAKPOINT_ACTION = PREFIX + "open_breakpoint_action_context"; //$NON-NLS-1$
	public static final String RELAUNCH_ACTION = PREFIX + "relaunch_action_context"; //$NON-NLS-1$
	public static final String RELAUNCH_LAST_ACTION = PREFIX + "relaunch_last_action_context"; //$NON-NLS-1$
	public static final String RELAUNCH_HISTORY_ACTION = PREFIX + "relaunch_history_action_context"; //$NON-NLS-1$
	public static final String REMOVE_ACTION = PREFIX + "remove_action_context"; //$NON-NLS-1$
	public static final String REMOVE_ALL_ACTION = PREFIX + "remove_all_action_context"; //$NON-NLS-1$
	public static final String RESUME_ACTION = PREFIX + "resume_action_context"; //$NON-NLS-1$
	public static final String SHOW_QUALIFIED_NAMES_ACTION = PREFIX + "show_qualified_names_action_context"; //$NON-NLS-1$
	public static final String SHOW_TYPES_ACTION = PREFIX + "show_types_action_context"; //$NON-NLS-1$
	public static final String STEP_INTO_ACTION = PREFIX + "step_into_action_context"; //$NON-NLS-1$
	public static final String STEP_OVER_ACTION = PREFIX + "step_over_action_context"; //$NON-NLS-1$
	public static final String STEP_RETURN_ACTION = PREFIX + "step_return_action_context"; //$NON-NLS-1$
	public static final String SUSPEND_ACTION = PREFIX + "suspend_action_context"; //$NON-NLS-1$
	public static final String TERMINATE_ACTION = PREFIX + "terminate_action_context"; //$NON-NLS-1$
	public static final String TERMINATE_ALL_ACTION = PREFIX + "terminate_all_action_context"; //$NON-NLS-1$
	public static final String TERMINATE_AND_REMOVE_ACTION = PREFIX + "terminate_and_remove_action_context"; //$NON-NLS-1$
	public static final String SHOW_DETAIL_PANE_ACTION = PREFIX + "show_detail_pane_action_context"; //$NON-NLS-1$
	public static final String SHOW_BREAKPOINTS_FOR_MODEL_ACTION = PREFIX + "show_breakpoints_for_model_action_context"; //$NON-NLS-1$
		
	// Views
	public static final String DEBUG_VIEW = PREFIX + "debug_view_context"; //$NON-NLS-1$
	public static final String PROCESS_VIEW = PREFIX + "process_view_context"; //$NON-NLS-1$
	public static final String VARIABLE_VIEW = PREFIX + "variable_view_context"; //$NON-NLS-1$
	public static final String INSPECTOR_VIEW = PREFIX + "inspector_view_context"; //$NON-NLS-1$
	public static final String BREAKPOINT_VIEW = PREFIX + "breakpoint_view_context"; //$NON-NLS-1$
	public static final String CONSOLE_VIEW = PREFIX + "console_view_context"; //$NON-NLS-1$
	public static final String EXPRESSION_VIEW = PREFIX + "expression_view_context"; //$NON-NLS-1$
	
	// Preference pages
	public static final String DEBUG_PREFERENCE_PAGE = PREFIX + "debug_preference_page_context"; //$NON-NLS-1$
	public static final String CONSOLE_PREFERENCE_PAGE = PREFIX + "console_preference_page_context"; //$NON-NLS-1$
	public static final String VARIABLE_VIEWS_PREFERENCE_PAGE = PREFIX + "variable_views_preference_page_context"; //$NON-NLS-1$

	// Dialogs
	public static final String LAUNCH_CONFIGURATION_DIALOG = PREFIX + "launch_configuration_dialog"; //$NON-NLS-1$
	
}

