/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Dialog to prompt the user for confirmation before
 * exiting the workbench. User is given the option
 * to not show this dialog again. The prompt before
 * exit preference will be adjusted if the user selects
 * the OK button.
 */
public class PromptOnExitDialog extends Dialog {
	private String productName;
	
	private Label confirmationLabel;
	private Button neverPromptButton;
	
	/**
	 * Creates a dialog to prompt the user for
	 * confirmation before exiting.
	 * 
	 * @param parentShell the shell to parent this dialog
	 * @param productName the name of the product closing
	 */
	public PromptOnExitDialog(Shell parentShell, String productName) {
		super(parentShell);
		this.productName = productName;
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(WorkbenchMessages.getString("PromptOnExitDialog.shellTitle")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(shell, IHelpContextIds.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		Font font = parent.getFont();

		confirmationLabel = new Label(composite, SWT.WRAP);
		confirmationLabel.setText(WorkbenchMessages.format("PromptOnExitDialog.message", new Object[] {productName})); //$NON-NLS-1$
		confirmationLabel.setFont(font);
		confirmationLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		neverPromptButton = new Button(composite, SWT.CHECK);
		neverPromptButton.setText(WorkbenchMessages.getString("PromptOnExitDialog.choice")); //$NON-NLS-1$
		neverPromptButton.setFont(font);
		neverPromptButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		return composite;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.setValue(IPreferenceConstants.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, !neverPromptButton.getSelection());

		super.okPressed();
	}
}
