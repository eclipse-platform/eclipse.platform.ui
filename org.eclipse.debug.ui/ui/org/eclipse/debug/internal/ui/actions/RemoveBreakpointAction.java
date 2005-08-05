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
package org.eclipse.debug.internal.ui.actions;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

public class RemoveBreakpointAction extends AbstractRemoveActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getSelection();
		if (selection.isEmpty()) {
			return;
		}
		final List state = ((BreakpointsView)getView()).getSelectionState();
		final Iterator itr= selection.iterator();
		final CoreException[] exception= new CoreException[1];
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				List breakpointsToDelete= new ArrayList();
				boolean deleteContainers= false;
				while (itr.hasNext()) {		
						Object next= itr.next();
						if (next instanceof IBreakpoint) {
							breakpointsToDelete.add(next);
						} else if (next instanceof BreakpointContainer) {
						    if (!deleteContainers) {
						        // Prompt the user to delete containers only once.
						        deleteContainers = MessageDialog.openConfirm(getView().getSite().getShell(), ActionMessages.RemoveBreakpointAction_0, ActionMessages.RemoveBreakpointAction_1); // 
						        if (!deleteContainers) {
						            // User cancelled. Do nothing
						            return;
						        }
						    }
						    // To get here, deletion has to have been confirmed.
						    IBreakpoint[] breakpoints = ((BreakpointContainer) next).getBreakpoints();
						    for (int i = 0; i < breakpoints.length; i++) {
                                breakpointsToDelete.add(breakpoints[i]);
                            }
						}
				}
				final IBreakpoint[] breakpoints= (IBreakpoint[]) breakpointsToDelete.toArray(new IBreakpoint[0]);
				new Job(ActionMessages.RemoveBreakpointAction_2) { 
                    protected IStatus run(IProgressMonitor pmonitor) {
                        try {
                            DebugPlugin.getDefault().getBreakpointManager().removeBreakpoints(breakpoints, true);
                            if (state != null) {
                            	Runnable r = new Runnable() {
								
									public void run() {
										((BreakpointsView) getView()).preserveSelectionState(state);
									}
								};
								DebugUIPlugin.getStandardDisplay().asyncExec(r);
                            }
                            return Status.OK_STATUS;
                        } catch (CoreException e) {
                            DebugUIPlugin.log(e);
                        }
                        return Status.CANCEL_STATUS;
                    }   
                }.schedule();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
		} catch (CoreException ce) {
			exception[0]= ce;
		}
		if (exception[0] != null) {
			IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				DebugUIPlugin.errorDialog(window.getShell(), ActionMessages.RemoveBreakpointAction_Removing_a_breakpoint_4,ActionMessages.RemoveBreakpointAction_Exceptions_occurred_attempting_to_remove_a_breakpoint__5 , exception[0]); // 
			} else {
				DebugUIPlugin.log(exception[0]);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction(Object element) {
		//not used
	}

	protected boolean isEnabledFor(Object element) {
		if (element instanceof BreakpointContainer)
			return ((BreakpointContainer)element).getChildren().length > 0;
		return super.isEnabledFor(element);
	}
}
