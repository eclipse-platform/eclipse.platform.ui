package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Terminates all launches.
 */
public class TerminateAllAction extends Action {
	
	private static final String PREFIX= "terminate_all_action.";
	private static final String STATUS= "status";
	private static final String ERROR= "error.";

	public TerminateAllAction() {
		super(DebugUIUtils.getResourceString(PREFIX + TEXT));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.TERMINATE_ALL_ACTION });
			
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_TERMINATE_ALL));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_ALL));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_ALL));
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
	 * Updates the enabled state of this action.
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

