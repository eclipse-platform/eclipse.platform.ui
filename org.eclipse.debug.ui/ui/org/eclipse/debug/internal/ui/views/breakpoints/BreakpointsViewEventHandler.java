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
						String autoGroup= fView.getAutoGroup();
						if (autoGroup != null) {
						    // Add any new breakpoints to the "default group"
							for (int i = 0; i < breakpoints.length; i++) {
								try {
									breakpoints[i].setGroup(autoGroup);
								} catch (CoreException e) {
								}
							}
						}
						CheckboxTreeViewer viewer = fView.getCheckboxViewer();
						viewer.refresh();
						if (autoGroup != null) {
						    // After updating to pick up structural changes (possible new group creation),
						    // update the checked state of the default group.
							int enabledChildren= 0;
							Object[] children = fView.getTreeContentProvider().getChildren(autoGroup);
							for (int i = 0; i < children.length; i++) {
                                try {
                                    if (((IBreakpoint) children[i]).isEnabled()) {
                                        enabledChildren++;
                                    }
                                } catch (CoreException e) {
                                }
                            }
							if (enabledChildren == children.length) {
							    viewer.setChecked(autoGroup, true);
							    viewer.setGrayed(autoGroup, false);
							} else {
							    viewer.setGrayChecked(autoGroup, enabledChildren > 0);
							}
						}
						MultiStatus status= new MultiStatus(DebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, DebugUIViewsMessages.getString("BreakpointsViewEventHandler.4"), null); //$NON-NLS-1$
						for (int i = 0; i < breakpoints.length; i++) {
							IBreakpoint breakpoint = breakpoints[i];
							// check if the breakpoint is still registered at this time
							if (!DebugPlugin.getDefault().getBreakpointManager().isRegistered(breakpoint)) {
								continue;
							}
							try {
								boolean enabled= breakpoint.isEnabled();
								if (viewer.getChecked(breakpoint) != enabled) {
									viewer.setChecked(breakpoint, breakpoint.isEnabled());								
								}

                                if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
                                	fView.updateViewerBackground();
                                }
							} catch (CoreException e) {
								status.add(DebugUIPlugin.newErrorStatus(DebugUIViewsMessages.getString("BreakpointsViewEventHandler.5"),e)); //$NON-NLS-1$
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
						boolean refreshCheckedState= false;
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
									boolean needsRefresh= false;
									if (newGroup != oldGroup) { // new == old if they're both null
										if (newGroup == null || oldGroup == null) {
											// one is null, one isn't => changed
											needsRefresh= true;
										} else { // moved from one group to another ?
											needsRefresh= !newGroup.equals(oldGroup);
										}
									}
									if (needsRefresh) {
										// If the group has changed, completely refresh the view to
										// pick up structural changes.
										fView.getViewer().refresh();
										fView.initializeCheckedState();
										return;
									}
								}
								try {
									boolean enabled= breakpoint.isEnabled();
									if (viewer.getChecked(breakpoint) != enabled) {
										refreshCheckedState= true;
										viewer.setChecked(breakpoint, breakpoint.isEnabled());
										viewer.update(breakpoint, null);							
									}
								} catch (CoreException e) {
									DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), DebugUIViewsMessages.getString("BreakpointsViewEventHandler.1"), DebugUIViewsMessages.getString("BreakpointsViewEventHandler.2"), e); //$NON-NLS-1$ //$NON-NLS-2$
									DebugUIPlugin.log(e);
								}
							}
						}
						if (refreshCheckedState) {
							fView.initializeCheckedState();
						}
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
					fView.initializeCheckedState();
				}
			});
		}
	}
}
