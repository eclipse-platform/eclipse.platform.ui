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
package org.eclipse.debug.internal.ui.views.breakpoints;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.jface.viewers.CheckboxTableViewer;
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
		if (fView.isAvailable() && fView.isVisible()) {		
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable()) {
						CheckboxTableViewer viewer = (CheckboxTableViewer)fView.getViewer(); 
						viewer.add(breakpoints);
						MultiStatus status= new MultiStatus(DebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "", null);
						for (int i = 0; i < breakpoints.length; i++) {
							IBreakpoint breakpoint = breakpoints[i];
							try {
								boolean enabled= breakpoint.isEnabled();
								if (viewer.getChecked(breakpoint) != enabled) {
									viewer.setChecked(breakpoint, breakpoint.isEnabled());								
								}
							} catch (CoreException e) {
								status.add(DebugUIPlugin.newErrorStatus("Exception accessing breakpoint",e));
								DebugUIPlugin.log(e);
							}	
						}
						if (!status.isOK()) {
							DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), DebugUIViewsMessages.getString("BreakpointsViewEventHandler.1"), DebugUIViewsMessages.getString("BreakpointsViewEventHandler.2"), status); //$NON-NLS-1$ //$NON-NLS-2$
						}
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
		if (fView.isAvailable() && fView.isVisible()) {
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
		if (fView.isAvailable() & fView.isVisible()) {
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable()) {
						CheckboxTableViewer viewer = (CheckboxTableViewer)fView.getViewer(); 
						viewer.getControl().setRedraw(false);
						for (int i = 0; i < breakpoints.length; i++) {
							IBreakpoint breakpoint = breakpoints[i];
							IMarker marker= breakpoint.getMarker();
							if (marker != null && marker.exists()) {
								// only refresh if still exists
								try {
									boolean enabled= breakpoint.isEnabled();
									if (viewer.getChecked(breakpoint) != enabled) {
										viewer.setChecked(breakpoint, breakpoint.isEnabled());								
									}
								} catch (CoreException e) {
									DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), DebugUIViewsMessages.getString("BreakpointsViewEventHandler.1"), DebugUIViewsMessages.getString("BreakpointsViewEventHandler.2"), e); //$NON-NLS-1$ //$NON-NLS-2$
									DebugUIPlugin.log(e);
								}
								viewer.refresh(breakpoint);
							}
						}
						viewer.getControl().setRedraw(true);
						fView.updateObjects();
					}
				}
			});
		}
	}
}
