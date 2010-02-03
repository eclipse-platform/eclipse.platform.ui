/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;


import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
 

/**
 * A dialog used to edit a single launch configuration.
 */
public class LaunchConfigurationPropertiesDialog extends LaunchConfigurationsDialog implements ILaunchConfigurationListener {
	
	/**
	 * The launch configuration to display
	 */
	private ILaunchConfiguration fLaunchConfiguration;

	/**
	 * Constructs a new launch configuration dialog on the given
	 * parent shell.
	 * 
	 * @param shell the parent shell
	 * @param selection the selection used to initialize this dialog, typically the 
	 *  current workbench selection
	 * @param group launch group
	 */
	public LaunchConfigurationPropertiesDialog(Shell shell, ILaunchConfiguration launchConfiguration, LaunchGroupExtension group) {
		super(shell, group);
		fLaunchConfiguration = launchConfiguration;
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
	}
	
	/**
	 * Constructs a new launch configuration dialog on the given
	 * parent shell.
	 * 
	 * @param shell the parent shell
	 * @param selection the selection used to initialize this dialog, typically the 
	 *  current workbench selection
	 * @param reservednames a set of names of virtual launch configurations that need to be considered 
	 *  when configuration names are generated
	 * @param group launch group
	 */
	public LaunchConfigurationPropertiesDialog(Shell shell, ILaunchConfiguration launchConfiguration, LaunchGroupExtension group, Set reservednames) {
		super(shell, group);
		fLaunchConfiguration = launchConfiguration;
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
		fReservedNames = reservednames;
	}
	
	/**
	 * Returns the launch configuration being displayed.
	 * 
	 * @return ILaunchConfiguration
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#initializeBounds()
	 */
	protected void initializeBounds() {
		super.initializeBounds();
		resize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#initializeContent()
	 */
	protected void initializeContent() {
		ILaunchConfiguration launchConfiguration = getLaunchConfiguration();
		if (shouldSetDefaultsOnOpen() && launchConfiguration instanceof ILaunchConfigurationWorkingCopy) {
			ILaunchConfigurationWorkingCopy wc = (ILaunchConfigurationWorkingCopy) launchConfiguration;
			doSetDefaults(wc);
		}
		getTabViewer().setInput(launchConfiguration);
		IStatus status = getInitialStatus();
		if (status != null) {
			handleStatus(status);
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#close()
	 */
	public boolean close() {
	    if (!isSafeToClose()) {
	        return false;
	    }
		DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
		return super.close();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#addContent(org.eclipse.swt.widgets.Composite)
	 */
	protected void addContent(Composite dialogComp) {
		GridData gd;
		Composite topComp = new Composite(dialogComp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(gd);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		topLayout.marginHeight = 5;
		topLayout.marginWidth = 5;
		topComp.setLayout(topLayout);
		topComp.setFont(dialogComp.getFont());
	
		// Set the things that TitleAreaDialog takes care of 
		setTitle(getTitleAreaTitle());
		setMessage(IInternalDebugCoreConstants.EMPTY_STRING);
		setModeLabelState();
	
		// Build the launch configuration edit area and put it into the composite.
		Composite editAreaComp = createLaunchConfigurationEditArea(topComp);
		gd = new GridData(GridData.FILL_BOTH);
		editAreaComp.setLayoutData(gd);
		editAreaComp.setFont(dialogComp.getFont());
		
		dialogComp.layout(true);
		applyDialogFont(dialogComp);
	}
	
	/**
	 * returns the title area title of the dialog
	 * @return the title area title
	 */
	protected String getTitleAreaTitle() {
		return LaunchConfigurationsMessages.LaunchConfigurationPropertiesDialog_Edit_launch_configuration_properties_1; 
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
					
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#getShellTitle()
	 */
	protected String getShellTitle() {
		return LaunchConfigurationsMessages.LaunchConfigurationPropertiesDialog_Properties_for__0__2;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.LAUNCH_CONFIGURATION_PROPERTIES_DIALOG;
	}
  	 	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#updateButtons()
	 */
	public void updateButtons() {
		getTabViewer().refresh();
		getButton(IDialogConstants.OK_ID).setEnabled(getTabViewer().canSave());
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		getTabViewer().handleApplyPressed();
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#open()
	 */
	public int open() {
		setOpenMode(-1);
		return super.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog#getDialogSettingsSectionName()
	 */
	protected String getDialogSettingsSectionName() {
		return IDebugUIConstants.PLUGIN_ID + ".LAUNCH_CONFIGURATION_PROPERTIES_DIALOG_SECTION"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		if (getLaunchConfiguration().equals(manager.getMovedFrom(configuration))) {
			// this config was re-named, update the dialog with the new config
			fLaunchConfiguration = configuration;
			getTabViewer().setInput(getLaunchConfiguration());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {}
}
