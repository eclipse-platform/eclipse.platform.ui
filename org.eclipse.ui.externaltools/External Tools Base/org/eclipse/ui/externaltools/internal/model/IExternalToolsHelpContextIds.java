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
	public static final String TARGET_MOVE_UP_ACTION = PREFIX + "target_move_up_action_context"; //$NON-NLS-1$
	public static final String TARGET_MOVE_DOWN_ACTION = PREFIX + "target_move_down_action_context"; //$NON-NLS-1$
	public static final String SEARCH_FOR_BUILDFILES_ACTION = PREFIX + "search_for_buildfiles_action_context"; //$NON-NLS-1$

	public static final String EDIT_LAUNCH_CONFIGURATION_ACTION = PREFIX + "edit_launch_configuration_action_context"; //$NON-NLS-1$
	public static final String RUN_TARGET_ACTION = PREFIX + "run_target_action_context"; //$NON-NLS-1$
	public static final String REMOVE_PROJECT_ACTION = PREFIX + "remove_project_action_context"; //$NON-NLS-1$
	public static final String ADD_BUILDFILE_ACTION = PREFIX + "add_buildfile_action_context"; //$NON-NLS-1$
	public static final String REFRESH_BUILDFILE_ACTION = PREFIX + "refresh_buildfile_action_context"; //$NON-NLS-1$

	// Dialogs
	public static final String FILE_SELECTION_DIALOG = PREFIX + "file_selection_dialog_context"; //$NON-NLS-1$
	public static final String ADD_CUSTOM_DIALOG = PREFIX + "add_custom_dialog_context"; //$NON-NLS-1$
	public static final String ADD_PROPERTY_DIALOG = PREFIX + "add_property_dialog_context"; //$NON-NLS-1$
	public static final String SEARCH_FOR_BUILDFILES_DIALOG = PREFIX + "search_for_buildfiles_dialog_context"; //$NON-NLS-1$
	public static final String STATUS_DIALOG = PREFIX + "status_dialog_context"; //$NON-NLS-1$
	public static final String MESSAGE_WITH_TOGGLE_DIALOG = PREFIX + "message_with_toggle_dialog_context"; //$NON-NLS-1$
	public static final String TARGET_ORDER_DIALOG = PREFIX + "target_order_dialog_context"; //$NON-NLS-1$
	
	// Preference Pages
	public static final String EXTERNAL_TOOLS_PREFERENCE_PAGE = PREFIX + "preference_page_context";  //$NON-NLS-1$
		
	// Property Pages
	public static final String EXTERNAL_TOOLS_BUILDER_PROPERTY_PAGE = PREFIX + "builder_property_page_context"; //$NON-NLS-1$

	//Launch configuration dialog tabs
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_BUILDER_TAB = PREFIX + "builders_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_PROGRAM_MAIN_TAB = PREFIX + "program_main_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_REFRESH_TAB = PREFIX + "refresh_tab_context"; //$NON-NLS-1$
}
