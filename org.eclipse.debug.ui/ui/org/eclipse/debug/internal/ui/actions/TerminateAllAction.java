package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Terminates all launches.
 */
public class TerminateAllAction extends Action implements IUpdate {
	
	public TerminateAllAction() {
		super(ActionMessages.getString("TerminateAllAction.Termi&nate_All_1")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("TerminateAllAction.Terminate_All_2")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.TERMINATE_ALL_ACTION });
			
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_TERMINATE_ALL));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_ALL));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_ALL));
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= lManager.getLaunches();
		MultiStatus ms = new MultiStatus(DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 
			DebugException.REQUEST_FAILED, ActionMessages.getString("TerminateAllAction.Terminate_all_failed_3"), null); //$NON-NLS-1$
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
			DebugUIPlugin.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), ActionMessages.getString("TerminateAllAction.Terminate_all_2"),ActionMessages.getString("TerminateAllAction.Exceptions_occurred_attempting_to_terminate_all._5") , ms); //$NON-NLS-1$ //$NON-NLS-2$
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