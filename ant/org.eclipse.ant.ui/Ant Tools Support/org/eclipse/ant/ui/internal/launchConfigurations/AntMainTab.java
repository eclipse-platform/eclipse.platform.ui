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
package org.eclipse.ant.ui.internal.launchConfigurations;

import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.IAntUIHelpContextIds;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.variables.IVariableConstants;
import org.eclipse.debug.ui.variables.VariableUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsLaunchConfigurationMessages;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsMainTab;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.ui.FileSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

public class AntMainTab extends ExternalToolsMainTab {

	protected Button captureOutputButton;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		updateCaptureOutput(configuration);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		setAttribute(IExternalToolConstants.ATTR_CAPTURE_OUTPUT, configuration, captureOutputButton.getSelection(), true);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		WorkbenchHelp.setHelp(mainComposite, IAntUIHelpContextIds.ANT_MAIN_TAB);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());
		createLocationComponent(mainComposite);
		createWorkDirectoryComponent(mainComposite);
		createArgumentComponent(mainComposite);
		createVerticalSpacer(mainComposite, 2);
		createRunBackgroundComponent(mainComposite);
		createCaptureOutputComponent(mainComposite);
	}
	
	/**
	 * Creates the controls needed to edit the capture output attribute of an
	 * external tool
	 *
	 * @param parent the composite to create the controls in
	 */
	protected void createCaptureOutputComponent(Composite parent) {
		captureOutputButton = new Button(parent, SWT.CHECK);
		captureOutputButton.setText(AntLaunchConfigurationMessages.getString("AntMainTab.Capture_&output_1")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		captureOutputButton.setLayoutData(data);
		captureOutputButton.setFont(parent.getFont());
		captureOutputButton.addSelectionListener(getSelectionAdapter());
	}
	
	protected void updateCaptureOutput(ILaunchConfiguration configuration) {
		boolean captureOutput= true;
		try {
			captureOutput= configuration.getAttribute(IExternalToolConstants.ATTR_CAPTURE_OUTPUT, true);
		} catch (CoreException ce) {
			AntUIPlugin.log(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsMainTab.Error_reading_configuration_7"), ce); //$NON-NLS-1$
		}
		captureOutputButton.setSelection(captureOutput);
	}

	/**
	 * Prompts the user for a workspace location within the workspace and sets
	 * the location as a String containing the workspace_loc variable or
	 * <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkspaceLocationButtonSelected() {
		FileSelectionDialog dialog;
		dialog = new FileSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), AntLaunchConfigurationMessages.getString("AntMainTab.&Select_a_build_file__1")); //$NON-NLS-1$
		dialog.setFileFilter("*.xml", true); //$NON-NLS-1$
		dialog.open();
		IFile file = dialog.getResult();
		if (file == null) {
			return;
		}
		StringBuffer buf = new StringBuffer();
		VariableUtil.buildVariableTag(IVariableConstants.VAR_WORKSPACE_LOC, file.getFullPath().toString(), buf);
		String text= buf.toString();
		if (text != null) {
			locationField.setText(text);
		}
	}

	/**
	 * @see org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsMainTab#getWorkingDirectoryLabel()
	 */
	protected String getWorkingDirectoryLabel() {
		return AntLaunchConfigurationMessages.getString("AntMainTab.Base_&Directory__3"); //$NON-NLS-1$
	}
}
