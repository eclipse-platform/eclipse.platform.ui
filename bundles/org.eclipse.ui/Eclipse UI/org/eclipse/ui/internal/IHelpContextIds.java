package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
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
	public static final String PREFIX = PlatformUI.PLUGIN_ID + ".";
	
	// Actions
	public static final String ADD_BOOKMARK_ACTION = PREFIX + "add_bookmark_action_context";
	public static final String INCREMENTAL_BUILD_ACTION = PREFIX + "incremental_build_action_context";
	public static final String FULL_BUILD_ACTION = PREFIX + "full_build_action_context";
	public static final String CLOSE_RESOURCE_ACTION = PREFIX + "close_resource_action_context";
	public static final String OPEN_RESOURCE_ACTION = PREFIX + "open_resource_action_context";
	public static final String OPEN_FILE_ACTION = PREFIX + "open_file_action_context";
	public static final String OPEN_SYSTEM_EDITOR_ACTION = PREFIX + "open_system_editor_action_context";
	public static final String REFRESH_ACTION = PREFIX + "refresh_action_context";
	public static final String MOVE_RESOURCE_ACTION = PREFIX + "move_resource_action_context";
	public static final String COPY_RESOURCE_ACTION = PREFIX + "copy_resource_action_context";
	public static final String MOVE_PROJECT_ACTION = PREFIX + "move_project_action_context";
	public static final String COPY_PROJECT_ACTION = PREFIX + "copy_project_action_context";
	public static final String RENAME_RESOURCE_ACTION = PREFIX + "rename_resource_action_context";
	public static final String DELETE_RESOURCE_ACTION = PREFIX + "delete_resource_action_context";
	public static final String PROPERTY_DIALOG_ACTION = PREFIX + "property_dialog_action_context";
	public static final String CREATE_FOLDER_ACTION = PREFIX + "create_folder_action_context";
	public static final String CREATE_FILE_ACTION = PREFIX + "create_file_action_context";
	public static final String NEW_ACTION = PREFIX + "new_action_context";
	public static final String IMPORT_ACTION = PREFIX + "import_action_context";
	public static final String EXPORT_ACTION = PREFIX + "export_action_context";
	public static final String SCRUB_LOCAL_ACTION = PREFIX + "scrub_local_action_context";
	public static final String SET_PAGE_ACTION = PREFIX + "set_page_action_context";
	public static final String SAVE_PERSPECTIVE_ACTION = PREFIX + "save_perspective_action_context";
	public static final String SAVE_AS_ACTION = PREFIX + "save_as_action_context";
	public static final String SAVE_ALL_ACTION = PREFIX + "save_all_action_context";
	public static final String SAVE_ACTION = PREFIX + "save_action_context";
	public static final String ABOUT_ACTION = PREFIX + "about_action_context";
	public static final String CLOSE_ALL_ACTION = PREFIX + "close_all_action_context";
	public static final String CLOSE_PAGE_ACTION = PREFIX + "close_page_action_context";
	public static final String CLOSE_PART_ACTION = PREFIX + "close_part_action_context";
	public static final String EDIT_ACTION_SETS_ACTION = PREFIX + "edit_action_sets_action_context";
	public static final String EDIT_PERSPECTIVES_ACTION = PREFIX + "edit_perspectives_action_context";
	public static final String GLOBAL_INCREMENTAL_BUILD_ACTION = PREFIX + "global_incremental_build_action_context";
	public static final String GLOBAL_FULL_BUILD_ACTION = PREFIX + "global_full_build_action_context";
	public static final String DELETE_RETARGET_ACTION = PREFIX + "delete_retarget_action_context";

	
	// Dialogs
	public static final String ABOUT_DIALOG = PREFIX + "about_dialog_context";
	public static final String ACTION_SET_SELECTION_DIALOG = PREFIX + "action_set_selection_dialog_context";
	public static final String EDITOR_SELECTION_DIALOG = PREFIX + "editor_selection_dialog_context";
	public static final String FILE_EXTENSION_DIALOG = PREFIX + "file_extension_dialog_context";
	public static final String PREFERENCE_DIALOG = PREFIX + "preference_dialog_context";
	public static final String WELCOME_DIALOG = PREFIX + "welcome_dialog_context";

	// Editors
	
	// Preference pages
	public static final String FILE_EDITORS_PREFERENCE_PAGE = PREFIX + "file_editors_preference_page_context";
	
	// Windows
	public static final String DETACHED_WINDOW = PREFIX + "detached_window_context";
	public static final String WORKBENCH_WINDOW = PREFIX + "workbench_window_context";

	// Wizard pages
	public static final String NEW_PROJECT_WIZARD_PAGE = PREFIX + "new_project_wizard_page_context";

	// Wizards
	public static final String NEW_WIZARD = PREFIX + "new_wizard_context";
}
