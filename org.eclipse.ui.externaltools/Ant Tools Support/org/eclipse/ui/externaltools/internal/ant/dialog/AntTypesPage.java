package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ant.core.Type;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;

/**
 * Sub-page that allows the user to enter custom types
 * to be used when running Ant build files.
 */
public class AntTypesPage extends AntPage {
	private static final int ADD_TYPE_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_TYPE_BUTTON = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;

	private final AntTypesLabelProvider labelProvider = new AntTypesLabelProvider();
	private Button editButton;
	private Button removeButton;

	/**
	 * Creates an instance.
	 */
	public AntTypesPage(AntPreferencePage preferencePage) {
		super(preferencePage);
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		createButton(parent, "AntTypesPage.addTypeButtonTitle", ADD_TYPE_BUTTON); //$NON-NLS-1$
		editButton = createButton(parent, "AntTypesPage.editTypeButtonTitle", EDIT_TYPE_BUTTON); //$NON-NLS-1$
		createSeparator(parent);
		removeButton = createButton(parent, "AntTypesPage.removeButtonTitle", REMOVE_BUTTON); //$NON-NLS-1$
	}
	
	/**
	 * Allows the user to enter a custom type.
	 */
	private void addTypeButtonPressed() {
		String title = ToolMessages.getString("AntTypesPage.addTypeDialogTitle"); //$NON-NLS-1$
		String msg = ToolMessages.getString("AntTypesPage.addTypeDialogDescription"); //$NON-NLS-1$
		AddTaskDialog dialog = new AddTaskDialog(getShell(), title, msg);
		if (dialog.open() == Dialog.CANCEL)
			return;

		Type type = new Type();
		type.setTypeName(dialog.getTaskName());
		type.setClassName(dialog.getClassName());
		type.setLibrary(dialog.getLibrary());
		addContent(type);
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
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
	
	/**
	 * Creates the tab item that contains this sub-page.
	 */
	public TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(ToolMessages.getString("AntTypesPage.typesPageTitle")); //$NON-NLS-1$
		item.setImage(labelProvider.getTypeImage());
		item.setData(this);
		item.setControl(createControl(folder));
		return item;
	}
	
	/**
	 * Allows the user to edit a custom Ant type.
	 */	
	private void editTypeButtonPressed() {
		String title = ToolMessages.getString("AntTypesPage.editTypeDialogTitle"); //$NON-NLS-1$
		String msg = ToolMessages.getString("AntTypesPage.editTypeDialogDescription"); //$NON-NLS-1$
		AddTaskDialog dialog = new AddTaskDialog(getShell(), title, msg);
		Type type = (Type) getSelectedElement();
		if (type == null)
			return;
		dialog.setClassName(type.getClassName());
		dialog.setTaskName(type.getTypeName());
		dialog.setLibrary(type.getLibrary());
		if (dialog.open() == Dialog.CANCEL)
			return;

		type.setTypeName(dialog.getTaskName());
		type.setClassName(dialog.getClassName());
		type.setLibrary(dialog.getLibrary());
		updateContent(type);
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
	 * Label provider for type elements
	 */
	private static final class AntTypesLabelProvider extends LabelProvider implements ITableLabelProvider {
		private static final String IMG_TYPE = "icons/full/obj16/type.gif"; //$NON-NLS-1$;
		private Image typeImage;
	
		/**
		 * Creates an instance.
		 */
		public AntTypesLabelProvider() {
		}
		
		/* (non-Javadoc)
		 * Method declared on IBaseLabelProvider.
		 */
		public void dispose() {
			if (typeImage != null) {
				typeImage.dispose();
				typeImage = null;
			}
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return getTypeImage();
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public String getColumnText(Object element, int columnIndex) {
			Type type = (Type) element;
			return type.getTypeName() + " (" + type.getLibrary().getFile() + ": " + type.getClassName() + ")"; //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
		}
		
		public Image getTypeImage() {
			if (typeImage == null) {
				ImageDescriptor desc = ExternalToolsPlugin.getDefault().getImageDescriptor(IMG_TYPE);
				typeImage = desc.createImage();
			}
			return typeImage;
		}
	}
}