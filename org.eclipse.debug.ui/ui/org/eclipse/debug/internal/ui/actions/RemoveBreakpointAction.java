/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.ui.IBreakpointContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
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
		final Object newSelection= computeNewSelection(selection);
		final Iterator itr= selection.iterator();
		final CoreException[] exception= new CoreException[1];
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
					if (newSelection != null) {
						((BreakpointsView) getView()).getCheckboxViewer().setSelection(new StructuredSelection(newSelection));
					}
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
	 * Computes the selection that should be set after the given selection is
	 * removed.
	 * @param selection the current selection
	 * @return the selection that should be set after the given selection is
	 *  removed
	 */
	protected Object computeNewSelection(IStructuredSelection selection) {
		Object newSelection= null;
		Object[] selected = selection.toArray();
		Object firstSelection= selected[0];
		BreakpointsView view = ((BreakpointsView) getView());
		CheckboxTreeViewer viewer= view.getCheckboxViewer();
		ITreeContentProvider provider = view.getTreeContentProvider();
		Object parent = provider.getParent(firstSelection);
		if (parent != null) {
			Object[] peers = provider.getChildren(parent); // elements at the same level
			// Sort the array so it matches the viewer's tree
			viewer.getSorter().sort(viewer, peers);
			for (int i = 0; i < peers.length; i++) {
				Object peer = peers[i];
				if (peer == firstSelection) {
					newSelection= findUnselected(peers, i, selected);
					break;
				}
			}
			if (newSelection == null) {
				// If no unselected peer elements could be found, the parent is
				// going to end up disappearing too. If the parent has a parent,
				// try selecting it (although it might end up getting deleted too).
				parent= provider.getParent(parent);
				if (parent != null) {
					newSelection= parent;
				}
			}
		}
		return newSelection;
	}
	
	/**
	 * Finds the first unselected element in the given set of peer elements
	 * starting at the given index. The returned element will not be one
	 * of the given selected elements.
	 * @param peers the set of elements to search
	 * @param fromIndex the index to start searching at. This index corresponds to
	 *  the index of the first selected elements in the given collection of peers
	 * @param selected the currently selected elements
	 * @return the first unseleced element in the given set
	 */
	private Object findUnselected(Object[] peers, int fromIndex, Object[] selected) {
		if (fromIndex < peers.length - 1) {
			// First, look for an element after the given index
			for (int i = fromIndex + 1; i < peers.length; i++) {
				Object candidate= peers[i];
				boolean unselected= true;
				for (int j = 0; j < selected.length; j++) {
					if (selected[j] == candidate) {
						unselected= false;
						break;
					}
				}
				if (unselected) {
					return candidate;
				}
			}
		}
		if (fromIndex > 0) {
			// If no unselected elements are found *after* the given index,
			// return the first element before it.
			return peers[fromIndex - 1];
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction(Object element) {
		//not used
	}
}