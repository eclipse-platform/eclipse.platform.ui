/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.Iterator;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.internal.ui.views.breakpoints.WorkingSetCategory;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Removes a breakpoint from a breakpoint working set.
 */
public class RemoveFromWorkingSetAction extends BreakpointSelectionAction {
        
    /**
     * Constructs action to remove breakpoints from a category.
     * 
     * @param view
     */
    public RemoveFromWorkingSetAction(BreakpointsView view) {
        super(BreakpointGroupMessages.RemoveFromWorkingSetAction_0, view); 
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        Iterator iterator = getStructuredSelection().iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof IBreakpoint) {
                IBreakpoint breakpoint = (IBreakpoint) object;
                BreakpointContainer[] containers = getBreakpointsView().getMovedFromContainers(breakpoint);
                if (containers != null) {
                    for (int i = 0; i < containers.length; i++) {
                        BreakpointContainer container = containers[i];
                        container.getOrganizer().removeBreakpoint(breakpoint, container.getCategory());
                    }
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        if (selection.isEmpty() || !getBreakpointsView().isShowingGroups()) {
            return false;
        }
        Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof IBreakpoint) {
                IBreakpoint breakpoint = (IBreakpoint) object;
                BreakpointContainer[] containers = getBreakpointsView().getMovedFromContainers(breakpoint);
                if (containers == null || containers.length == 0) {
                    return false;
                }
                for (int i = 0; i < containers.length; i++) {
                    BreakpointContainer container = containers[i];
                    if (container.getCategory() instanceof WorkingSetCategory) {
                        WorkingSetCategory category = (WorkingSetCategory) container.getCategory();
                        if (!IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(category.getWorkingSet().getId())) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
