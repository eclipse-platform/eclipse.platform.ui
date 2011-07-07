/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointSetOrganizer;
import org.eclipse.jface.action.IAction;

/**
 * An action which clears (sets the null) the default breakpoint group.
 */
public class ClearDefaultBreakpointGroupAction extends BreakpointWorkingSetAction {
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        BreakpointSetOrganizer.setDefaultWorkingSet(null);
    }

    protected void update() {
        fAction.setEnabled(BreakpointSetOrganizer.getDefaultWorkingSet() != null);
    }
}
