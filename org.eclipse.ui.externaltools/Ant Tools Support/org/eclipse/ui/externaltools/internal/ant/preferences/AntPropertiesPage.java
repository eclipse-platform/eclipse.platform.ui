package org.eclipse.ui.externaltools.internal.ant.preferences;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.Property;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.ExternalToolsContentProvider;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

/**
 * Preference page for setting global Ant user properties.
 * All properties specified here will be set as user properties on the 
 * project for any Ant build
 */
public class AntPropertiesPage extends AntPage {
	
	private static final int ADD_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_BUTTON = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;
	
	private static final int ADD_PROPERTY_FILE_BUTTON = IDialogConstants.CLIENT_ID + 4;
	private static final int EDIT_PROPERTY_FILE_BUTTON = IDialogConstants.CLIENT_ID + 5;
	private static final int REMOVE_PROPERTY_FILE_BUTTON = IDialogConstants.CLIENT_ID + 6;
	
	private Button addButton;
	private Button addFileButton;
	private Button removeFileButton;
	
	private TableViewer fileTableViewer;
	private ExternalToolsContentProvider fileContentProvider;
	
	private final AntPropertiesLabelProvider labelProvider = new AntPropertiesLabelProvider();
	
	private IDialogSettings fDialogSettings;
	
	/**
	 * Creates an instance.
	 */
	public AntPropertiesPage(AntRuntimePreferencePage preferencePage) {
		super(preferencePage);
		fDialogSettings= ExternalToolsPlugin.getDefault().getDialogSettings();
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
	protected void addButtonsToButtonGroup(Composite parent) {
		if (editButton == null) {
			addButton= createButton(parent, "AntPropertiesPage.addButton", ADD_BUTTON); //$NON-NLS-1$;
			editButton= createButton(parent, "AntPropertiesPage.editButton", EDIT_BUTTON); //$NON-NLS-1$;
			removeButton= createButton(parent, "AntPropertiesPage.removeButton", REMOVE_BUTTON); //$NON-NLS-1$;
		} else {
			addFileButton= createButton(parent, "AntPropertiesPage.addFileButton", ADD_PROPERTY_FILE_BUTTON); //$NON-NLS-1$;
			removeFileButton= createButton(parent, "AntPropertiesPage.removeFileButton", REMOVE_PROPERTY_FILE_BUTTON); //$NON-NLS-1$;
		}
	}
	
	/**
	 * Creates the tab item that contains this sub-page.
	 */
	protected TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntPropertiesPage.title")); //$NON-NLS-1$
		item.setImage(labelProvider.getPropertyImage());
		item.setData(this);
		item.setControl(createContents(folder));
		return item;
	}
	
