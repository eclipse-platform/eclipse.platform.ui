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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.IAction;

/**
 * Abstract action which automatically groups all breakpoints in the breakpoint manager
 */
public abstract class AbstractGroupBreakpointsAction extends AbstractBreakpointsViewAction {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            IBreakpoint breakpoint = breakpoints[i];
            String group = getGroup(breakpoint);
            try {                
                breakpoint.setGroup(group);
            } catch (CoreException e) {
            }
        }
    }
    
    /**
     * Returns the group into which the given breakpoint should be placed or
     * <code>null</code>.
     * 
     * @param breakpoint the breakpoint
     * @return the group into which the given breakpoint should be placed (may
     *  be <code>null</code>).
     */
    protected abstract String getGroup(IBreakpoint breakpoint);

}
