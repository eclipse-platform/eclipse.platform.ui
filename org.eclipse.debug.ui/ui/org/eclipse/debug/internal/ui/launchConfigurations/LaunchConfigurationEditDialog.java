/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * This class is used to construct a launch configuraityon dialog used to edit a launch configuration and continue,
 * not allowing a launch to occur.
 * 
 * @since 3.3
 * 
 * EXPERIMENTAL
 */
public class LaunchConfigurationEditDialog extends LaunchConfigurationDialog {

	public LaunchConfigurationEditDialog(Shell shell, ILaunchConfiguration launchConfiguration, LaunchGroupExtension group) {
		super(shell, launchConfiguration, group);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog#getTitleAreaTitle()
	 */
	protected String getTitleAreaTitle() {
		return LaunchConfigurationsMessages.LaunchConfigurationEditDialog_0; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ID_CLOSE_BUTTON, LaunchConfigurationsMessages.LaunchConfigurationEditDialog_1, true);  
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog#updateButtons()
	 */
	public void updateButtons() {
		getTabViewer().refresh();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog#getDialogSettingsSectionName()
	 */
	protected String getDialogSettingsSectionName() {
		return IDebugUIConstants.PLUGIN_ID + ".LAUNCH_CONFIGURATION_EDIT_DIALOG_SECTION"; //$NON-NLS-1$
	}
}
