package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;

public abstract class StepActionDelegate extends AbstractListenerActionDelegate {
	
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object object) throws DebugException {
		if (object instanceof IStep) {
			stepAction((IStep)object);
		}
	}

	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		if (element instanceof IStep) {
			return checkCapability((IStep)element);
		}
		return false;
	}

	/**
	 * @see AbstractDebugActionDelegate#enableForMultiSelection()
	 */
	protected boolean enableForMultiSelection() {
		return false;
	}

	/**
	 * Returns whether the <code>IStep</code> has the capability to perform the
	 * requested step action.
	 */
	protected abstract boolean checkCapability(IStep element);

	/**
	 * Performs the specific step action.
	 *
	 * @exception DebugException if the action fails
	 */
	protected abstract void stepAction(IStep element) throws DebugException;
}