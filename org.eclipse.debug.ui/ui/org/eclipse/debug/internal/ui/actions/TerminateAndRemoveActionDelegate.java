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
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;

public class TerminateAndRemoveActionDelegate extends ControlActionDelegate {

	/**
	 * @see ControlActionDelegate
	 */
	public void initializeForOwner(ControlAction controlAction) {		
		super.initializeForOwner(controlAction);
		controlAction.setEnabled(!controlAction.getStructuredSelection().isEmpty());
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(Object element) throws DebugException {
		ITerminate terminate= (ITerminate)element;
		if (!terminate.isTerminated()) {
			terminate.terminate();
		}		
		
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

	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		if (element instanceof ITerminate) {
			ITerminate terminate= (ITerminate)element;
			//do not want to terminate an attach launch that does not
			//have termination enabled
			return terminate.canTerminate() || terminate.isTerminated();
		}
		return false;
	}	
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.TERMINATE_AND_REMOVE_ACTION;
	}

	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_TERMINATE_AND_REMOVE));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_AND_REMOVE));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_REMOVE));
	}	
	/**
	 * @see ControlActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("TerminateAndRemoveActionDelegate.Terminate_and_remove_failed_1"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("TerminateAndRemoveActionDelegate.Exceptions_occurred_attempting_to_terminate_and_remove_2"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return getToolTipText();
	}
	/**
	 * @see ControlActionDelegate#getToolTipText()
	 */
	protected String getToolTipText() {
		return ActionMessages.getString("TerminateAndRemoveActionDelegate.Terminate_and_Remove;_3"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return ActionMessages.getString("TerminateAndRemoveActionDelegate.Ter&minate_and_Remove_4"); //$NON-NLS-1$
	}
}