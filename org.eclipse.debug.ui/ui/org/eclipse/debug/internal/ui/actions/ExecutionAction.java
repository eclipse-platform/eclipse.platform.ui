package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This is the super class of the Run & Debug actions which appears in the desktop menu and toolbar.
 */
public abstract class ExecutionAction implements IActionDelegate2 {
	
	/**
	 * @see IActionDelegate2#runWithEvent(IAction, Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
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
		dialog.setOpenMode(LaunchConfigurationsDialog.LAUNCH_CONFIGURATION_DIALOG_LAUNCH_LAST);
		dialog.open();
	}
	
	/**
	 * Returns the mode of a launcher to use for this action
	 */
	protected abstract String getMode();
	
	/**
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		openLaunchConfigurationDialog();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}