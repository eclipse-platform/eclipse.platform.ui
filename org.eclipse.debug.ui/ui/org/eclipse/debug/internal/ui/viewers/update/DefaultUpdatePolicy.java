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
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;
import org.eclipse.debug.internal.ui.viewers.TreePath;
import org.eclipse.debug.internal.ui.viewers.TreeSelection;

/**
 * Default update policy updates a viewer based on model deltas.
 * 
 * @since 3.2
 */
public class DefaultUpdatePolicy extends AbstractUpdatePolicy implements IModelChangedListener {

	public void modelChanged(IModelDeltaNode delta) {
		updateNodes(new IModelDeltaNode[]{delta});
	}

	protected void updateNodes(IModelDeltaNode[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			IModelDeltaNode node = nodes[i];
			int flags = node.getFlags();

			if ((flags & IModelDeltaNode.CHANGED) != 0) {
				handleChange(node);
			} else if ((flags & IModelDeltaNode.ADDED) != 0) {
				handleAdd(node);
			} else if ((flags & IModelDeltaNode.REMOVED) != 0) {
				handleRemove(node);
			}

			updateNodes(node.getNodes());
		}
	}

	protected void handleChange(IModelDeltaNode node) {
		int flags = node.getFlags();
		AsynchronousViewer viewer = getViewer();
		if (viewer != null) {
			if ((flags & IModelDeltaNode.STATE) != 0) {
				viewer.update(node.getElement());
			}
			if ((flags & IModelDeltaNode.CONTENT) != 0) {
				viewer.refresh(node.getElement());
			}
			if ((flags & IModelDeltaNode.SELECT) != 0) {
				viewer.update(node.getElement());
				TreePath treePath = getTreePath(node);
				((AsynchronousTreeViewer) getViewer()).setSelection(new TreeSelection(treePath));
			}
			if ((flags & IModelDeltaNode.EXPAND) != 0) {
				viewer.update(node.getElement());
				TreePath treePath = getTreePath(node);
				((AsynchronousTreeViewer) getViewer()).expand(new TreeSelection(treePath));
			}
		}
	}

	protected void handleAdd(IModelDeltaNode node) {
		int flags = node.getFlags();
		final TreePath treePath = getTreePath(node);

		((AsynchronousTreeViewer) getViewer()).add(treePath);

		if ((flags & IModelDeltaNode.STATE) != 0) {
			// do nothing??
		}
		if ((flags & IModelDeltaNode.CONTENT) != 0) {
			// do nothing??
		}
		if ((flags & IModelDeltaNode.SELECT) != 0) {
			((AsynchronousTreeViewer) getViewer()).setSelection(new TreeSelection(treePath));
		}
		if ((flags & IModelDeltaNode.EXPAND) != 0) {
			((AsynchronousTreeViewer) getViewer()).expand(new TreeSelection(treePath));
		}
	}

	protected void handleRemove(IModelDeltaNode node) {
		TreePath treePath = getTreePath(node);
		((AsynchronousTreeViewer) getViewer()).remove(treePath);
	}

	protected TreePath getTreePath(IModelDeltaNode node) {
		ArrayList list = new ArrayList();
		list.add(0, node.getElement());
		while (node.getParent() != null) {
			node = node.getParent();
			list.add(0, node.getElement());
		}

		return new TreePath(list.toArray());
	}

}
