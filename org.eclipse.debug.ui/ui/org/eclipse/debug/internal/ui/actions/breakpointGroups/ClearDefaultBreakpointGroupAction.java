/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

/**
 * An action which clears (sets the null) the default breakpoint group.
 * @see org.eclipse.debug.core.IBreakpointManager#setAutoGroup(String)
 */
public class ClearDefaultBreakpointGroupAction extends AbstractBreakpointsViewAction {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        DebugPlugin.getDefault().getBreakpointManager().setAutoGroup(null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
