/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.preferences;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ant.core.Property;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.swt.widgets.TableColumn;

public class AntPropertiesBlock {
	
	private IAntBlockContainer container;
	
	private Button editButton;
	private Button removeButton;
	private Button addButton;
	
	private Button addFileButton;
	private Button addExternalFileButton;
	private Button removeFileButton;

	private TableViewer propertyTableViewer;
	private TableViewer fileTableViewer;

	private final AntObjectLabelProvider labelProvider = new AntObjectLabelProvider();

	private IDialogSettings dialogSettings;
	
	private boolean tablesEnabled= true;
    
	private final String[] fTableColumnHeaders= {
	        AntPreferencesMessages.AntPropertiesBlock_0, AntPreferencesMessages.AntPropertiesBlock_5, AntPreferencesMessages.AntPropertiesBlock_6
	};
	
	private final ColumnLayoutData[] fTableColumnLayouts= {
	        new ColumnWeightData(30),
	        new ColumnWeightData(40),
            new ColumnWeightData(30)
	};
	
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
			} else if (event.widget == addExternalFileButton) {
				addExternalPropertyFile();
			} else if (event.widget == removeFileButton) {
				remove(fileTableViewer);
			} 
		}
	};
	
	/**
	 * Key listener that delegates for key pressed events.
	 */
	private KeyAdapter keyListener= new KeyAdapter() {
		public void keyPressed(KeyEvent event) {
			if (event.getSource() == propertyTableViewer) {
				if (removeButton.isEnabled() && event.character == SWT.DEL && event.stateMask == 0) {
					remove(propertyTableViewer);
				}
			} else if (event.getSource() == fileTableViewer) {
				if (removeFileButton.isEnabled() && event.character == SWT.DEL && event.stateMask == 0) {
					remove(fileTableViewer);
				}
			}
		}	
	};
	
	/**
	 * Selection changed listener that delegates selection events.
	 */
	private ISelectionChangedListener tableListener= new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			if (tablesEnabled) {
				if (event.getSource() == propertyTableViewer) {
					propertyTableSelectionChanged((IStructuredSelection) event.getSelection());
				} else if (event.getSource() == fileTableViewer) {
					fileTableSelectionChanged((IStructuredSelection) event.getSelection());
				}
			}
		}
	};
	
	public AntPropertiesBlock(IAntBlockContainer container) {
		this.container= container; 
	}

	private void addPropertyFile() {
		String title= AntPreferencesMessages.AntPropertiesFileSelectionDialog_12;
		String message= AntPreferencesMessages.AntPropertiesFileSelectionDialog_13;
		String filterExtension= "properties"; //$NON-NLS-1$
		String filterMessage= AntPreferencesMessages.AntPropertiesFileSelectionDialog_14;
		
		Object[] existingFiles= getPropertyFiles();
		List propFiles= new ArrayList(existingFiles.length);
		for (int j = 0; j < existingFiles.length; j++) {
			String file = (String)existingFiles[j];
			try {
                propFiles.add(AntUtil.getFileForLocation(VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(file), null));
            } catch (CoreException e) {
                AntUIPlugin.log(e.getStatus());
            }
		}
			
		FileSelectionDialog dialog= new FileSelectionDialog(propertyTableViewer.getControl().getShell(), propFiles, title, message, filterExtension, filterMessage);
		if (dialog.open() == Window.OK) {
			Object[] elements= dialog.getResult();
			for (int i = 0; i < elements.length; i++) {
				IFile file = (IFile)elements[i];
				String varExpression= VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", file.getFullPath().toString()); //$NON-NLS-1$
				((AntContentProvider)fileTableViewer.getContentProvider()).add(varExpression);
			}
			container.update();
		}
	}
	
	public void createControl(Composite top, String propertyLabel, String propertyFileLabel) {
		Font font= top.getFont();
		dialogSettings= AntUIPlugin.getDefault().getDialogSettings();

		Label label = new Label(top, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(propertyLabel);

		propertyTableViewer= createTableViewer(top, true);
		propertyTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty() && editButton.isEnabled()) {
					edit();
				}
			}
		});
		
		propertyTableViewer.getTable().addKeyListener(keyListener);	
		
		createButtonGroup(top);

		label = new Label(top, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan =2;
		label.setLayoutData(gd);
		label.setFont(font);
		label.setText(propertyFileLabel);

		fileTableViewer= createTableViewer(top, false);
		fileTableViewer.getTable().addKeyListener(keyListener);	
		createButtonGroup(top);
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
		buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL));
		buttonGroup.setFont(top.getFont());

		addButtonsToButtonGroup(buttonGroup);
	}
	
	/**
	 * Creates and returns a configured table viewer in the given parent
	 */
	private TableViewer createTableViewer(Composite parent, boolean setColumns) {
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		table.setLayoutData(data);
		table.setFont(parent.getFont());
		
		TableViewer tableViewer= new TableViewer(table);
		tableViewer.setContentProvider(new AntContentProvider());
		tableViewer.setLabelProvider(labelProvider);
		tableViewer.addSelectionChangedListener(tableListener);
        
        if (setColumns) {
            TableLayout tableLayout = new TableLayout();
            table.setLayout(tableLayout);
            table.setHeaderVisible(true);
            table.setLinesVisible(true);
            
            for (int i = 0; i < fTableColumnHeaders.length; i++) {
                tableLayout.addColumnData(fTableColumnLayouts[i]);
                TableColumn column = new TableColumn(table, SWT.NONE, i);
                column.setResizable(fTableColumnLayouts[i].resizable);
                column.setText(fTableColumnHeaders[i]);
            }
        }
            
		return tableViewer;
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		if (editButton == null) {
			addButton= createPushButton(parent, AntPreferencesMessages.AntPropertiesBlock_1);
			editButton= createPushButton(parent, AntPreferencesMessages.AntPropertiesBlock_2);
			removeButton= createPushButton(parent, AntPreferencesMessages.AntPropertiesBlock_3);
		} else {
			addFileButton= createPushButton(parent, AntPreferencesMessages.AntPropertiesBlock_4);
			addExternalFileButton= createPushButton(parent, AntPreferencesMessages.AntPropertiesBlock_14);
			removeFileButton= createPushButton(parent, AntPreferencesMessages.AntPropertiesBlock_removeFileButton);
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
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		button.setLayoutData(gridData);
		return button;
	}
	
	/**
	 * Allows the user to enter external property files
	 */
	private void addExternalPropertyFile() {
		String lastUsedPath;
		lastUsedPath= dialogSettings.get(IAntUIConstants.DIALOGSTORE_LASTEXTFILE);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog = new FileDialog(fileTableViewer.getControl().getShell(), SWT.MULTI);
		dialog.setFilterExtensions(new String[] { "*.properties", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$;
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
			((AntContentProvider)fileTableViewer.getContentProvider()).add(path.toOSString());
		}
	
		dialogSettings.put(IAntUIConstants.DIALOGSTORE_LASTEXTFILE, filterPath.toOSString());
		container.update();
	}
	
	private void remove(TableViewer viewer) {
		AntContentProvider antContentProvider= (AntContentProvider)viewer.getContentProvider();
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		antContentProvider.remove(sel);
		container.update();
	}
	
	/**
	 * Allows the user to enter a user property
	 */
	private void addProperty() {
		String title = AntPreferencesMessages.AntPropertiesBlock_Add_Property_2;
		AddPropertyDialog dialog = new AddPropertyDialog(propertyTableViewer.getControl().getShell(), title, new String[]{"", ""}); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		
		String[] pair= dialog.getNameValuePair();
		String name= pair[0];
		if (!overwrite(name)) {
			return;
		}
		Property prop = new Property();
		prop.setName(name);
		prop.setValue(pair[1]);
		((AntContentProvider)propertyTableViewer.getContentProvider()).add(prop);
		container.update();
	}

	private void edit() {
		IStructuredSelection selection= (IStructuredSelection) propertyTableViewer.getSelection();
		Property prop = (Property) selection.getFirstElement();
		
		String originalName= prop.getName();
		String title = AntPreferencesMessages.AntPropertiesBlock_Edit_User_Property_5;
		AddPropertyDialog dialog = new AddPropertyDialog(propertyTableViewer.getControl().getShell(), title, new String[]{prop.getName(), prop.getValue(false)});
	
		if (dialog.open() == Window.CANCEL) {
			return;
		}

		String[] pair= dialog.getNameValuePair();
		String name= pair[0];
		if (!name.equals(originalName)) {
			if (!overwrite(name)){
				return;
			}
		}
		prop.setName(name);
		prop.setValue(pair[1]);
		//trigger a resort
		propertyTableViewer.refresh();
		container.update();
	}
	
	private boolean overwrite(String name) {
		Object[] properties= getProperties();
		for (int i = 0; i < properties.length; i++) {
			Property property = (Property)properties[i];
			String propertyName = property.getName();
			if (propertyName.equals(name)) {
				if (property.isDefault()) {
					MessageDialog.openError(propertyTableViewer.getControl().getShell(), AntPreferencesMessages.AntPropertiesBlock_17, MessageFormat.format(AntPreferencesMessages.AntPropertiesBlock_18, new String[]{propertyName, property.getPluginLabel()}));
					return false;
				} 
				boolean overWrite= MessageDialog.openQuestion(propertyTableViewer.getControl().getShell(), AntPreferencesMessages.AntPropertiesBlock_15, MessageFormat.format(AntPreferencesMessages.AntPropertiesBlock_16, new String[] {name}));
				if (!overWrite) {
					return false;
				}
				((AntContentProvider)propertyTableViewer.getContentProvider()).remove(property);
				break;
			}					
		}
		return true;
	}
	
	/**
	 * Handles selection changes in the Property file table viewer.
	 */
	private void fileTableSelectionChanged(IStructuredSelection newSelection) {
		removeFileButton.setEnabled(newSelection.size() > 0);
	}

	/**
	 * Handles selection changes in the Property table viewer.
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
		return ((AntContentProvider)propertyTableViewer.getContentProvider()).getElements(null);
	}
	
	public Object[] getPropertyFiles() {
		return ((AntContentProvider)fileTableViewer.getContentProvider()).getElements(null);
	}
	
	public void setEnabled(boolean enable) {
		setTablesEnabled(enable);
		addButton.setEnabled(enable);
		addExternalFileButton.setEnabled(enable);
		addFileButton.setEnabled(enable);
		editButton.setEnabled(enable);
		removeButton.setEnabled(enable);
		removeFileButton.setEnabled(enable);
        
		if (enable) {
			propertyTableViewer.setSelection(propertyTableViewer.getSelection());
			fileTableViewer.setSelection(fileTableViewer.getSelection());
		}
	}
	
	public void setTablesEnabled(boolean tablesEnabled) {
		this.tablesEnabled= tablesEnabled;
	}
}