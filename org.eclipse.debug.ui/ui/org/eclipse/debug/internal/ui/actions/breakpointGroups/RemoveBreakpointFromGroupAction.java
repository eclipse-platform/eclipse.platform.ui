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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * 
 */
public class RemoveBreakpointFromGroupAction extends AbstractBreakpointsViewAction {
    
    private IBreakpoint[] fBreakpoints= new IBreakpoint[0];

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        for (int i = 0; i < fBreakpoints.length; i++) {
            try {
                fBreakpoints[i].setGroup(null);
            } catch (CoreException e) {
            }
        }
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
