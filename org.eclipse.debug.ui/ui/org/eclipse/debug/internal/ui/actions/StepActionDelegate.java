package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.jface.action.IAction;

public abstract class StepActionDelegate extends ListenerActionDelegate {
	
	/**
	 * @see ControlActionDelegate#doAction(Object)
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
	 * @see ControlActionDelegate#isEnabledFor(Object)
	 */
	public boolean isEnabledFor(Object element) {
		if (element instanceof IStep) {
			return checkCapability((IStep) element);
		}
		return false;
	}

	/**
	 * @see ControlActionDelegate#enableForMultiSelection()
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
	
	/**
	 * @see ListenerActionDelegate#doHandleDebugEvent(DebugEvent)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		IAction action= getAction();
		switch (event.getKind()) {
			case DebugEvent.TERMINATE :
				action.setEnabled(false);
				break;
			case DebugEvent.RESUME :
				action.setEnabled(false);
				break;
			case DebugEvent.SUSPEND :
				update(getAction(), getSelection());
				break;
		}
	}		
}