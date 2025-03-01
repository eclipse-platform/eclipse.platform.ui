/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 95292 - [Intro]
 *     		Help > Welcome missing F1 context
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the workbench.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 */
// TODO This class has been xcloned to org.eclipse.ui.internal.ide package; remove all IDE-specific constants from here
public interface IWorkbenchHelpContextIds {
	String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

	// Missing context help
	String MISSING = PREFIX + "missing"; //$NON-NLS-1$

	// Actions

	String DOCK_ON_PERSPECTIVE_ACTION = PREFIX + "dock_on_perspective_action_context"; //$NON-NLS-1$

	String SHOW_TEXT_PERSPECTIVE_ACTION = PREFIX + "show_text_perspective_action_context"; //$NON-NLS-1$

	String PROPERTY_DIALOG_ACTION = PREFIX + "property_dialog_action_context"; //$NON-NLS-1$

	String NEW_ACTION = PREFIX + "new_action_context"; //$NON-NLS-1$

	String IMPORT_ACTION = PREFIX + "import_action_context"; //$NON-NLS-1$

	String EXPORT_ACTION = PREFIX + "export_action_context"; //$NON-NLS-1$

	String SAVE_PERSPECTIVE_ACTION = PREFIX + "save_perspective_action_context"; //$NON-NLS-1$

	String SAVE_AS_ACTION = PREFIX + "save_as_action_context"; //$NON-NLS-1$

	String SAVE_ALL_ACTION = PREFIX + "save_all_action_context"; //$NON-NLS-1$

	String SAVE_ACTION = PREFIX + "save_action_context"; //$NON-NLS-1$

	String ABOUT_ACTION = PREFIX + "about_action_context"; //$NON-NLS-1$

	String CLOSE_ALL_ACTION = PREFIX + "close_all_action_context"; //$NON-NLS-1$

	String CLOSE_OTHERS_ACTION = PREFIX + "close_others_action_context"; //$NON-NLS-1$

	String LOCK_TOOLBAR_ACTION = PREFIX + "lock_toolbar_action_context"; //$NON-NLS-1$

	String CLOSE_PAGE_ACTION = PREFIX + "close_page_action_context"; //$NON-NLS-1$

	String CLOSE_PART_ACTION = PREFIX + "close_part_action_context"; //$NON-NLS-1$

	String EDIT_ACTION_SETS_ACTION = PREFIX + "edit_action_sets_action_context"; //$NON-NLS-1$

	String DELETE_RETARGET_ACTION = PREFIX + "delete_retarget_action_context"; //$NON-NLS-1$

	String CLOSE_ALL_PAGES_ACTION = PREFIX + "close_all_pages_action_context"; //$NON-NLS-1$

	String OPEN_NEW_WINDOW_ACTION = PREFIX + "open_new_window_action_context"; //$NON-NLS-1$

	String OPEN_PREFERENCES_ACTION = PREFIX + "open_preferences_action_context"; //$NON-NLS-1$

	String QUIT_ACTION = PREFIX + "quit_action_context"; //$NON-NLS-1$

	String RESET_PERSPECTIVE_ACTION = PREFIX + "reset_perspective_action_context"; //$NON-NLS-1$

	String TOGGLE_EDITORS_VISIBILITY_ACTION = PREFIX + "target_editors_visibility_action_context"; //$NON-NLS-1$

	String SHOW_VIEW_ACTION = PREFIX + "show_view_action_context"; //$NON-NLS-1$

	String SHOW_VIEW_OTHER_ACTION = PREFIX + "show_view_other_action_context"; //$NON-NLS-1$

	String OPEN_PERSPECTIVE_ACTION = PREFIX + "open_perspective_action_context"; //$NON-NLS-1$

	String CLOSE_ALL_SAVED_ACTION = PREFIX + "close_all_saved_action_context"; //$NON-NLS-1$

	String SHOW_VIEW_MENU_ACTION = PREFIX + "show_view_menu_action_context"; //$NON-NLS-1$

	String WORKBENCH_EDITORS_ACTION = PREFIX + "workbench_editors_action_context"; //$NON-NLS-1$

	String CELL_CUT_ACTION = PREFIX + "cell_cut_action_context"; //$NON-NLS-1$

	String CELL_COPY_ACTION = PREFIX + "cell_copy_action_context"; //$NON-NLS-1$

	String CELL_PASTE_ACTION = PREFIX + "cell_paste_action_context"; //$NON-NLS-1$

	String CELL_DELETE_ACTION = PREFIX + "cell_delete_action_context"; //$NON-NLS-1$

	String CELL_FIND_ACTION = PREFIX + "cell_find_action_context"; //$NON-NLS-1$

	String CELL_SELECT_ALL_ACTION = PREFIX + "cell_select_all_action_context"; //$NON-NLS-1$

