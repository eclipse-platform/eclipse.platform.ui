/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Relaunches the last launch.
 */
public abstract class RelaunchLastAction implements IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow fWorkbenchWindow;
	
	private IAction fAction;
	
	/**
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void dispose(){
	}

	/**
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void init(IWorkbenchWindow window){
		fWorkbenchWindow = window;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action){		
		try {
			final ILaunchConfiguration configuration = getLastLaunch();
			if (configuration != null) {
				if (configuration.supportsMode(getMode())) {
					DebugUITools.launch(configuration, getMode());
				} else {
					String configName = configuration.getName();
					String title = ActionMessages.RelaunchLastAction_Cannot_relaunch_1; 
					String message = MessageFormat.format(ActionMessages.RelaunchLastAction_Cannot_relaunch___0___because_it_does_not_support__2__mode_2, new String[] {configName, getMode()}); 
					MessageDialog.openError(getShell(), title, message);				
				}
			} else {
				// If the history is empty, just open the launch config dialog
				openLaunchConfigurationDialog();
			}
		} catch (CoreException ce) {
			DebugUIPlugin.errorDialog(getShell(), ActionMessages.RelaunchLastAction_Error_relaunching_3, ActionMessages.RelaunchLastAction_Error_encountered_attempting_to_relaunch_4, ce); // 
		}
	}
	
	/**
	 * Open the launch configuration dialog, passing in the current workbench selection.
	 */
	private void openLaunchConfigurationDialog() {
		IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
		if (dwindow == null) {
			return;
		}
		LaunchConfigurationsDialog dialog = new LaunchConfigurationsDialog(DebugUIPlugin.getShell(), DebugUIPlugin.getDefault().getLaunchConfigurationManager().getDefaultLanuchGroup(getMode()));		
		dialog.setOpenMode(LaunchConfigurationsDialog.LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_LAST_LAUNCHED);
		dialog.open();
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection){
		if (fAction == null) {
			initialize(action);
		}		
	}
	
	/**
	 * Set the enabled state of the underlying action based on whether there are any
	 * registered launch configuration types that understand how to launch in the
	 * mode of this action.
	 */
	private void initialize(IAction action) {
		fAction = action;
		action.setEnabled(existsConfigTypesForMode());	
	}
	
	/**
	 * Return whether there are any registered launch configuration types for
	 * the mode of this action.
	 * 
	 * @return whether there are any registered launch configuration types for
	 * the mode of this action
	 */
	private boolean existsConfigTypesForMode() {
		ILaunchConfigurationType[] configTypes = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes();
		for (int i = 0; i < configTypes.length; i++) {
			ILaunchConfigurationType configType = configTypes[i];
			if (configType.supportsMode(getMode())) {
				return true;
			}
		}		
		return false;
	}
	
	
	/**
	 * Return the last launch that occurred in the workspace.
	 */
	protected ILaunchConfiguration getLastLaunch() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLastLaunch(getLaunchGroupId());
	}
	
	protected Shell getShell() {
		return fWorkbenchWindow.getShell();
	}

	/**
	 * Returns the mode (run or debug) of this action.
	 */
	public abstract String getMode();
	
	/**
	 * Returns the launch group id of this action.
	 */
	public abstract String getLaunchGroupId();	

}

