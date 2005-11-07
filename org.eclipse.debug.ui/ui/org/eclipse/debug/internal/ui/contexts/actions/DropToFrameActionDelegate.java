/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDropToFrame;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

/**
 * Action delegate which performs a drop to frame.
 */
public class DropToFrameActionDelegate extends AbstractDebugContextActionDelegate {

    class UpdateJob extends Job {
        IAction fAction;
        ISelection fSelection;
        
        UpdateJob() {
            super("Update Action Enablement"); //$NON-NLS-1$ System Job. Never intended to be seen by users.
        }
        
        void setAction(IAction action) {
            fAction = action;
        }
        void setSelection(ISelection selection) {
            fSelection = selection;
        }

        protected IStatus run(IProgressMonitor monitor) {
            DropToFrameActionDelegate.super.update(fAction, fSelection);
            return Status.OK_STATUS;
        }
        
    }
    
    private UpdateJob fUpdateJob = new UpdateJob();
    
    public DropToFrameActionDelegate() {
        super();
        fUpdateJob.setSystem(true);
    }
    
    /**
     * Performs the drop to frame.
     * @see AbstractDebugActionDelegate#doAction(Object)
     */
    protected void doAction(Object element) throws DebugException {
        if (element instanceof IDropToFrame) {
            IDropToFrame dropToFrame= (IDropToFrame) element;
            if (dropToFrame.canDropToFrame()) {
                dropToFrame.dropToFrame();
            }
        }
    }

    /**
     * Enable the action for implementers of IDropToFrame which are able to perform
     * the drop to frame operation.
     */
    protected boolean isEnabledFor(Object element) {
        return element instanceof IDropToFrame && ((IDropToFrame) element).canDropToFrame();
    }

    protected void update(IAction action, ISelection selection) {
        fUpdateJob.setAction(action);
        fUpdateJob.setSelection(selection);
        fUpdateJob.schedule();
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.actions.AbstractDebugContextActionDelegate#getTarget(java.lang.Object)
	 */
	protected Object getTarget(Object selectee) {
		if (selectee instanceof IDropToFrame) {
			return selectee;
		}
		if (selectee instanceof IAdaptable) {
			return ((IAdaptable)selectee).getAdapter(IDropToFrame.class);
		}
		return null;
	}	    
}
