/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.*;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to toggle method breakpoints.
 * 
 * @since 3.0
 */
public class ToggleMethodBreakpointObjectActionDelegate	extends	ToggleBreakpointObjectActionDelegate {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.ToggleBreakpointObjectActionDelegate#performAction(org.eclipse.debug.internal.ui.actions.IToggleBreakpointsTarget, org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	protected void performAction(IToggleBreakpointsTarget target, IWorkbenchPart part, ISelection selection) throws CoreException {
		target.toggleMethodBreakpoints(part, selection);
	}
}
