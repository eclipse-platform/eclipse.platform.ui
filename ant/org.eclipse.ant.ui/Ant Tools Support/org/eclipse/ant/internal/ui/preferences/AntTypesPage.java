/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.preferences;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.core.Type;
import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.ant.internal.ui.model.IAntUIHelpContextIds;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Sub-page that allows the user to enter custom types
 * to be used when running Ant build files.
 */
public class AntTypesPage extends AntPage {
	private static final int ADD_TYPE_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_TYPE_BUTTON = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;

	private final AntTypesLabelProvider labelProvider = new AntTypesLabelProvider();
	/**
	 * Creates an instance.
	 */
	public AntTypesPage(AntRuntimePreferencePage preferencePage) {
		super(preferencePage);
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		createPushButton(parent, AntPreferencesMessages.getString("AntTypesPage.2"), ADD_TYPE_BUTTON); //$NON-NLS-1$
		editButton = createPushButton(parent, AntPreferencesMessages.getString("AntTypesPage.3"), EDIT_TYPE_BUTTON); //$NON-NLS-1$
		createSeparator(parent);
		removeButton = createPushButton(parent, AntPreferencesMessages.getString("AntTypesPage.1"), REMOVE_BUTTON); //$NON-NLS-1$
	}
	
	/**
	 * Allows the user to enter a custom type.
	 */
	private void addType() {
		String title = AntPreferencesMessages.getString("AntTypesPage.addTypeDialogTitle"); //$NON-NLS-1$
		AddCustomDialog dialog = getCustomDialog(title, IAntUIHelpContextIds.ADD_TYPE_DIALOG);
		if (dialog.open() == Window.CANCEL) {
			return;
		}

		Type type = new Type();
		type.setTypeName(dialog.getName());
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
				addType();
				break;
			case EDIT_TYPE_BUTTON :
				edit(getSelection());
				break;
			case REMOVE_BUTTON :
				remove();
				break;
		}
	}
	
	/**
	 * Creates the tab item that contains this sub-page.
	 */
	protected TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntTypesPage.typesPageTitle")); //$NON-NLS-1$
		item.setImage(labelProvider.getTypeImage());
		item.setData(this);
		Composite top = new Composite(folder, SWT.NONE);
		top.setFont(folder.getFont());			
		item.setControl(createContents(top));
		
		connectToFolder(item, folder);
				
		return item;
	}
	
	/**
	 * Allows the user to edit a custom Ant type.
	 */	
	protected void edit(IStructuredSelection selection) {
		Type type = (Type) selection.getFirstElement();
		String title = AntPreferencesMessages.getString("AntTypesPage.editTypeDialogTitle"); //$NON-NLS-1$
		AddCustomDialog dialog = getCustomDialog(title, IAntUIHelpContextIds.EDIT_TYPE_DIALOG);
		dialog.setClassName(type.getClassName());
		dialog.setName(type.getTypeName());
		dialog.setLibrary(type.getLibrary());
		if (dialog.open() == Window.CANCEL) {
			return;
		}

		type.setTypeName(dialog.getName());
		type.setClassName(dialog.getClassName());
		type.setLibrary(dialog.getLibrary());
		updateContent(type);
	}
	
	private AddCustomDialog getCustomDialog(String title, String helpContext) {
		Iterator types= getContents(true).iterator();
		List names= new ArrayList();
		while (types.hasNext()) {
			Type aTask = (Type) types.next();
			names.add(aTask.getTypeName());
		}
		
		AddCustomDialog dialog = new AddCustomDialog(getShell(), getPreferencePage().getLibraryURLs(), names, helpContext);
		dialog.setTitle(title);
		dialog.setAlreadyExistsErrorMsg(AntPreferencesMessages.getString("AntTypesPage.8")); //$NON-NLS-1$
		dialog.setNoNameErrorMsg(AntPreferencesMessages.getString("AntTypesPage.9")); //$NON-NLS-1$
		return dialog;
	}

	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected ITableLabelProvider getLabelProvider() {
		return labelProvider;
	}

		/**
	 * Label provider for type elements
	 */
	private static final class AntTypesLabelProvider extends LabelProvider implements ITableLabelProvider {
		/**
		 * Creates an instance.
		 */
		public AntTypesLabelProvider() {
		}
		
		/* (non-Javadoc)
		 * Method declared on IBaseLabelProvider.
		 */
		public void dispose() {
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
			StringBuffer text= new StringBuffer(type.getTypeName());
			text.append(" ("); //$NON-NLS-1$
			text.append(type.getLibrary().getFile());
			text.append(": "); //$NON-NLS-1$
			text.append(type.getClassName());
			text.append(')');
			if (type.isDefault()) {
				text.append(MessageFormat.format(AntPreferencesMessages.getString("AntTypesPage.10"), new String[]{type.getPluginLabel()})); //$NON-NLS-1$
			}
			return text.toString();
		}
		
		public Image getTypeImage() {
			return AntUIImages.getImage(IAntUIConstants.IMG_ANT_TYPE);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.AntPage#initialize()
	 */
	protected void initialize() {
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		setInput(prefs.getTypes());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.AntPage#initialize()
	 */
	protected String getHelpContextId() {
		return IAntUIHelpContextIds.ANT_TYPES_PAGE;
	}
}
