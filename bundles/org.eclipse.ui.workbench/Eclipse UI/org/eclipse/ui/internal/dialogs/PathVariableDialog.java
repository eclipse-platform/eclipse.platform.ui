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

import java.io.File;
import java.util.Set;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Dialog that prompts the user for defining a variable's name and value. It
 * supports creating a new variable or editing an existing one. The difference
 * between the two uses is just a matter of which messages to present to the
 * user and whether the "Ok" button starts enabled or not.
 */
public class PathVariableDialog extends TitleAreaDialog {

    // UI widgets
	private Button okButton;
    private Label variableNameLabel;
    private Label variableValueLabel;    
	private Text variableNameField;
	private Text variableValueField;
	private Button fileButton;
	private Button folderButton;

    /**
     * This dialog type: <code>NEW_VARIABLE</code> or
     * <code>EXISTING_VARIABLE</code>.
     */
	private int type;
    
    /**
     * The name of the variable being edited.
     */
    private String variableName;
    /**
     * The value of the variable being edited.
     */
    private String variableValue;        
    /**
     * The original name of the variable being edited. It is used when testing
     * if the current variable's name is already in use.
     */
    private String originalName;
    
    /**
     * Used to select the proper message depending on the current mode
     * (new/existing variable).
     */    
	private String typeKeySuffix;

    /**
     * Reference to the path variable manager. It is used for validating
     * variable names.
     */
	private IPathVariableManager pathVariableManager;
    
    /**
     * Set of variable names currently in use. Used when warning the user that
     * the currently selected name is already in use by another variable.
     */
	private Set namesInUse;
    
    /**
     * The current validation status. Its value can be one of the following:<ul>
     * <li><code>IMessageProvider.NONE</code> (default);</li>
     * <li><code>IMessageProvider.WARNING</code>;</li>
     * <li><code>IMessageProvider.ERROR</code>;</li>
     * </ul>
     * Used when validating the user input.
     */
    private int validationStatus;
    
    /**
     * The standard message to be shown when there are no problems being
     * reported.
     */    
    private String standardMessage;

