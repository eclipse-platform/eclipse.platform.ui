package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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

	// Dialogs
	public static final String FILTERS_DIALOG = PREFIX + "task_filters_dialog_context"; //$NON-NLS-1$
	public static final String PROPERTIES_DIALOG = PREFIX + "task_properties_dialog_context"; //$NON-NLS-1$
	
	// Views
	public static final String TASK_LIST_VIEW = PREFIX + "task_list_view_context"; //$NON-NLS-1$
}
