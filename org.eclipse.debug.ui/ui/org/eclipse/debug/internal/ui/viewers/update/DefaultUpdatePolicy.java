/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import java.util.ArrayList;

import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.AsynchronousViewer;
import org.eclipse.debug.internal.ui.viewers.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;
import org.eclipse.debug.internal.ui.viewers.TreePath;
import org.eclipse.debug.internal.ui.viewers.TreeSelection;

/**
 * Update policy for the launch manager.
 * 
 * @since 3.2
 */
public class DefaultUpdatePolicy extends AbstractUpdatePolicy implements IModelChangedListener {

	public DefaultUpdatePolicy() {
		super();
	}

	public void init(AsynchronousViewer viewer) {
		super.init(viewer);
	}

	public synchronized void dispose() {
		super.dispose();
	}

	public void modelChanged(IModelDelta delta) {
		IModelDeltaNode[] nodes = delta.getNodes();
		updateNodes(nodes);
	}

	private void updateNodes(IModelDeltaNode[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			IModelDeltaNode node = nodes[i];
			int flags = node.getFlags();

			if ((flags & IModelDelta.CHANGED) != 0) {
				handleChange(node);
			} else if ((flags & IModelDelta.ADDED) != 0) {
				handleAdd(node);
			} else if ((flags & IModelDelta.REMOVED) != 0) {
				handleRemove(node);
			}

			IModelDeltaNode[] childNodes = node.getNodes();
			for (int j = 0; j < childNodes.length; j++) {
				updateNodes(childNodes);
			}
		}
	}

	private void handleChange(IModelDeltaNode node) {
		int flags = node.getFlags();
		AsynchronousViewer viewer = getViewer();
		if (viewer != null) {
			if ((flags & IModelDelta.STATE) != 0) {
				viewer.update(node.getElement());
			}
			if ((flags & IModelDelta.CONTENT) != 0) {
				viewer.refresh(node.getElement());
			}
			if ((flags & IModelDelta.SELECT) != 0) {
				viewer.update(node.getElement());
				TreePath treePath = getTreePath(node);
				((AsynchronousTreeViewer) getViewer()).setSelection(new TreeSelection(treePath));
			}
			if ((flags & IModelDelta.EXPAND) != 0) {
				viewer.update(node.getElement());
				TreePath treePath = getTreePath(node);
				((AsynchronousTreeViewer) getViewer()).expand(new TreeSelection(treePath));
			}
		}
	}

	private void handleAdd(IModelDeltaNode node) {
		int flags = node.getFlags();
		final TreePath treePath = getTreePath(node);

		((AsynchronousTreeViewer) getViewer()).add(treePath);

		if ((flags & IModelDelta.STATE) != 0) {
			// do nothing??
		}
		if ((flags & IModelDelta.CONTENT) != 0) {
			// do nothing??
		}
		if ((flags & IModelDelta.SELECT) != 0) {
			((AsynchronousTreeViewer) getViewer()).setSelection(new TreeSelection(treePath));
		}
		if ((flags & IModelDelta.EXPAND) != 0) {
			((AsynchronousTreeViewer) getViewer()).expand(new TreeSelection(treePath));
		}
	}

	private void handleRemove(IModelDeltaNode node) {
		TreePath treePath = getTreePath(node);
		((AsynchronousTreeViewer) getViewer()).remove(treePath);
	}

	private TreePath getTreePath(IModelDeltaNode node) {
		ArrayList list = new ArrayList();
		list.add(0, node.getElement());
		while (node.getParent() != null) {
			node = node.getParent();
			list.add(0, node.getElement());
		}

		return new TreePath(list.toArray());
	}

}
