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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;

public abstract class StepActionDelegate extends AbstractDebugContextActionDelegate {
	
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object object) throws DebugException {
		if (object instanceof IStep) {
			stepAction((IStep)object);
		}
	}
	
	/**
	 * @see AbstractDebugActionDelegate#isRunInBackground()
	 */
	protected boolean isRunInBackground() {
		return true;
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.actions.AbstractDebugContextActionDelegate#getTarget(java.lang.Object)
	 */
	protected Object getTarget(Object selectee) {
		if (selectee instanceof IStep) {
			return selectee;
		}
		if (selectee instanceof IAdaptable) {
			return ((IAdaptable)selectee).getAdapter(IStep.class);
		}
		return null;
	}
	
	
}
