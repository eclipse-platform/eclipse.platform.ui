/**
 * Copyright IBM Corporation 2003
 */
package org.eclipse.ui.externaltools.internal.launchConfigurations;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.externaltools.internal.dialog.ExternalToolVariableForm;
import org.eclipse.ui.externaltools.internal.group.IGroupDialogPage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolVariable;


class VariableSelectionDialog extends SelectionDialog {
	private final ExternalToolsMainTab externalToolsMainTab;
	private ExternalToolVariableForm form;
	public VariableSelectionDialog(ExternalToolsMainTab externalToolsMainTab, Shell parent) {
		super(parent);
		setShellStyle(SWT.RESIZE);
		this.externalToolsMainTab = externalToolsMainTab;
		setTitle(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsMainTab.Select_variable_10")); //$NON-NLS-1$
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
		return composite;
	}

	public ExternalToolVariableForm getForm() {
		return form;
	}
}