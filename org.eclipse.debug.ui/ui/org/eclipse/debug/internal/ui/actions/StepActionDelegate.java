package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.jface.action.IAction;

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
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.
	 * IAction)
	 */
  		
 	public void init(IAction action) {
 		action.setActionDefinitionId(getActionDefinitionId());
	}
	
	protected abstract String getActionDefinitionId();
}