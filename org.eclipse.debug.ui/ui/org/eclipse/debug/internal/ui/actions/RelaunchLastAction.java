package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Relaunches the last launch.
 */
public abstract class RelaunchLastAction implements IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow fWorkbenchWindow;
	
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
			final LaunchConfigurationHistoryElement historyElement = getLastLaunch();
			if (historyElement != null) {
				final ILaunchConfiguration historyConfig = historyElement.getLaunchConfiguration();
				if (historyConfig.supportsMode(getMode())) {
					if (!DebugUITools.saveAndBuildBeforeLaunch()) {
						return;
					}
					BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
						public void run() {							
							RelaunchActionDelegate.relaunch(historyConfig, getMode());
						}
					});				
				} else {
					String configName = historyElement.getLaunchConfiguration().getName();
					String title = ActionMessages.getString("RelaunchLastAction.Cannot_relaunch_1"); //$NON-NLS-1$
					String message = MessageFormat.format(ActionMessages.getString("RelaunchLastAction.Cannot_relaunch_[{0}]_because_it_does_not_support_{2}_mode_2"), new String[] {configName, getMode()}); //$NON-NLS-1$
					MessageDialog.openError(getShell(), title, message);				
				}
			} else {
				// If the history is empty, just open the launch config dialog
				openLaunchConfigurationDialog();
			}
		} catch (CoreException ce) {
			DebugUIPlugin.errorDialog(getShell(), ActionMessages.getString("RelaunchLastAction.Error_relaunching_3"), ActionMessages.getString("RelaunchLastAction.Error_encountered_attempting_to_relaunch_4"), ce); //$NON-NLS-1$ //$NON-NLS-2$
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
		IStructuredSelection selection= DebugUIPlugin.resolveSelection(dwindow);
		LaunchConfigurationDialog dialog = new LaunchConfigurationDialog(DebugUIPlugin.getShell(), selection, getMode());		
		dialog.setOpenMode(LaunchConfigurationDialog.LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_LAST_LAUNCHED);
		dialog.open();
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection){
	}
	
	/**
	 * Return the last launch that occurred in the workspace.
	 */
	protected LaunchConfigurationHistoryElement getLastLaunch() {
		return DebugUIPlugin.getLaunchConfigurationManager().getLastLaunch();
	}
	
	protected Shell getShell() {
		return fWorkbenchWindow.getShell();
	}

	/**
	 * Returns the mode (run or debug) of this action.
	 */
	public abstract String getMode();

}

