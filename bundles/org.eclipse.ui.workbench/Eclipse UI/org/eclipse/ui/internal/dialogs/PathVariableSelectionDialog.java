/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.ui.internal.dialogs;

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
 */
public class PathVariableSelectionDialog extends SelectionDialog {
	private PathVariablesBlock pathVariablesBlock;

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
	pathVariablesBlock = new PathVariablesBlock(false, variableType);
	setShellStyle(getShellStyle() | SWT.RESIZE);
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	WorkbenchHelp.setHelp(shell, IHelpContextIds.PATH_VARIABLE_SELECTION_DIALOG);
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	// create composite 
	Composite dialogArea = (Composite)super.createDialogArea(parent);

	pathVariablesBlock.createContents(dialogArea);
	return dialogArea;
}
/**
 * Sets the dialog result to the selected path variable name(s). 
 */
protected void okPressed() {
	if (pathVariablesBlock.performOk()) {
		String[] variableNames = pathVariablesBlock.getSelection(); 
		setSelectionResult(variableNames);
	}
	else {
		setSelectionResult(null);
	}
	super.okPressed();
}
}
