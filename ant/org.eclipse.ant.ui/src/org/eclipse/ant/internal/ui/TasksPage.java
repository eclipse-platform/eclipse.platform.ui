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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A widget group for the tasks tab of the ant classpath preference page.
 */
public class TasksPage extends CustomizeAntPage {
	protected Control control;

	//button constants
	protected static final int ADD_TASK_BUTTON= IDialogConstants.CLIENT_ID + 1;
	protected static final int EDIT_TASK_BUTTON = IDialogConstants.CLIENT_ID + 2;
	protected static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;

public TasksPage() {
}
protected void addButtonsToButtonGroup(Composite parent) {
	createButton(parent, "preferences.customize.addTaskButtonTitle", ADD_TASK_BUTTON);
	createButton(parent, "preferences.customize.editTaskButtonTitle", EDIT_TASK_BUTTON);
	createSeparator(parent);
	createButton(parent, "preferences.customize.removeButtonTitle", REMOVE_BUTTON);
}
protected void buttonPressed(int buttonID) {
	super.buttonPressed(buttonID);
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
}