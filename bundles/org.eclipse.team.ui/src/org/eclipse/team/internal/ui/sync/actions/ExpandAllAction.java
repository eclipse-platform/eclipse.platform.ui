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