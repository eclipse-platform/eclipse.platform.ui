package org.eclipse.ui.views.tasklist;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This action opens the properties dialog for the current task.
 */
/* package */ class TaskPropertiesAction extends TaskAction {
	
/**
 * Creates the action.
 */
public TaskPropertiesAction(TaskList tasklist, String id) {
	super(tasklist, id);
	WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.TASK_PROPERTIES_ACTION);
}

/**
 * Performs this action.
 */
public void run() {
	IStructuredSelection sel = (IStructuredSelection) getTaskList().getSelection();
	Object o = sel.getFirstElement();
	if (o instanceof IMarker) {
		TaskPropertiesDialog dialog = new TaskPropertiesDialog(getShell());
		dialog.setMarker((IMarker) o);
		dialog.open();
	}
}

}
