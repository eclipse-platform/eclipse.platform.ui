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

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointGroupContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * An action which acts on a selection of breakpoint group containers
 */
public abstract class AbstractBreakpointGroupAction extends AbstractBreakpointsViewAction {

    /**
     * The selected breakpoint group containers.
     */
    private List fGroupContainers= new ArrayList();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		IStructuredSelection selection= (IStructuredSelection) sel;
		int selectionSize= selection.size();
		fGroupContainers.clear();
		if (selectionSize != 0) {
			Iterator iter = selection.iterator();
			int index= 0;
			while (iter.hasNext()) {
			    Object element= iter.next();
			    if (element instanceof BreakpointGroupContainer) {
			        fGroupContainers.add(element);
			    } else {
			        fGroupContainers.clear();
			        break;
			    }
			}
		}
		action.setEnabled(fGroupContainers.size() > 0);
	}
	
	/**
	 * Returns the selected breakpoint groups.
	 * @return the selected breakpoint groups
	 */
	public String[] getSelectedGroups() {
		List groupNames= new ArrayList();
		Iterator iter = fGroupContainers.iterator();
		while (iter.hasNext()) {
			BreakpointGroupContainer container = (BreakpointGroupContainer) iter.next();
			groupNames.add(container.getName());
		}
	    return (String[]) groupNames.toArray(new String[0]);
	}
	
	/**
	 * Returns the breakpoints within the given group
	 * @param group the group
	 * @return the breakpoints within the given group
	 */
	public IBreakpoint[] getBreakpoints(String group) {
		Set breakpoints= new HashSet();
		Iterator iter = fGroupContainers.iterator();
		while (iter.hasNext()) {
			BreakpointGroupContainer container = (BreakpointGroupContainer) iter.next();
			IBreakpoint[] children = container.getBreakpoints();
			for (int i = 0; i < children.length; i++) {
				breakpoints.add(children[0]);
			}
		}
	    return (IBreakpoint[]) breakpoints.toArray(new IBreakpoint[0]);
	}

}
