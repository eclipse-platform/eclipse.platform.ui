package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

/**
 * Help context ids for the external tools.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 */
public interface IHelpContextIds {
	public static final String PREFIX = "org.eclipse.ui.externaltools."; //$NON-NLS-1$

	// Actions
	public static final String NEW_TOOL_ACTION = PREFIX + "new_tool_action_context"; //$NON-NLS-1$
	public static final String DUPLICATE_TOOL_ACTION = PREFIX + "duplicate_tool_action_context"; //$NON-NLS-1$
	public static final String DELETE_TOOL_ACTION = PREFIX + "delete_tool_action_context"; //$NON-NLS-1$
	public static final String RENAME_TOOL_ACTION = PREFIX + "rename_tool_action_context"; //$NON-NLS-1$
	public static final String REFRESH_VIEW_ACTION = PREFIX + "refresh_view_action_context"; //$NON-NLS-1$
	public static final String RUN_TOOL_ACTION = PREFIX + "run_tool_action_context"; //$NON-NLS-1$
	public static final String RUN_WITH_TOOL_ACTION = PREFIX + "run_with_tool_action_context"; //$NON-NLS-1$
	public static final String EDIT_TOOL_PROPERTIES_ACTION = PREFIX + "edit_tool_properties_action_context"; //$NON-NLS-1$
	public static final String ANT_ACTION = PREFIX + "ant_action_context"; //$NON-NLS-1$
	
	// Dialogs
	public static final String RESOURCE_SELECTION_DIALOG = PREFIX + "resource_selection_dialog_context"; //$NON-NLS-1$

	// Preference Pages
	public static final String ANT_PREFERENCE_PAGE = PREFIX + "ant_preference_page_context"; //$NON-NLS-1$
	public static final String ADD_TASK_DIALOG = PREFIX + "add_task_dialog_context"; //$NON-NLS-1$

	// Property Pages
	public static final String TOOL_MAIN_PROPERTY_PAGE = PREFIX + "tool_main_property_page_context"; //$NON-NLS-1$
	public static final String TOOL_OPTION_PROPERTY_PAGE = PREFIX + "tool_option_property_page_context"; //$NON-NLS-1$
	public static final String TOOL_REFRESH_PROPERTY_PAGE = PREFIX + "tool_refresh_property_page_context"; //$NON-NLS-1$
	public static final String ANT_TARGETS_PROPERTY_PAGE = PREFIX + "ant_targets_property_page_context"; //$NON-NLS-1$
	
	// Views
	public static final String EXTERNAL_TOOLS_VIEW = PREFIX + "external_tools_view_context"; //$NON-NLS-1$

	// Wizards
	public static final String ANT_LAUNCH_WIZARD = PREFIX + "ant_launch_wizard_context"; //$NON-NLS-1$
	
	// Wizard Pages
	public static final String TOOL_MAIN_WIZARD_PAGE = PREFIX + "tool_main_wizard_page_context"; //$NON-NLS-1$
	public static final String TOOL_OPTION_WIZARD_PAGE = PREFIX + "tool_option_wizard_page_context"; //$NON-NLS-1$
	public static final String TOOL_REFRESH_WIZARD_PAGE = PREFIX + "tool_refresh_wizard_page_context"; //$NON-NLS-1$
	public static final String ANT_TARGETS_WIZARD_PAGE = PREFIX + "ant_targets_wizard_page_context"; //$NON-NLS-1$
	public static final String ANT_LAUNCH_WIZARD_PAGE = PREFIX + "ant_launch_wizard_page_context"; //$NON-NLS-1$
}
