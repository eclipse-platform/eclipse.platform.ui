/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the workbench.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
public interface IHelpContextIds {
	public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$
	
	// Actions
	public static final String ADD_BOOKMARK_ACTION = PREFIX + "add_bookmark_action_context"; //$NON-NLS-1$
	public static final String ADD_TASK_ACTION = PREFIX + "add_task_action_context"; //$NON-NLS-1$	
	public static final String INCREMENTAL_BUILD_ACTION = PREFIX + "incremental_build_action_context"; //$NON-NLS-1$
	public static final String FULL_BUILD_ACTION = PREFIX + "full_build_action_context"; //$NON-NLS-1$
	public static final String CLOSE_RESOURCE_ACTION = PREFIX + "close_resource_action_context"; //$NON-NLS-1$
	public static final String OPEN_RESOURCE_ACTION = PREFIX + "open_resource_action_context"; //$NON-NLS-1$
	public static final String OPEN_FILE_ACTION = PREFIX + "open_file_action_context"; //$NON-NLS-1$
	public static final String OPEN_SYSTEM_EDITOR_ACTION = PREFIX + "open_system_editor_action_context"; //$NON-NLS-1$
	public static final String REFRESH_ACTION = PREFIX + "refresh_action_context"; //$NON-NLS-1$
	public static final String MOVE_RESOURCE_ACTION = PREFIX + "move_resource_action_context"; //$NON-NLS-1$
	public static final String COPY_RESOURCE_ACTION = PREFIX + "copy_resource_action_context"; //$NON-NLS-1$
	public static final String MOVE_PROJECT_ACTION = PREFIX + "move_project_action_context"; //$NON-NLS-1$
	public static final String COPY_PROJECT_ACTION = PREFIX + "copy_project_action_context"; //$NON-NLS-1$
	public static final String RENAME_RESOURCE_ACTION = PREFIX + "rename_resource_action_context"; //$NON-NLS-1$
	public static final String DELETE_RESOURCE_ACTION = PREFIX + "delete_resource_action_context"; //$NON-NLS-1$
	public static final String PROPERTY_DIALOG_ACTION = PREFIX + "property_dialog_action_context"; //$NON-NLS-1$
	public static final String PROJECT_PROPERTY_DIALOG_ACTION = PREFIX + "project_property_dialog_action_context"; //$NON-NLS-1$
	public static final String CREATE_FOLDER_ACTION = PREFIX + "create_folder_action_context"; //$NON-NLS-1$
	public static final String CREATE_FILE_ACTION = PREFIX + "create_file_action_context"; //$NON-NLS-1$
	public static final String NEW_ACTION = PREFIX + "new_action_context"; //$NON-NLS-1$
	public static final String IMPORT_ACTION = PREFIX + "import_action_context"; //$NON-NLS-1$
	public static final String EXPORT_ACTION = PREFIX + "export_action_context"; //$NON-NLS-1$
	public static final String SCRUB_LOCAL_ACTION = PREFIX + "scrub_local_action_context"; //$NON-NLS-1$
	public static final String SET_PAGE_ACTION = PREFIX + "set_page_action_context"; //$NON-NLS-1$
	public static final String SAVE_PERSPECTIVE_ACTION = PREFIX + "save_perspective_action_context"; //$NON-NLS-1$
	public static final String SAVE_AS_ACTION = PREFIX + "save_as_action_context"; //$NON-NLS-1$
	public static final String SAVE_ALL_ACTION = PREFIX + "save_all_action_context"; //$NON-NLS-1$
	public static final String SAVE_ACTION = PREFIX + "save_action_context"; //$NON-NLS-1$
	public static final String ABOUT_ACTION = PREFIX + "about_action_context"; //$NON-NLS-1$
	public static final String CLOSE_ALL_ACTION = PREFIX + "close_all_action_context"; //$NON-NLS-1$
	public static final String LOCK_TOOLBAR_ACTION = PREFIX + "lock_toolbar_action_context"; //$NON-NLS-1$
	public static final String CLOSE_PAGE_ACTION = PREFIX + "close_page_action_context"; //$NON-NLS-1$
	public static final String CLOSE_PART_ACTION = PREFIX + "close_part_action_context"; //$NON-NLS-1$
	public static final String EDIT_ACTION_SETS_ACTION = PREFIX + "edit_action_sets_action_context"; //$NON-NLS-1$
	public static final String EDIT_PERSPECTIVES_ACTION = PREFIX + "edit_perspectives_action_context"; //$NON-NLS-1$
	public static final String GLOBAL_INCREMENTAL_BUILD_ACTION = PREFIX + "global_incremental_build_action_context"; //$NON-NLS-1$
	public static final String GLOBAL_FULL_BUILD_ACTION = PREFIX + "global_full_build_action_context"; //$NON-NLS-1$
	public static final String DELETE_RETARGET_ACTION = PREFIX + "delete_retarget_action_context"; //$NON-NLS-1$
	public static final String CLOSE_ALL_PAGES_ACTION = PREFIX + "close_all_pages_action_context"; //$NON-NLS-1$
	public static final String OPEN_NEW_PAGE_ACTION = PREFIX + "open_new_page_action_context"; //$NON-NLS-1$
	public static final String OPEN_NEW_WINDOW_ACTION = PREFIX + "open_new_window_action_context"; //$NON-NLS-1$
	public static final String OPEN_PREFERENCES_ACTION = PREFIX + "open_preferences_action_context"; //$NON-NLS-1$
	public static final String OPEN_NEW_ACTION = PREFIX + "open_new_action_context"; //$NON-NLS-1$
	public static final String NEXT_PAGE_ACTION = PREFIX + "next_page_action_context"; //$NON-NLS-1$
	public static final String QUICK_START_ACTION = PREFIX + "quick_start_action_context"; //$NON-NLS-1$
	public static final String TIPS_AND_TRICKS_ACTION = PREFIX + "tips_and_tricks_action_context"; //$NON-NLS-1$
	public static final String QUIT_ACTION = PREFIX + "quit_action_context"; //$NON-NLS-1$
	public static final String RESET_PERSPECTIVE_ACTION = PREFIX + "reset_perspective_action_context"; //$NON-NLS-1$
	public static final String TOGGLE_EDITORS_VISIBILITY_ACTION = PREFIX + "target_editors_visibility_action_context"; //$NON-NLS-1$
	public static final String SWITCH_TO_PERSPECTIVE_ACTION = PREFIX + "switch_to_perspective_action_context"; //$NON-NLS-1$
	public static final String SHOW_VIEW_ACTION = PREFIX + "show_view_action_context"; //$NON-NLS-1$
	public static final String SHOW_VIEW_OTHER_ACTION = PREFIX + "show_view_other_action_context"; //$NON-NLS-1$
	public static final String OPEN_PERSPECTIVE_ACTION = PREFIX + "open_perspective_action_context"; //$NON-NLS-1$
	public static final String OPEN_PERSPECTIVE_OTHER_ACTION = PREFIX + "open_perspective_other_action_context"; //$NON-NLS-1$
	public static final String CLOSE_ALL_SAVED_ACTION = PREFIX + "close_all_saved_action_context"; //$NON-NLS-1$
	public static final String SHOW_VIEW_MENU_ACTION = PREFIX + "show_view_menu_action_context"; //$NON-NLS-1$
	public static final String WORKBENCH_EDITORS_ACTION = PREFIX + "workbench_editors_action_context"; //$NON-NLS-1$
	public static final String NEW_WIZARD_SHORTCUT_ACTION = PREFIX + "new_wizard_shortcut_action_context"; //$NON-NLS-1$
	public static final String TEXT_CUT_ACTION = PREFIX + "text_cut_action_context"; //$NON-NLS-1$
	public static final String TEXT_COPY_ACTION = PREFIX + "text_copy_action_context"; //$NON-NLS-1$
	public static final String TEXT_PASTE_ACTION = PREFIX + "text_paste_action_context"; //$NON-NLS-1$
	public static final String TEXT_DELETE_ACTION = PREFIX + "text_delete_action_context"; //$NON-NLS-1$
	public static final String TEXT_SELECT_ALL_ACTION = PREFIX + "text_select_all_action_context"; //$NON-NLS-1$
	public static final String CELL_CUT_ACTION = PREFIX + "cell_cut_action_context"; //$NON-NLS-1$
	public static final String CELL_COPY_ACTION = PREFIX + "cell_copy_action_context"; //$NON-NLS-1$
	public static final String CELL_PASTE_ACTION = PREFIX + "cell_paste_action_context"; //$NON-NLS-1$
	public static final String CELL_DELETE_ACTION = PREFIX + "cell_delete_action_context"; //$NON-NLS-1$
	public static final String CELL_FIND_ACTION = PREFIX + "cell_find_action_context"; //$NON-NLS-1$
	public static final String CELL_SELECT_ALL_ACTION = PREFIX + "cell_select_all_action_context"; //$NON-NLS-1$
	public static final String CELL_UNDO_ACTION = PREFIX + "cell_undo_action_context"; //$NON-NLS-1$
	public static final String CELL_REDO_ACTION = PREFIX + "cell_redo_action_context"; //$NON-NLS-1$
	public static final String SHOW_PART_PANE_MENU_ACTION = PREFIX + "show_part_pane_menu_action_context"; //$NON-NLS-1$
	public static final String CYCLE_PART_FORWARD_ACTION = PREFIX + "cycle_part_forward_action_context"; //$NON-NLS-1$
	public static final String CYCLE_PART_BACKWARD_ACTION = PREFIX + "cycle_part_backward_action_context"; //$NON-NLS-1$
	public static final String CYCLE_EDITOR_FORWARD_ACTION = PREFIX + "cycle_editor_forward_action_context"; //$NON-NLS-1$
	public static final String CYCLE_EDITOR_BACKWARD_ACTION = PREFIX + "cycle_editor_backward_action_context"; //$NON-NLS-1$
	public static final String CYCLE_PERSPECTIVE_FORWARD_ACTION = PREFIX + "cycle_perspective_forward_action_context"; //$NON-NLS-1$
	public static final String CYCLE_PERSPECTIVE_BACKWARD_ACTION = PREFIX + "cycle_perspective_backward_action_context"; //$NON-NLS-1$
	public static final String ACTIVATE_EDITOR_ACTION = PREFIX + "activate_editor_action_context"; //$NON-NLS-1$
	public static final String OPEN_WORKSPACE_FILE_ACTION = PREFIX + "open_workspace_file_action_context"; //$NON-NLS-1$
	public static final String SELECT_WORKING_SET_ACTION = PREFIX + "select_working_set_action_context"; //$NON-NLS-1$
	public static final String CLEAR_WORKING_SET_ACTION = PREFIX + "clear_working_set_action_context"; //$NON-NLS-1$
	public static final String EDIT_WORKING_SET_ACTION = PREFIX + "edit_working_set_action_context"; //$NON-NLS-1$
	public static final String SHOW_IN_ACTION = PREFIX + "show_in_action_context"; //$NON-NLS-1$
	public static final String NAVIGATION_HISTORY_FORWARD = PREFIX + "navigation_history_forward"; //$NON-NLS-1$
	public static final String NAVIGATION_HISTORY_BACKWARD = PREFIX + "navigation_history_backward"; //$NON-NLS-1$
		
