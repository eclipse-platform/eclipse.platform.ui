/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Improve usability of the breakpoint view (Bug 238956)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.actions.SelectAllAction;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Tree;

public class SelectAllBreakpointsAction extends SelectAllAction implements IBreakpointsListener {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#isEnabled()
	 */
	protected boolean isEnabled() {
		return DebugPlugin.getDefault().getBreakpointManager().hasBreakpoints();
	}

	public void run(IAction action) {	
		Viewer viewer = ((AbstractDebugView) getView()).getViewer();
		((Tree) viewer.getControl()).selectAll();
		// ensure that the selection change callback is fired
		viewer.setSelection(viewer.getSelection());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		if (getAction() != null && !getAction().isEnabled()) {
			update();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		if (getAction() != null) {
			update();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#initialize()
	 */
	protected void initialize() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.SelectAllAction#getActionId()
	 */
	protected String getActionId() {
		return IDebugView.SELECT_ALL_ACTION;
	}
}
