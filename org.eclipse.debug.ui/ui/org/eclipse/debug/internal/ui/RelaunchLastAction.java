package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * Re-runs or re-debugs the last launch.
 */
public class RelaunchLastAction extends Action implements IWorkbenchWindowActionDelegate {

	public RelaunchLastAction() {
		super("&Re-Run/Debug@F10");
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

