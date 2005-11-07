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
package org.eclipse.debug.internal.ui.contexts.actions;

 
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Removes all breakpoints from the source (markers) and remove all
 * breakpoints from processes
 */
public class RemoveAllBreakpointsAction extends AbstractRemoveAllActionDelegate implements IBreakpointsListener {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractRemoveAllActionDelegate#doAction()
	 */
	protected void doAction() {
		final IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		final IBreakpoint[] breakpoints= breakpointManager.getBreakpoints();
		if (breakpoints.length < 1) {
			return;
		}
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		boolean proceed = MessageDialog.openQuestion(window.getShell(), ActionMessages.RemoveAllBreakpointsAction_0, ActionMessages.RemoveAllBreakpointsAction_1);  
		if (proceed) {
            new Job(ActionMessages.RemoveAllBreakpointsAction_2) { 
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        breakpointManager.removeBreakpoints(breakpoints, true);
                    } catch (CoreException e) {
                        DebugUIPlugin.log(e);
                        return Status.CANCEL_STATUS;
                    }
                    return Status.OK_STATUS;
                }
            }.schedule();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractRemoveAllActionDelegate#update()
	 */
	protected void update() {
		getAction().setEnabled(
			DebugPlugin.getDefault().getBreakpointManager().hasBreakpoints());
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		if (getAction() != null && !getAction().isEnabled()){
			update();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		if (getAction() != null) {
			update();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);	
		super.dispose();
	}
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        super.init(window);
        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }
}
