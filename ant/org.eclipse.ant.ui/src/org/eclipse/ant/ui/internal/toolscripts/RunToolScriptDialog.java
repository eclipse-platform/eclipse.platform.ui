/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ant.ui.internal.toolscripts;

import org.eclipse.ant.core.toolscripts.ExternalToolScript;
import org.eclipse.ant.core.toolscripts.ToolScript;
import org.eclipse.ant.ui.internal.AntUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 */
public class RunToolScriptDialog extends Dialog {
	protected Combo scriptField;
	protected ToolScript toolScript;
	protected static final int USE_WIZARD_ID = IDialogConstants.CLIENT_ID + 1;
/**
 * Constructor for RunToolScriptDialog.
 * @param parentShell
 */
protected RunToolScriptDialog(Shell parentShell) {
	super(parentShell);
}
/**
 * @see Dialog#createButtonsForButtonBar(Composite)
 */
protected void createButtonsForButtonBar(Composite parent) {
	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	createButton(parent, USE_WIZARD_ID, "Use Wizard...", false);
}
/**
 * @see Dialog#createDialogArea(Composite)
 */
protected Control createDialogArea(Composite parent) {
	Composite topLevel = new Composite(parent, SWT.NONE);
	topLevel.setLayout(new GridLayout());
	topLevel.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	Label label = new Label(topLevel, SWT.NONE);
	label.setText("Enter the name of the program or tool script to run:");
	
	scriptField = new Combo(topLevel, SWT.DROP_DOWN | SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
	scriptField.setLayoutData(data);
	
	//fill in the history
	String[] history = AntUIPlugin.getPlugin().getCommandHistory();
	if (history != null) {
		for (int i = 0; i < history.length; i++) {
			scriptField.add(history[i]);
		}
	}
	
	return topLevel;
}
/**
 * Returns the tool script entered by the user.  Returns
 * null if nothing was entered or the user cancelled.
 */
public ToolScript getToolScript() {
	return toolScript;
}
/**
 * @see Dialog#okPressed()
 */
protected void okPressed() {
	String commandLine = scriptField.getText();
	if (commandLine != null && commandLine.length() > 0) {
		toolScript = new ExternalToolScript(commandLine);
	}
	//add to history
	AntUIPlugin.getPlugin().addToCommandHistory(commandLine);
	super.okPressed();
}

}
