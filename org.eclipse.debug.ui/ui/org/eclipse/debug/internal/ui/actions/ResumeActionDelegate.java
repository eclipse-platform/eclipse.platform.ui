package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ResumeActionDelegate extends AbstractListenerActionDelegate {

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object object) throws DebugException {
		IDebugElement element= (IDebugElement)object;
		if (element instanceof ISuspendResume) {
			ISuspendResume suspendResume= (ISuspendResume)element;
			if (suspendResume.canResume()) {
				suspendResume.resume();
			}
		}
	}

	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		return element instanceof ISuspendResume && ((ISuspendResume) element).canResume();
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
		return ActionMessages.getString("ResumeActionDelegate.Resume_failed._1"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("ResumeActionDelegate.Exceptions_occurred_attempting_to_resume._2"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("ResumeActionDelegate.Resume_3"); //$NON-NLS-1$
	}
}