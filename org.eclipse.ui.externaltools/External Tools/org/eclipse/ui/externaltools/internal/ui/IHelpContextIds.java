package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
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
	public static final String ANT_ACTION = PREFIX + "ant_action_context"; //$NON-NLS-1$
	
	// Dialogs
	public static final String CONFIGURE_DIALOG = PREFIX + "configure_dialog_context"; //$NON-NLS-1$
	public static final String EDIT_DIALOG = PREFIX + "edit_dialog_context"; //$NON-NLS-1$
	public static final String ADD_TASK_DIALOG = PREFIX + "add_task_dialog_context"; //$NON-NLS-1$
	public static final String PROJECT_SELECTION_DIALOG = PREFIX + "project_selection_dialog_context"; //$NON-NLS-1$
	public static final String REFRESH_SELECTION_DIALOG = PREFIX + "refresh_selection_dialog_context"; //$NON-NLS-1$
	public static final String TARGET_SELECTION_DIALOG = PREFIX + "target_selection_dialog_context"; //$NON-NLS-1$
	public static final String VARIABLE_SELECTION_DIALOG = PREFIX + "variable_selection_dialog_context"; //$NON-NLS-1$
	public static final String RESOURCE_SELECTION_DIALOG = PREFIX + "resource_selection_dialog_context"; //$NON-NLS-1$

	// Preference Pages
	public static final String ANT_PREFERENCE_PAGE = PREFIX + "ant_preference_page_context"; //$NON-NLS-1$
	public static final String LOG_CONSOLE_PREFERENCE_PAGE = PREFIX + "log_console_preference_page_context"; //$NON-NLS-1$
	
	// Views
	public static final String LOG_CONSOLE_VIEW = PREFIX + "log_console_view_context"; //$NON-NLS-1$

	// Wizards
	public static final String ANT_LAUNCH_WIZARD = PREFIX + "ant_launch_wizard_context"; //$NON-NLS-1$
	
	// Wizard Pages
	public static final String ANT_LAUNCH_WIZARD_PAGE = PREFIX + "ant_launch_wizard_page_context"; //$NON-NLS-1$
	
}
