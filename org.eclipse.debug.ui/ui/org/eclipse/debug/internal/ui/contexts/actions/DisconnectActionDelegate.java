/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.internal.ui.actions.ActionMessages;

public class DisconnectActionDelegate extends AbstractDebugContextActionDelegate {

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) throws DebugException {
		if (element instanceof IDisconnect) {
			 ((IDisconnect) element).disconnect();
		}
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
		return ActionMessages.DisconnectActionDelegate_Exceptions_occurred_attempting_to_disconnect__2; 
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.DisconnectActionDelegate_Disconnect_failed_1; 
	}
	
	/**
	 * @see ListenerActionDelegate#doHandleDebugEvent(DebugEvent)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {	
		if (event.getKind() == DebugEvent.TERMINATE && event.getSource() instanceof IDebugTarget) {
			update(getAction(), getContext());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.actions.AbstractDebugContextActionDelegate#getTarget(java.lang.Object)
	 */
	protected Object getTarget(Object selectee) {
		if (selectee instanceof IDisconnect) {
			return selectee;
		}
		if (selectee instanceof IAdaptable) {
			return ((IAdaptable)selectee).getAdapter(IDisconnect.class);
		}
		return null;
	}	
}
