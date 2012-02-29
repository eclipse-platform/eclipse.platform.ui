/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.contextlaunching.ContextRunner;
import org.eclipse.debug.internal.ui.contextlaunching.LaunchingResourceManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.ibm.icu.text.MessageFormat;

/**
 * Re-launches the last launch.
 * 
 * @see ContextRunner
 * @see org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager
 * @see ILaunchConfiguration
 * @see RunLastAction
 * @see DebugLastAction
 * @see ProfileLastAction
 * 
 */
public abstract class RelaunchLastAction implements IWorkbenchWindowActionDelegate, IPropertyChangeListener {

	private IWorkbenchWindow fWorkbenchWindow;
	
	private IAction fAction;
	
	/**
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void dispose(){
		DebugUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
		fWorkbenchWindow = window;
		DebugUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action){		
		if(LaunchingResourceManager.isContextLaunchEnabled()) {
			ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(getLaunchGroupId());
			ContextRunner.getDefault().launch(group);
			return;
		}
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
		LaunchConfigurationsDialog dialog = new LaunchConfigurationsDialog(DebugUIPlugin.getShell(), DebugUIPlugin.getDefault().getLaunchConfigurationManager().getDefaultLaunchGroup(getMode()));		
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
	 * @param action the {@link IAction} to initialize
	 */
	private void initialize(IAction action) {
		fAction = action;
		if(fAction != null) {
			fAction.setEnabled(existsConfigTypesForMode());
			fAction.setText(getText());
			fAction.setToolTipText(getTooltipText());
			String commandId = getCommandId();
			ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			if (service != null) {
				Command command = service.getCommand(commandId);
				command.undefine();
				command = service.getCommand(commandId);
				command.define(DebugUIPlugin.removeAccelerators(getText()), getDescription(), service.getCategory("org.eclipse.debug.ui.category.run")); //$NON-NLS-1$
			}
		}
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
	 * @return the filtered last launch
	 */
	protected ILaunchConfiguration getLastLaunch() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getFilteredLastLaunch(getLaunchGroupId());
	}
	
	/**
	 * Returns the parent shell for this menu item
	 * @return the parent shell
	 */
	protected Shell getShell() {
		return fWorkbenchWindow.getShell();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getProperty().equals(IInternalDebugUIConstants.PREF_USE_CONTEXTUAL_LAUNCH)) {
			initialize(fAction);
		}
	}
	
	/**
	 * Returns the mode (run or debug) of this action.
	 * @return the mode 
	 */
	public abstract String getMode();
	
	/**
	 * Returns the launch group id of this action.
	 * @return  the launch group id
	 */
	public abstract String getLaunchGroupId();	

	/**
	 * Returns the text to display on the menu item.
	 * @return the text for the menu item
	 * 
	 * @since 3.3
	 */
	protected abstract String getText();
	
	/**
	 * Returns the text to display in the menu item tooltip
	 * @return the text for the tooltip
	 * 
	 * @since 3.3
	 */
	protected abstract String getTooltipText();
	
	/**
	 * Returns the command id this action is associated with.
	 * 
	 * @return command id
	 * @since 3.3
	 */
	protected abstract String getCommandId();
	
	/**
	 * Returns a description for this action (to associate with command).
	 * 
	 * @return command description
	 * @since 3.3
	 */
	protected abstract String getDescription();	
}