	// Dialogs
	public static final String ABOUT_DIALOG = PREFIX + "about_dialog_context"; //$NON-NLS-1$
	public static final String ABOUT_PLUGINS_DIALOG = PREFIX + "about_plugins_dialog_context"; //$NON-NLS-1$
	public static final String ABOUT_FEATURES_PLUGINS_DIALOG = PREFIX + "about_features_plugins_dialog_context"; //$NON-NLS-1$
	public static final String ABOUT_FEATURES_DIALOG = PREFIX + "about_features_dialog_context"; //$NON-NLS-1$
	public static final String ACTION_SET_SELECTION_DIALOG = PREFIX + "action_set_selection_dialog_context"; //$NON-NLS-1$
	public static final String EDITOR_SELECTION_DIALOG = PREFIX + "editor_selection_dialog_context"; //$NON-NLS-1$
	public static final String FILE_EXTENSION_DIALOG = PREFIX + "file_extension_dialog_context"; //$NON-NLS-1$
	public static final String PREFERENCE_DIALOG = PREFIX + "preference_dialog_context"; //$NON-NLS-1$
	public static final String PROPERTY_DIALOG = PREFIX + "property_dialog_context"; //$NON-NLS-1$
	public static final String SAVE_PERSPECTIVE_DIALOG = PREFIX + "save_perspective_dialog_context"; //$NON-NLS-1$
	public static final String SELECT_PERSPECTIVE_DIALOG = PREFIX + "select_perspective_dialog_context"; //$NON-NLS-1$
	public static final String PROJECT_LOCATION_SELECTION_DIALOG = PREFIX + "project_location_selection_dialog_context"; //$NON-NLS-1$
	public static final String SHOW_VIEW_DIALOG = PREFIX + "show_view_dialog_context"; //$NON-NLS-1$
	public static final String SHOW_PROJECT_PERSPECTIVE_DIALOG = PREFIX + "show_project_perspective_dialog_context"; //$NON-NLS-1$
	public static final String SAVE_AS_DIALOG = PREFIX + "save_as_dialog_context"; //$NON-NLS-1$
	public static final String TYPE_FILTERING_DIALOG = PREFIX + "type_filtering_dialog_context"; //$NON-NLS-1$
	public static final String CONTAINER_SELECTION_DIALOG = PREFIX + "container_selection_dialog_context"; //$NON-NLS-1$
	public static final String FILE_SELECTION_DIALOG = PREFIX + "file_selection_dialog_context"; //$NON-NLS-1$
	public static final String LIST_SELECTION_DIALOG = PREFIX + "list_selection_dialog_context"; //$NON-NLS-1$
	public static final String YES_NO_CANCEL_LIST_SELECTION_DIALOG = PREFIX + "yes_no_cancel_list_selection_dialog_context"; //$NON-NLS-1$
	public static final String RESOURCE_SELECTION_DIALOG = PREFIX + "resource_selection_dialog_context"; //$NON-NLS-1$
	public static final String GOTO_LINE_DIALOG = PREFIX + "goto_line_dialog_context"; //$NON-NLS-1$	
	public static final String WORKING_SET_SELECTION_DIALOG = PREFIX + "working_set_selection_dialog_context" ; //$NON-NLS-1$
	public static final String DELETE_PROJECT_DIALOG = PREFIX + "delete_project_dialog_context"; //$NON-NLS-1$
	public static final String FILTER_DIALOG = PREFIX + "filter_dialog_context"; //$NON-NLS-1$
	public static final String MARKER_RESOLUTION_SELECTION_DIALOG = PREFIX + "marker_resolution_selection_dialog_context"; //$NON-NLS-1$
	public static final String WORKBENCH_EDITORS_DIALOG = PREFIX + "workbench_editors_dialog"; //$NON-NLS-1$
	public static final String WELCOME_PAGE_SELECTION_DIALOG = PREFIX + "welcome_page_selection_dialog"; //$NON-NLS-1$
	public static final String TIPS_AND_TRICKS_PAGE_SELECTION_DIALOG = PREFIX + "tips_and_tricks_page_selection_dialog"; //$NON-NLS-1$
	public static final String OPEN_RESOURCE_DIALOG = PREFIX + "open_resource_dialog"; //$NON-NLS-1$
	public static final String NEW_FOLDER_DIALOG = PREFIX + "new_folder_dialog"; //$NON-NLS-1$
	public static final String PATH_VARIABLE_SELECTION_DIALOG = PREFIX + "path_variable_selection_dialog"; //$NON-NLS-1$
	public static final String EXIT_PROMPT_ON_CLOSE_LAST_WINDOW = PREFIX + "exit_prompt_on_close_last_window"; //$NON-NLS-1$
		