	String CELL_UNDO_ACTION = PREFIX + "cell_undo_action_context"; //$NON-NLS-1$

	String CELL_REDO_ACTION = PREFIX + "cell_redo_action_context"; //$NON-NLS-1$

	String SHOW_PART_PANE_MENU_ACTION = PREFIX + "show_part_pane_menu_action_context"; //$NON-NLS-1$

	String CYCLE_PART_FORWARD_ACTION = PREFIX + "cycle_part_forward_action_context"; //$NON-NLS-1$

	String CYCLE_PART_BACKWARD_ACTION = PREFIX + "cycle_part_backward_action_context"; //$NON-NLS-1$

	String CYCLE_EDITOR_FORWARD_ACTION = PREFIX + "cycle_editor_forward_action_context"; //$NON-NLS-1$

	String CYCLE_EDITOR_BACKWARD_ACTION = PREFIX + "cycle_editor_backward_action_context"; //$NON-NLS-1$

	String CYCLE_PERSPECTIVE_FORWARD_ACTION = PREFIX + "cycle_perspective_forward_action_context"; //$NON-NLS-1$

	String CYCLE_PERSPECTIVE_BACKWARD_ACTION = PREFIX + "cycle_perspective_backward_action_context"; //$NON-NLS-1$

	String ACTIVATE_EDITOR_ACTION = PREFIX + "activate_editor_action_context"; //$NON-NLS-1$

	String MAXIMIZE_PART_ACTION = PREFIX + "maximize_part_action_context"; //$NON-NLS-1$

	String MINIMIZE_PART_ACTION = PREFIX + "minimize_part_action_context"; //$NON-NLS-1$

	String SELECT_WORKING_SET_ACTION = PREFIX + "select_working_set_action_context"; //$NON-NLS-1$

	String CLEAR_WORKING_SET_ACTION = PREFIX + "clear_working_set_action_context"; //$NON-NLS-1$

	String EDIT_WORKING_SET_ACTION = PREFIX + "edit_working_set_action_context"; //$NON-NLS-1$

	String SHOW_IN_ACTION = PREFIX + "show_in_action_context"; //$NON-NLS-1$

	String NAVIGATION_HISTORY_FORWARD = PREFIX + "navigation_history_forward"; //$NON-NLS-1$

	String NAVIGATION_HISTORY_BACKWARD = PREFIX + "navigation_history_backward"; //$NON-NLS-1$

	String HELP_CONTENTS_ACTION = PREFIX + "help_contents_action_context"; //$NON-NLS-1$

	String HELP_SEARCH_ACTION = PREFIX + "help_search_action_context"; //$NON-NLS-1$

	String DYNAMIC_HELP_ACTION = PREFIX + "dynamic_help_action_context"; //$NON-NLS-1$

	String INTRO_ACTION = PREFIX + "intro_action_context"; //$NON-NLS-1$

	// // Dialogs
	String ABOUT_DIALOG = PREFIX + "about_dialog_context"; //$NON-NLS-1$

	String ABOUT_PLUGINS_DIALOG = PREFIX + "about_plugins_dialog_context"; //$NON-NLS-1$

	String ABOUT_FEATURES_PLUGINS_DIALOG = PREFIX + "about_features_plugins_dialog_context"; //$NON-NLS-1$

	String ABOUT_FEATURES_DIALOG = PREFIX + "about_features_dialog_context"; //$NON-NLS-1$

	String SYSTEM_SUMMARY_DIALOG = PREFIX + "system_summary_dialog_context"; //$NON-NLS-1$

	String ACTION_SET_SELECTION_DIALOG = PREFIX + "action_set_selection_dialog_context"; //$NON-NLS-1$

	String EDITOR_SELECTION_DIALOG = PREFIX + "editor_selection_dialog_context"; //$NON-NLS-1$

	String FILE_EXTENSION_DIALOG = PREFIX + "file_extension_dialog_context"; //$NON-NLS-1$

	String PREFERENCE_DIALOG = PREFIX + "preference_dialog_context"; //$NON-NLS-1$

	String PROBLEMS_VIEW = PREFIX + "problem_view_context";//$NON-NLS-1$

	String PROPERTY_DIALOG = PREFIX + "property_dialog_context"; //$NON-NLS-1$

	String SAVE_PERSPECTIVE_DIALOG = PREFIX + "save_perspective_dialog_context"; //$NON-NLS-1$

	String SELECT_PERSPECTIVE_DIALOG = PREFIX + "select_perspective_dialog_context"; //$NON-NLS-1$

	String SHOW_VIEW_DIALOG = PREFIX + "show_view_dialog_context"; //$NON-NLS-1$

	String TYPE_FILTERING_DIALOG = PREFIX + "type_filtering_dialog_context"; //$NON-NLS-1$

