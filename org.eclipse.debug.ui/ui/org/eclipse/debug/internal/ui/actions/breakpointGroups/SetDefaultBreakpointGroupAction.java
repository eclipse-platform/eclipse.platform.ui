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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;

/**
 * 
 */
public class SetDefaultBreakpointGroupAction extends AbstractBreakpointsViewAction {
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        String currentGroup= fView.getAutoGroup();
        if (currentGroup == null) {
            currentGroup= ""; //$NON-NLS-1$
        }
        SelectBreakpointGroupDialog dialog= new SelectBreakpointGroupDialog(fView, BreakpointGroupMessages.getString("SetDefaultBreakpointGroupAction.1"), BreakpointGroupMessages.getString("SetDefaultBreakpointGroupAction.2"), currentGroup, null); //$NON-NLS-1$ //$NON-NLS-2$
        if (dialog.open() != Window.OK) {
            return;
        }
        String group= dialog.getValue();
        fView.setAutoGroup(group);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
