package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDisconnect;

public class DisconnectActionDelegate extends AbstractListenerActionDelegate {

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) throws DebugException {
		if (element instanceof IDisconnect)
			 ((IDisconnect) element).disconnect();
	}

	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		return element instanceof IDisconnect && ((IDisconnect) element).canDisconnect();
	}
		
	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("DisconnectActionDelegate.Disconnect_failed_1"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("DisconnectActionDelegate.Exceptions_occurred_attempting_to_disconnect._2"); //$NON-NLS-1$
	}
	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("DisconnectActionDelegate.Disconnect_3"); //$NON-NLS-1$
	}
	
	/**
	 * @see ListenerActionDelegate#doHandleDebugEvent(DebugEvent)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {	
		if (event.getKind() == DebugEvent.TERMINATE) {
			update(getAction(), getSelection());
		}
	}
}