package org.eclipse.ui.views.tasklist;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the task list view.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
/*package*/ interface ITaskListHelpContextIds {
	public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

	// Actions
	public static final String PURGE_COMPLETED_TASK_ACTION = PREFIX + "purge_completed_task_action_context"; //$NON-NLS-1$
	public static final String COPY_TASK_ACTION = PREFIX + "copy_task_action_context"; //$NON-NLS-1$
	public static final String PASTE_TASK_ACTION = PREFIX + "paste_task_action_context"; //$NON-NLS-1$
	public static final String NEW_TASK_ACTION = PREFIX + "new_task_action_context"; //$NON-NLS-1$
	public static final String REMOVE_TASK_ACTION = PREFIX + "remove_task_action_context"; //$NON-NLS-1$
	public static final String GOTO_TASK_ACTION = PREFIX + "goto_task_action_context"; //$NON-NLS-1$
	public static final String FILTERS_ACTION = PREFIX + "filters_action_context"; //$NON-NLS-1$
	public static final String MARK_COMPLETED_ACTION = PREFIX + "mark_completed_action_context"; //$NON-NLS-1$
	public static final String RESOLVE_MARKER_ACTION = PREFIX + "resolve_marker_action_context"; //$NON-NLS-1$
	public static final String SELECT_ALL_TASKS_ACTION = PREFIX + "select_all_tasks_action_context"; //$NON-NLS-1$
	public static final String TASK_PROPERTIES_ACTION = PREFIX + "task_properties_action_context"; //$NON-NLS-1$

	// Dialogs
	public static final String FILTERS_DIALOG = PREFIX + "task_filters_dialog_context"; //$NON-NLS-1$
	public static final String PROPERTIES_DIALOG = PREFIX + "task_properties_dialog_context"; //$NON-NLS-1$
	
	// Views
	public static final String TASK_LIST_VIEW = PREFIX + "task_list_view_context"; //$NON-NLS-1$
}
