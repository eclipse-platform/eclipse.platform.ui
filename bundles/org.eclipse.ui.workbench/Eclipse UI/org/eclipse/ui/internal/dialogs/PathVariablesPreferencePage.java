/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.util.*;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Preference page for path variables. This preference page shows all path
 * variables currently defined in the workspace's path variable manager. It
 * allows the user to add, edit and remove path variables. The changes are kept
 * in temporary collections, so only when the user confirms them (by confirming
 * when closing the "Preferences" dialog) all changes are effectively commited
 * to the path variable manager.
 * 
 * @see org.eclipse.ui.internal.dialogs.PathVariableDialog
 */
public class PathVariablesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	// temporary collection for keeping currently defined variables    
	private SortedMap tempPathVariables;

	// set of removed variables' names
	private Set removedVariableNames;

	// reference to the workspace's path variable manager    
	private IPathVariableManager pathVariableManager;

	// buttons for adding/removing/editing path variables     
	private Button addButton;
	private Button removeButton;
	private Button editButton;

	// widget for showing currently defined variables
	private Table variableTable;

	// file image
	private final Image FILE_IMG = WorkbenchImages.getImage(ISharedImages.IMG_OBJ_FILE);

	// folder image
	private final Image FOLDER_IMG = WorkbenchImages.getImage(ISharedImages.IMG_OBJ_FOLDER);

	/**
	 * Constructs a preference page of path variables, initializing internal state
	 * and omitting "Restore Defaults"/"Apply Changes" buttons.
	 */
	public PathVariablesPreferencePage() {
		this.pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();

		this.removedVariableNames = new HashSet();
		this.tempPathVariables = new TreeMap();

		this.noDefaultAndApplyButton();
	}

	/**
	 * Resets this page's internal state and creates its UI contents.
	 * 
	 * @see PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {

		// initialize internal model
		initTemporaryState();

		Font font = parent.getFont();

		// define container & its gridding
		Composite pageComponent = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		pageComponent.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		pageComponent.setLayoutData(data);
		pageComponent.setFont(font);

		//layout the contents

		Label topLabel = new Label(pageComponent, SWT.NONE);
		topLabel.setText(WorkbenchMessages.getString("PathVariablesPreference.explanation")); //$NON-NLS-1$
		topLabel.setLayoutData(data);
		topLabel.setFont(font);

		// layout the table & its buttons
		Label variableLabel = new Label(pageComponent, SWT.LEFT);
		variableLabel.setText(WorkbenchMessages.getString("PathVariablesPreference.variables")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		variableLabel.setLayoutData(data);
		variableLabel.setFont(font);

		variableTable = new Table(pageComponent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		variableTable.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				editSelectedVariable();
			}
			public void widgetSelected(SelectionEvent e) {
				updateEnabledState();
			}
		});
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = variableTable.getItemHeight() * 7;
		variableTable.setLayoutData(data);
		variableTable.setFont(font);

		Composite groupComponent = new Composite(pageComponent, SWT.NULL);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		groupComponent.setLayout(groupLayout);
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		groupComponent.setLayoutData(data);
		groupComponent.setFont(font);

		addButton = new Button(groupComponent, SWT.PUSH);
		addButton.setText(WorkbenchMessages.getString("PathVariablesPreference.addVariable")); //$NON-NLS-1$
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addNewVariable();
			}
		});
		addButton.setFont(font);
		setButtonLayoutData(addButton);

		removeButton = new Button(groupComponent, SWT.PUSH);
		removeButton.setText(WorkbenchMessages.getString("PathVariablesPreference.removeVariable")); //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeSelectedVariables();
			}
		});
		removeButton.setFont(font);
		setButtonLayoutData(removeButton);

		editButton = new Button(groupComponent, SWT.PUSH);
		editButton.setText(WorkbenchMessages.getString("PathVariablesPreference.editVariable")); //$NON-NLS-1$
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSelectedVariable();
			}
		});
		editButton.setFont(font);
		setButtonLayoutData(editButton);

		// populate table with current internal state and set buttons' initial state
		updateUIState(null);

		return pageComponent;

	}

	/**
	 * (Re-)Initialize collections used to mantain temporary state.
	 */
	private void initTemporaryState() {
		tempPathVariables.clear();
		String[] varNames = pathVariableManager.getPathVariableNames();
		for (int i = 0; i < varNames.length; i++) {
			IPath value = pathVariableManager.getValue(varNames[i]);
			// the value may not exist any more
			if (value != null)
				tempPathVariables.put(varNames[i], value);
		}
		removedVariableNames.clear();
	}

	/**
	 * Updates buttons'enabled state, depending on the number of currently selected
	 * variables in the table.
	 */
	private void updateEnabledState() {
		int itemsSelectedCount = variableTable.getSelectionCount();
		editButton.setEnabled(itemsSelectedCount == 1);
		removeButton.setEnabled(itemsSelectedCount > 0);
	}
	/**
	 * Rebuilds table widget state with the current list of variables (reflecting
	 * any changes, additions and removals), and selects the item corresponding to
	 * the given variable name. If the variable name is <code>null</code>, the
	 * first item (if any) will be selected.
	 * 
	 * @param selectedVarName the name for the variable to be selected (may be
	 * <code>null</code>)
	 * @see IPathVariableManager#getPathVariableNames()
	 * @see IPathVariableManager#getValue(String)
	 */
	private void updateVariableTable(String selectedVarName) {
		variableTable.removeAll();
		int selectedVarIndex = 0;
		for (Iterator varNames = tempPathVariables.keySet().iterator(); varNames.hasNext();) {
			TableItem item = new TableItem(variableTable, SWT.NONE);
			String varName = (String) varNames.next();
			IPath value = (IPath) tempPathVariables.get(varName);
			item.setText(varName + " - " + value.toString()); //$NON-NLS-1$ 
			// the corresponding variable name is stored in each table widget item
			item.setData(varName);
			item.setImage(value.toFile().isFile() ? FILE_IMG : FOLDER_IMG);
			if (varName.equals(selectedVarName))
				selectedVarIndex = variableTable.getItemCount() - 1;
		}
		if (variableTable.getItemCount() > selectedVarIndex)
			variableTable.setSelection(selectedVarIndex);
	}

	/**
	 * Empty implementation. This page does not use the workbench.
	 * 
	 * @see IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}

	/**
	 * Commits the temporary state to the path variable manager in response to user
	 * confirmation.
	 * 
	 * @see PreferencePage#performOk()
	 * @see IPathVariableManager#setValue(String, IPath)
	 */
	public boolean performOk() {
		try {
			// first process removed variables  
			for (Iterator removed = removedVariableNames.iterator(); removed.hasNext();) {
				String removedVariableName = (String) removed.next();
				// only removes variables that have not been added again
				if (!tempPathVariables.containsKey(removedVariableName))
					pathVariableManager.setValue(removedVariableName, null);
			}

			// then process the current collection of variables, adding/updating them
			for (Iterator current = tempPathVariables.entrySet().iterator(); current.hasNext();) {
				Map.Entry entry = (Map.Entry) current.next();
				String variableName = (String) entry.getKey();
				IPath variableValue = (IPath) entry.getValue();
				pathVariableManager.setValue(variableName, variableValue);
			}

			// re-initialize temporary state
			initTemporaryState();

			// performOk accepted
			return true;
		} catch (CoreException ce) {
			ErrorDialog.openError(getShell(), null, null, ce.getStatus());
		}
		return false;
	}

	/**
	 * Opens a dialog for editing an existing variable.
	 * 
	 * @see PathVariableDialog
	 */
	private void editSelectedVariable() {
		// retrieves the name and value for the currently selected variable
		TableItem item = variableTable.getItem(variableTable.getSelectionIndex());
		String variableName = (String) item.getData();
		IPath variableValue = (IPath) tempPathVariables.get(variableName);

		// constructs a dialog for editing the variable's current name and value
		PathVariableDialog dialog =
			new PathVariableDialog(this.getShell(), PathVariableDialog.EXISTING_VARIABLE, pathVariableManager, tempPathVariables.keySet());
		dialog.setVariableName(variableName);
		dialog.setVariableValue(variableValue.toString());

		// opens the dialog - just returns if the user cancels it
		if (dialog.open() == Window.CANCEL)
			return;

		// the name can be changed, so we remove the current variable definition...    
		removedVariableNames.add(variableName);
		tempPathVariables.remove(variableName);

		String newVariableName = dialog.getVariableName();
		IPath newVariableValue = new Path(dialog.getVariableValue());

		// and add it again (maybe with a different name)
		tempPathVariables.put(newVariableName, newVariableValue);

		// now we must refresh the UI state
		updateUIState(newVariableName);

	}
	/**
	 * Removes the currently selected variables.
	 */
	private void removeSelectedVariables() {
		// remove each selected element
		int[] selectedIndices = variableTable.getSelectionIndices();
		for (int i = 0; i < selectedIndices.length; i++) {
			TableItem selectedItem = variableTable.getItem(selectedIndices[i]);
			String varName = (String) selectedItem.getData();
			removedVariableNames.add(varName);
			tempPathVariables.remove(varName);
		}
		updateUIState(null);
	}

	/**
	 * Updates the UI's current state: refreshes the table with the current defined
	 * variables, selects the item corresponding to the given variable (selects
	 * the first item if <code>null</code> is provided) and updates the enabled
	 * state for the Add/Remove/Edit buttons.
	 * 
	 * @param selectedVarName the name of the variable to be selected (may be null)
	 */
	private void updateUIState(String selectedVarName) {
		updateVariableTable(selectedVarName);
		updateEnabledState();
	}

	/**
	 * Opens a dialog for creating a new variable.
	 */
	private void addNewVariable() {
		// constructs a dialog for editing the new variable's current name and value   
		PathVariableDialog dialog =
			new PathVariableDialog(this.getShell(), PathVariableDialog.NEW_VARIABLE, pathVariableManager, tempPathVariables.keySet());

		// opens the dialog - just returns if the user cancels it
		if (dialog.open() == Window.CANCEL)
			return;

		// otherwise, adds the new variable (or updates an existing one) in the 
		// temporary collection of currently defined variables    
		String newVariableName = dialog.getVariableName();
		IPath newVariableValue = new Path(dialog.getVariableValue());
		tempPathVariables.put(newVariableName, newVariableValue);

		// the UI must be updated
		updateUIState(newVariableName);
	}

}