	// Editors
	public static final String WELCOME_EDITOR = PREFIX + "welcome_editor_context"; //$NON-NLS-1$
	
	// Preference pages
	public static final String BUILD_ORDER_PREFERENCE_PAGE = PREFIX + "build_order_preference_page_context"; //$NON-NLS-1$
	public static final String FILE_EDITORS_PREFERENCE_PAGE = PREFIX + "file_editors_preference_page_context"; //$NON-NLS-1$
	public static final String FILE_STATES_PREFERENCE_PAGE = PREFIX + "file_states_preference_page_context"; //$NON-NLS-1$
	public static final String PERSPECTIVES_PREFERENCE_PAGE = PREFIX + "perspectives_preference_page_context"; //$NON-NLS-1$
	public static final String VIEWS_PREFERENCE_PAGE = PREFIX + "views_preference_page_context"; //$NON-NLS-1$
	public static final String COMPARE_VIEWERS_PREFERENCE_PAGE = PREFIX + "compare_viewers_preference_page_context"; //$NON-NLS-1$
	public static final String WORKBENCH_EDITOR_PREFERENCE_PAGE = PREFIX + "workbench_editor_preference_page_context"; //$NON-NLS-1$
	public static final String WORKBENCH_PREFERENCE_PAGE = PREFIX + "workbench_preference_page_context"; //$NON-NLS-1$
	public static final String FONT_PREFERENCE_PAGE = PREFIX + "font_preference_page_context"; //$NON-NLS-1$
	public static final String DECORATORS_PREFERENCE_PAGE = PREFIX + "decorators_preference_page_context"; //$NON-NLS-1$
	public static final String STARTUP_PREFERENCE_PAGE = PREFIX + "startup_preference_page_context"; //$NON-NLS-1$
	public static final String LINKED_RESOURCE_PREFERENCE_PAGE = PREFIX + "linked_resource_preference_page_context"; //$NON-NLS-1$

