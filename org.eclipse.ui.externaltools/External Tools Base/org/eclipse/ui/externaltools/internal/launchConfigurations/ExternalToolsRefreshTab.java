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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.variables.ExternalToolVariable;
import org.eclipse.debug.ui.variables.ExternalToolVariableForm;
import org.eclipse.debug.ui.variables.IGroupDialogPage;
import org.eclipse.debug.ui.variables.VariableUtil;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;

public class ExternalToolsRefreshTab extends AbstractLaunchConfigurationTab implements IGroupDialogPage {

	private ExternalToolVariableForm variableForm;
	
	protected Button refreshField;
	protected Button recursiveField;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		WorkbenchHelp.setHelp(getControl(), IExternalToolsHelpContextIds.EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_REFRESH_TAB);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());
		createVerticalSpacer(mainComposite, 1);
		createRefreshComponent(mainComposite);
		createRecursiveComponent(mainComposite);
		createScopeComponent(mainComposite);
	}
	
	/**
	 * Creates the controls needed to edit the refresh recursive
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	private void createRecursiveComponent(Composite parent) {
		recursiveField = new Button(parent, SWT.CHECK);
		recursiveField.setText(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsRefreshTab.Recursively_&include_sub-folders_1")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		recursiveField.setLayoutData(data);
		recursiveField.setFont(parent.getFont());
		recursiveField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	/**
	 * Creates the controls needed to edit the refresh scope
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	private void createRefreshComponent(Composite parent) {
		refreshField = new Button(parent, SWT.CHECK);
		refreshField.setText(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsRefreshTab.&Refresh_resources_after_running_tool_1")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		refreshField.setLayoutData(data);
		refreshField.setFont(parent.getFont());
		refreshField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnabledState();
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	/**
	 * Creates the controls needed to edit the refresh scope variable
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	private void createScopeComponent(Composite parent) {
		String label = ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsRefreshTab.Choose_scope_v&ariable___2"); //$NON-NLS-1$
		ExternalToolVariable[] vars = ExternalToolsPlugin.getDefault().getRefreshVariableRegistry().getVariables();
		variableForm = new ExternalToolVariableForm(label, vars);
		variableForm.createContents(parent, this);
	}
	

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateRefresh(configuration);
		updateRecursive(configuration);
		updateScope(configuration);
	}
	/**
	 * Method udpateScope.
	 * @param configuration
	 */
	private void updateScope(ILaunchConfiguration configuration) {
		String scope = null;
		try {
			scope= configuration.getAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, (String)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsRefreshTab.Exception_reading_launch_configuration_3"), ce); //$NON-NLS-1$
		}
		String varName = null;
		String varValue = null;
		if (scope != null) {
			VariableUtil.VariableDefinition varDef = VariableUtil.extractVariableTag(scope, 0);
			varName = varDef.name;
			varValue = varDef.argument;
		}
		variableForm.selectVariable(varName, varValue);
	}
	/**
	 * Method updateRecursive.
	 * @param configuration
	 */
	private void updateRecursive(ILaunchConfiguration configuration) {
		boolean recursive= true;
		try {
			recursive= configuration.getAttribute(IExternalToolConstants.ATTR_REFRESH_RECURSIVE, true);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsRefreshTab.Exception_reading_launch_configuration_3"), ce); //$NON-NLS-1$
		}
		recursiveField.setSelection(recursive);
	}
	/**
	 * Method updateRefresh.
	 * @param configuration
	 */
	private void updateRefresh(ILaunchConfiguration configuration) {
		String scope= null;
		try {
			scope= configuration.getAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, (String)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsRefreshTab.Exception_reading_launch_configuration_3"), ce); //$NON-NLS-1$
		}
		refreshField.setSelection(scope != null);
		updateEnabledState();		
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		if (refreshField.getSelection()) {
			configuration.setAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, variableForm.getSelectedVariable());
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, (String)null);
		}
		
		setAttribute(IExternalToolConstants.ATTR_REFRESH_RECURSIVE, configuration, recursiveField.getSelection(), true);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsRefreshTab.Refres&h_6"); //$NON-NLS-1$
	}
	
	/**
	 * Updates the enablement state of the fields.
	 */
	private void updateEnabledState() {
		if (refreshField != null) {
			if (recursiveField != null) {
				recursiveField.setEnabled(refreshField.getSelection());
			}
			if (variableForm != null) {
				variableForm.setEnabled(refreshField.getSelection());
			}
		}
	}
	
	/**
	 * @see IGroupDialogPage#setErrorMessage(String)
	 */
	public void setErrorMessage(String errorMessage) {
		super.setErrorMessage(errorMessage);
	}

	/**
	 * @see org.eclipse.ui.externaltools.group.IGroupDialogPage#updateValidState()
	 */
	public void updateValidState() {
		updateLaunchConfigurationDialog();
	}

	/**
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	public int getMessageType() {
		if (getErrorMessage() != null) {
			return IMessageProvider.ERROR;
		} else if (getMessage() != null) {
			return IMessageProvider.WARNING;
		}
		return IMessageProvider.NONE;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return ExternalToolsImages.getImage(IExternalToolConstants.IMG_TAB_REFRESH);
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		return getErrorMessage() == null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		if (variableForm != null) {
			variableForm.dispose();
		}
		super.dispose();
	}
}