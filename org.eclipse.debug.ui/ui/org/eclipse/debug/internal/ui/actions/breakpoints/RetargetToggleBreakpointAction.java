/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;


/**
 * Global retargettable toggle breakpoint action.
 * 
 * @since 3.3
 */
public class RetargetToggleBreakpointAction extends RetargetBreakpointAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetBreakpointAction#performAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	protected void performAction(Object target, ISelection selection, IWorkbenchPart part) throws CoreException {
		if (target instanceof IToggleBreakpointsTargetExtension) {
			IToggleBreakpointsTargetExtension ext = (IToggleBreakpointsTargetExtension) target;
			ext.toggleBreakpoints(part, selection);
		} else {
			((IToggleBreakpointsTarget)target).toggleLineBreakpoints(part, selection);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetBreakpointAction#canPerformAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean canPerformAction(Object target, ISelection selection, IWorkbenchPart part) {
		if (target instanceof IToggleBreakpointsTargetExtension) {
			IToggleBreakpointsTargetExtension ext = (IToggleBreakpointsTargetExtension) target;
			return ext.canToggleBreakpoints(part, selection);
		} else {
			return ((IToggleBreakpointsTarget)target).canToggleLineBreakpoints(part, selection);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#getOperationUnavailableMessage()
	 */
	protected String getOperationUnavailableMessage() {
		return Messages.RetargetToggleBreakpointAction_0;
	}	
}
