/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Registers View (Bug 53640)
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

 
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
	public static final String CHANGE_VALUE_ACTION = PREFIX + "change_value_action_context"; //$NON-NLS-1$	
	public static final String OPEN_BREAKPOINT_ACTION = PREFIX + "open_breakpoint_action_context"; //$NON-NLS-1$
	public static final String RELAUNCH_HISTORY_ACTION = PREFIX + "relaunch_history_action_context"; //$NON-NLS-1$	
	public static final String SHOW_DETAIL_PANE_ACTION = PREFIX + "show_detail_pane_action_context"; //$NON-NLS-1$
	public static final String SHOW_BREAKPOINTS_FOR_MODEL_ACTION = PREFIX + "show_breakpoints_for_model_action_context"; //$NON-NLS-1$
	public static final String SHOW_TYPES_ACTION = PREFIX + "show_types_action_context"; //$NON-NLS-1$
	public static final String VARIABLES_CONTENT_PROVIDERS_ACTION = PREFIX + "variables_content_providers_action_context"; //$NON-NLS-1$
	public static final String VARIABLES_SELECT_LOGICAL_STRUCTURE = PREFIX + "variables_select_logical_structure"; //$NON-NLS-1$
	public static final String SELECT_WORKING_SET_ACTION = PREFIX + "select_working_set_context"; //$NON-NLS-1$			

	public static final String CLEAR_WORKING_SET_ACTION = PREFIX + "clear_working_set_context"; //$NON-NLS-1$
	public static final String EDIT_LAUNCH_CONFIGURATION_ACTION = PREFIX + "edit_launch_configuration_action_context"; //$NON-NLS-1$
	public static final String OPEN_LAUNCH_CONFIGURATION_ACTION = PREFIX + "open_launch_configuration_action_context"; //$NON-NLS-1$
	public static final String ADD_WATCH_EXPRESSION_DIALOG= PREFIX + "add_watch_expression_dialog_context"; //$NON-NLS-1$
	public static final String EDIT_WATCH_EXPRESSION_DIALOG= PREFIX + "edit_watch_expression_dialog_context"; //$NON-NLS-1$
	public static final String LINK_BREAKPOINTS_WITH_DEBUG_ACTION= PREFIX + "link_breakpoints_with_debug_context"; //$NON-NLS-1$
	public static final String EDIT_SOURCELOOKUP_ACTION = PREFIX + "edit_source_lookup_path_context";//$NON-NLS-1$
	public static final String LOOKUP_SOURCE_ACTION = PREFIX + "lookup_source_context";//$NON-NLS-1$
	public static final String SKIP_ALL_BREAKPOINT_ACTION = PREFIX + "skip_all_breakpoints_context"; //$NON-NLS-1$
	public static final String AUTO_MANAGE_VIEWS_ACTION = PREFIX + "auto_manage_views_context"; //$NON-NLS-1$
	public static final String FIND_VARIABLE_ACTION = PREFIX + "find_variable_context"; //$NON-NLS-1$
    public static final String ASSIGN_VALUE_ACTION = PREFIX + "assign_value_context"; //$NON-NLS-1$
    public static final String CONSOLE_TERMINATE_ACTION = PREFIX + "console_terminate_action_context"; //$NON-NLS-1$
    public static final String CONSOLE_REMOVE_ALL_TERMINATED = PREFIX + "console_remove_all_terminated_context"; //$NON-NLS-1$
    public static final String CONSOLE_REMOVE_LAUNCH = PREFIX + "console_remove_launch_context"; //$NON-NLS-1$;
    
	// Views
	public static final String DEBUG_VIEW = PREFIX + "debug_view_context"; //$NON-NLS-1$
	public static final String VARIABLE_VIEW = PREFIX + "variable_view_context"; //$NON-NLS-1$
	public static final String BREAKPOINT_VIEW = PREFIX + "breakpoint_view_context"; //$NON-NLS-1$
	public static final String EXPRESSION_VIEW = PREFIX + "expression_view_context"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_VIEW = PREFIX + "launch_configuration_view_context"; //$NON-NLS-1$
	public static final String REGISTERS_VIEW = PREFIX + "registers_view_context"; //$NON-NLS-1$
	
	// Preference pages
	public static final String DEBUG_PREFERENCE_PAGE = PREFIX + "debug_preference_page_context"; //$NON-NLS-1$
	public static final String CONSOLE_PREFERENCE_PAGE = PREFIX + "console_preference_page_context"; //$NON-NLS-1$
	public static final String DEBUG_ACTION_GROUPS_PREFERENCE_PAGE = PREFIX + "debug_action_groups_views_preference_page_context"; //$NON-NLS-1$
	public static final String LAUNCH_HISTORY_PREFERENCE_PAGE = PREFIX + "launch_history_preference_page_context"; //$NON-NLS-1$
	public static final String SIMPLE_VARIABLE_PREFERENCE_PAGE = PREFIX + "simple_variable_preference_page_context"; //$NON-NLS-1$
	
	// Dialogs
	public static final String LAUNCH_CONFIGURATION_DIALOG = PREFIX + "launch_configuration_dialog"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_PROPERTIES_DIALOG = PREFIX + "launch_configuration_properties_dialog"; //$NON-NLS-1$
	public static final String SINGLE_LAUNCH_CONFIGURATION_DIALOG = PREFIX + "single_launch_configuration_dialog"; //$NON-NLS-1$
	public static final String VARIABLE_SELECTION_DIALOG = PREFIX + "variable_selection_dialog_context"; //$NON-NLS-1$
	public static final String EDIT_SOURCELOOKUP_DIALOG = PREFIX + "edit_source_lookup_path_dialog";//$NON-NLS-1$
	public static final String SOURCELOOKUP_TAB = PREFIX + "source_container_lookup_tab";//$NON-NLS-1$
	public static final String ADD_SOURCE_CONTAINER_DIALOG = PREFIX + "add_source_container_dialog";//$NON-NLS-1$
	public static final String ADD_PROJECT_CONTAINER_DIALOG = PREFIX + "project_source_container_dialog";//$NON-NLS-1$
	public static final String ADD_FOLDER_CONTAINER_DIALOG = PREFIX + "folder_source_container_dialog";//$NON-NLS-1$
	public static final String ADD_ARCHIVE_CONTAINER_DIALOG = PREFIX + "archive_source_container_dialog";//$NON-NLS-1$
	public static final String MULTIPLE_SOURCE_DIALOG = PREFIX + "multiple_source_selection_dialog";//$NON-NLS-1$
	
	// Property pages
	public static final String PROCESS_PROPERTY_PAGE = PREFIX + "process_property_page_context"; //$NON-NLS-1$
	
	// Launch configuration dialog pages
	public static final String LAUNCH_CONFIGURATION_DIALOG_COMMON_TAB = PREFIX + "launch_configuration_dialog_common_tab"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_DIALOG_PERSPECTIVE_TAB = PREFIX + "launch_configuration_dialog_perspective_tab"; //$NON-NLS-1$	
	public static final String LAUNCH_CONFIGURATION_DIALOG_REFRESH_TAB = PREFIX + "launch_configuration_dialog refresh_tab"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_DIALOG_ENVIRONMENT_TAB = PREFIX +  "launch_configuration_dialog_environment_tab"; //$NON-NLS-1$
	
	// Working set page
	public static final String WORKING_SET_PAGE = PREFIX + "working_set_page_context"; //$NON-NLS-1$			
	
	
	
	//Editor	
	public static final String NO_SOURCE_EDITOR = PREFIX + "debugger_editor_no_source_common";//$NON-NLS-1$


    
	
}