	// Property pages
	public static final String PROJECT_REFERENCE_PROPERTY_PAGE = PREFIX + "project_reference_property_page_context"; //$NON-NLS-1$
	public static final String PROJECT_CAPABILITY_PROPERTY_PAGE = PREFIX + "project_capability_property_page_context"; //$NON-NLS-1$
	public static final String RESOURCE_INFO_PROPERTY_PAGE = PREFIX + "resource_info_property_page_context"; //$NON-NLS-1$
		
	// Windows
	public static final String DETACHED_WINDOW = PREFIX + "detached_window_context"; //$NON-NLS-1$
	public static final String WORKBENCH_WINDOW = PREFIX + "workbench_window_context"; //$NON-NLS-1$

	// Wizard pages
	public static final String NEW_PROJECT_WIZARD_PAGE = PREFIX + "new_project_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_PROJECT_REFERENCE_WIZARD_PAGE = PREFIX + "new_project_reference_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_PROJECT_CAPABILITY_WIZARD_PAGE = PREFIX + "new_project_capability_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_PROJECT_REVIEW_WIZARD_PAGE = PREFIX + "new_project_review_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_PROJECT_CONFIGURE_WIZARD_PAGE = PREFIX + "new_project_configure_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_FOLDER_WIZARD_PAGE = PREFIX + "new_folder_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_FILE_WIZARD_PAGE = PREFIX + "new_file_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_WIZARD_SELECTION_WIZARD_PAGE = PREFIX + "new_wizard_selection_wizard_page_context"; //$NON-NLS-1$
	public static final String EXPORT_WIZARD_SELECTION_WIZARD_PAGE = PREFIX + "export_wizard_selection_wizard_page_context"; //$NON-NLS-1$
	public static final String IMPORT_WIZARD_SELECTION_WIZARD_PAGE = PREFIX + "import_wizard_selection_wizard_page_context"; //$NON-NLS-1$
	public static final String WORKING_SET_RESOURCE_PAGE = PREFIX + "working_set_resource_page"; //$NON-NLS-1$	
	public static final String WORKING_SET_TYPE_PAGE = PREFIX + "working_set_type_page"; //$NON-NLS-1$	
	public static final String NEW_LINK_WIZARD_PAGE = PREFIX + "new_link_wizard_page_context"; //$NON-NLS-1$
	
