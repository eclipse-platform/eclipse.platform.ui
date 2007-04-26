/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsViewer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Item;

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
        BreakpointsViewer viewer = (BreakpointsViewer) getBreakpointsView().getViewer();
        Item[] items = viewer.getSelectedItems();
        IBreakpoint breakpoint = null;
        BreakpointContainer container = null;
        for(int i = 0; i < items.length; i++) {
        	if(items[i].getData() instanceof IBreakpoint) {
        		breakpoint = (IBreakpoint) items[i].getData();
        		container = viewer.getRemovableContainer(items[i]);
        		if(container != null) {
        			container.getOrganizer().removeBreakpoint(breakpoint, container.getCategory());
        		}
        	}
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        Object element = selection.getFirstElement();
        if(element instanceof BreakpointContainer) {
        	return ((BreakpointContainer) element).getCategory().equals(IDebugUIConstants.BREAKPOINT_WORKINGSET_ID);
        }
        return false;
    }
}
