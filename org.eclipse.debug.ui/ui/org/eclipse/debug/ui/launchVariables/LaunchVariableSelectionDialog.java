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
package org.eclipse.debug.ui.launchVariables;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.variables.ILaunchVariable;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.launchVariables.LaunchVariableMessages;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog that prompts the user to select a launch configuration variable.
 * @since 3.0
 */
public class LaunchVariableSelectionDialog extends SelectionDialog {
	private LaunchConfigurationVariableForm form;
	private Composite formComposite;
	public LaunchVariableSelectionDialog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setTitle(LaunchVariableMessages.getString("VariableSelectionDialog.0")); //$NON-NLS-1$
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
		
		final Button contextVariables= createRadioButton(composite, LaunchVariableMessages.getString("VariableSelectionDialog.1")); //$NON-NLS-1$
		contextVariables.setSelection(true);
		contextVariables.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (contextVariables.getSelection()) {
					replaceVariableComposite(DebugPlugin.getDefault().getLaunchVariableManager().getContextVariables());
				}
			}
		});
		final Button simpleVariables= createRadioButton(composite, LaunchVariableMessages.getString("VariableSelectionDialog.2")); //$NON-NLS-1$
		simpleVariables.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (simpleVariables.getSelection()) {
				replaceVariableComposite(DebugPlugin.getDefault().getLaunchVariableManager().getSimpleVariables());
			}
		}
	});
		createVariableFormComposite(composite, DebugPlugin.getDefault().getLaunchVariableManager().getContextVariables());
		return composite;
	}
	
	/**
	 * Replaces the variable form with a form containing the given variables.
	 */
	private void replaceVariableComposite(ILaunchVariable[] variables) {
		if (formComposite != null) {
			formComposite.dispose();
		}
		createVariableFormComposite((Composite) getDialogArea(), variables);
		((Composite) getDialogArea()).layout(true);
		getDialogArea().redraw();
	}
	
	protected Button createRadioButton(Composite parent, String label) {
		Button button= new Button(parent, SWT.RADIO);
		button.setText(label);
		button.setLayoutData(new GridData());
		return button;
	}
	
	protected void createVariableFormComposite(Composite parent, ILaunchVariable[] variables) {
		formComposite= new Composite(parent, SWT.NONE);
		formComposite.setLayout(new GridLayout());
		form= new LaunchConfigurationVariableForm(LaunchVariableMessages.getString("VariableSelectionDialog.3"), variables); //$NON-NLS-1$
		form.createContents(formComposite, new IVariableComponentContainer() {
			
			public void setErrorMessage(String errorMessage) {
				LaunchVariableSelectionDialog.this.setMessage(errorMessage);
			}

			public void updateValidState() {
			}

			public String getMessage() {
				if (!form.isValid()) {
					return LaunchVariableMessages.getString("VariableSelectionDialog.4"); //$NON-NLS-1$
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
	}

	/**
	 * Returns this dialog's variable selection form, which allows
	 * the user to choose and configure a variable.
	 * @return this dialog's <code>LaunchConfigurationVariableForm</code>
	 */
	public LaunchConfigurationVariableForm getForm() {
		return form;
	}
}