	// Wizards
	public static final String NEW_WIZARD = PREFIX + "new_wizard_context"; //$NON-NLS-1$
	public static final String NEW_WIZARD_SHORTCUT = PREFIX + "new_wizard_shortcut_context"; //$NON-NLS-1$
	public static final String NEW_FILE_WIZARD = PREFIX + "new_file_wizard_context"; //$NON-NLS-1$
	public static final String NEW_FOLDER_WIZARD = PREFIX + "new_folder_wizard_context"; //$NON-NLS-1$
	public static final String NEW_PROJECT_WIZARD = PREFIX + "new_project_wizard_context"; //$NON-NLS-1$
	public static final String IMPORT_WIZARD = PREFIX + "import_wizard_context"; //$NON-NLS-1$
	public static final String EXPORT_WIZARD = PREFIX + "export_wizard_context"; //$NON-NLS-1$
	public static final String UPDATE_CAPABILITY_WIZARD = PREFIX + "update_capability_wizard_context"; //$NON-NLS-1$
	public static final String WORKING_SET_NEW_WIZARD = PREFIX + "working_set_new_wizard_context"; //$NON-NLS-1$	
	public static final String WORKING_SET_EDIT_WIZARD = PREFIX + "working_set_edit_wizard_context"; //$NON-NLS-1$		
}
