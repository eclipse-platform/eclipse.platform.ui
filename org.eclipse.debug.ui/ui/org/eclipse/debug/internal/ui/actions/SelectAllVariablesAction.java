package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
 
public class SelectAllVariablesAction extends SelectAllAction {

	protected void update() {
		if (!(getView() instanceof IDebugView)) {
			return;
		}
		Viewer viewer= ((IDebugView)getView()).getViewer();
		getAction().setEnabled(((TreeViewer)viewer).getTree().getItemCount() != 0);
	}
	
	protected String getActionId() {
		return IDebugView.SELECT_ALL_ACTION + ".Variables"; //$NON-NLS-1$
	}
}
