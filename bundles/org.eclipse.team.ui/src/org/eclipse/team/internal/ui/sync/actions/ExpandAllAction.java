/*
 * Created on Jun 16, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.team.internal.ui.sync.actions;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.actions.ActionContext;


class ExpandAllAction extends Action {
	private final SyncViewerActions actions;
	public ExpandAllAction(SyncViewerActions actions) {
		super("Expand All");
		this.actions = actions;
	}
	public void run() {
		expandSelection();
	}
	public void update() {
		setEnabled(getTreeViewer() != null && hasSelection());
	}
	protected void expandSelection() {
		AbstractTreeViewer treeViewer = getTreeViewer();
		if (treeViewer != null) {
			ISelection selection = getSelection();
			if (selection instanceof IStructuredSelection) {
				Iterator elements = ((IStructuredSelection)selection).iterator();
				while (elements.hasNext()) {
					Object next = elements.next();
					treeViewer.expandToLevel(next, AbstractTreeViewer.ALL_LEVELS);
				}
			}
		}
	}
	private AbstractTreeViewer getTreeViewer() {
		Viewer viewer = actions.getSyncView().getViewer();
		if (viewer instanceof AbstractTreeViewer) {
			return (AbstractTreeViewer)viewer;
		}
		return null;
	}
	private ISelection getSelection() {
		ActionContext context = actions.getContext();
		if (context == null) return null;
		return actions.getContext().getSelection();
	}
	private boolean hasSelection() {
		ISelection selection = getSelection();
		return (selection != null && !selection.isEmpty());
	}
}