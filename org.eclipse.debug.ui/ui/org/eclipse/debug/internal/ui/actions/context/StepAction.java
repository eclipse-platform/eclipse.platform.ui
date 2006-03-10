/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;

public abstract class StepAction extends AbstractDebugContextAction {
	
	/*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#doAction(java.lang.Object)
	 */
	protected void doAction(Object object) {
        if (object instanceof IStep) {
			stepAction((IStep)object);
		}
	}

	/*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#isEnabledFor(java.lang.Object)
	 */
	protected void isEnabledFor(Object element, IBooleanRequestMonitor monitor) {
		if (element instanceof IStep) {
			checkCapability((IStep)element, monitor);
		}
	}


    protected abstract void checkCapability(IStep element, IBooleanRequestMonitor monitor);
	/**
	 * Performs the specific step action.
	 *
	 * @exception DebugException if the action fails
	 */
	protected abstract void stepAction(IStep element);
}
