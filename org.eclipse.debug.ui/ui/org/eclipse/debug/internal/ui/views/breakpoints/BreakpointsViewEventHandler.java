/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak - bug 57999
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * Handles breakpoint events and activity manager events (which can affect filtering),
 * updating the breakpoints view and viewer.
 */
public class BreakpointsViewEventHandler implements IBreakpointsListener, IActivityManagerListener {

	private BreakpointsView fView;

	/**
	 * Constructs an event handler for the breakpoints view.
	 */
	public BreakpointsViewEventHandler(BreakpointsView view) {
		fView= view;
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		if (activitySupport != null) {
			activitySupport.getActivityManager().addActivityManagerListener(this);
		}
	}
	
	/**
	 * When this event handler is disposed, remove it as a listener.
	 */
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		if (activitySupport != null) {
			activitySupport.getActivityManager().removeActivityManagerListener(this);
		}
	}

	/**
	 * @see IBreakpointsListener#breakpointsAdded(IBreakpoint[])
	 */
	public void breakpointsAdded(final IBreakpoint[] breakpoints) {
		if (fView.isAvailable() && fView.isVisible()) {		
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable()) {
						fView.getCheckboxViewer().refresh();
						if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
                        	fView.updateViewerBackground();
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
						CheckboxTreeViewer viewer= (CheckboxTreeViewer)fView.getViewer();
						viewer.refresh();
						fView.updateObjects();
					}
				}
			});
		}
	}

	/**
	 * @see IBreakpointsListener#breakpointsChanged(IBreakpoint[], IMarkerDelta[])
	 */
	public void breakpointsChanged(final IBreakpoint[] breakpoints, final IMarkerDelta[] deltas) {
		if (fView.isAvailable() & fView.isVisible()) {
			fView.asyncExec(new Runnable() {
				public void run() {
					if (fView.isAvailable()) {
						CheckboxTreeViewer viewer = (CheckboxTreeViewer)fView.getViewer();
						List groupChanged= getGroupChangeBreakpoints(breakpoints, deltas);
						if (groupChanged.size() > 0) {
							// If the groups has changed, completely refresh the view to
							// pick up structural changes.
							fView.getViewer().refresh();
							if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
							    fView.updateViewerBackground();
							}
							fView.updateObjects();
							// Fire a selection change to update contributed actions
							viewer.setSelection(viewer.getSelection());
							return;
						}
						
						for (int i = 0; i < breakpoints.length; i++) {
							IBreakpoint breakpoint = breakpoints[i];
							IMarker marker= breakpoint.getMarker();
							if (marker != null && marker.exists()) {
								try {
									boolean enabled= breakpoint.isEnabled();
									if (viewer.getChecked(breakpoint) != enabled) {
										viewer.setChecked(breakpoint, breakpoint.isEnabled());
										fView.updateParents(breakpoint, enabled);
									}
									viewer.update(breakpoint, null);
								} catch (CoreException e) {
									DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), DebugUIViewsMessages.getString("BreakpointsViewEventHandler.1"), DebugUIViewsMessages.getString("BreakpointsViewEventHandler.2"), e); //$NON-NLS-1$ //$NON-NLS-2$
									DebugUIPlugin.log(e);
								}
							}
						}
						fView.updateObjects();
					}
				}
			});
		}
	}
	
	/**
	 * Returns a list of breakpoints (from the given list) that have changed groups.
	 * @param breakpoints
	 * @param deltas
	 * @return
	 */
	private List getGroupChangeBreakpoints(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		List groupChanged= new ArrayList();
	    for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			IMarker marker= breakpoint.getMarker();
			if (marker != null && marker.exists()) {
				IMarkerDelta delta= deltas[i];
				if (delta != null) {
					String oldGroup= (String) delta.getAttribute(IBreakpoint.GROUP);
					String newGroup= null;
					try {
						newGroup= breakpoint.getGroup();
					} catch (CoreException e1) {
					}
					if (newGroup != oldGroup) { // new == old if they're both null
						if (newGroup == null || oldGroup == null || !newGroup.equals(oldGroup)) {
							// one is null, one isn't => changed
						    // both not null && !one.equals(other) => changed
							groupChanged.add(breakpoint);
						}
					}
				}
			}
		}
	    return groupChanged;
	}

	/**
	 * When new activities are added or enabled, refresh the view contents to add/remove
	 * breakpoints related to the affected activities.
	 */
	public void activityManagerChanged(final ActivityManagerEvent activityManagerEvent) {
		if (fView.isAvailable() & fView.isVisible() && activityManagerEvent.haveEnabledActivityIdsChanged()) {
			fView.asyncExec(new Runnable() {
				public void run() {
					fView.getViewer().refresh();
				}
			});
		}
	}
}
