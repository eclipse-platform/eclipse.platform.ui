/************************************************************************
Copyright (c) 2002, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A selection dialog which shows the path variables defined in the 
 * workspace.
 * The <code>getResult</code> method returns the name(s) of the 
 * selected path variable(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 *  PathVariableSelectionDialog dialog =
 *    new PathVariableSelectionDialog(getShell(), IResource.FOLDER);
 *	dialog.open();
 *	String[] result = (String[]) dialog.getResult();
 * </pre> 	
 * </p>
 * 
 * @since 2.1
 */
public class PathVariableSelectionDialog extends SelectionDialog {
	private static final int EXTEND_ID = IDialogConstants.CLIENT_ID + 1;
	private PathVariablesGroup pathVariablesGroup;
	private int variableType;

/**
 * Creates a path variable selection dialog.
 *
 * @param parentShell the parent shell
 * @param variableType the type of variables that are displayed in 
 * 	this dialog. <code>IResource.FILE</code> and/or <code>IResource.FOLDER</code>
 * 	logically ORed together.
 */
public PathVariableSelectionDialog(Shell parentShell, int variableType) {
	super(parentShell);
	setTitle(WorkbenchMessages.getString("PathVariableSelectionDialog.title")); //$NON-NLS-1$
	this.variableType = variableType;
	pathVariablesGroup = new PathVariablesGroup(
		false, 
		variableType,
		new Listener() {
			public void handleEvent(Event event) {
				updateExtendButtonState();
			}
		}
	);
	setShellStyle(getShellStyle() | SWT.RESIZE);
}
/**
 * Handles an "Extend" button press.
 * 
 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
 */
protected void buttonPressed(int buttonId) {
	if (buttonId == EXTEND_ID) {
		FileFolderSelectionDialog dialog = new FileFolderSelectionDialog(getShell(), false, variableType);
		PathVariablesGroup.PathVariableElement selection = pathVariablesGroup.getSelection()[0];
		dialog.setTitle(WorkbenchMessages.getString("PathVariableSelectionDialog.ExtensionDialog.title")); //$NON-NLS-1$
		dialog.setMessage(WorkbenchMessages.format("PathVariableSelectionDialog.ExtensionDialog.description", new Object[] {selection.name})); //$NON-NLS-1$
		dialog.setInput(selection.path.toFile());
		if (dialog.open() == FileFolderSelectionDialog.OK && pathVariablesGroup.performOk()) {
			setExtensionResult(selection, (File) dialog.getResult()[0]);
			super.okPressed();
		}
	}
	else
		super.buttonPressed(buttonId);
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	WorkbenchHelp.setHelp(shell, IHelpContextIds.PATH_VARIABLE_SELECTION_DIALOG);
}
/**
 * Adds an Extend button in addition to OK, Cancel.
 * 
 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
 */
protected void createButtonsForButtonBar(Composite parent) {
	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	createButton(parent, EXTEND_ID, WorkbenchMessages.getString("PathVariableSelectionDialog.extendButton"), false);	
	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	updateExtendButtonState();
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	// create composite 
	Composite dialogArea = (Composite)super.createDialogArea(parent);

	pathVariablesGroup.createContents(dialogArea);
	return dialogArea;
}
/**
 * Disposes the path variables group.
 * @see org.eclipse.jface.window.Window#close()
 */
public boolean close() {
	pathVariablesGroup.dispose();
	return super.close();
}
/**
 * Sets the dialog result to the selected path variable name(s). 
 */
protected void okPressed() {
	if (pathVariablesGroup.performOk()) {
		PathVariablesGroup.PathVariableElement[] selection = pathVariablesGroup.getSelection();
		String[] variableNames = new String[selection.length];
		
		for (int i = 0; i < selection.length; i++)
			variableNames[i] = selection[i].name;
		setSelectionResult(variableNames);
	}
	else {
		setSelectionResult(null);
	}
	super.okPressed();
}
/**
 * Sets the dialog result to the concatenated variable name and extension.
 * 
 * @param variable variable selected in the variables list and extended
 * 	by <code>extensionFile</code>
 * @param extensionFile file selected to extend the variable.
 */
private void setExtensionResult(PathVariablesGroup.PathVariableElement variable, File extensionFile) {
	IPath extensionPath = new Path(extensionFile.getPath());
	int matchCount = extensionPath.matchingFirstSegments(variable.path);
	IPath resultPath = new Path(variable.name);
		
	extensionPath = extensionPath.removeFirstSegments(matchCount);
	resultPath = resultPath.append(extensionPath);			
	setSelectionResult(new String[] {resultPath.toOSString()});
}
/**
 * Updates the enabled state of the Extend button based on the 
 * current variable selection.
 */
protected void updateExtendButtonState() {
	PathVariablesGroup.PathVariableElement[] selection = pathVariablesGroup.getSelection();
	Button extendButton = getButton(EXTEND_ID);
	
	if (extendButton == null)
		return;
	if (selection.length == 1) {
		File file = selection[0].path.toFile();	
		if (file.exists() == false || file.isFile())
			extendButton.setEnabled(false);
		else
			extendButton.setEnabled(true);
	}
	else
		extendButton.setEnabled(false);	
}

}