	/**
	 * Creates the table viewer.
	 */
	protected void createTable(Composite parent) {
		if (getTableViewer() == null) {
			super.createTable(parent);
		} else {
			Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
			GridData data= new GridData(GridData.FILL_BOTH);
			data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			table.setLayoutData(data);
			table.setFont(parent.getFont());
			fileContentProvider = getContentProvider();
			fileTableViewer = new TableViewer(table);
			fileTableViewer.setContentProvider(fileContentProvider);
			fileTableViewer.setLabelProvider(getLabelProvider());
			fileTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					fileTableSelectionChanged((IStructuredSelection) event.getSelection());
				}
			});
		}
	}
	
	protected Composite createContents(Composite parent) {
		Font font = parent.getFont();
		
		Composite top = new Composite(parent, SWT.NONE);
		top.setFont(font);
		
		Label label = new Label(top, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntPropertiesPage.&Global_properties__1")); //$NON-NLS-1$
		
		super.createContents(top);
		
		label = new Label(top, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntPropertiesPage.Glo&bal_property_files__2")); //$NON-NLS-1$
		
		createTable(top);
		createButtonGroup(top);
		
		return top;
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case ADD_BUTTON :
				addProperty();
				break;
			case EDIT_BUTTON :
				edit(getSelection());
				break;
			case REMOVE_BUTTON :
				remove();
				break;
			case ADD_PROPERTY_FILE_BUTTON :
				addPropertyFile();
				break;
			case REMOVE_PROPERTY_FILE_BUTTON :
				remove(fileTableViewer);
				break;
		}
	}
	
	/**
	 * Allows the user to enter a global user property
	 */
	private void addProperty() {
		String title = AntPreferencesMessages.getString("AntPropertiesPage.Add_Property_2");  //$NON-NLS-1$
		AddPropertyDialog dialog = new AddPropertyDialog(getShell(), title, new String[]{"", ""}); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog.open() == Dialog.CANCEL) {
			return;
		}

		Property prop = new Property();
		String[] pair= dialog.getNameValuePair();
		prop.setName(pair[0]);
		prop.setValue(pair[1]);
		addContent(prop);
	}
	
	protected void edit(IStructuredSelection selection) {
		Property prop = (Property) selection.getFirstElement();
		if (prop == null) {
			return;
		}
		String title = AntPreferencesMessages.getString("AntPropertiesPage.Edit_User_Property_5"); //$NON-NLS-1$
		AddPropertyDialog dialog = new AddPropertyDialog(getShell(), title, new String[]{prop.getName(), prop.getValue()});
		
		if (dialog.open() == Dialog.CANCEL) {
			return;
		}

		String[] pair= dialog.getNameValuePair();
		prop.setName(pair[0]);
		prop.setValue(pair[1]);
		updateContent(prop);
	}

	/**
	 * Handles selection changes in the Property file table viewer.
	 */
	private void fileTableSelectionChanged(IStructuredSelection newSelection) {
		int size = newSelection.size();
		removeFileButton.setEnabled(size > 0);
	}
	
	/**
	 * Sets the contents of the tables on this page.
	 */
	protected void initialize() {
		getTableViewer().setInput(Arrays.asList(AntCorePlugin.getPlugin().getPreferences().getCustomProperties()));
		fileTableViewer.setInput(Arrays.asList(AntCorePlugin.getPlugin().getPreferences().getCustomPropertyFiles()));
		tableSelectionChanged((IStructuredSelection) getTableViewer().getSelection());
		fileTableSelectionChanged((IStructuredSelection)fileTableViewer.getSelection());
	}
	
	protected void performDefaults() {
		getTableViewer().setInput(new ArrayList(0));
		fileTableViewer.setInput(new ArrayList(0));
		tableSelectionChanged((IStructuredSelection) getTableViewer().getSelection());
		fileTableSelectionChanged((IStructuredSelection)fileTableViewer.getSelection());
	}
	
	/**
	 * Allows the user to enter property files
	 */
	private void addPropertyFile() {
		String lastUsedPath;
		lastUsedPath= fDialogSettings.get(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTFILE);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
		dialog.setFilterExtensions(new String[] { "*.properties" }); //$NON-NLS-1$;
		dialog.setFilterPath(lastUsedPath);

		String result = dialog.open();
		if (result == null) {
			return;
		}
		IPath filterPath= new Path(dialog.getFilterPath());
		String[] results= dialog.getFileNames();
		for (int i = 0; i < results.length; i++) {
			String fileName = results[i];	
			IPath path= filterPath.append(fileName).makeAbsolute();	
			addContent(path.toOSString());
		}
		
		fDialogSettings.put(IExternalToolsUIConstants.DIALOGSTORE_LASTEXTFILE, filterPath.toOSString());
	}
	
	/**
	 * @see org.eclipse.ui.externaltools.internal.ant.preferences.AntPage#addContent(java.lang.Object)
	 */
	protected void addContent(Object o) {
		if (o instanceof String) {
			fileContentProvider.add(o);
		} else {
			super.addContent(o);
		}
	}
	
	/**
	 * Returns the specified property files
	 * 
	 * @return String[]
	 */
	protected String[] getPropertyFiles() {
		Object[] elements = fileContentProvider.getElements(null);
		String[] files= new String[elements.length];
		for (int i = 0; i < elements.length; i++) {
			files[i] = (String)elements[i];
		}
		return files;
	}
}