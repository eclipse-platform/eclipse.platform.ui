package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStep;

public abstract class StepActionDelegate extends ControlActionDelegate {
	
	/**
	 * @see ControlActionDelegate
	 */
	public void doAction(Object object) throws DebugException {
		IDebugElement element= (IDebugElement)object;
		IStep steppable= null;
		if (element instanceof IStep) {
			steppable= (IStep) element;
			stepAction(steppable);
		}
	}

	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		if (element instanceof IStep) {
			return checkCapability((IStep) element);
		}
		return false;
	}

	/**
	 * @see ControlActionDelegate
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