package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunch;import org.eclipse.jface.action.Action;import org.eclipse.jface.action.IAction;import org.eclipse.jface.viewers.ISelection;import org.eclipse.swt.custom.BusyIndicator;import org.eclipse.swt.widgets.Display;import org.eclipse.ui.IWorkbenchWindow;import org.eclipse.ui.IWorkbenchWindowActionDelegate;import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Re-runs or re-debugs the last launch.
 */
public class RelaunchLastAction extends Action implements IWorkbenchWindowActionDelegate {
	
	
	public RelaunchLastAction() {
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.RELAUNCH_LAST_ACTION });
	}
	
	/**
	 * @see IAction
	 */
	public void run() {
		final ILaunch recent= DebugUIPlugin.getDefault().getLastLaunch();
		if (recent == null) {
			Display.getCurrent().beep();
		} else {
			BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
				public void run() {
					RelaunchActionDelegate.relaunch(recent);
				}
			});
		}
	}

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
	 * @see IActionDelegate
	 */
	public void run(IAction action){
		run();
	}

	/**
	 * @see IActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection selection){
	}
}

