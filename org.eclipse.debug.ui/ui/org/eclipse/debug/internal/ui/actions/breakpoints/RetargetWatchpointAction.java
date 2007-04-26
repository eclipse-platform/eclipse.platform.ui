/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;


/**
 * Global retargettable toggle watchpoint action.
 * 
 * @since 3.0
 */
public class RetargetWatchpointAction extends RetargetBreakpointAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetBreakpointAction#performAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	protected void performAction(Object target, ISelection selection, IWorkbenchPart part) throws CoreException {
		((IToggleBreakpointsTarget)target).toggleWatchpoints(part, selection);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetBreakpointAction#canPerformAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean canPerformAction(Object target, ISelection selection, IWorkbenchPart part) {
		return ((IToggleBreakpointsTarget)target).canToggleWatchpoints(part, selection);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#getOperationUnavailableMessage()
	 */
	protected String getOperationUnavailableMessage() {
		return Messages.RetargetWatchpointAction_0;
	}	
}
