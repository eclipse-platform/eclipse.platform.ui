package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SuspendActionDelegate extends AbstractListenerActionDelegate {

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) throws DebugException {
		if (element instanceof ISuspendResume) {
			 ((ISuspendResume) element).suspend();
		}
	}
	
	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		return element instanceof ISuspendResume && ((ISuspendResume)element).canSuspend();
	}

	/**
	 * @see AbstractDebugActionDelegate#getEnableStateForSelection(IStructuredSelection)
	 */
	protected boolean getEnableStateForSelection(IStructuredSelection selection) {	 		
		if (selection.size() == 1) {
			return isEnabledFor(selection.getFirstElement());
		} else {
			 return false;
		}
	}

	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("SuspendActionDelegate.Exceptions_occurred_attempting_to_suspend._2"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("SuspendActionDelegate.Suspend_failed_1"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("SuspendActionDelegate.Suspend_3"); //$NON-NLS-1$
	}
}