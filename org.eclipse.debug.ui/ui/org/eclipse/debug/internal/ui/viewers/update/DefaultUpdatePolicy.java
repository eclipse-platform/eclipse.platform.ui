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
import org.eclipse.debug.internal.ui.viewers.TreePath;
import org.eclipse.debug.internal.ui.viewers.TreeSelection;

/**
 * Default update policy updates a viewer based on model deltas.
 * 
 * @since 3.2
 */
public class DefaultUpdatePolicy extends AbstractUpdatePolicy implements IModelChangedListener {

	public void modelChanged(IModelDelta delta) {
		updateNodes(new IModelDelta[]{delta});
	}

	protected void updateNodes(IModelDelta[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			IModelDelta node = nodes[i];
			int flags = node.getFlags();

			if ((flags & IModelDelta.CHANGED) != 0) {
				handleChange(node);
			} else if ((flags & IModelDelta.ADDED) != 0) {
				handleAdd(node);
			} else if ((flags & IModelDelta.REMOVED) != 0) {
				handleRemove(node);
			}

			updateNodes(node.getNodes());
		}
	}

	protected void handleChange(IModelDelta node) {
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

	protected void handleAdd(IModelDelta node) {
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

	protected void handleRemove(IModelDelta node) {
		TreePath treePath = getTreePath(node);
		((AsynchronousTreeViewer) getViewer()).remove(treePath);
	}

	protected TreePath getTreePath(IModelDelta node) {
		ArrayList list = new ArrayList();
		list.add(0, node.getElement());
		while (node.getParent() != null) {
			node = node.getParent();
			list.add(0, node.getElement());
		}

		return new TreePath(list.toArray());
	}

}
