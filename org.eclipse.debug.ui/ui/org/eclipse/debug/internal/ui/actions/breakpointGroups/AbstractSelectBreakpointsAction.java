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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * 
 */
public abstract class AbstractSelectBreakpointsAction extends AbstractBreakpointsViewAction {

    private IBreakpoint[] fBreakpoints= new IBreakpoint[0];

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
	    if (fBreakpoints.length < 1) {
	        return;
	    }
	    List elementsToSelect= chooseSimilarBreakpoints();
	    if (elementsToSelect.size() > 0) {
	        IStructuredSelection selection= new StructuredSelection(elementsToSelect);
	        fView.getViewer().setSelection(selection, true);
	    }
	}
	
	public abstract boolean breakpointsMatch(IBreakpoint breakpointOne, IBreakpoint breakpointTwo);

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.breakpointGroups.AbstractSelectBreakpointsAction#getElementsToSelect(java.lang.Object[])
     */
    public List chooseSimilarBreakpoints() {
        Set breakpointsToSelect= new HashSet();
        IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            IBreakpoint breakpoint = breakpoints[i];
            for (int j = 0; j < fBreakpoints.length; j++) {
                IBreakpoint selected = fBreakpoints[j];
                if (breakpointsMatch(breakpoint, selected)) {
                    breakpointsToSelect.add(breakpoint);
                }
            }
        }
        return new ArrayList(breakpointsToSelect);
    }
    
    public void selectionChanged(IAction action, ISelection selection) {
        List selectedBreakpoints= new ArrayList();
        Iterator iter = ((IStructuredSelection) selection).iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element instanceof IBreakpoint) {
                selectedBreakpoints.add(element);
            } else {
                selectedBreakpoints.clear();
                break;
            }
        }
        fBreakpoints= (IBreakpoint[]) selectedBreakpoints.toArray(new IBreakpoint[0]);
        action.setEnabled(fBreakpoints.length > 0);
    }

}
