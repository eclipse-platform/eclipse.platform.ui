package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
 public class SelectAllVariablesAction extends SelectAllAction {

	protected void update() {
		if (!(getView() instanceof AbstractDebugView)) {
			return;
		}
		Viewer viewer= ((AbstractDebugView)getView()).getViewer();
		getAction().setEnabled(((TreeViewer)viewer).getTree().getItemCount() != 0);
	}
	
	protected String getActionId() {
		return AbstractDebugView.SELECT_ALL_ACTION + ".Variables"; //$NON-NLS-1$
	}
}
