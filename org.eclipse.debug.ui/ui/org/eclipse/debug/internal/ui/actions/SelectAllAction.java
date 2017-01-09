/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewPart;

public abstract class SelectAllAction extends AbstractRemoveAllActionDelegate {

	private IViewPart fView;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractSelectionActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		fView = view;
		IDebugView debugView = getView().getAdapter(IDebugView.class);
		if (debugView != null) {
			debugView.setAction(getActionId(), getAction());
		}
		super.init(view);
	}

	protected IViewPart getView() {
		return fView;
	}

	protected abstract String getActionId();

	private void collectExpandedAndVisible(TreeItem[] items, List<TreeItem> result) {
		for (int i= 0; i < items.length; i++) {
			TreeItem item= items[i];
			result.add(item);
			if (item.getExpanded()) {
				collectExpandedAndVisible(item.getItems(), result);
			}
		}
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action){
		if (!(getView() instanceof IDebugView)) {
			return;
		}
		Viewer viewer = ((IDebugView) getView()).getViewer();
		if (viewer instanceof TreeViewer) {
			ArrayList<TreeItem> allVisible= new ArrayList<TreeItem>();
			Tree tree= ((TreeViewer) viewer).getTree();
			collectExpandedAndVisible(tree.getItems(), allVisible);
			tree.setSelection(allVisible.toArray(new TreeItem[allVisible.size()]));
			// force viewer selection change
			viewer.setSelection(viewer.getSelection());
		}
	}

}
