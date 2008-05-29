/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;


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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ID_LAUNCH_BUTTON, getLaunchButtonText(), true);
		createButton(parent, ID_CLOSE_BUTTON, LaunchConfigurationsMessages.LaunchConfigurationDialog_Close_1, false);  
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog#getTitleAreaTitle()
	 */
	protected String getTitleAreaTitle() {
		return LaunchConfigurationsMessages.LaunchConfigurationDialog_Modify_attributes_and_launch__1; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.SINGLE_LAUNCH_CONFIGURATION_DIALOG;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog#updateButtons()
	 */
	public void updateButtons() {
		// Launch button
		getTabViewer().refresh();
		getButton(ID_LAUNCH_BUTTON).setEnabled(getTabViewer().canLaunch() & getTabViewer().canLaunchWithModes() & !getTabViewer().hasDuplicateDelegates());
		
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog#getDialogSettingsSectionName()
	 */
	protected String getDialogSettingsSectionName() {
		return IDebugUIConstants.PLUGIN_ID + ".SINGLE_LAUNCH_CONFIGURATION_DIALOG_SECTION"; //$NON-NLS-1$
	}
}
