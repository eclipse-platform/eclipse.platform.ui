package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;

public class DisconnectActionDelegate extends ListenerActionDelegate {

	/**
	 * @see ControlActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) throws DebugException {
		if (element instanceof IDisconnect)
			 ((IDisconnect) element).disconnect();
	}

	/**
	 * @see ControlActionDelegate#isEnabledFor(Object)
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IDisconnect && ((IDisconnect) element).canDisconnect();
	}
	
	/**
	 * @see ControlActionDelegate#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.DISCONNECT_ACTION;
	}

	/**
	 * @see ControlActionDelegate#setActionImages(IAction)
	 */
	protected void setActionImages(IAction action) {		
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DISCONNECT));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DISCONNECT));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DISCONNECT));
	}
		
	/**
	 * @see ControlActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("DisconnectActionDelegate.Disconnect_failed_1"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("DisconnectActionDelegate.Exceptions_occurred_attempting_to_disconnect._2"); //$NON-NLS-1$
	}
	/**
	 * @see ControlActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("DisconnectActionDelegate.Disconnect_3"); //$NON-NLS-1$
	}
	/**
	 * @see ControlActionDelegate#getToolTipText()
	 */
	protected String getToolTipText() {
		return ActionMessages.getString("DisconnectActionDelegate.Disconnect_3"); //$NON-NLS-1$	
	}

	/**
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return ActionMessages.getString("DisconnectActionDelegate.&Disconnect_4"); //$NON-NLS-1$
	}
	
	/**
	 * @see ListenerActionDelegate#doHandleDebugEvent(DebugEvent)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {	
		if (event.getKind() == DebugEvent.TERMINATE) {
			getAction().setEnabled(false);
		}
	}
}