package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegateWithEvent;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This is the super class of the Run & Debug actions which appears in the desktop menu and toolbar.
 */
public abstract class ExecutionAction implements IActionDelegateWithEvent {
	
	/**
	 * @see IActionDelegateWithEvent#runWithEvent(IAction, Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		openLaunchConfigurationDialog();
	}

	/**
	 * Open the launch configuration dialog, passing in the current workbench selection.
	 */
	private void openLaunchConfigurationDialog() {
		IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
		if (dwindow == null) {
			return;
		}
		IStructuredSelection selection= DebugUIPlugin.resolveSelection(dwindow);
		LaunchConfigurationDialog dialog = new LaunchConfigurationDialog(DebugUIPlugin.getShell(), selection, getMode());		
		dialog.setOpenMode(LaunchConfigurationDialog.LAUNCH_CONFIGURATION_DIALOG_LAUNCH_LAST);
		dialog.open();
	}
	
	/**
	 * Returns the mode of a launcher to use for this action
	 */
	protected abstract String getMode();
	
}