package org.eclipse.debug.internal.ui.views.breakpoints;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.TableViewer;

/**
 * Handles breakpoint events, updating the breakpoints view
 * and viewer.
 */
public class BreakpointsViewEventHandler implements IBreakpointsListener {

	private BreakpointsView fView;

	/**
	 * Constructs an event handler for the breakpoints view.
	 */
	public BreakpointsViewEventHandler(BreakpointsView view) {
		fView= view;
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}
	
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
	}

	/**
	 * @see IBreakpointsListener#breakpointsAdded(IBreakpoint[])
	 */
	public void breakpointsAdded(final IBreakpoint[] breakpoints) {
		if (fView.isAvailable()) {		
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable()) {
						((TableViewer)fView.getViewer()).add(breakpoints);
						fView.updateObjects();
					}
				}
			});
		}
	}

	/**
	 * @see IBreakpointsListener#breakpointsRemoved(IBreakpoint[], IMarkerDelta[])
	 */
	public void breakpointsRemoved(final IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		if (fView.isAvailable()) {
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable()) {
						TableViewer viewer= (TableViewer)fView.getViewer();
						int[] indices= viewer.getTable().getSelectionIndices();
						viewer.getControl().setRedraw(false);
						viewer.remove(breakpoints);
						viewer.getControl().setRedraw(true);
						if (viewer.getSelection().isEmpty()) {
							if (indices.length > 0) {
								int index= indices[0];
								viewer.getTable().select(Math.min(index, viewer.getTable().getItemCount() - 1));
							}
							//fire the selection changed as does not occur when
							//setting selection on the swt widget
							viewer.setSelection(viewer.getSelection());
						}
						fView.updateObjects();
					}
				}
			});
		}
	}

	/**
	 * @see IBreakpointsListener#breakpointsChanged(IBreakpoint[], IMarkerDelta[])
	 */
	public void breakpointsChanged(final IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		if (fView.isAvailable()) {
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable()) {
						((TableViewer)fView.getViewer()).refresh(breakpoints);
						fView.updateObjects();
					}
				}
			});
		}
	}
}