    /**
     * Constant for defining this dialog as intended to create a new variable
     * (value = 1).
     */
    public final static int NEW_VARIABLE = 1;
    /**
     * Constant for defining this dialog as intended to edit an existing
     * variable (value = 2).
     */    
    public final static int EXISTING_VARIABLE = 2;    

/**
 * Constructs a dialog for editing a new/existing path variable.
 * 
 * @param parentShell the parent shell
 * @param type the dialog type: <code>NEW_VARIABLE</code> or
 * <code>EXISTING_VARIABLE</code>
 * @param pathVariableManager a reference to the path variable manager
 * @param namesInUse a set of variable names currently in use 
 */
public PathVariableDialog(Shell parentShell, int type, IPathVariableManager pathVariableManager, Set namesInUse) {
	super(parentShell);
	this.type = type;
	this.typeKeySuffix = type == NEW_VARIABLE ? "newVariable" : "existingVariable"; //$NON-NLS-1$ //$NON-NLS-2$        
	this.variableName = ""; //$NON-NLS-1$
	this.variableValue = ""; //$NON-NLS-1$

	this.pathVariableManager = pathVariableManager;
	this.namesInUse = namesInUse;
}
/**
 * Configures this dialog's shell, setting the shell's text.
 * 
 * @see org.eclipse.jface.window.Window#configureShell(Shell)
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText(WorkbenchMessages.getString("PathVariableDialog.shellTitle." + typeKeySuffix)); //$NON-NLS-1$
}
/**
 * Creates and returns the contents of this dialog (except for the button bar).
 * 
 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea
 */
protected Control createDialogArea(Composite parent) {
	// top level composite
	Composite parentComposite = (Composite) super.createDialogArea(parent);
    
    // creates dialog area composite
	Composite contents = createComposite(parentComposite);

    // creates and lay outs dialog area widgets 
	createWidgets(contents,parent.getFont());
    
    // validate possibly already incorrect variable definitions
    if (type == EXISTING_VARIABLE)
        validateVariableValue();

	return contents;
}

/**
 * Creates and configures this dialog's main composite.
 * 
 * @param parentComposite parent's composite
 * @return this dialog's main composite
 */
private Composite createComposite(Composite parentComposite) {
	// creates a composite with standard margins and spacing
	Composite contents = new Composite(parentComposite, SWT.NONE);
	
	FormLayout layout = new FormLayout();
	
	layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
	
	contents.setLayout(layout);
	contents.setFont(parentComposite.getFont());
	
	setTitle(WorkbenchMessages.getString("PathVariableDialog.dialogTitle." + typeKeySuffix)); //$NON-NLS-1$
	setMessage(WorkbenchMessages.getString("PathVariableDialog.message." + typeKeySuffix)); //$NON-NLS-1$);
	return contents;
}

/**
 * Creates widgets for this dialog.
 * 
 * @param parent the parent composite where to create widgets
 * @param contents 
 */
private void createWidgets(Composite contents,Font font) {
	FormData data;
	
	String nameLabelText = WorkbenchMessages.getString("PathVariableDialog.variableName"); //$NON-NLS-1$
	String valueLabelText = WorkbenchMessages.getString("PathVariableDialog.variableValue"); //$NON-NLS-1$
	
	// variable name label
	variableNameLabel = new Label(contents, SWT.LEFT);
	variableNameLabel.setText(nameLabelText);
	
	data = new FormData();
	variableNameLabel.setLayoutData(data);
	variableNameLabel.setFont(font);
	
	// variable value label
	variableValueLabel = new Label(contents, SWT.LEFT);
	variableValueLabel.setText(valueLabelText);
	
	data = new FormData();
	data.top = new FormAttachment(variableNameLabel, convertVerticalDLUsToPixels(10));
	variableValueLabel.setLayoutData(data);
	variableValueLabel.setFont(font);
	
	// the larger label will be used in the left attachments for the fields  
	Label largerLabel = nameLabelText.length() > valueLabelText.length() ? variableNameLabel : variableValueLabel;
	
	// variable name field
	variableNameField = new Text(contents, SWT.SINGLE | SWT.BORDER);
	variableNameField.setText(variableName);
	
	data = new FormData();
	data.width = convertWidthInCharsToPixels(50);
	data.left = new FormAttachment(largerLabel, convertHorizontalDLUsToPixels(5));
	variableNameField.setLayoutData(data);
	variableNameField.setFocus();
	
	variableNameField.addModifyListener(new ModifyListener() {        
	    public void modifyText(ModifyEvent event) {
            variableNameModified();
	    }
	});
	
	// variable value field
	variableValueField = new Text(contents, SWT.SINGLE | SWT.BORDER);
	variableValueField.setText(variableValue);
	
	data = new FormData();
	data.width = convertWidthInCharsToPixels(50);
	data.left = new FormAttachment(largerLabel, convertHorizontalDLUsToPixels(5));
	data.top = new FormAttachment(variableNameLabel, convertVerticalDLUsToPixels(10));
	variableValueField.setLayoutData(data);
	
	variableValueField.addModifyListener(new ModifyListener() {
	    public void modifyText(ModifyEvent event) {
			variableValueModified();
	    }
	});
	
	// select file path button
	fileButton = new Button(contents, SWT.PUSH);
	fileButton.setText(WorkbenchMessages.getString("PathVariableDialog.file")); //$NON-NLS-1$
	
	data = new FormData();
	data.top = new FormAttachment(variableNameLabel, convertVerticalDLUsToPixels(10));
	data.left = new FormAttachment(variableValueField, convertHorizontalDLUsToPixels(10));
	data.right = new FormAttachment(100, -5);
	fileButton.setLayoutData(data);
	
	fileButton.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent e) {
	        selectFile();
	    }
	});    
	
	// select folder path button
	folderButton = new Button(contents, SWT.PUSH);
	folderButton.setText(WorkbenchMessages.getString("PathVariableDialog.folder")); //$NON-NLS-1$
	
	data = new FormData();
	data.top = new FormAttachment(variableValueLabel, convertVerticalDLUsToPixels(10));
	data.left = new FormAttachment(variableValueField, convertHorizontalDLUsToPixels(10));
	data.right = new FormAttachment(100, -5);
	folderButton.setLayoutData(data);
	
	folderButton.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent e) {
	        selectFolder();
	    }
	});
}

/**
 * Fires validations (variable name first) and updates enabled state for the
 * "Ok" button accordingly.
 */
private void variableNameModified() {
    // updates and validates the variable name
    variableName = variableNameField.getText().trim();
    validationStatus = IMessageProvider.NONE;
    okButton.setEnabled(validateVariableName() && validateVariableValue());            
}
/**
 * Fires validations (variable value first) and updates enabled state for the
 * "Ok" button accordingly.
 */
private void variableValueModified() {
    // updates and validates the variable value
    variableValue = variableValueField.getText().trim();
    validationStatus = IMessageProvider.NONE;
    okButton.setEnabled(validateVariableValue() && validateVariableName());
}

/**
 * Opens a dialog where the user can select a folder path.
 */
private void selectFolder() {
	DirectoryDialog dialog = new DirectoryDialog(getShell());
	dialog.setText(WorkbenchMessages.getString("PathVariableDialog.selectFolderTitle")); //$NON-NLS-1$
	dialog.setMessage(WorkbenchMessages.getString("PathVariableDialog.selectFolderMessage")); //$NON-NLS-1$
	dialog.setFilterPath(variableValue);
	String res = dialog.open();
	if (res != null) {
		variableValue = new Path(res).makeAbsolute().toString();
		variableValueField.setText(variableValue);
	}
}

