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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.IAction;

/**
 * An action that removes all breakpoints from a group.
 */
public class DissolveBreakpointGroupAction extends AbstractBreakpointGroupAction {
    
    public DissolveBreakpointGroupAction() {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
	    String[] groups = getSelectedGroups();
	    for (int i = 0; i < groups.length; i++) {
	        IBreakpoint[] breakpoints = getBreakpoints(groups[i]);
	        for (int j = 0; j < breakpoints.length; j++) {
                try {
                    breakpoints[j].setGroup(null);
                } catch (CoreException e) {
                }
            }
        }
	}

}
