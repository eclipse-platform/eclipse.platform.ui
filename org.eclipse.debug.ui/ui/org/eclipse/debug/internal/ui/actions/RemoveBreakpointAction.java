/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchWindow;

public class RemoveBreakpointAction extends AbstractRemoveActionDelegate {

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getSelection();
		final Iterator itr= selection.iterator();
		final CoreException[] exception= new CoreException[0];
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
				List breakpointsToDelete= new ArrayList();
				boolean deleteContainers= false;
				while (itr.hasNext()) {		
						Object next= itr.next();
						if (next instanceof IBreakpoint) {
							breakpointsToDelete.add(next);
						} else if (next instanceof IBreakpointContainer) {
						    if (!deleteContainers) {
						        // Prompt the user to delete containers only once.
						        deleteContainers = MessageDialog.openConfirm(getView().getSite().getShell(), ActionMessages.getString("RemoveBreakpointAction.0"), ActionMessages.getString("RemoveBreakpointAction.1")); //$NON-NLS-1$ //$NON-NLS-2$
						        if (!deleteContainers) {
						            // User cancelled. Do nothing
						            return;
						        }
						    }
						    // To get here, deletion has to have been confirmed.
						    IBreakpoint[] breakpoints = ((IBreakpointContainer) next).getBreakpoints();
						    for (int i = 0; i < breakpoints.length; i++) {
                                breakpointsToDelete.add(breakpoints[i]);
                            }
						}
				}
				IBreakpoint[] breakpoints= (IBreakpoint[]) breakpointsToDelete.toArray(new IBreakpoint[0]);
				try {
					breakpointManager.removeBreakpoints(breakpoints, true);
				} catch (CoreException ce) {
					exception[0]= ce;
				}
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
				DebugUIPlugin.errorDialog(window.getShell(), ActionMessages.getString("RemoveBreakpointAction.Removing_a_breakpoint_4"),ActionMessages.getString("RemoveBreakpointAction.Exceptions_occurred_attempting_to_remove_a_breakpoint._5") , exception[0]); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				DebugUIPlugin.log(exception[0]);
			}
		}
	}
	
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) {
		//not used
	}
}

