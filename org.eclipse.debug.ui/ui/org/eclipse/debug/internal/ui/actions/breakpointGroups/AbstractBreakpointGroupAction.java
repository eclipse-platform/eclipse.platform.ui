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

import java.util.Iterator;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * An action which acts on a selection of breakpoint groups
 */
public abstract class AbstractBreakpointGroupAction extends AbstractBreakpointsViewAction {

    /**
     * The selected breakpoint groups.
     */
    protected String[] fGroups;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		IStructuredSelection selection= (IStructuredSelection) sel;
		int selectionSize= selection.size();
		if (selectionSize == 0) {
		    fGroups= new String[0];
		} else {
			fGroups= new String[selection.size()];
			Iterator iter = selection.iterator();
			int index= 0;
			while (iter.hasNext()) {
			    Object element= iter.next();
			    if (element instanceof String) {
			        fGroups[index++]= (String) element;
			    } else {
			        fGroups= new String[0];
			        break;
			    }
			}
		}
		action.setEnabled(fGroups.length > 0);
	}
	
	/**
	 * Returns the selected breakpoint groups.
	 * @return the selected breakpoint groups
	 */
	public String[] getSelectedGroups() {
	    return fGroups;
	}
	
	/**
	 * Returns the breakpoints within the given group
	 * @param group the group
	 * @return the breakpoints within the given group
	 */
	public IBreakpoint[] getBreakpoints(String group) {
	    Object[] children = fView.getTreeContentProvider().getChildren(group);
	    IBreakpoint[] breakpoints= new IBreakpoint[children.length];
	    for (int i = 0; i < children.length; i++) {
            breakpoints[i]= (IBreakpoint) children[i];
        }
	    return breakpoints;
	}

}
