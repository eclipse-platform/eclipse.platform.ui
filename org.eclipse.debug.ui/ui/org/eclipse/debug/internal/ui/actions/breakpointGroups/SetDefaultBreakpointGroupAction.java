/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointSetOrganizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkingSet;

/**
 * Action which prompts the user to set a default breakpoint group.
 */
public class SetDefaultBreakpointGroupAction extends AbstractBreakpointsViewAction {
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
    	SelectBreakpointWorkingsetDialog sbwsd = new SelectBreakpointWorkingsetDialog(DebugUIPlugin.getShell());
    	sbwsd.setTitle(BreakpointGroupMessages.SetDefaultBreakpointGroupAction_0);
    	IWorkingSet workingSet = BreakpointSetOrganizer.getDefaultWorkingSet();
    	if (workingSet != null){
    		sbwsd.setInitialSelections(new Object[]{workingSet});
    	}
    	if(sbwsd.open() == Window.OK) {
    		BreakpointSetOrganizer.setDefaultWorkingSet((IWorkingSet) sbwsd.getResult()[0]);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {}

}
