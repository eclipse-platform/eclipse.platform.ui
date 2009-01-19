/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * Allows the user to manage working sets.
 */
public class WorkingSetsAction extends AbstractBreakpointsViewAction {
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IWorkingSetSelectionDialog selectionDialog = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(
        		DebugUIPlugin.getShell(), 
        		false, 
        		new String[] {IDebugUIConstants.BREAKPOINT_WORKINGSET_ID});
        selectionDialog.open();
    }
}
