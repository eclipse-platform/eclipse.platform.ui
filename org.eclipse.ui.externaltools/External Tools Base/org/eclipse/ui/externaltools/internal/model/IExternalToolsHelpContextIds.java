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
package org.eclipse.ui.externaltools.internal.model;


/**
 * Help context ids for the external tools.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 */
public interface IExternalToolsHelpContextIds {
	public static final String PREFIX = "org.eclipse.ui.externaltools."; //$NON-NLS-1$

	// Actions
	public static final String REMOVE_ALL_ACTION = PREFIX + "remove_all_action_context"; //$NON-NLS-1$
	public static final String SEARCH_FOR_BUILDFILES_ACTION = PREFIX + "search_for_buildfiles_action_context"; //$NON-NLS-1$

	public static final String EDIT_LAUNCH_CONFIGURATION_ACTION = PREFIX + "edit_launch_configuration_action_context"; //$NON-NLS-1$
	public static final String RUN_TARGET_ACTION = PREFIX + "run_target_action_context"; //$NON-NLS-1$
	public static final String REMOVE_PROJECT_ACTION = PREFIX + "remove_project_action_context"; //$NON-NLS-1$
	public static final String ADD_BUILDFILE_ACTION = PREFIX + "add_buildfile_action_context"; //$NON-NLS-1$

	// Dialogs
	public static final String FILE_SELECTION_DIALOG = PREFIX + "file_selection_dialog_context"; //$NON-NLS-1$
	public static final String ADD_CUSTOM_DIALOG = PREFIX + "add_custom_dialog_context"; //$NON-NLS-1$
	public static final String ADD_PROPERTY_DIALOG = PREFIX + "add_property_dialog_context"; //$NON-NLS-1$
	public static final String SEARCH_FOR_BUILDFILES_DIALOG = PREFIX + "search_for_buildfiles_dialog_context"; //$NON-NLS-1$
	public static final String STATUS_DIALOG = PREFIX + "status_dialog_context"; //$NON-NLS-1$
	public static final String MESSAGE_WITH_TOGGLE_DIALOG = PREFIX + "message_with_toggle_dialog_context"; //$NON-NLS-1$
	public static final String TARGET_ORDER_DIALOG = PREFIX + "target_order_dialog_context"; //$NON-NLS-1$
	public static final String VARIABLE_SELECTION_DIALOG = PREFIX + "variable_selection_dialog_context"; //$NON-NLS-1$
	
	// Preference Pages
	public static final String ANT_PREFERENCE_PAGE = PREFIX + "ant_preference_page_context"; //$NON-NLS-1$
	public static final String ANT_RUNTIME_PREFERENCE_PAGE = PREFIX + "ant_runtime_preference_page_context"; //$NON-NLS-1$
	public static final String ANT_EDITOR_PREFERENCE_PAGE = PREFIX + "ant_editor_preference_page_context"; //$NON-NLS-1$
	public static final String ANT_TYPES_PAGE = PREFIX + "ant_types_page_context"; //$NON-NLS-1$
	public static final String ANT_CLASSPATH_PAGE = PREFIX + "ant_classpath_page_context"; //$NON-NLS-1$
	
	public static final String ANT_PROPERTIES_PAGE = PREFIX + "ant_properties_page_context"; //$NON-NLS-1$
	public static final String ANT_TASKS_PAGE = PREFIX + "ant_tasks_page_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_PREFERENCE_PAGE = PREFIX + "preference_page_context";  //$NON-NLS-1$
		
	// Property Pages
	public static final String EXTERNAL_TOOLS_BUILDER_PROPERTY_PAGE = PREFIX + "builder_property_page_context"; //$NON-NLS-1$
	
	// Views
	public static final String ANT_VIEW = PREFIX + "ant_view_context"; //$NON-NLS-1$

	//Launch configuration dialog tabs
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_ANT_MAIN_TAB = PREFIX + "ant_main_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_ANT_TARGETS_TAB = PREFIX + "ant_targets_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_ANT_PROPERTIES_TAB = PREFIX + "ant_properties_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_ANT_CLASSPATH_TAB = PREFIX + "ant_classpath_tab_context"; //$NON-NLS-1$
	
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_BUILDER_TAB = PREFIX + "builders_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_PROGRAM_MAIN_TAB = PREFIX + "program_main_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_REFRESH_TAB = PREFIX + "refresh_tab_context"; //$NON-NLS-1$
}
