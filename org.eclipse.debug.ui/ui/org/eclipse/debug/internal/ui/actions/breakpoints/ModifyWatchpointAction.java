/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IWatchpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action that modifies a watchpoint's access/modification attributes
 */
public abstract class ModifyWatchpointAction implements IObjectActionDelegate, IActionDelegate2 {
    
    private IStructuredSelection fWatchpoints = null;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        try {
	        if (fWatchpoints != null) {
	            Iterator iterator = fWatchpoints.iterator();
	            while (iterator.hasNext()) {
	                IWatchpoint watchpoint = (IWatchpoint)iterator.next();
	                toggleWatchpoint(watchpoint, action.isChecked());
	            }
	        }
        } catch (CoreException e) {
            DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), ActionMessages.ModifyWatchpointAction_0, ActionMessages.ModifyWatchpointAction_1, e.getStatus()); // 
        }

    }
    
    /**
     * Toggles the watch point attribute to the given value.
     * 
     * @param watchpoint the watchpoint to toggle
     * @param b on or off
     * @throws CoreException if an exception occurs
     */
    protected abstract void toggleWatchpoint(IWatchpoint watchpoint, boolean b) throws CoreException;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            fWatchpoints = (IStructuredSelection) selection;
            if (!selection.isEmpty()) {
	            Iterator iterator = fWatchpoints.iterator();
	            while (iterator.hasNext()) {
	                Object next = iterator.next();
	                if (next instanceof IWatchpoint) {
	                    IWatchpoint watchpoint = (IWatchpoint) next;
	                    action.setChecked(isChecked(watchpoint));
	                    if (!isEnabled(watchpoint)) {
	                        action.setEnabled(false);
	                        return;
	                    }
	                }
	            }
	            action.setEnabled(true);
	            return;
            }
        }
        action.setEnabled(false);
    }

    /**
     * Returns whether the action should be checke for the current selection
     * 
     * @param watchpoint selected watchpoint
     * @return whether the action should be checked for the current selection
     */
    protected abstract boolean isChecked(IWatchpoint watchpoint);

    /**
     * Returns whether this action is enabled for the given watchpoint.
     * 
     * @param watchpoint the watchpoint to examine
     * @return whether this action is enabled for the given watchpoint
     */
    protected abstract boolean isEnabled(IWatchpoint watchpoint);

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    public void init(IAction action) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#dispose()
     */
    public void dispose() {
        fWatchpoints = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(IAction action, Event event) {
        run(action);
    }
}
