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
package org.eclipse.debug.internal.ui.launchConfigurations;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog used to edit & launch a single launch configuration.
 */
public class LaunchConfigurationDialog extends LaunchConfigurationPropertiesDialog {

	/**
	 * Constructs a dialog
	 * 
	 * @param shell
	 * @param launchConfiguration
	 * @param group
	 */
	public LaunchConfigurationDialog(Shell shell, ILaunchConfiguration launchConfiguration, LaunchGroupExtension group) {
		super(shell, launchConfiguration, group);
	}

	/**
	 * This dialog has 'Launch' and 'Close' buttons.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ID_LAUNCH_BUTTON, getLaunchButtonText(), true);
		createButton(parent, ID_CLOSE_BUTTON, LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Close_1"), false);  //$NON-NLS-1$
	}
	
	protected String getShellTitle() {
		return getLaunchConfiguration().getName();
	}
	
	protected String getTitleAreaTitle() {
		return LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Modify_attributes_and_launch._1"); //$NON-NLS-1$
	}
	/**
	 * @see ILaunchConfigurationDialog#updateButtons()
	 */
	public void updateButtons() {
		if (isInitializingTabs()) {
			return;
		}
				
		// Launch button
		getTabViewer().refresh();
		getButton(ID_LAUNCH_BUTTON).setEnabled(getTabViewer().canLaunch());
		
	}
		
	protected String getHelpContextId() {
		return IDebugHelpContextIds.SINGLE_LAUNCH_CONFIGURATION_DIALOG;
	}
	
	protected void initializeContent() {
		getTabViewer().setInput(getLaunchConfiguration());
		IStatus status = getInitialStatus();
		if (status != null) {
			handleStatus(status);
		}		
		resize();
	}
		
	/**
	 * Returns the name of the section that this dialog stores its settings in
	 *
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return IDebugUIConstants.PLUGIN_ID + ".SINGLE_LAUNCH_CONFIGURATION_DIALOG_SECTION"; //$NON-NLS-1$
	}
}
