/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide.dialogs;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * @since 3.4
 *
 */
public class ImportTypeDialog extends MessageDialog implements SelectionListener {

	/**
	 * @param parentShell
	 * @param operationMask
	 */
	public ImportTypeDialog(Shell parentShell, int operationMask) {
		super(parentShell, IDEWorkbenchMessages.ImportTypeDialog_title, null, IDEWorkbenchMessages.ImportTypeDialog_question,
				MessageDialog.QUESTION, new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 0);
		
		this.operationMask = operationMask;
		currentSelection = 0;
		String tmp = readContextPreference(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_TYPE);
		if (tmp.length() > 0)
			currentSelection = Integer.parseInt(tmp);
		currentSelection = currentSelection & operationMask;
		if (currentSelection == 0) {
			if ((operationMask & IMPORT_COPY) != 0)
				currentSelection = IMPORT_COPY;
			else
				currentSelection = IMPORT_MOVE;
		}
	}

	// the format of the context is operationMask,value:operationMask,value:operationMask,value
	private String readContextPreference(String key) {
		String value = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getString(key);
		String [] keyPairs = value.split(":"); //$NON-NLS-1$
		for (int i = 0; i < keyPairs.length; i++) {
			String [] element = keyPairs[i].split(","); //$NON-NLS-1$
			if (element.length == 2) {
				if (element[0].equals(Integer.toString(operationMask)))
					return element[1];
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	private void writeContextPreference(String key, String value) {
		String oldValue = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getString(key);
		StringBuffer buffer = new StringBuffer();
		String [] keyPairs = oldValue.split(":"); //$NON-NLS-1$
		boolean found = false;
		for (int i = 0; i < keyPairs.length; i++) {
			if (i > 0)
				buffer.append(":"); //$NON-NLS-1$
			String [] element = keyPairs[i].split(","); //$NON-NLS-1$
			if (element.length == 2) {
				if (element[0].equals(Integer.toString(operationMask))) {
					buffer.append(element[0] + "," + value); //$NON-NLS-1$
					found = true;
				}
				else
					buffer.append(keyPairs[i]);
			}
		}
		if (!found) {
			if (buffer.length() > 0)
				buffer.append(":"); //$NON-NLS-1$
			buffer.append(Integer.toString(operationMask) + "," + value); //$NON-NLS-1$
		}
		String newValue = buffer.toString();
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(key, newValue);
	}
	
	/**
	 * Generate linked resources relative to their preferred variable (typically the PROJECT_LOC variable)
	 */
	public static final String AUTOMATIC = "PROJECT_LOC"; //$NON-NLS-1$
	/**
	 * Generate linked resources with absolute path locations.
	 */
	public static final String ABSOLUTE_PATH = "default"; //$NON-NLS-1$

	/**
	 * @return The currently selected variable, or AUTOMATIC or ABSOLUTE_PATH
	 */
	public String getVariable() {
		if (variable == ABSOLUTE_PATH)
			return null;
		return variable;
	}

	/**
	 * Copy the files and folders to the destination
	 */
	public final static int IMPORT_COPY 			= 1;
	/**
	 * Recreate the file and folder hierarchy using groups and links
	 */
	public final static int IMPORT_GROUPS_AND_LINKS = 2;
	/**
	 * Create linked resources for each file and folder
	 */
	public final static int IMPORT_LINK 			= 4;
	/**
	 * Move the files and folders to the destination
	 */
	public final static int IMPORT_MOVE 			= 8;
	/**
	 * Do not perform an import operation
	 */
	public final static int IMPORT_NONE 			= 0;

	/**
	 * @return The current selection (one of IMPORT_COPY, IMPORT_GROUPS_AND_LINKS, IMPORT_LINK and IMPORT_MOVE)
	 */
	public int getSelection() {
		return currentSelection;
	}
	
	private int operationMask;
	private int currentSelection;
	private Button copyButton = null;
	private Button shadowCopyButton = null;
	private Button linkButton = null;
	private Button moveButton = null;
	private Combo variableCombo = null;
	private Button alwaysPerformThisOperation = null;
	private Label variableLabel = null;
	private String variable = ABSOLUTE_PATH;
	private IProject receivingProject = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#open()
	 */
	public int open() {
		String showDialogMap = readContextPreference(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_SHOW_DIALOG);
		if (showDialogMap.length() == 0 || !Boolean.valueOf(showDialogMap).booleanValue())
			return super.open();
		return Window.OK;
	}

	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, 0);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		composite.setLayoutData(gridData);

 		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		layout.marginLeft = 32;
		composite.setLayout(layout);

		if ((operationMask & IMPORT_COPY) != 0) {
			copyButton = new Button(composite, SWT.RADIO);
			copyButton.setText(IDEWorkbenchMessages.ImportTypeDialog_copyFilesAndDirectories);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			copyButton.setLayoutData(gridData);
			copyButton.setData(new Integer(IMPORT_COPY));
			copyButton.addSelectionListener(this);
		}
		
		if ((operationMask & IMPORT_MOVE) != 0) {
			moveButton = new Button(composite, SWT.RADIO);
			moveButton.setText(IDEWorkbenchMessages.ImportTypeDialog_moveFilesAndDirectories);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			moveButton.setLayoutData(gridData);
			moveButton.setData(new Integer(IMPORT_MOVE));
			moveButton.addSelectionListener(this);
		}

		if ((operationMask & IMPORT_GROUPS_AND_LINKS) != 0) {
			shadowCopyButton = new Button(composite, SWT.RADIO);
			shadowCopyButton.setText(IDEWorkbenchMessages.ImportTypeDialog_recreateFilesAndDirectories);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			shadowCopyButton.setLayoutData(gridData);
			shadowCopyButton.setData(new Integer(IMPORT_GROUPS_AND_LINKS));
			shadowCopyButton.addSelectionListener(this);
		}
		if ((operationMask & IMPORT_LINK) != 0) {
			linkButton = new Button(composite, SWT.RADIO);
			linkButton.setText(IDEWorkbenchMessages.ImportTypeDialog_createLinks);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			linkButton.setLayoutData(gridData);
			linkButton.setData(new Integer(IMPORT_LINK));
			linkButton.addSelectionListener(this);
		}

		if ((operationMask & (IMPORT_GROUPS_AND_LINKS | IMPORT_LINK)) != 0) {
			Composite variableGroup = new Composite(composite, 0);
			gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			variableGroup.setLayoutData(gridData);

	 		layout = new GridLayout();
			layout.numColumns = 2;
			layout.verticalSpacing = 9;
			variableGroup.setLayout(layout);

			variableLabel = new Label(variableGroup, 0);
			variableLabel.setText(IDEWorkbenchMessages.ImportTypeDialog_importElementsAs);
			gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			variableLabel.setLayoutData(gridData);
			
			variableCombo = new Combo(variableGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING); // GridData.FILL_HORIZONTAL);
			variableCombo.setLayoutData(gridData);
			variableCombo.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					if (variableCombo.getSelectionIndex() == (variableCombo.getItemCount() -1))
						editVariables();
					else
						selectVariable(variableCombo.getItem(variableCombo.getSelectionIndex()));
				}
				public void widgetSelected(SelectionEvent e) {
					if (variableCombo.getSelectionIndex() == (variableCombo.getItemCount() -1))
						editVariables();
					else
						selectVariable(variableCombo.getItem(variableCombo.getSelectionIndex()));
				}
			});
			setupVariableContent();
			selectVariable(readContextPreference(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_VARIABLE));
		}
		
		alwaysPerformThisOperation = new Button(composite, SWT.CHECK);
		alwaysPerformThisOperation.setText(IDEWorkbenchMessages.ImportTypeDialog_alwaysPerformThisOperation);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		alwaysPerformThisOperation.setLayoutData(gridData);

		refreshSelection();
		return composite;
	}
	
	private void editVariables() {
		PathVariableEditDialog dialog = new PathVariableEditDialog(getShell());
		dialog.setProject(receivingProject);
		if (dialog.open() == IDialogConstants.OK_ID) {
			String[] variableNames = (String[]) dialog.getResult();
			if (variableNames != null && variableNames.length >= 1) {
				variable = variableNames[0];
			}
		}
		setupVariableContent();
		selectVariable(variable);
	}

	private void selectVariable(String var) {
		if (var.equals(IDEWorkbenchMessages.ImportTypeDialog_automatic) || var.equals(AUTOMATIC)) {
			variableCombo.select(0);
			variable = var;
		}
		else
		if (var.equals(IDEWorkbenchMessages.ImportTypeDialog_absolutePath) || var.equals(ABSOLUTE_PATH)) {
			variableCombo.select(1);
			variable = var;
		}
		else {
			String[] items = variableCombo.getItems();
			for (int i = 0; i < items.length; i++) {
				if (var.equals(items[i])) {
					variableCombo.select(i);
					if (i == 0)
						variable = AUTOMATIC;
					else
					if (i == 1)
						variable = ABSOLUTE_PATH;
					else
						variable = items[i];
					return;
				}
			}
			variableCombo.select(0);
			variable = AUTOMATIC;
		}
	}
	private void setupVariableContent() {
		String[] items;
		IPathVariableManager pathVariableManager;
		if (receivingProject != null)
			pathVariableManager = receivingProject.getPathVariableManager();
		else
			pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		String[] variables = pathVariableManager.getPathVariableNames();
		items = new String[variables.length + 3];
		items[0] = IDEWorkbenchMessages.ImportTypeDialog_automatic;
		items[1] = IDEWorkbenchMessages.ImportTypeDialog_absolutePath;
		for (int i = 0; i < variables.length; i++)
			items[i + 2] = variables[i];
		items[items.length - 1] = IDEWorkbenchMessages.ImportTypeDialog_editVariables;
		variableCombo.setItems(items);
		super.getShell().layout(true);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			writeContextPreference(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_TYPE, Integer.toString(currentSelection));
			String storageVariable = currentSelection == 0? AUTOMATIC: (currentSelection == 1 ? ABSOLUTE_PATH:variable);
			writeContextPreference(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_VARIABLE, storageVariable);
			writeContextPreference(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_SHOW_DIALOG, Boolean.toString(alwaysPerformThisOperation.getSelection()));
		}
		super.buttonPressed(buttonId);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		currentSelection = ((Integer) e.widget.getData()).intValue();
		refreshSelection();
	}

	public void widgetSelected(SelectionEvent e) {
		currentSelection = ((Integer) e.widget.getData()).intValue();
		refreshSelection();
	}
	
	private void refreshSelection() {
		if (copyButton != null)
			copyButton.setSelection(currentSelection == IMPORT_COPY);
		if (shadowCopyButton != null)
			shadowCopyButton.setSelection(currentSelection == IMPORT_GROUPS_AND_LINKS);
		if (linkButton != null)
			linkButton.setSelection(currentSelection == IMPORT_LINK);
		if (moveButton != null)
			moveButton.setSelection(currentSelection == IMPORT_MOVE);
		if (variableCombo != null)
			variableCombo.setEnabled((currentSelection & (IMPORT_GROUPS_AND_LINKS | IMPORT_LINK)) != 0);
		if (variableLabel != null)
			variableLabel.setEnabled((currentSelection & (IMPORT_GROUPS_AND_LINKS | IMPORT_LINK)) != 0);
	}

	/** Set the project that is the destination of the import operation
	 * @param project
	 */
	public void setProject(IProject project) {
		receivingProject = project;
	}
}
