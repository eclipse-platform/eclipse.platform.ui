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
package org.eclipse.debug.internal.ui.preferences;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.variables.ILaunchVariableManager;
import org.eclipse.debug.core.variables.ISimpleLaunchVariable;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Preference page for creating and configuring simple
 * launch variables.
 * 
 * @see org.eclipse.debug.core.variables.ISimpleLaunchVariable
 * @see org.eclipse.debug.core.variables.ISimpleVariableRegistry
 */
public class SimpleVariablePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private TableViewer variableTable;
	protected Button envAddButton;
	protected Button envEditButton;
	protected Button envRemoveButton;
	
	protected SimpleVariableContentProvider variableContentProvider= new SimpleVariableContentProvider();
	 
	protected static String[] variableTableColumnProperties= {
		"variable", //$NON-NLS-1$
		"value", //$NON-NLS-1$
		"description" //$NON-NLS-1$
	};
	protected String[] variableTableColumnHeaders= {
		DebugPreferencesMessages.getString("SimpleVariablePreferencePage.3"), //$NON-NLS-1$
		DebugPreferencesMessages.getString("SimpleVariablePreferencePage.4"), //$NON-NLS-1$
		DebugPreferencesMessages.getString("SimpleVariablePreferencePage.15") //$NON-NLS-1$
	};
	protected ColumnLayoutData[] variableTableColumnLayouts= {
		new ColumnWeightData(25),
		new ColumnWeightData(25),
		new ColumnWeightData(50)
	};
	
	public SimpleVariablePreferencePage() {
		setDescription(DebugPreferencesMessages.getString("SimpleVariablePreferencePage.5")); //$NON-NLS-1$
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.SIMPLE_VARIABLE_PREFERENCE_PAGE);
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
		Composite buttonComposite= createButtons(composite);
		configureTableResizing(composite, buttonComposite, variableTable.getTable());
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
		gridData.widthHint = 300;
		tableComposite.setLayout(layout);
		tableComposite.setLayoutData(gridData);
		tableComposite.setFont(font);
		// Create table
		variableTable = new TableViewer(tableComposite);
		Table table = variableTable.getTable();
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setFont(font);
		gridData = new GridData(GridData.FILL_BOTH);
		variableTable.getControl().setLayoutData(gridData);
		variableTable.setContentProvider(variableContentProvider);
		variableTable.setColumnProperties(variableTableColumnProperties);
		variableTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleTableSelectionChanged(event);
			}
		});
		
		variableTable.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleEditButtonPressed();
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
	 * @return the button composite that is created
	 */
	private Composite createButtons(Composite parent) {
		// Create button composite
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		GridData gdata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(glayout);
		buttonComposite.setLayoutData(gdata);
		buttonComposite.setFont(parent.getFont());

		// Create a little vertical space
		Label label= new Label(buttonComposite, SWT.NONE);
		GridData gd= new GridData();
		gd.horizontalSpan= 1;
		label.setLayoutData(gd);
		// Create buttons
		envAddButton = SWTUtil.createPushButton(buttonComposite, DebugPreferencesMessages.getString("SimpleVariablePreferencePage.7"), null); //$NON-NLS-1$
		envAddButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event) {
				handleAddButtonPressed();
			}
		});
		envEditButton = SWTUtil.createPushButton(buttonComposite, DebugPreferencesMessages.getString("SimpleVariablePreferencePage.8"), null); //$NON-NLS-1$
		envEditButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event) {
				handleEditButtonPressed();
			}
		});
		envEditButton.setEnabled(false);
		envRemoveButton = SWTUtil.createPushButton(buttonComposite, DebugPreferencesMessages.getString("SimpleVariablePreferencePage.9"), null); //$NON-NLS-1$
		envRemoveButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event) {
				handleRemoveButtonPressed();
			}
		});
		envRemoveButton.setEnabled(false);
		
		return buttonComposite;
	}
	
	private void handleAddButtonPressed() {
		InputDialog dialog= new InputDialog(getShell(), DebugPreferencesMessages.getString("SimpleVariablePreferencePage.10"), DebugPreferencesMessages.getString("SimpleVariablePreferencePage.11"), null, null); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog.open() != Dialog.OK) {
			return;
		}
		String name= dialog.getValue().trim();
		if (name.length() > 0) {
			ISimpleLaunchVariable variable= DebugPlugin.getDefault().getLaunchVariableManager().newSimpleVariable(name, null, null);
			if (editVariable(variable)) {
				variableContentProvider.addVariable(variable);
				variableTable.refresh();
			}
		}
	}
	
	private void handleEditButtonPressed() {
		IStructuredSelection selection= (IStructuredSelection) variableTable.getSelection();
		ISimpleLaunchVariable variable= (ISimpleLaunchVariable) selection.getFirstElement();
		if (variable == null) {
			return;
		}
		editVariable(variable);
	}
	
	/**
	 * Prompt the user to edit the selection variable.
	 * @param variable the variable to edit
	 * @return <code>true</code> if the user confirmed the edit,
	 * 	<code>false</code> if the user cancelled.
	 */
	private boolean editVariable(ISimpleLaunchVariable variable) {
		InputDialog dialog= new InputDialog(getShell(), DebugPreferencesMessages.getString("SimpleVariablePreferencePage.12"), MessageFormat.format(DebugPreferencesMessages.getString("SimpleVariablePreferencePage.13"), new String[] {variable.getName()}), variable.getValue(), null); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog.open() != Dialog.OK) {
			return false;
		}
		variable.setValue(dialog.getValue());
		variableTable.update(variable, null);
		return true;
	}
	
	/**
	 * Remove the selection variables.
	 */
	private void handleRemoveButtonPressed() {
		IStructuredSelection selection= (IStructuredSelection) variableTable.getSelection();
		ISimpleLaunchVariable[] variables= (ISimpleLaunchVariable[]) selection.toList().toArray(new ISimpleLaunchVariable[0]);
		variableContentProvider.removeVariables(variables); 
		variableTable.refresh();
	}
	
	/**
	 * Responds to a selection changed event in the environment table
	 * @param event the selection change event
	 */
	protected void handleTableSelectionChanged(SelectionChangedEvent event) {
		boolean enabled = !(variableTable.getSelection().isEmpty());
		envEditButton.setEnabled(enabled);
		envRemoveButton.setEnabled(enabled);
	}

	public void init(IWorkbench workbench) {
	}

	/**
	 * Revert to the previously saved state.
	 */
	public boolean performCancel() {
		variableContentProvider.discardChanges();
		return super.performCancel();
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
	private ILaunchVariableManager getVariableManager() {
		return DebugPlugin.getDefault().getLaunchVariableManager();
	}
	
	private class SimpleVariableContentProvider implements IStructuredContentProvider {
		/**
		 * The content provider stores a copy of the variables for use during editing.
		 * The edited variables are saved to the launch manager when saveChanges()
		 * is called.
		 */
		private List editedVariables= new ArrayList();
		private ILaunchVariableManager variableManager;
		
		public Object[] getElements(Object inputElement) {
			return editedVariables.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null || !(newInput instanceof ILaunchVariableManager)){
				return;
			}
			variableManager= (ILaunchVariableManager) newInput;
			discardChanges();
			if (viewer instanceof TableViewer){
				((TableViewer)viewer).setSorter(new ViewerSorter() {
					public int compare(Viewer viewer, Object e1, Object e2) {
						if (e1 == null) {
							return -1;
						} else if (e2 == null) {
							return 1;
						} else {
							return ((ISimpleLaunchVariable)e1).getName().compareTo(((ISimpleLaunchVariable)e2).getName());
						}
					}
				});
			}
		}
		public void addVariable(ISimpleLaunchVariable variable) {
			editedVariables.add(variable);
		}
		public void removeVariables(ISimpleLaunchVariable[] variables) {
			for (int i= 0; i < variables.length; i++) {
				editedVariables.remove(variables[i]);
			}
		}
		/**
		 * Discards the edited variable state and restores the variables from the
		 * variable manager.
		 */
		public void discardChanges() {
			if (variableManager == null) {
				return;
			}
			editedVariables.clear();
			ISimpleLaunchVariable[] simpleVariables= variableManager.getSimpleVariables();
			for (int i = 0; i < simpleVariables.length; i++) {
				ISimpleLaunchVariable variable= simpleVariables[i];
				editedVariables.add(variableManager.newSimpleVariable(variable.getName(), variable.getValue(), variable.getDescription()));
			}
		}
		/**
		 * Saves the edited variable state to the variable manager.
		 */
		public void saveChanges() {
			if (variableManager == null) {
				return;
			}
			variableManager.removeSimpleVariables(variableManager.getSimpleVariables());
			variableManager.addSimpleVariables((ISimpleLaunchVariable[]) editedVariables.toArray(new ISimpleLaunchVariable[0]));
		}
	}
	
	private class SimpleVariableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof ISimpleLaunchVariable) {
				switch (columnIndex) {
					case 0 :
						return ((ISimpleLaunchVariable) element).getName();		
					case 1:
						String value= ((ISimpleLaunchVariable) element).getValue(); 
						if (value == null) {
							value= ""; //$NON-NLS-1$
						}
						return value;
					case 2:
						String description= ((ISimpleLaunchVariable) element).getDescription();
						if (description == null) {
							description= ""; //$NON-NLS-1$
						}
						return description;
				}
			}
			return null;
		}
	}
	
	/**
	 * Correctly resizes the table so no phantom columns appear
	 */
	private void configureTableResizing(final Composite parent, final Composite buttons, final Table table) {
		parent.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle area = parent.getClientArea();
				Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int width = area.width - 2 * table.getBorderWidth();
				if (preferredSize.y > area.height) {
					// Subtract the scrollbar width from the total column width
					// if a vertical scrollbar will be required
					Point vBarSize = table.getVerticalBar().getSize();
					width -= vBarSize.x;
				}
				width-= buttons.getSize().x;
				Point oldSize = table.getSize();
				
				if (oldSize.x > width) {
					// table is getting smaller so make the columns
					// smaller first and then resize the table to
					// match the client area width
					table.getColumn(0).setWidth(width/2);
					table.getColumn(1).setWidth(width/2);
					table.setSize(width, area.height);
				} else {
					// table is getting bigger so make the table
					// bigger first and then make the columns wider
					// to match the client area width
					table.setSize(width, area.height);
					table.getColumn(0).setWidth(width/2);
					table.getColumn(1).setWidth(width/2);
				 }
			}
		});
	}
}