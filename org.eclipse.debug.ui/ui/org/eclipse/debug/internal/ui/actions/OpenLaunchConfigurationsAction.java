package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Abstract action for opening the launch configuration dialog in run or debug
 * mode.
 */
public abstract class OpenLaunchConfigurationsAction extends Action implements IWorkbenchWindowActionDelegate {
	
	/**
	 * The launch configuration type this action will cause to be created in the launch 
	 * configuration dialog.
	 */
	private ILaunchConfigurationType fConfigType;
	
	/**
	 * Action when a delegate, otherwise <code>null</code>
	 */
	private IAction fAction;

	public OpenLaunchConfigurationsAction() {
		super();
		setConfigType(null);
		ImageDescriptor imageDescriptor = null;
		if (getMode() == ILaunchManager.DEBUG_MODE) {
			imageDescriptor = DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG);
		} else {
			imageDescriptor = DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN);			
		}	
		setText(getLabelText()); 
		setImageDescriptor(imageDescriptor);	
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run();
	}

	protected void setConfigType(ILaunchConfigurationType configType) {
		fConfigType = configType;
	}
	
	protected ILaunchConfigurationType getConfigType() {
		return fConfigType;
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			LaunchConfigurationsDialog dialog = new LaunchConfigurationsDialog(window.getShell(), DebugUIPlugin.getDefault().getLaunchConfigurationManager().getDefaultLanuchGroup(getMode()));
			dialog.setOpenMode(LaunchConfigurationsDialog.LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_LAST_LAUNCHED);
			dialog.open();
		}		
	}

	/**
	 * Returns the mode in which to open the launch configuration
	 * dialog.
	 * 
	 * @return on of <code>RUN_MODE</code> or <code>DEBUG_MODE</code>
	 * @see ILaunchManager#RUN_MODE
	 * @see ILaunchManager#DEBUG_MODE
	 */
	protected abstract String getMode();
	
	/**
	 * Return a String label for this action.
	 */
	protected abstract String getLabelText();
	
}
