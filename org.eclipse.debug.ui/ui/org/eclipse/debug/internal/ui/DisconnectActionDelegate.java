package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;

public class DisconnectActionDelegate extends ControlActionDelegate {

	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(Object element) throws DebugException {
		if (element instanceof IDisconnect)
			 ((IDisconnect) element).disconnect();
	}

	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IDisconnect && ((IDisconnect) element).canDisconnect();
	}
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.DISCONNECT_ACTION;
	}

	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {		
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DISCONNECT));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DISCONNECT));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DISCONNECT));
	}	
	/*
	 * @see ControlActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return DebugUIMessages.getString("DisconnectActionDelegate.Disconnect_failed_1"); //$NON-NLS-1$
	}

	/*
	 * @see ControlActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return DebugUIMessages.getString("DisconnectActionDelegate.Exceptions_occurred_attempting_to_disconnect._2"); //$NON-NLS-1$
	}

	/*
	 * @see ControlActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return DebugUIMessages.getString("DisconnectActionDelegate.Disconnect_3"); //$NON-NLS-1$
	}
	/*
	 * @see ControlActionDelegate#getToolTipText()
	 */
	protected String getToolTipText() {
		return DebugUIMessages.getString("DisconnectActionDelegate.&Disconnect_4"); //$NON-NLS-1$
	}

	/*
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return DebugUIMessages.getString("DisconnectActionDelegate.Disconnect_5"); //$NON-NLS-1$
	}
}