	String LIST_SELECTION_DIALOG = PREFIX + "list_selection_dialog_context"; //$NON-NLS-1$

	String YES_NO_CANCEL_LIST_SELECTION_DIALOG = PREFIX + "yes_no_cancel_list_selection_dialog_context"; //$NON-NLS-1$

	String WORKING_SET_SELECTION_DIALOG = PREFIX + "working_set_selection_dialog_context"; //$NON-NLS-1$

	String WORKBENCH_EDITORS_DIALOG = PREFIX + "workbench_editors_dialog"; //$NON-NLS-1$

	// // Editors
	String FILE_EDITORS_PREFERENCE_PAGE = PREFIX + "file_editors_preference_page_context"; //$NON-NLS-1$

	String PERSPECTIVES_PREFERENCE_PAGE = PREFIX + "perspectives_preference_page_context"; //$NON-NLS-1$

	String VIEWS_PREFERENCE_PAGE = PREFIX + "views_preference_page_context"; //$NON-NLS-1$

	String FONTS_PREFERENCE_PAGE = PREFIX + "font_preference_page_context"; //$NON-NLS-1$

	String KEYS_PREFERENCE_PAGE = PREFIX + "keys_preference_page_context"; //$NON-NLS-1$

	String LINK_HANDLERS_PREFERENCE_PAGE = PREFIX + "link_handlers_page_context"; //$NON-NLS-1$

	String WORKBENCH_EDITOR_PREFERENCE_PAGE = PREFIX + "workbench_editor_preference_page_context"; //$NON-NLS-1$

	String CONTENT_TYPES_PREFERENCE_PAGE = PREFIX + "content_types_preference_page_context"; //$NON-NLS-1$

	String WORKBENCH_PREFERENCE_PAGE = PREFIX + "workbench_preference_page_context"; //$NON-NLS-1$

	String GLOBALIZATION_PREFERENCE_PAGE = PREFIX + "globalization_preference_page_context"; //$NON-NLS-1$

	String DECORATORS_PREFERENCE_PAGE = PREFIX + "decorators_preference_page_context"; //$NON-NLS-1$

	String STARTUP_PREFERENCE_PAGE = PREFIX + "startup_preference_page_context"; //$NON-NLS-1$

	String WORKSPACES_PREFERENCE_PAGE = PREFIX + "workspaces_preference_page_context"; //$NON-NLS-1$

	String RESPONSIVE_UI = PREFIX + "responsive_ui_context"; //$NON-NLS-1$

	String LARGE_FILE_ASSOCIATIONS_PREFERENCE_PAGE = PREFIX + "large_file_associations_preference_page_context"; //$NON-NLS-1$

	// // Windows
	String DETACHED_WINDOW = PREFIX + "detached_window_context"; //$NON-NLS-1$

	String WORKBENCH_WINDOW = PREFIX + "workbench_window_context"; //$NON-NLS-1$

	// // Wizard pages

	String NEW_WIZARD_SELECTION_WIZARD_PAGE = PREFIX + "new_wizard_selection_wizard_page_context"; //$NON-NLS-1$

	String EXPORT_WIZARD_SELECTION_WIZARD_PAGE = PREFIX + "export_wizard_selection_wizard_page_context"; //$NON-NLS-1$

	String IMPORT_WIZARD_SELECTION_WIZARD_PAGE = PREFIX + "import_wizard_selection_wizard_page_context"; //$NON-NLS-1$

	String WORKING_SET_TYPE_PAGE = PREFIX + "working_set_type_page"; //$NON-NLS-1$

	// Wizards
	String NEW_WIZARD = PREFIX + "new_wizard_context"; //$NON-NLS-1$

	String NEW_WIZARD_SHORTCUT = PREFIX + "new_wizard_shortcut_context"; //$NON-NLS-1$

	String IMPORT_WIZARD = PREFIX + "import_wizard_context"; //$NON-NLS-1$

	String EXPORT_WIZARD = PREFIX + "export_wizard_context"; //$NON-NLS-1$

	String WORKING_SET_NEW_WIZARD = PREFIX + "working_set_new_wizard_context"; //$NON-NLS-1$

	String WORKING_SET_EDIT_WIZARD = PREFIX + "working_set_edit_wizard_context"; //$NON-NLS-1$

	String CAPABILITY_PREFERENCE_PAGE = PREFIX + "capabilities_preference_page_context"; //$NON-NLS-1$

	String TOGGLE_COOLBAR_ACTION = PREFIX + "toggle_coolbar_action"; //$NON-NLS-1$

	String PREFERENCES_IMPORT_WIZARD_PAGE = PREFIX + "preferences_import_wizard_page"; //$NON-NLS-1$

	String PREFERENCES_EXPORT_WIZARD_PAGE = PREFIX + "preferences_export_wizard_page"; //$NON-NLS-1$
}
