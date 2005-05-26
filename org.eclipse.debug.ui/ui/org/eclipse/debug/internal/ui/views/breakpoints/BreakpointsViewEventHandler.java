/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak - bug 57999
 *     Michael Fraenkel - bug 84385
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * Handles breakpoint events and activity manager events (which can affect grouping),
 * updating the breakpoints view and viewer.
 */
public class BreakpointsViewEventHandler implements IBreakpointsListener, IActivityManagerListener, IBreakpointManagerListener {

	private BreakpointsView fView;

	/**
	 * Constructs an event handler for the breakpoints view.
	 */
	public BreakpointsViewEventHandler(BreakpointsView view) {
		fView= view;
		IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
		breakpointManager.addBreakpointListener(this);
		breakpointManager.addBreakpointManagerListener(this);
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		if (activitySupport != null) {
			activitySupport.getActivityManager().addActivityManagerListener(this);
		}
	}
	
	/**
	 * When this event handler is disposed, remove it as a listener.
	 */
	public void dispose() {
		IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
		breakpointManager.removeBreakpointListener(this);
		breakpointManager.removeBreakpointManagerListener(this);
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
						CheckboxTreeViewer viewer = fView.getCheckboxViewer();
                        viewer.getControl().setRedraw(false);
                        BreakpointsContentProvider provider = (BreakpointsContentProvider)viewer.getContentProvider();
                        provider.reorganize();
                        
						// This code is left in as a test case for platform bug 77075
						//for (int i = 0; i < breakpoints.length; i++) { 
							//viewer.expandToLevel(breakpoints[i], AbstractTreeViewer.ALL_LEVELS);
						//}
                        // expand as required
                        for (int i = 0; i < breakpoints.length; i++) {
                            IBreakpoint breakpoint = breakpoints[i];
                            BreakpointContainer[] roots = provider.getRoots(breakpoint);
                            if (roots != null) {
                                for (int j = 0; j < roots.length; j++) {
                                    viewer.expandToLevel(roots[j], AbstractTreeViewer.ALL_LEVELS);
                                }
                            }
                        }
                        fView.initializeCheckedState();
                        viewer.setSelection(new StructuredSelection(breakpoints));
                        viewer.getControl().setRedraw(true);
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
                        viewer.getControl().setRedraw(false);
                        ((BreakpointsContentProvider)viewer.getContentProvider()).reorganize();
                        fView.initializeCheckedState();
                        viewer.getControl().setRedraw(true);
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
                        viewer.getControl().setRedraw(false);
                        BreakpointsContentProvider provider = (BreakpointsContentProvider) viewer.getContentProvider();
                        Set updates = new HashSet();
						for (int i = 0; i < breakpoints.length; i++) {
							IBreakpoint breakpoint = breakpoints[i];
                            viewer.update(breakpoint, null);
                            BreakpointContainer[] containers = provider.getContainers(breakpoint);
                            if (containers != null) {
                                for (int j = 0; j < containers.length; j++ ) {
                                    updates.add(containers[j]);
                                }
                            } else {
                            	updates.add(breakpoint);
                            }
						}
                        Object[] objects = updates.toArray();
                        for (int i = 0; i < objects.length; i++) {
                            fView.updateCheckedState(objects[i]);
                        }
                        viewer.getControl().setRedraw(true);
						fView.updateObjects();
					}
				}
			});
		}
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManagerListener#breakpointManagerEnablementChanged(boolean)
	 */
	public void breakpointManagerEnablementChanged(boolean enabled) {
		if (fView.isAvailable() & fView.isVisible()) {
			fView.asyncExec(new Runnable() {
				public void run() {
					fView.getViewer().refresh();
				}
			});
		}		
	}
}
