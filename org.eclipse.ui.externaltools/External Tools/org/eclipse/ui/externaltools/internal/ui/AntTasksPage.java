package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.ant.internal.core.Task;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;

import org.eclipse.jface.dialogs.Dialog;

/**
 * A widget group for the tasks tab of the ant classpath preference page.
 */
public class AntTasksPage extends AntPage {
	private Button editButton;
	private Button removeButton;

	//button constants
	private static final int ADD_TASK_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_TASK_BUTTON =
		IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;

	public AntTasksPage() {
	}
	protected void addButtonsToButtonGroup(Composite parent) {
		createButton(parent, "AntTasksPage.addTaskButtonTitle", ADD_TASK_BUTTON); //$NON-NLS-1$
		editButton = createButton(parent, "AntTasksPage.editTaskButtonTitle", EDIT_TASK_BUTTON); //$NON-NLS-1$
		createSeparator(parent);
		removeButton = createButton(parent, "AntTasksPage.removeButtonTitle", REMOVE_BUTTON); //$NON-NLS-1$
	}
	private void addTaskButtonPressed() {
		String title = ToolMessages.getString("AntTasksPage.addTaskDialogTitle"); //$NON-NLS-1$
		String msg = ToolMessages.getString("AntTasksPage.addTaskDialogDescription"); //$NON-NLS-1$
		AddTaskDialog dialog =
			new AddTaskDialog(tableViewer.getControl().getShell(), title, msg);
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
			case ADD_TASK_BUTTON :
				addTaskButtonPressed();
				break;
			case EDIT_TASK_BUTTON :
				editTaskButtonPressed();
				break;
			case REMOVE_BUTTON :
				removeButtonPressed();
				break;
		}
	}
	/**
	 * Creates and returns a tab item that contains this widget group.
	 */
	public TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(ToolMessages.getString("AntTasksPage.title")); //$NON-NLS-1$
		item.setImage(
			PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_TASK_TSK));
		item.setData(this);
		item.setControl(createControl(folder));
		return item;
	}
	private void editTaskButtonPressed() {
		String title = ToolMessages.getString("AntTasksPage.editTaskDialogTitle"); //$NON-NLS-1$
		String msg = ToolMessages.getString("AntTasksPage.editTaskDialogDescription"); //$NON-NLS-1$
		AddTaskDialog dialog =
			new AddTaskDialog(tableViewer.getControl().getShell(), title, msg);
		IStructuredSelection selection =
			(IStructuredSelection) tableViewer.getSelection();
		if (selection.isEmpty())
			return;
		Task task = (Task) selection.getFirstElement();
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