/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.ui.IViewPart;

public class SelectAllBreakpointsAction extends SelectAllAction implements IBreakpointsListener {

	protected void update() {
		getAction().setEnabled(
			DebugPlugin.getDefault().getBreakpointManager().hasBreakpoints());
	}

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) {
		if (!(getView() instanceof BreakpointsView)) {
			return;
		}
		CheckboxTreeViewer viewer = ((BreakpointsView) getView()).getCheckboxViewer();
		viewer.getTree().selectAll();
		//ensure that the selection change callback is fired
		viewer.setSelection(viewer.getSelection());
	}

	/**
	 * @see IBreakpointsListener#breakpointsAdded(IBreakpoint[])breakpointAdded(IBreakpoint)
	 */
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		if (getAction() != null && !getAction().isEnabled()) {
			update();
		}
	}

	/**
	 * @see IBreakpointsListener#breakpointsChanged(IBreakpoint[], IMarkerDelta[])breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
	}

	/**
     * @see IBreakpointsListener#breakpointsRemoved(IBreakpoint[], IMarkerDelta[])
	 */
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		if (getAction() != null) {
			update();
		}
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}
	
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);	
		super.dispose();
	}
	
	protected String getActionId() {
		return IDebugView.SELECT_ALL_ACTION;
	}
}