/**
 * Opens a dialog where the user can select a file path.
 */
private void selectFile() {
	FileDialog dialog = new FileDialog(getShell());
	dialog.setText(WorkbenchMessages.getString("PathVariableDialog.selectFileTitle")); //$NON-NLS-1$
	dialog.setFilterPath(variableValue);
	String res = dialog.open();
	if (res != null) {
		variableValue = new Path(res).makeAbsolute().toString();
		variableValueField.setText(variableValue);
	}
}

/**
 * Adds buttons to this dialog's button bar.
 * 
 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar
 */
protected void createButtonsForButtonBar(Composite parent) {
	okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	okButton.setEnabled(type == EXISTING_VARIABLE);
    
	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
}

/**
 * Validates the current variable name, and updates this dialog's message.
 * 
 * @return true if the name is valid, false otherwise
 */
private boolean validateVariableName() {    
    
    // if the current validationStatus is ERROR, no additional validation applies
    if (validationStatus == IMessageProvider.ERROR)
        return false;

    // assumes everything will be ok
	String message = null;
	int newValidationStatus = IMessageProvider.NONE;    
    
	if (variableName.length() == 0) {
        // the variable name is empty
		newValidationStatus = IMessageProvider.ERROR;
		message = WorkbenchMessages.getString("PathVariableDialog.variableNameEmptyMessage"); //$NON-NLS-1$
	} else if (!pathVariableManager.validateName(variableName).isOK()) {
        // the variable name is not valid
        //TODO: can get a better message from the returned IStatus object
		newValidationStatus = IMessageProvider.ERROR;
		message = WorkbenchMessages.getString("PathVariableDialog.variableNameInvalidMessage"); //$NON-NLS-1$
	} else if (namesInUse.contains(variableName) && !variableName.equals(originalName)) {
        // the variable name is already in use (warning)
		message = WorkbenchMessages.getString("PathVariableDialog.variableAlreadyExistsMessage"); //$NON-NLS-1$
		newValidationStatus = IMessageProvider.WARNING;
	}

    // overwrite the current validation status / message only if everything is ok (clearing them)
    // or if we have a more serious problem than the current one
	if (validationStatus == IMessageProvider.NONE || newValidationStatus == IMessageProvider.ERROR) {
		validationStatus = newValidationStatus;
		setMessage(message, validationStatus);
	}
    // only ERRORs are not acceptable
	return validationStatus != IMessageProvider.ERROR;
}

/**
 * Validates the current variable value, and updates this dialog's message.
 * 
 * @return true if the value is valid, false otherwise
 */
private boolean validateVariableValue() {

    // if the current validationStatus is ERROR, no additional validation applies
    if (validationStatus == IMessageProvider.ERROR)
        return false;

    // assumes everything will be ok
	String message = null;
	int newValidationStatus = IMessageProvider.NONE;

	if (variableValue.length() == 0) {
        // the variable value is empty
		message = WorkbenchMessages.getString("PathVariableDialog.variableValueEmptyMessage"); //$NON-NLS-1$
		newValidationStatus = IMessageProvider.ERROR;
	} else if (!Path.EMPTY.isValidPath(variableValue)) {
        // the variable value is an invalid path
		message = WorkbenchMessages.getString("PathVariableDialog.variableValueInvalidMessage"); //$NON-NLS-1$
		newValidationStatus = IMessageProvider.ERROR;
	} else if (!new File(variableValue).exists()) {
        // the path does not exist (warning)
        message = WorkbenchMessages.getString("PathVariableDialog.pathDoesNotExistMessage"); //$NON-NLS-1$
        newValidationStatus = IMessageProvider.WARNING;
    }

    // overwrite the current validation status / message only if everything is ok (clearing them)
    // or if we have a more serious problem than the current one
	if (validationStatus == IMessageProvider.NONE || newValidationStatus > validationStatus) {
		validationStatus = newValidationStatus;
		setMessage(message, validationStatus);
	}
    
    // only ERRORs are not acceptable
	return validationStatus != IMessageProvider.ERROR;
}

/**
 * Returns the variable name.
 * 
 * @return the variable name
 */
public String getVariableName() {
	return variableName;
}

/**
 * Returns the variable value.
 * 
 * @return the variable value
 */
public String getVariableValue() {
	return variableValue;
}

/**
 * Sets the variable name.
 * 
 * @param variableName the new variable name
 */
public void setVariableName(String variableName) {
	this.variableName = variableName.trim();
	this.originalName = this.variableName;
}

/**
 * Sets the variable value.
 * 
 * @param variableValue the new variable value
 */
public void setVariableValue(String variableValue) {
	this.variableValue = variableValue;
}

}
