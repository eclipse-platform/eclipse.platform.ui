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
package org.eclipse.debug.internal.ui.preferences;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.MultipleInputDialog;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Preference page for creating and configuring simple
 * launch variables.
 * 
 * @see org.eclipse.debug.core.variables.IValueVariable
 * @see org.eclipse.debug.core.variables.ISimpleVariableRegistry
 */
public class StringVariablePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private TableViewer variableTable;
	protected Button envAddButton;
	protected Button envEditButton;
	protected Button envRemoveButton;
	
	protected SimpleVariableContentProvider variableContentProvider= new SimpleVariableContentProvider();
	
	protected static final String NAME_LABEL= DebugPreferencesMessages.SimpleVariablePreferencePage_10; 
	protected static final String VALUE_LABEL = DebugPreferencesMessages.SimpleVariablePreferencePage_11; 
	protected static final String DESCRIPTION_LABEL = DebugPreferencesMessages.SimpleVariablePreferencePage_12; 
	
	protected static String[] variableTableColumnProperties= {
		"variable", //$NON-NLS-1$
		"value", //$NON-NLS-1$
		"description" //$NON-NLS-1$
	};
	protected String[] variableTableColumnHeaders= {
		DebugPreferencesMessages.SimpleVariablePreferencePage_3, 
		DebugPreferencesMessages.SimpleVariablePreferencePage_4, 
		DebugPreferencesMessages.SimpleVariablePreferencePage_5
	};
	protected ColumnLayoutData[] variableTableColumnLayouts= {
		new ColumnWeightData(33),
		new ColumnWeightData(33),
		new ColumnWeightData(34)
	};
	
	public StringVariablePreferencePage() {
		setDescription(DebugPreferencesMessages.SimpleVariablePreferencePage_6); 
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.SIMPLE_VARIABLE_PREFERENCE_PAGE);
	}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Font font= parent.getFont();
		//The main composite
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight=0;
		layout.marginWidth=0;
		layout.numColumns= 2;
		composite.setLayout(layout);
		composite.setFont(font);
		
		createTable(composite);
		createButtons(composite);
		return composite;
	}
	
	/**
	 * Creates and configures the table containing launch configuration variables
	 * and their associated value.
	 */
	private void createTable(Composite parent) {
		Font font= parent.getFont();
		// Create table composite
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 150;
		gridData.widthHint = 400;
		tableComposite.setLayout(layout);
		tableComposite.setLayoutData(gridData);
		tableComposite.setFont(font);
		// Create table
		variableTable = new TableViewer(tableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = variableTable.getTable();
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(font);
		gridData = new GridData(GridData.FILL_BOTH);
		variableTable.getControl().setLayoutData(gridData);
		variableTable.setContentProvider(variableContentProvider);
		variableTable.setColumnProperties(variableTableColumnProperties);
		variableTable.setSorter(new ViewerSorter() {
			public int compare(Viewer iViewer, Object e1, Object e2) {
				if (e1 == null) {
					return -1;
				} else if (e2 == null) {
					return 1;
				} else {
					return ((IValueVariable)e1).getName().compareToIgnoreCase(((IValueVariable)e2).getName());
				}
			}
		});
		
		variableTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleTableSelectionChanged(event);
			}
		});
		
		variableTable.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!variableTable.getSelection().isEmpty()) {
					handleEditButtonPressed();
				}
			}
		});
		variableTable.getTable().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					handleRemoveButtonPressed();
				}
			}
		});
				
		// Create columns
		for (int i = 0; i < variableTableColumnHeaders.length; i++) {
			tableLayout.addColumnData(variableTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(variableTableColumnLayouts[i].resizable);
			tc.setText(variableTableColumnHeaders[i]);
		}
		variableTable.setInput(getVariableManager());
		variableTable.setLabelProvider(new SimpleVariableLabelProvider());
	}
	
	/**
	 * Creates the new/edit/remove buttons for the variable table
	 * @param parent the composite in which the buttons should be created
	 */
	private void createButtons(Composite parent) {
		// Create button composite
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		GridData gdata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		buttonComposite.setLayout(glayout);
		buttonComposite.setLayoutData(gdata);
		buttonComposite.setFont(parent.getFont());
		
		// Create buttons
		envAddButton = SWTUtil.createPushButton(buttonComposite, DebugPreferencesMessages.SimpleVariablePreferencePage_7, null); 
		envAddButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event) {
				handleAddButtonPressed();
			}
		});
		envEditButton = SWTUtil.createPushButton(buttonComposite, DebugPreferencesMessages.SimpleVariablePreferencePage_8, null); 
		envEditButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event) {
				handleEditButtonPressed();
			}
		});
		envEditButton.setEnabled(false);
		envRemoveButton = SWTUtil.createPushButton(buttonComposite, DebugPreferencesMessages.SimpleVariablePreferencePage_9, null); 
		envRemoveButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event) {
				handleRemoveButtonPressed();
			}
		});
		envRemoveButton.setEnabled(false);
	}
	
	private void handleAddButtonPressed() {
		MultipleInputDialog dialog= new MultipleInputDialog(getShell(), DebugPreferencesMessages.SimpleVariablePreferencePage_13); 
		dialog.addTextField(NAME_LABEL, null, false);
		dialog.addBrowseField(VALUE_LABEL, null, true);
		dialog.addTextField(DESCRIPTION_LABEL, null, true);

		if (dialog.open() != Window.OK) {
			return;
		}

		String name= dialog.getStringValue(NAME_LABEL).trim();
		if (name != null && name.length() > 0) {
			String description= dialog.getStringValue(DESCRIPTION_LABEL);
			IValueVariable variable= getVariableManager().newValueVariable(name, description);
			variable.setValue(dialog.getStringValue(VALUE_LABEL));	
			addVariable(variable);
		}
	}
	
	/**
	 * Attempts to add the given variable. Returns whether the variable
	 * was added or not (as when the user answers not to overwrite an
	 * existing variable).
	 * @param variable the variable to add
	 * @return whether the variable was added
	 */
	public boolean addVariable(IValueVariable variable) {
		String name= variable.getName();
		List editedVariables= variableContentProvider.getVariables();
		Iterator iter= editedVariables.iterator();
		while (iter.hasNext()) {
			IValueVariable currentVariable = (IValueVariable) iter.next();
			String variableName = currentVariable.getName();
			if (variableName.equals(name)) {
				boolean overWrite= MessageDialog.openQuestion(getShell(), DebugPreferencesMessages.SimpleVariablePreferencePage_15, MessageFormat.format(DebugPreferencesMessages.SimpleVariablePreferencePage_16, new String[] {name}));  // 
				if (!overWrite) {
					return false;
				}
				variableContentProvider.removeVariables(new IValueVariable[]{currentVariable});
				break;
			}
		}
		variableContentProvider.addVariables(new IValueVariable[]{variable});
		variableTable.refresh();
		return true;
	}
	
	private void handleEditButtonPressed() {
		IStructuredSelection selection= (IStructuredSelection) variableTable.getSelection();
		IValueVariable variable= (IValueVariable) selection.getFirstElement();
		if (variable == null) {
			return;
		}
		String value= variable.getValue();
		if (value == null) {
			value= ""; //$NON-NLS-1$
		}
		String description= variable.getDescription();
		if (description == null) {
			description= ""; //$NON-NLS-1$
		}
		String originalName= variable.getName();
		MultipleInputDialog dialog= new MultipleInputDialog(getShell(), DebugPreferencesMessages.SimpleVariablePreferencePage_14); 
		dialog.addTextField(NAME_LABEL, originalName, false);
		dialog.addBrowseField(VALUE_LABEL, value, true);
		dialog.addTextField(DESCRIPTION_LABEL, description, true);
	
		if (dialog.open() == Window.OK) {
			String name= dialog.getStringValue(NAME_LABEL);
			value= dialog.getStringValue(VALUE_LABEL);
			description= dialog.getStringValue(DESCRIPTION_LABEL);
			if (!name.equals(originalName)) {
				IValueVariable newVariable = getVariableManager().newValueVariable(name, description);
				newVariable.setValue(value);
				if (addVariable(newVariable)) {
					variableContentProvider.removeVariables(new IValueVariable[]{variable});
					variableTable.refresh();
				}
			} else {
				if (value != null) {
					variable.setValue(value);
				}
				if (description != null) {
					variable.setDescription(description);
				}
				variableTable.update(variable, null);
			}
		}
	}
	
	/**
	 * Remove the selection variables.
	 */
	private void handleRemoveButtonPressed() {
		IStructuredSelection selection= (IStructuredSelection) variableTable.getSelection();
		List variablesToRemove= selection.toList();
		StringBuffer contributedVariablesToRemove= new StringBuffer();
		Iterator iter= variablesToRemove.iterator();
		while (iter.hasNext()) {
			IValueVariable variable = (IValueVariable) iter.next();
			if (variable.isContributed()) {
				contributedVariablesToRemove.append('\t').append(variable.getName()).append('\n');
			}
		}
		if (contributedVariablesToRemove.length() > 0) {
			boolean remove= MessageDialog.openQuestion(getShell(), DebugPreferencesMessages.SimpleLaunchVariablePreferencePage_21, MessageFormat.format(DebugPreferencesMessages.SimpleLaunchVariablePreferencePage_22, new String[] {contributedVariablesToRemove.toString()})); // 
			if (!remove) {
				return;
			}
		}
		IValueVariable[] variables= (IValueVariable[]) variablesToRemove.toArray(new IValueVariable[0]);
		variableContentProvider.removeVariables(variables); 
		variableTable.refresh();
	}
	
	/**
	 * Responds to a selection changed event in the variable table
	 * @param event the selection change event
	 */
	protected void handleTableSelectionChanged(SelectionChangedEvent event) {
		int size = ((IStructuredSelection)event.getSelection()).size();
		envEditButton.setEnabled(size == 1);
		envRemoveButton.setEnabled(size > 0);
	}

	public void init(IWorkbench workbench) {
	}

	/**
	 * Clear the variables.
	 */
	protected void performDefaults() {
		variableContentProvider.discardChanges();
		variableTable.refresh();
		super.performDefaults();
	}
	
	/**
	 * Sets the saved state for reversion.
	 */
	public boolean performOk() {
		variableContentProvider.saveChanges();
		return super.performOk();
	}

	/**
	 * Returns the DebugPlugin's singleton instance of the
	 * launch variable manager
	 * @return the singleton instance of the simple variable registry.
	 */
	private IStringVariableManager getVariableManager() {
		return VariablesPlugin.getDefault().getStringVariableManager();
	}
	
	private class SimpleVariableContentProvider implements IStructuredContentProvider {
		/**
		 * The content provider stores a copy of the variables for use during editing.
		 * The edited variables are saved to the launch manager when saveChanges()
		 * is called.
		 */
		private List fVariables = new ArrayList();
		
		public Object[] getElements(Object inputElement) {
			return fVariables.toArray();
		}
		
		/**
		 * Removes the given variables from the 'copied list'
		 * 
		 * @param variables variables to remove
		 */
		public void removeVariables(IValueVariable[] variables) {
			for (int i = 0; i < variables.length; i++) {
				fVariables.remove(variables[i]);
			}			
		}
		
		/**
		 * Adds the given variables to the 'copied list'
		 * 
		 * @param variables variables to add
		 */
		public void addVariables(IValueVariable[] variables) {
			for (int i = 0; i < variables.length; i++) {
				fVariables.add(variables[i]);
			}			
		}		

		public void dispose() {
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null || !(newInput instanceof IStringVariableManager)){
				return;
			}
			discardChanges();
		}

		/**
		 * Saves the edited variable state to the variable manager.
		 */
		public void saveChanges() {
			IStringVariableManager manager = getVariableManager();
			manager.removeVariables(manager.getValueVariables());
			try {
				manager.addVariables((IValueVariable[]) fVariables.toArray(new IValueVariable[0]));
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(getShell(), DebugPreferencesMessages.StringVariablePreferencePage_24, DebugPreferencesMessages.StringVariablePreferencePage_25, e.getStatus()); // 
			}
		}
		
		/**
		 * Re-initializes to the variables currently stored in the manager.
		 */
		public void discardChanges() {
			IStringVariableManager manager = getVariableManager();
			IValueVariable[] variables = manager.getValueVariables();
			for (int i = 0; i < variables.length; i++) {
				IValueVariable variable = variables[i];
				IValueVariable copy = manager.newValueVariable(variable.getName(), variable.getDescription());
				copy.setValue(variable.getValue());
				fVariables.add(copy);
			}			
		}
		
		/**
		 * Returns the 'working set' of variables
		 * 
		 * @return the working set of variables (not yet saved)
		 */
		public List getVariables() {
			return fVariables;
		}
	}
	
	private class SimpleVariableLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IValueVariable) {
				IValueVariable variable= (IValueVariable) element;
				switch (columnIndex) {
					case 0 :
						StringBuffer buffer= new StringBuffer(variable.getName());
						if (variable.isContributed()) {
                            String pluginId = getVariableManager().getContributingPluginId(variable);
                            if (pluginId != null) {
                                buffer.append(MessageFormat.format(DebugPreferencesMessages.StringVariablePreferencePage_0, new String[] {pluginId})); 
                            } else {
                                buffer.append(DebugPreferencesMessages.SimpleLaunchVariablePreferencePage_23); 
                            }
						}
						return buffer.toString();
					case 1:
						String value= variable.getValue(); 
						if (value == null) {
							value= ""; //$NON-NLS-1$
						}
						return value;
					case 2:
						String description= variable.getDescription();
						if (description == null) {
							description= ""; //$NON-NLS-1$
						}
						return description;
				}
			}
			return null;
		}
		public Color getForeground(Object element) {
			return null;
		}
		public Color getBackground(Object element) {
			if (element instanceof IValueVariable) {
				if (((IValueVariable) element).isContributed()) {
					Display display= Display.getCurrent();
					return display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);		
				}
			}
			return null;
		}
	}
}
