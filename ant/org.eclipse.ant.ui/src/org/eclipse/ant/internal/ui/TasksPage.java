/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.ant.internal.ui;

import org.eclipse.ant.internal.core.Task;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A widget group for the tasks tab of the ant classpath preference page.
 */
public class TasksPage extends CustomizeAntPage {
	protected Button editButton;
	protected Button removeButton;

	//button constants
	protected static final int ADD_TASK_BUTTON= IDialogConstants.CLIENT_ID + 1;
	protected static final int EDIT_TASK_BUTTON = IDialogConstants.CLIENT_ID + 2;
	protected static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;

public TasksPage() {
}
protected void addButtonsToButtonGroup(Composite parent) {
	createButton(parent, "preferences.customize.addTaskButtonTitle", ADD_TASK_BUTTON);
	editButton = createButton(parent, "preferences.customize.editTaskButtonTitle", EDIT_TASK_BUTTON);
	createSeparator(parent);
	removeButton = createButton(parent, "preferences.customize.removeButtonTitle", REMOVE_BUTTON);
}
protected void addTaskButtonPressed() {
	String title = Policy.bind("preferences.customize.addTaskDialogTitle");
	String msg = Policy.bind("preferences.customize.addTaskDialogDescription");
	AddTaskDialog dialog = new AddTaskDialog(tableViewer.getControl().getShell(), title, msg);
	if (dialog.open() == Dialog.CANCEL)
		return;
		
	Task task = new Task();
	task.setTaskName(dialog.getTaskName());
	task.setClassName(dialog.getClassName());
	task.setLibrary(dialog.getLibrary());
	contentProvider.add(task);	
}
protected void buttonPressed(int buttonId) {
	switch (buttonId) {
		case ADD_TASK_BUTTON:
			addTaskButtonPressed();
			break;
		case EDIT_TASK_BUTTON:
			editTaskButtonPressed();
			break;
		case REMOVE_BUTTON:
			removeButtonPressed();
			break;
	}
}
/**
 * Creates and returns a tab item that contains this widget group.
 */
public TabItem createTabItem(TabFolder folder) {
	TabItem item = new TabItem(folder, SWT.NONE);
	item.setText(Policy.bind("preferences.customize.tasksPageTitle"));
	item.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_TASK_TSK));
	item.setData(this);
	item.setControl(createControl(folder));
	return item;
}
protected void editTaskButtonPressed() {
	String title = Policy.bind("preferences.customize.editTaskDialogTitle");
	String msg = Policy.bind("preferences.customize.editTaskDialogDescription");
	AddTaskDialog dialog = new AddTaskDialog(tableViewer.getControl().getShell(), title, msg);
	IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
	if (selection.isEmpty())
		return;
	Task task = (Task)selection.getFirstElement();
	dialog.setClassName(task.getClassName());
	dialog.setTaskName(task.getTaskName());
	dialog.setLibrary(task.getLibrary());
	if (dialog.open() == Dialog.CANCEL)
		return;
		
	task.setTaskName(dialog.getTaskName());
	task.setClassName(dialog.getClassName());
	task.setLibrary(dialog.getLibrary());
	tableViewer.update(task, null);
}
/**
 * @see CustomizeAntPage#tableSelectionChanged(ISelection)
 */
protected void tableSelectionChanged(IStructuredSelection newSelection) {
	int size = newSelection.size();
	editButton.setEnabled(size == 1);
	removeButton.setEnabled(size > 0);
}

}