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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.breakpoints.WorkingSetBreakpointOrganizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * An action which prompts the user to asign a breakpoint to a group.
 * @see org.eclipse.debug.core.model.IBreakpoint#setGroup(String)
 */
public class AddBreakpointToGroupAction extends AbstractBreakpointsViewAction {
	
	/**
	 * The currently selected breakpoints
	 */
	private Object[] fBreakpoints= null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
        IWorkingSet workingSet = WorkingSetBreakpointOrganizer.getDefaultWorkingSet();
        IWorkingSetSelectionDialog selectionDialog = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(DebugUIPlugin.getShell(), false);
        if (workingSet != null) {
            selectionDialog.setSelection(new IWorkingSet[]{workingSet});
        }
        if (selectionDialog.open() == Window.OK) {
            IWorkingSet[] sets = selectionDialog.getSelection();
            if (sets.length == 1) {
                IWorkingSet set = sets[0];
                if ("org.eclipse.debug.ui.breakpointWorkingSet".equals(set.getId())) { //$NON-NLS-1$
                    IAdaptable[] elements = set.getElements();
                    Set newElements = new HashSet(elements.length + fBreakpoints.length);
                    for (int i = 0; i < elements.length; i++) {
                        newElements.add(elements[i]);
                    }
                    for (int i = 0; i < fBreakpoints.length; i++) {
                        newElements.add(fBreakpoints[i]);
                    }
                    set.setElements((IAdaptable[]) newElements.toArray(new IAdaptable[newElements.size()]));
                }
            }
        }	    
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		IStructuredSelection selection= (IStructuredSelection) sel;
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			if (!(iterator.next() instanceof IBreakpoint)) {
				action.setEnabled(false);
				fBreakpoints= null;
				return;
			}
		}
		action.setEnabled(true);
		fBreakpoints= selection.toArray();
	}

}
