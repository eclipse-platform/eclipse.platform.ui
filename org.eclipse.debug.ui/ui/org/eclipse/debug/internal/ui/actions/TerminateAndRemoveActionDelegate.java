package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ITerminate;

public class TerminateAndRemoveActionDelegate extends AbstractDebugActionDelegate {

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) throws DebugException {
		try {
			if (element instanceof ITerminate) {
				ITerminate terminate= (ITerminate)element;
				if (!terminate.isTerminated()) {
					terminate.terminate();
				}		
			}
		} finally {
			ILaunch launch= null;
			if (element instanceof ILaunch) {
				launch= (ILaunch) element;
			} else if (element instanceof IDebugElement) {
				launch= ((IDebugElement) element).getLaunch();
			} else if (element instanceof IProcess) {
				launch= ((IProcess) element).getLaunch();
			}
			ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
			lManager.removeLaunch(launch);
		}
	}

	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		if (element instanceof ITerminate) {
			ITerminate terminate= (ITerminate)element;
			//do not want to terminate an attach launch that does not
			//have termination enabled
			return terminate.canTerminate() || terminate.isTerminated();
		}
		return false;
	}	

	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("TerminateAndRemoveActionDelegate.Terminate_and_remove_failed_1"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("TerminateAndRemoveActionDelegate.Exceptions_occurred_attempting_to_terminate_and_remove_2"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("TerminateAndRemoveActionDelegate.Terminate_and_Remove;_3"); //$NON-NLS-1$
	}
}