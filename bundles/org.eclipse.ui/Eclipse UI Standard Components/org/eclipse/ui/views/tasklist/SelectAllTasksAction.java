package org.eclipse.ui.views.tasklist;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.help.WorkbenchHelp;
 
/**
 * This action selects all tasks currently showing in the task list.
 */
/* package */ class SelectAllTasksAction extends TaskAction {
/**
 * Creates the action.
 */
protected SelectAllTasksAction(TaskList tasklist, String id) {
	super(tasklist, id);
	WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.SELECT_ALL_TASKS_ACTION);
}
/**
 * Selects all resources in the view.
 */
public void run() {
	getTaskList().cancelEditing();
	TableViewer viewer = getTaskList().getTableViewer();
	viewer.getTable().selectAll();
	// force viewer selection change
	viewer.setSelection(viewer.getSelection());
}
}
