package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

public class RemoveBreakpointAction extends AbstractRemoveActionDelegate {

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getSelection();
		final Iterator itr= selection.iterator();
		final MultiStatus ms = new MultiStatus(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
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
						}
					} catch (CoreException ce) {
						ms.merge(ce.getStatus());
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null);
		} catch (CoreException ce) {
			ms.merge(ce.getStatus());
		}
		if (!ms.isOK()) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), ActionMessages.getString("RemoveBreakpointAction.Removing_a_breakpoint_4"),ActionMessages.getString("RemoveBreakpointAction.Exceptions_occurred_attempting_to_remove_a_breakpoint._5") , ms); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) throws DebugException {
	}
	
	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		return element instanceof IBreakpoint;
	}
}

