package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;
 
/**
 * Removes all terminated/detached launches from the UI. 
 * Clears the launches output as well.
 */
public class RemoveTerminatedAction extends Action {

	boolean fRemoveDebug;

	public RemoveTerminatedAction(boolean removeDebug) {
		super(DebugUIMessages.getString("RemoveTerminatedAction.Remove_&All_Terminated_1")); //$NON-NLS-1$
		setToolTipText(DebugUIMessages.getString("RemoveTerminatedAction.Remove_All_Terminated_Launches_2")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_TERMINATED));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_TERMINATED));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_TERMINATED));
		fRemoveDebug= removeDebug;
		setEnabled(false);
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.REMOVE_ACTION });
	}

	/**
	 * Removes all of the terminated launches relevant to the
	 * current perspective. Has the 
	 * side effect of clearing the console output.
	 * @see IAction
	 */
	public void run() {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		Object[] launches= lManager.getLaunches();
		for (int i= 0; i < launches.length; i++) {
			ILaunch launch= (ILaunch)launches[i];
			if (launch.isTerminated() && isLaunchRelevantToCurrentPerpective(launch)) {
				lManager.deregisterLaunch(launch);
			}
		}
	}

	/** 
	 * Updates the enabled state of this action to enabled if at
	 * least one launch is terminated and relative to the current perspective.
	 */
	public void update() {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= lManager.getLaunches();
		for (int i= 0; i < launches.length; i++) {
			ILaunch launch= launches[i];
			if (launch.isTerminated() && isLaunchRelevantToCurrentPerpective(launch)) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
	}

	/**
	 * Returns whether this launch is relevant to the 
	 * current perspective...ie Run or Debug.
	 * For example, if in the debug perspective (fRemoveDebug= true)
	 * and the launch does not have a debug target, <code>false</code>
	 * will be returned.
	 */
	protected boolean isLaunchRelevantToCurrentPerpective(ILaunch launch) {
		if (fRemoveDebug) {
			return launch.getDebugTarget() != null;
		}
		IProcess[] processes= launch.getProcesses();
		return (processes != null && processes.length > 0);
	}
}

