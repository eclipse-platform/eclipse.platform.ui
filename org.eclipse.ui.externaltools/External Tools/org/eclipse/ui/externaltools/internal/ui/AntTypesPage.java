package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.ant.internal.core.Type;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.externaltools.internal.core.*;

import org.eclipse.jface.dialogs.Dialog;

/**
 * A widget group for the types tab of the ant classpath preference page.
 */
public class AntTypesPage extends AntPage {
	private Button editButton;
	private Button removeButton;

	//button constants
	private static final int ADD_TYPE_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_TYPE_BUTTON =
		IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;

	public AntTypesPage() {
	}
	protected void addButtonsToButtonGroup(Composite parent) {
		createButton(parent, "AntTypesPage.addTypeButtonTitle", ADD_TYPE_BUTTON); //$NON-NLS-1$
		editButton = createButton(parent, "AntTypesPage.editTypeButtonTitle", EDIT_TYPE_BUTTON); //$NON-NLS-1$
		createSeparator(parent);
		removeButton = createButton(parent, "AntTypesPage.removeButtonTitle", REMOVE_BUTTON); //$NON-NLS-1$
	}
	private void addTypeButtonPressed() {
		String title = ToolMessages.getString("AntTypesPage.addTypeDialogTitle"); //$NON-NLS-1$
		String msg = ToolMessages.getString("AntTypesPage.addTypeDialogDescription"); //$NON-NLS-1$
		AddTaskDialog dialog =
			new AddTaskDialog(tableViewer.getControl().getShell(), title, msg);
		if (dialog.open() == Dialog.CANCEL)
			return;

		Type type = new Type();
		type.setTypeName(dialog.getTaskName());
		type.setClassName(dialog.getClassName());
		type.setLibrary(dialog.getLibrary());
		contentProvider.add(type);
	}
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case ADD_TYPE_BUTTON :
				addTypeButtonPressed();
				break;
			case EDIT_TYPE_BUTTON :
				editTypeButtonPressed();
				break;
			case REMOVE_BUTTON :
				removeButtonPressed();
				break;
		}
	}
	public TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(ToolMessages.getString("AntTypesPage.typesPageTitle")); //$NON-NLS-1$
		final Image image =
			ExternalToolsPlugin
				.getDefault()
				.getImageDescriptor(ExternalToolsPlugin.IMG_TYPE)
				.createImage();
		item.setImage(image);
		item.setData(this);
		item.setControl(createControl(folder));
		item.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (image != null)
					image.dispose();
			}
		});
		return item;
	}
	private void editTypeButtonPressed() {
		String title = ToolMessages.getString("AntTypesPage.editTypeDialogTitle"); //$NON-NLS-1$
		String msg = ToolMessages.getString("AntTypesPage.editTypeDialogDescription"); //$NON-NLS-1$
		AddTaskDialog dialog =
			new AddTaskDialog(tableViewer.getControl().getShell(), title, msg);
		IStructuredSelection selection =
			(IStructuredSelection) tableViewer.getSelection();
		if (selection.isEmpty())
			return;
		Type type = (Type) selection.getFirstElement();
		dialog.setClassName(type.getClassName());
		dialog.setTaskName(type.getTypeName());
		dialog.setLibrary(type.getLibrary());
		if (dialog.open() == Dialog.CANCEL)
			return;

		type.setTypeName(dialog.getTaskName());
		type.setClassName(dialog.getClassName());
		type.setLibrary(dialog.getLibrary());
		tableViewer.update(type, null);
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