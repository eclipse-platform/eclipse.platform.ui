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
package org.eclipse.ant.ui.internal.preferences;


import java.util.Iterator;
import java.util.Map;

import org.eclipse.ant.core.Property;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.IAntUIConstants;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.externaltools.internal.ui.ExternalToolsContentProvider;

public class AntPropertiesBlock {
	
	private IAntBlockContainer container;
	
	private Button editButton;
	private Button removeButton;
	private Button addButton;
	private Button addFileButton;
	private Button removeFileButton;

	private TableViewer propertyTableViewer;
	private TableViewer fileTableViewer;

	private final AntPropertiesLabelProvider labelProvider = new AntPropertiesLabelProvider();

	private IDialogSettings dialogSettings;
	
	/**
	 * Button listener that delegates for widget selection events.
	 */
	private SelectionAdapter buttonListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			if (event.widget == addButton) {
				addProperty();
			} else if (event.widget == editButton) {
				edit();
			} else if (event.widget == removeButton) {
				remove(propertyTableViewer);
			} else if (event.widget == addFileButton) {
				addPropertyFile();
			} else if (event.widget == removeFileButton) {
				remove(fileTableViewer);
			} else if (event.widget == removeButton) {
			}
		}
	};
	
	/**
	 * Selection changed listener that delegates selection events.
	 */
	private ISelectionChangedListener tableListener= new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSource() == propertyTableViewer) {
				propertyTableSelectionChanged((IStructuredSelection) event.getSelection());
			} else if (event.getSource() == fileTableViewer) {
				fileTableSelectionChanged((IStructuredSelection) event.getSelection());
			}
		}
	};
	
	public AntPropertiesBlock(IAntBlockContainer container) {
		this.container= container; 
	}
	
	public void createControl(Composite top, String propertyLabel, String propertyFileLabel) {
		Font font= top.getFont();
		dialogSettings= AntUIPlugin.getDefault().getDialogSettings();
		
		createVerticalSpacer(top, 2);

		Label label = new Label(top, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(propertyLabel);

		propertyTableViewer= createTableViewer(top);
		propertyTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				edit();
			}
		});
		
		createButtonGroup(top);

		label = new Label(top, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(propertyFileLabel);

		fileTableViewer= createTableViewer(top);
		createButtonGroup(top);
	}
	
	/**
	 * Create some empty space.
	 */
	private void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
	}	
	
	/**
	 * Creates the group which will contain the buttons.
	 */
	private void createButtonGroup(Composite top) {
		Composite buttonGroup = new Composite(top, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonGroup.setLayout(layout);
		buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttonGroup.setFont(top.getFont());

		addButtonsToButtonGroup(buttonGroup);
	}
	
	/**
	 * Creates and returns a configured table viewer in the given parent
	 */
	private TableViewer createTableViewer(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		table.setLayoutData(data);
		table.setFont(parent.getFont());
		
		TableViewer tableViewer= new TableViewer(table);
		tableViewer.setContentProvider(new ExternalToolsContentProvider());
		tableViewer.setLabelProvider(labelProvider);
		tableViewer.addSelectionChangedListener(tableListener);
		return tableViewer;
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		if (editButton == null) {
			addButton= createPushButton(parent, AntPreferencesMessages.getString("AntPropertiesBlock.addButton")); //$NON-NLS-1$
			editButton= createPushButton(parent, AntPreferencesMessages.getString("AntPropertiesBlock.editButton"));  //$NON-NLS-1$
			removeButton= createPushButton(parent, AntPreferencesMessages.getString("AntPropertiesBlock.removeButton"));  //$NON-NLS-1$
		} else {
			addFileButton= createPushButton(parent, AntPreferencesMessages.getString("AntPropertiesBlock.addFileButton")); //$NON-NLS-1$
			removeFileButton= createPushButton(parent, AntPreferencesMessages.getString("AntPropertiesBlock.removeFileButton")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Creates and returns a configured button in the given composite with the given
	 * label. Widget selection callbacks for the returned button will be processed
	 * by the <code>buttonListener</code>
	 */
	private Button createPushButton(Composite parent, String label) {
		Button button= container.createPushButton(parent, label);
		button.addSelectionListener(buttonListener);
		return button;
	}
	
	/**
	 * Allows the user to enter property files
	 */
	private void addPropertyFile() {
		String lastUsedPath;
		lastUsedPath= dialogSettings.get(IAntUIConstants.DIALOGSTORE_LASTEXTFILE);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog = new FileDialog(propertyTableViewer.getControl().getShell(), SWT.MULTI);
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
			((ExternalToolsContentProvider)fileTableViewer.getContentProvider()).add(path.toOSString());
		}
	
		dialogSettings.put(IAntUIConstants.DIALOGSTORE_LASTEXTFILE, filterPath.toOSString());
		container.update();
	}
	
	private void remove(TableViewer viewer) {
		ExternalToolsContentProvider antContentProvider= (ExternalToolsContentProvider)viewer.getContentProvider();
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		antContentProvider.remove(sel);
		container.update();
	}
	
	/**
	 * Allows the user to enter a global user property
	 */
	private void addProperty() {
		String title = AntPreferencesMessages.getString("AntPropertiesBlock.Add_Property_2");  //$NON-NLS-1$
		AddPropertyDialog dialog = new AddPropertyDialog(propertyTableViewer.getControl().getShell(), title, new String[]{"", ""}); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog.open() == Window.CANCEL) {
			return;
		}

		Property prop = new Property();
		String[] pair= dialog.getNameValuePair();
		prop.setName(pair[0]);
		prop.setValue(pair[1]);
		((ExternalToolsContentProvider)propertyTableViewer.getContentProvider()).add(prop);
		container.update();
	}

	private void edit() {
		IStructuredSelection selection= (IStructuredSelection) propertyTableViewer.getSelection();
		Property prop = (Property) selection.getFirstElement();
		if (prop == null) {
			return;
		}
		String title = AntPreferencesMessages.getString("AntPropertiesBlock.Edit_User_Property_5"); //$NON-NLS-1$
		AddPropertyDialog dialog = new AddPropertyDialog(propertyTableViewer.getControl().getShell(), title, new String[]{prop.getName(), prop.getValue()});
	
		if (dialog.open() == Window.CANCEL) {
			return;
		}

		String[] pair= dialog.getNameValuePair();
		prop.setName(pair[0]);
		prop.setValue(pair[1]);
		//trigger a resort
		propertyTableViewer.refresh();
		container.update();
	}
	
	/**
	 * Handles selection changes in the Property file table viewer.
	 */
	private void fileTableSelectionChanged(IStructuredSelection newSelection) {
		removeFileButton.setEnabled(newSelection.size() > 0);
	}

	/**
	 * Handles selection changes in the Property file table viewer.
	 */
	private void propertyTableSelectionChanged(IStructuredSelection newSelection) {
		int size = newSelection.size();
		boolean enabled= true;

		Iterator itr= newSelection.iterator();
		while (itr.hasNext()) {
			Object element = itr.next();
			if (element instanceof Property) {
				Property property= (Property)element;
				if (property.isDefault()) {
					enabled= false;
					break;
				}
			}
		}
		editButton.setEnabled(enabled && size == 1);
		removeButton.setEnabled(enabled && size > 0);
		
	}
	
	public void populatePropertyViewer(Map properties) {
		if (properties == null) {
			propertyTableViewer.setInput(new Property[0]);
			return;
		} 
		Property[] result = new Property[properties.size()];
		Iterator entries= properties.entrySet().iterator();
		int i= 0;
		while (entries.hasNext()) {
			Map.Entry element = (Map.Entry) entries.next();
			Property property = new Property();
			property.setName((String)element.getKey());
			property.setValue((String)element.getValue());
			result[i]= property;
			i++;
		}
		propertyTableViewer.setInput(result);
	}
	
	public void setPropertiesInput(Property[] properties) {
		propertyTableViewer.setInput(properties);
	}
		
	public void setPropertyFilesInput(String[] files) {
		fileTableViewer.setInput(files);
	}
	
	public void update() {
		propertyTableSelectionChanged((IStructuredSelection) propertyTableViewer.getSelection());
		fileTableSelectionChanged((IStructuredSelection)fileTableViewer.getSelection());
	}
	
	public Object[] getProperties() {
		return ((ExternalToolsContentProvider)propertyTableViewer.getContentProvider()).getElements(null);
	}
	
	public Object[] getPropertyFiles() {
		return ((ExternalToolsContentProvider)fileTableViewer.getContentProvider()).getElements(null);
	}
}
