package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.*;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Terminates all launches.
 */
public class TerminateAllAction extends Action implements IUpdate {
	
	private static final String PREFIX= "terminate_all_action.";
	private static final String STATUS= "status";
	private static final String ERROR= "error.";

	public TerminateAllAction() {
		super(DebugUIUtils.getResourceString(PREFIX + TEXT));
	}

	/**
	 * @see Action
	 */
	public void run() {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= lManager.getLaunches();
		MultiStatus ms = new MultiStatus(DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 
			IDebugStatusConstants.REQUEST_FAILED, DebugUIUtils.getResourceString(PREFIX + STATUS), null);
		for (int i= 0; i < launches.length; i++) {
			ILaunch launch= (ILaunch) launches[i];
			if (!launch.isTerminated()) {
				try {
					launch.terminate();
				} catch (DebugException de) {
					ms.merge(de.getStatus());
				}
			}
		}
		if (!ms.isOK()) {
			DebugUIUtils.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), PREFIX + ERROR, ms);
		}
	}

	/**
	 * @see IUpdate
	 */
	public void update() {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= lManager.getLaunches();
		for (int i= 0; i< launches.length; i++) {
			ILaunch launch= launches[i];
			if (!launch.isTerminated()) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
	}
}

