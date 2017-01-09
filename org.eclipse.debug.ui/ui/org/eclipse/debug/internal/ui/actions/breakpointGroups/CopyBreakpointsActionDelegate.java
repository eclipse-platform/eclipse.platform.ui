/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.viewers.model.VirtualCopyToClipboardActionDelegate;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;

/**
 * Copies breakpoint labels to the text clipboard and breakpoint objects
 * to the breakpoint paste action.
 */
public class CopyBreakpointsActionDelegate extends VirtualCopyToClipboardActionDelegate implements IBreakpointsListener {

	private long fStamp;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.VirtualCopyToClipboardActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		super.run(action);
		LocalSelectionTransfer.getTransfer().setSelection(getSelection());
		fStamp = System.currentTimeMillis();
		LocalSelectionTransfer.getTransfer().setSelectionSetTime(fStamp);
		IAction pasteAction = ((AbstractDebugView)getView()).getAction(IDebugView.PASTE_ACTION);
        // update the enablement of the paste action
        // workaround since the clipboard does not suppot callbacks
        if (pasteAction instanceof PasteBreakpointsAction) {
        	PasteBreakpointsAction pba = (PasteBreakpointsAction) pasteAction;
        	if (pba.getStructuredSelection() != null) {
        		pba.selectionChanged(pba.getStructuredSelection());
        	}
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	@Override
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	@Override
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		// remove deleted breakpoints from drag/drop clipboard
		if (fStamp == LocalSelectionTransfer.getTransfer().getSelectionSetTime()) {
			ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
			if (selection instanceof IStructuredSelection) {
				Set<IBreakpoint> removed = new HashSet<IBreakpoint>();
				for (int i = 0; i < breakpoints.length; i++) {
					removed.add(breakpoints[i]);
				}
				boolean modified = false;
				List<Object> remain = new ArrayList<Object>();
				IStructuredSelection ss = (IStructuredSelection) selection;
				Iterator<?> iterator = ss.iterator();
				while (iterator.hasNext()) {
					Object bp = iterator.next();
					if (removed.contains(bp)) {
						modified = true;
					} else {
						remain.add(bp);
					}
				}
				if (modified) {
					LocalSelectionTransfer.getTransfer().setSelection(new StructuredSelection(remain));
					fStamp = System.currentTimeMillis();
					LocalSelectionTransfer.getTransfer().setSelectionSetTime(fStamp);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	@Override
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
	}
}
