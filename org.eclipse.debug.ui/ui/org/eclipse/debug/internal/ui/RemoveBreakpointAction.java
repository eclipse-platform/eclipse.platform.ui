package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

public class RemoveBreakpointAction extends SelectionProviderAction {

	private final static String PREFIX= "remove_breakpoint_action.";
	private final static String ERROR= "error.";
	private final static String STATUS= PREFIX + "status";

	public RemoveBreakpointAction(ISelectionProvider provider) {
		super(provider, DebugUIUtils.getResourceString(PREFIX + TEXT));
		setEnabled(!getStructuredSelection().isEmpty());
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
	}

	/**
	 * @see IAction
	 */
	public void run() {
		IStructuredSelection selection= getStructuredSelection();
		if (selection.isEmpty()) {
			return;
		}
		IStructuredSelection es= (IStructuredSelection)selection;
		final Iterator itr= es.iterator();
		final MultiStatus ms = new MultiStatus(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
			IDebugStatusConstants.REQUEST_FAILED, DebugUIUtils.getResourceString(STATUS), null);
 
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				while (itr.hasNext()) {
					try {
						breakpointManager.removeBreakpoint((IMarker)itr.next(), true);
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
			DebugUIUtils.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), PREFIX + ERROR, ms);
		}
	}

	/**
	 * @see SelectionProviderAction
	 */
	public void selectionChanged(IStructuredSelection sel) {
		setEnabled(!sel.isEmpty());
	}
}

