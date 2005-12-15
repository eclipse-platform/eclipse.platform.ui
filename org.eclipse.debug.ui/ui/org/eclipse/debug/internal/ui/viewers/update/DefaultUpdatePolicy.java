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
        updateNodes(new IModelDelta[] { delta });
    }

    protected void updateNodes(IModelDelta[] nodes) {
        AsynchronousTreeViewer viewer = (AsynchronousTreeViewer) getViewer();
        if (viewer == null) {
            return;
        }

        for (int i = 0; i < nodes.length; i++) {
            IModelDelta node = nodes[i];
            int flags = node.getFlags();

            TreePath treePath = getTreePath(node);
            if ((flags & IModelDelta.ADDED) != 0) {
                handleAdd(viewer, treePath);
            }
            if ((flags & IModelDelta.REMOVED) != 0) {
                handleRemove(viewer, treePath);
            }
            if ((flags & IModelDelta.CHANGED) != 0) {
                handleChange(viewer, node.getElement());
            }
            if ((flags & IModelDelta.CONTENT) != 0) {
                handleContent(viewer, node.getElement());
            }
            if ((flags & IModelDelta.EXPAND) != 0) {
                handleExpand(viewer, treePath);
            }
            if ((flags & IModelDelta.SELECT) != 0) {
                handleSelect(viewer, treePath);
            }
            if ((flags & IModelDelta.STATE) != 0) {
                handleState(viewer, node.getElement());
            }
            if ((flags & IModelDelta.INSERTED) != 0) {
                // TODO
            }
            if ((flags & IModelDelta.REPLACED) != 0) {
                // TODO
            }

            updateNodes(node.getNodes());
        }
    }

    protected void handleChange(AsynchronousTreeViewer viewer, Object element) {
        //do nothing...
    }

    protected void handleState(AsynchronousTreeViewer viewer, Object element) {
        viewer.update(element);
    }

    protected void handleSelect(AsynchronousTreeViewer viewer, TreePath treePath) {
        viewer.setSelection(new TreeSelection(treePath));
    }

    protected void handleExpand(AsynchronousTreeViewer viewer, TreePath treePath) {
        viewer.expand(new TreeSelection(treePath));
    }

    protected void handleContent(AsynchronousTreeViewer viewer, Object element) {
        viewer.refresh(element);
    }

    protected void handleRemove(AsynchronousTreeViewer viewer, TreePath treePath) {
        viewer.remove(treePath);
    }

    protected void handleAdd(AsynchronousTreeViewer viewer, TreePath treePath) {
        viewer.add(treePath);
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
