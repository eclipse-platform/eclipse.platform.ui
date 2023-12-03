/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Dina Sayed, dsayed@eg.ibm.com, IBM -  bug 269844
 *     Markus Schorn (Wind River Systems) -  bug 284447
 *     Christian Georgi (SAP)             -  bug 432480
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 502050
 *******************************************************************************/

package org.eclipse.ui.internal.ide;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;

/**
 * The IDEInternalPreferences are the internal constants used by the Workbench.
 */
public interface IDEInternalPreferences {
	// (boolean) Save all dirty editors before running a full or incremental build
	String SAVE_ALL_BEFORE_BUILD = "SAVE_ALL_BEFORE_BUILD"; //$NON-NLS-1$

	// (boolean) Refresh workspace on startup
	String REFRESH_WORKSPACE_ON_STARTUP = "REFRESH_WORKSPACE_ON_STARTUP"; //$NON-NLS-1$

	// (int) Workspace save interval in minutes
	// @issue we should drop this and have clients refer to the core preference instead. its not even kept up-to-date if client uses core api directly
	String SAVE_INTERVAL = "saveInterval"; //$NON-NLS-1$

	int MAX_SAVE_INTERVAL = 9999;

	// (boolean) Show Problems view to users when build contains errors
	// String SHOW_TASKS_ON_BUILD = "SHOW_TASKS_ON_BUILD"; //$NON-NLS-1$

	// (boolean) Prompt for exit confirmation when last window closed.
	String EXIT_PROMPT_ON_CLOSE_LAST_WINDOW = "EXIT_PROMPT_ON_CLOSE_LAST_WINDOW"; //$NON-NLS-1$

	// (String) Whether to open the preferred perspective when creating a new project
	String PROJECT_SWITCH_PERSP_MODE = "SWITCH_PERSPECTIVE_ON_PROJECT_CREATION"; //$NON-NLS-1$

	/**
	 * (String) Whether to open required projects when opening a project.
	 */
	String OPEN_REQUIRED_PROJECTS = "OPEN_REQUIRED_PROJECTS"; //$NON-NLS-1$

	/**
	 * (String) Whether to confirm closing unrelated projects.
	 */
	String CLOSE_UNRELATED_PROJECTS = "CLOSE_UNRELATED_PROJECTS"; //$NON-NLS-1$

	String PSPM_PROMPT = MessageDialogWithToggle.PROMPT;

	String PSPM_ALWAYS = MessageDialogWithToggle.ALWAYS;

	String PSPM_NEVER = MessageDialogWithToggle.NEVER;

	// (boolean) Whether or not to display the Welcome dialog on startup.
	String WELCOME_DIALOG = "WELCOME_DIALOG"; //$NON-NLS-1$

	//Whether or not to limit problems
	String LIMIT_PROBLEMS = "LIMIT_PROBLEMS"; //$NON-NLS-1$

	//The list of defined problems filters
	String PROBLEMS_FILTERS = "PROBLEMS_FILTERS"; //$NON-NLS-1$

	//problem limits
	String PROBLEMS_LIMIT = "PROBLEMS_LIMIT"; //$NON-NLS-1$

	//The list of defined tasks filters
	String TASKS_FILTERS = "TASKS_FILTERS"; //$NON-NLS-1$

//  The list of defined tasks filters
	String BOOKMARKS_FILTERS = "BOOKMARKS_FILTERS"; //$NON-NLS-1$

	//Enablement of marker limits
	String USE_MARKER_LIMITS = "USE_MARKER_LIMITS"; //$NON-NLS-1$

	//Value of marker limits
	String MARKER_LIMITS_VALUE = "MARKER_LIMITS_VALUE"; //$NON-NLS-1$

	// Type of import
	String IMPORT_FILES_AND_FOLDERS_TYPE = "IMPORT_FILES_AND_FOLDERS_TYPE"; //$NON-NLS-1$

	// (boolean) Using variable relative paths for the import file and folder dialog
	String IMPORT_FILES_AND_FOLDERS_RELATIVE = "IMPORT_FILES_AND_FOLDERS_RELATIVE"; //$NON-NLS-1$

