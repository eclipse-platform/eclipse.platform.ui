package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ant.core.Task;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;

/**
 * Sub-page that allows the user to enter custom tasks
 * to be used when running Ant build files.
 */
public class AntTasksPage extends AntPage {
	private static final int ADD_TASK_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_TASK_BUTTON = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;

	private final AntTasksLabelProvider labelProvider = new AntTasksLabelProvider();
	private Button editButton;
	private Button removeButton;

	/**
	 * Creates an instance.
	 */
	public AntTasksPage(AntPreferencePage preferencePage) {
		super(preferencePage);
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		createButton(parent, "AntTasksPage.addTaskButtonTitle", ADD_TASK_BUTTON); //$NON-NLS-1$
		editButton = createButton(parent, "AntTasksPage.editTaskButtonTitle", EDIT_TASK_BUTTON); //$NON-NLS-1$
		createSeparator(parent);
		removeButton = createButton(parent, "AntTasksPage.removeButtonTitle", REMOVE_BUTTON); //$NON-NLS-1$
	}
	
	/**
	 * Allows the user to enter a custom task.
	 */
	private void addTaskButtonPressed() {
		String title = ToolMessages.getString("AntTasksPage.addTaskDialogTitle"); //$NON-NLS-1$
		String msg = ToolMessages.getString("AntTasksPage.addTaskDialogDescription"); //$NON-NLS-1$
		AddTaskDialog dialog = new AddTaskDialog(getShell(), title, msg);
		if (dialog.open() == Dialog.CANCEL)
			return;

		Task task = new Task();
		task.setTaskName(dialog.getTaskName());
		task.setClassName(dialog.getClassName());
		task.setLibrary(dialog.getLibrary());
		addContent(task);
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
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
	 * Creates the tab item that contains this sub-page.
	 */
	public TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(ToolMessages.getString("AntTasksPage.title")); //$NON-NLS-1$
		item.setImage(labelProvider.getTaskImage());
		item.setData(this);
		item.setControl(createControl(folder));
		return item;
	}

	/**
	 * Allows the user to edit a custom Ant task.
	 */	
	private void editTaskButtonPressed() {
		String title = ToolMessages.getString("AntTasksPage.editTaskDialogTitle"); //$NON-NLS-1$
		String msg = ToolMessages.getString("AntTasksPage.editTaskDialogDescription"); //$NON-NLS-1$
		AddTaskDialog dialog = new AddTaskDialog(getShell(), title, msg);
		Task task = (Task) getSelectedElement();
		if (task == null)
			return;
		dialog.setClassName(task.getClassName());
		dialog.setTaskName(task.getTaskName());
		dialog.setLibrary(task.getLibrary());
		if (dialog.open() == Dialog.CANCEL)
			return;

		task.setTaskName(dialog.getTaskName());
		task.setClassName(dialog.getClassName());
		task.setLibrary(dialog.getLibrary());
		updateContent(task);
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected ITableLabelProvider getLabelProvider() {
		return labelProvider;
	}

	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void tableSelectionChanged(IStructuredSelection newSelection) {
		int size = newSelection.size();
		editButton.setEnabled(size == 1);
		removeButton.setEnabled(size > 0);
	}
	
	
	/**
	 * Label provider for task elements
	 */
	private static final class AntTasksLabelProvider extends LabelProvider implements ITableLabelProvider {
		private Image taskImage;
	
		/**
		 * Creates an instance.
		 */
		public AntTasksLabelProvider() {
		}
		
		/* (non-Javadoc)
		 * Method declared on IBaseLabelProvider.
		 */
		public void dispose() {
			// Task image is shared, do not dispose.
			taskImage = null;
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return getTaskImage();
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public String getColumnText(Object element, int columnIndex) {
			Task task = (Task) element;
			return task.getTaskName() + " (" + task.getLibrary().getFile() + ": " + task.getClassName() + ")"; //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
		}
		
		public Image getTaskImage() {
			if (taskImage == null)
				taskImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_TASK_TSK);
			return taskImage;
		}
	}
}