package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;


public class SelectAllBreakpointsAction extends SelectAllAction {

	protected void update() {
		getAction().setEnabled(
			DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length != 0);
	}	
		
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) {
		if (!(getView() instanceof AbstractDebugView)) {
			return;
		}
		Viewer viewer = ((AbstractDebugView) getView()).getViewer();
		if (!(viewer instanceof TableViewer)) {
			return;
		}
		((TableViewer) viewer).getTable().selectAll();
		//ensure that the selection change callback is fired
		viewer.setSelection(viewer.getSelection());
	}
}