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


import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
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
		final MultiStatus ms = new MultiStatus(DebugUIPlugin.getUniqueIdentifier(),
			DebugException.REQUEST_FAILED, 
			ActionMessages.getString("RemoveBreakpointAction.Breakpoint(s)_removal_failed_3"), null); //$NON-NLS-1$
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
				while (itr.hasNext()) {
					try {						
						Object next= itr.next();
						if (next instanceof IBreakpoint) {
							IBreakpoint breakpoint= (IBreakpoint)next;						
							breakpointManager.removeBreakpoint(breakpoint, true);
						} else if (next instanceof IBreakpointContainer) {
						    removeContainer((IBreakpointContainer) next, breakpointManager);
						}
					} catch (CoreException ce) {
						ms.merge(ce.getStatus());
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
		} catch (CoreException ce) {
			ms.merge(ce.getStatus());
		}
		if (!ms.isOK()) {
			IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				DebugUIPlugin.errorDialog(window.getShell(), ActionMessages.getString("RemoveBreakpointAction.Removing_a_breakpoint_4"),ActionMessages.getString("RemoveBreakpointAction.Exceptions_occurred_attempting_to_remove_a_breakpoint._5") , ms); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				DebugUIPlugin.log(ms);
			}
		}
	}
	
	/**
	 * The user has pressed "remove" on a container. Remove all breakpoints in the group.
	 */
	private void removeContainer(IBreakpointContainer container, IBreakpointManager manager) throws CoreException {
		boolean proceed = MessageDialog.openQuestion(getView().getSite().getShell(), "Delete Breakpoints", MessageFormat.format("Delete all breakpoints in {0}?", new String[] { container.getName() }));
		if (proceed) {
			IBreakpoint[] breakpoints = container.getBreakpoints();
			manager.removeBreakpoints(breakpoints, true);
		}
	}
	
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) {
		//not used
	}
}

