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
package org.eclipse.debug.ui.variables;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog that prompts the user to select an external tools variable
 */
public class VariableSelectionDialog extends SelectionDialog {
	private ExternalToolVariableForm form;
	public VariableSelectionDialog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setTitle(LaunchConfigurationsMessages.getString("VariableSelectionDialog.Select_variable_1")); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		WorkbenchHelp.setHelp(shell, IDebugHelpContextIds.VARIABLE_SELECTION_DIALOG);
	}
	protected Control createDialogArea(Composite parent) {
		// Create the dialog area
		Composite composite= (Composite)super.createDialogArea(parent);
		ExternalToolVariable[] variables= DebugUIPlugin.getDefault().getToolVariableRegistry().getVariables();
		form= new ExternalToolVariableForm(LaunchConfigurationsMessages.getString("VariableSelectionDialog.Choose_a_variable__2"), variables); //$NON-NLS-1$
		form.createContents(composite, new IGroupDialogPage() {
			
			public void setErrorMessage(String errorMessage) {
				VariableSelectionDialog.this.setMessage(errorMessage);
			}

			public void updateValidState() {
			}

			public String getMessage() {
				if (!form.isValid()) {
					return LaunchConfigurationsMessages.getString("VariableSelectionDialog.Invalid_selection_3"); //$NON-NLS-1$
				}
				return null;
			}

			public int getMessageType() {
				if (!form.isValid()) {
					return IMessageProvider.ERROR;
				}
				return 0;
			}
		});
		form.getVariableList().addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				okPressed();
			}
		});
		return composite;
	}

	public ExternalToolVariableForm getForm() {
		return form;
	}
}
