package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
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
					if (!DebugUIPlugin.saveAndBuild()) {
						return;
					}
					BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
						public void run() {							
							RelaunchActionDelegate.relaunch(historyConfig, getMode());
						}
					});				
				} else {
					String configName = historyElement.getLaunchConfiguration().getName();
					MessageDialog.openError(getShell(), "Cannot relaunch", "Cannot relaunch \'" + configName + "\' because it does not support " + getMode() + " mode");				
				}
			} else {
				Display.getCurrent().beep();
			}
		} catch (CoreException ce) {
			DebugUIPlugin.errorDialog(getShell(), "Error relaunching", "Error encountered attempting to relaunch", ce);
		}
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

