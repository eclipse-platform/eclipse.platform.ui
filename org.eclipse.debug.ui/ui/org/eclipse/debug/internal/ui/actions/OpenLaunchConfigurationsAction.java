package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Abstract action for opening the launch configuration dialog in run or debug mode.
 * This class is used in two ways: to simply open the dialog, and to open the dialog and 
 * create a new configuration of a specified type.
 */
public abstract class OpenLaunchConfigurationsAction extends Action implements IWorkbenchWindowActionDelegate {
	
	private boolean fCreateNewConfigMode;
	
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
		fCreateNewConfigMode = false;
		setConfigType(null);
		ImageDescriptor imageDescriptor = null;
		if (getMode() == ILaunchManager.DEBUG_MODE) {
			imageDescriptor = DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG);
		} else {
			imageDescriptor = DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN);			
		}	
		setText(ActionMessages.getString("OpenLaunchConfigurationsAction.Edit_Configurations_1")); //$NON-NLS-1$
		setImageDescriptor(imageDescriptor);	
	}
	
	/**
	 * Initialize this action from the specified <code>ILaunchConfigurationType</code>.
	 */
	protected OpenLaunchConfigurationsAction(ILaunchConfigurationType configType) {
		fCreateNewConfigMode = true;
		setConfigType(configType);
		setText(configType.getName());

		ImageDescriptor descriptor = DebugPluginImages.getImageDescriptor(configType.getIdentifier());
		if (descriptor == null) {
			if (getMode().equals(ILaunchManager.DEBUG_MODE)) {
				descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG);
			} else {
				descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN);
			}
		}

		if (descriptor != null) {
			setImageDescriptor(descriptor);
		}
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
			ISelection sel = window.getSelectionService().getSelection();
			IStructuredSelection ss = null;
			if (sel instanceof IStructuredSelection) {
				ss = (IStructuredSelection)sel;
			} else {
				ss = new StructuredSelection();
			}
			LaunchConfigurationDialog dialog = new LaunchConfigurationDialog(window.getShell(), ss, getMode());
			if (fCreateNewConfigMode) {
				dialog.setOpenMode(LaunchConfigurationDialog.LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_NEW_CONFIG_OF_TYPE);
				dialog.setInitialConfigType(getConfigType());
			} else {
				dialog.setOpenMode(LaunchConfigurationDialog.LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_LAST_LAUNCHED);				
			}
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
	
}
