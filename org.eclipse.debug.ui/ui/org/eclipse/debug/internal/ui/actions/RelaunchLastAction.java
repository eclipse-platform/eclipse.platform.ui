package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Re-runs or re-debugs the last launch.
 */
public class RelaunchLastAction implements IWorkbenchWindowActionDelegate {
	
	/**
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void dispose(){
	}

	/**
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void init(IWorkbenchWindow window){
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action){
		final LaunchConfigurationHistoryElement recent= DebugUIPlugin.getLaunchConfigurationManager().getLastLaunch();
		if (recent == null) {
			Display.getCurrent().beep();
		} else {
			if (!DebugUIPlugin.saveAndBuild()) {
				return;
			}
			BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
				public void run() {
					RelaunchActionDelegate.relaunch(recent);
				}
			});
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection){
	}
}