	// (string) Save all dirty editors before running a full or incremental build
	String IMPORT_FILES_AND_FOLDERS_MODE = "IMPORT_FILES_AND_FOLDERS_MODE"; //$NON-NLS-1$

	// (string) Save all dirty editors before running a full or incremental build
	String IMPORT_FILES_AND_FOLDERS_VIRTUAL_FOLDER_MODE = "IMPORT_FILES_AND_FOLDERS_VIRTUAL_FOLDER_MODE"; //$NON-NLS-1$

	String IMPORT_FILES_AND_FOLDERS_MODE_PROMPT = MessageDialogWithToggle.PROMPT;

	String IMPORT_FILES_AND_FOLDERS_MODE_MOVE_COPY = "MOVE_COPY"; //$NON-NLS-1$

	String IMPORT_FILES_AND_FOLDERS_MODE_LINK = "LINK"; //$NON-NLS-1$

	String IMPORT_FILES_AND_FOLDERS_MODE_LINK_AND_VIRTUAL_FOLDER = "LINK_AND_VIRTUAL_FOLDER"; //$NON-NLS-1$

	// Always show this import window
	String IMPORT_FILES_AND_FOLDERS_SHOW_DIALOG = "IMPORT_FILES_AND_FOLDERS_SHOW_DIALOG"; //$NON-NLS-1$

	/**
	 * Workspace name, will be displayed in the window title.
	 */
	String WORKSPACE_NAME = "WORKSPACE_NAME"; //$NON-NLS-1$

	/**
	 * Whether to show the (workspace) location in the window title.
	 */
	String SHOW_LOCATION = "SHOW_LOCATION"; //$NON-NLS-1$

	/**
	 * Whether to show the workspace name in the window title.
	 */
	String SHOW_LOCATION_NAME = "SHOW_LOCATION_NAME"; //$NON-NLS-1$

	/**
	 * Whether to show the perspective name in the window title.
	 */
	String SHOW_PERSPECTIVE_IN_TITLE = "SHOW_PERSPECTIVE_IN_TITLE"; //$NON-NLS-1$

	/**
	 * Whether to show the product name in the window title.
	 */
	String SHOW_PRODUCT_IN_TITLE = "SHOW_PRODUCT_IN_TITLE"; //$NON-NLS-1$

	/**
	 * System explore command, used to launch file manager showing selected
	 * resource.
	 */
	String WORKBENCH_SYSTEM_EXPLORER = "SYSTEM_EXPLORER"; //$NON-NLS-1$

	/**
	 * Warn the user that the workspace is going to be upgraded because the IDE is newer
	 */
	String WARN_ABOUT_WORKSPACE_INCOMPATIBILITY = "WARN_ABOUT_WORKSPACE_INCOMPATIBILITY"; //$NON-NLS-1$

	/**
	 * Show Problems view decorations on startup
	 */
	String SHOW_PROBLEMS_VIEW_DECORATIONS_ON_STARTUP = "SHOW_PROBLEMS_VIEW_DECORATIONS_ON_STARTUP"; //$NON-NLS-1$

	String MAX_SIMULTANEOUS_BUILD = "MAX_CONCURRENT_PROJECT_BUILDS"; //$NON-NLS-1$

	int MAX_MAX_SIMULTANEOUS_BUILD = 1000;

	/**
	 * Key to allow products define initial default grouping for the Problems view
	 * family
	 */
	String INITIAL_DEFAULT_MARKER_GROUPING = "INITIAL_DEFAULT_MARKER_GROUPING"; //$NON-NLS-1$

	/**
	 * Key for preference whether the problem view does check if a help context is
	 * really available before annotating a marker icon with the question mark
	 * symbol.
	 *
	 * <p>
	 * See bug 545615
	 * </p>
	 *
	 * <p>
	 * The default is true.
	 * </p>
	 */
	String HELP_CONTEXT_AVAILABILITY_CHECK = "helpContextAvailabilityCheck"; //$NON-NLS-1$

}
