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
package org.eclipse.ui.externaltools.internal.launchConfigurations;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.externaltools.internal.dialog.ExternalToolVariableForm;
import org.eclipse.ui.externaltools.internal.group.IGroupDialogPage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolVariable;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog that prompts the user to select an external tools variable
 */
class VariableSelectionDialog extends SelectionDialog {
	private ExternalToolVariableForm form;
	public VariableSelectionDialog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setTitle(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsMainTab.Select_variable_10")); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		WorkbenchHelp.setHelp(shell, IExternalToolsHelpContextIds.VARIABLE_SELECTION_DIALOG);
	}
	protected Control createDialogArea(Composite parent) {
		// Create the dialog area
		Composite composite= (Composite)super.createDialogArea(parent);
		ExternalToolVariable[] variables= ExternalToolsPlugin.getDefault().getToolVariableRegistry().getVariables();
		form= new ExternalToolVariableForm(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsMainTab.&Choose_a_variable__11"), variables); //$NON-NLS-1$
		form.createContents(composite, new IGroupDialogPage() {
			
			public void setErrorMessage(String errorMessage) {
				VariableSelectionDialog.this.setMessage(errorMessage);
			}

			public void updateValidState() {
			}

			public String getMessage() {
				if (!form.isValid()) {
					return ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsMainTab.Invalid_selection_12"); //$NON-NLS-1$
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
