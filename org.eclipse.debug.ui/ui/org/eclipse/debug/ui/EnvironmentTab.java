package org.eclipse.debug.ui;
/*******************************************************************************
 * Copyright (c) 2000, 2003 Keith Seitz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Keith Seitz (keiths@redhat.com) - initial implementation
 *     IBM Corporation - integration and code cleanup
 *******************************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Launch configuration tab for configuring the environment passed
 * into Runtime.exec(...) when a config is launched.
 */
public class EnvironmentTab extends AbstractLaunchConfigurationTab {

	private TableViewer environmentTable;
	private String[] envTableColumnHeaders =
	{
		"Variable",
		"Value",
	};
	private ColumnLayoutData[] envTableColumnLayouts =
	{
		new ColumnWeightData(50),
		new ColumnWeightData(50)
	};
	private static final String P_VARIABLE = "variable"; //$NON-NLS-1$
	private static final String P_VALUE = "value"; //$NON-NLS-1$
	private static String[] envTableColumnProperties =
	{
		P_VARIABLE,
		P_VALUE
	};
	private Button envAddButton;
	private Button envEditButton;
	private Button envRemoveButton;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());
		
		Composite composite = new Composite(mainComposite, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.heightHint = 150;
		composite.setLayout(glayout);
		composite.setLayoutData(gdata);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Environment");
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gdata);
		environmentTable = new TableViewer(composite);
		environmentTable.setContentProvider(new EnvironmentVariableContentProvider());
		environmentTable.setLabelProvider(new EnvironmentVariableLabelProvider());
		environmentTable.setColumnProperties(envTableColumnProperties);
		environmentTable.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				boolean enabled = !(environmentTable.getSelection().isEmpty());
				envEditButton.setEnabled(enabled);
				envRemoveButton.setEnabled(enabled);
			}
		});
		
		// Create columns
		Table table = environmentTable.getTable();
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		for (int i = 0; i < envTableColumnHeaders.length; i++)
		{
			tableLayout.addColumnData(envTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(envTableColumnLayouts[i].resizable);
			tc.setText(envTableColumnHeaders[i]);
		}
		gdata = new GridData(GridData.FILL_BOTH);
		environmentTable.getControl().setLayoutData(gdata);
		// Cell Editors
		TextCellEditor[] cellEditors = new TextCellEditor[envTableColumnHeaders.length];
		cellEditors[0] = new TextCellEditor(table);
		cellEditors[1] = new TextCellEditor(table);
		environmentTable.setCellEditors(cellEditors);
		environmentTable.setCellModifier(new ICellModifier()
		{
			public boolean canModify(Object element, String property) { return true; }
			public Object getValue(Object element, String property)
			{
				String result = null;
				EnvironmentVariable var = (EnvironmentVariable) element;
				if (property.equals(P_VARIABLE))
					result = var.getName();
				else if (property.equals(P_VALUE))
					result = var.getValue();
				return result;
			}
			public void modify(Object element, String property, Object value)
			{
				TableItem ti = (TableItem) element;
				EnvironmentVariable var = (EnvironmentVariable) ti.getData();
				if (property.equals(P_VARIABLE))
					var.setName((String) value);
				else if (property.equals(P_VALUE))
					var.setValue((String) value);
				else return;
				// update viewer's display and update the dialog
				String properties[] = new String[1];
				properties[0] = property;
				environmentTable.update(var, properties);
				updateLaunchConfigurationDialog();
			}
		});
		// Create buttons
		Composite bComposite = new Composite(mainComposite, SWT.NONE);
		glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		gdata = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		bComposite.setLayout(glayout);
		bComposite.setLayoutData(gdata);
		
		createVerticalSpacer(bComposite, 1);
		
		envAddButton = createPushButton(bComposite, "New", null);
		envAddButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				handleEnvAddButtonSelected();
			}
		});
		envEditButton = createPushButton(bComposite, "Edit", null);
		envEditButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				handleEnvEditButtonSelected();
			}
		});
		envEditButton.setEnabled(false);
		envRemoveButton = createPushButton(bComposite, "Remove", null);
		envRemoveButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				handleEnvRemoveButtonSelected();
			}
		});
		envRemoveButton.setEnabled(false);
	}
	
	/**
	 * Method handleEnvAddButtonSelected.
	 */
	private void handleEnvAddButtonSelected() {
		String name = new String("variable");
		String value = new String("value");
		EnvironmentVariable var = new EnvironmentVariable(name, value);
		environmentTable.add(var);
		environmentTable.editElement(var, 0);
	}

	/**
	 * Method handleEnvEditButtonSelected.
	 */
	private void handleEnvEditButtonSelected() {
		IStructuredSelection sel =
			(IStructuredSelection) environmentTable.getSelection();
		EnvironmentVariable var =
			(EnvironmentVariable) sel.getFirstElement();
		environmentTable.editElement(var, 1);
	}

	/**
	 * Method handleEnvRemoveButtonSelected.
	 */
	private void handleEnvRemoveButtonSelected() {
		IStructuredSelection sel =
			(IStructuredSelection) environmentTable.getSelection();
		EnvironmentVariable var =
			(EnvironmentVariable) sel.getFirstElement();
		environmentTable.remove(var);
	}

	/**
	 * Method updateEnvironment.
	 * @param configuration
	 */
	private void updateEnvironment(ILaunchConfiguration configuration) {
		environmentTable.setInput(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateEnvironment(configuration);
	}

	/**
	 * Stores the environment in the given configuration
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {	
		// Convert the table's items into a Map so that this can be saved in the
		// configuration's attributes.
		HashMap map = new HashMap();
		TableItem[] items = environmentTable.getTable().getItems();
		for (int i = 0; i < items.length; i++)
		{
			EnvironmentVariable var = (EnvironmentVariable) items[i].getData();
			map.put(var.getName(), var.getValue());
		} 
		if (map.size() == 0) {
			configuration.setAttribute(IDebugUIConstants.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		} else {
			configuration.setAttribute(IDebugUIConstants.ATTR_ENVIRONMENT_VARIABLES, map);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Environment";
	}

}
