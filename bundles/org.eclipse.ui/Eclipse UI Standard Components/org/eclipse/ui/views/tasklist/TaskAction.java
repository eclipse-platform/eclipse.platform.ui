package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * This is the base class of all the local actions used
 * in the task list view.
 */
/* package */ abstract class TaskAction extends Action {
	private TaskList taskList;
/**
 * TaskAction constructor.
 */
protected TaskAction(TaskList tasklist, String id) {
	super();
	this.taskList = tasklist;
	setId(id);
}
/**
 * Returns the shell to use within actions.
 */
protected Shell getShell() {
	return taskList.getSite().getShell();
}
/**
 * Returns the task list viewer.
 */
protected TaskList getTaskList() {
	return taskList;
}
/**
 * Stores the current state value of this toggle action
 * into the dialog store using action ID as a key.
 */
protected void storeValue() {
	IDialogSettings workbenchSettings = TaskList.getPlugin().getDialogSettings();
	IDialogSettings settings = workbenchSettings.getSection("TaskAction");//$NON-NLS-1$
	if(settings == null)
		settings = workbenchSettings.addNewSection("TaskAction");//$NON-NLS-1$
	settings.put(getId(), isChecked());
}
}
