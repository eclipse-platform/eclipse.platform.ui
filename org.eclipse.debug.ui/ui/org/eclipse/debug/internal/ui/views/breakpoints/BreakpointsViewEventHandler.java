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
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.TableViewer;

/**
 * Handles breakpoint events, updating the breakpoints view
 * and viewer.
 */
public class BreakpointsViewEventHandler implements IBreakpointListener {

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
	 * @see IBreakpointListener#breakpointAdded(IBreakpoint)
	 */
	public void breakpointAdded(final IBreakpoint breakpoint) {
		if (fView.isAvailable()&& breakpoint.getMarker().exists()) {		
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable() && breakpoint.getMarker().exists()) {
						((TableViewer)fView.getViewer()).add(breakpoint);
						fView.updateObjects();
					}
				}
			});
		}
	}

	/**
	 * @see IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved(final IBreakpoint breakpoint, IMarkerDelta delta) {
		if (fView.isAvailable()) {
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable()) {
						TableViewer viewer= (TableViewer)fView.getViewer();
						int[] indices= viewer.getTable().getSelectionIndices();
						viewer.remove(breakpoint);
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
	 * @see IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointChanged(final IBreakpoint breakpoint, IMarkerDelta delta) {
		if (fView.isAvailable() && breakpoint.getMarker().exists()) {
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable() && breakpoint.getMarker().exists()) {
						((TableViewer)fView.getViewer()).refresh(breakpoint);
						fView.updateObjects();
					}
				}
			});
		}
	}
}
