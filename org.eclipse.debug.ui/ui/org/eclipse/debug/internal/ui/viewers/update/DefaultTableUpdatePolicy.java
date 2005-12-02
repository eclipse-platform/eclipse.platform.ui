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

import org.eclipse.debug.internal.ui.viewers.AsynchronousTableViewer;
import org.eclipse.debug.internal.ui.viewers.AsynchronousViewer;
import org.eclipse.debug.internal.ui.viewers.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Default update policy updates a viewer based on model deltas.
 * 
 * @since 3.2
 */
public class DefaultTableUpdatePolicy extends AbstractUpdatePolicy implements IModelChangedListener {

    public void modelChanged(IModelDelta delta) {
        IModelDeltaNode[] nodes = delta.getNodes();
        updateNodes(nodes);
    }

    private void updateElementContent(IModelDeltaNode node) {
        AsynchronousViewer viewer = getViewer();
        if (viewer != null) {
            int flags = node.getFlags();
            if ((flags & IModelDelta.STATE) != 0) {
                viewer.update(node.getElement());
            }
            if ((flags & IModelDelta.CONTENT) != 0) {
                viewer.refresh(node.getElement());
            }
        }
    }

    private void updateSelection(Object element, int flags) {
        AsynchronousViewer viewer = getViewer();
        if (viewer != null) {
            if ((flags & IModelDelta.SELECT) != 0) {
                ((AsynchronousTableViewer) getViewer()).setSelection(new StructuredSelection(element));
            }
        }
    }

    protected void updateNodes(IModelDeltaNode[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            IModelDeltaNode node = nodes[i];
            int flags = node.getFlags();

            if ((flags & IModelDelta.CHANGED) != 0) {
                handleChange(node);
            } else if ((flags & IModelDelta.ADDED) != 0) {
                handleAdd(node);
            } else if ((flags & IModelDelta.REMOVED) != 0) {
                handleRemove(node);
            } else if ((flags & IModelDelta.REPLACED) != 0) {
                handleReplace(node);
            } else if ((flags & IModelDelta.INSERTED) != 0) {
                handleInsert(node);
            }

            IModelDeltaNode[] childNodes = node.getNodes();
            updateNodes(childNodes);
        }
    }

    private void handleInsert(IModelDeltaNode node) {
        AsynchronousTableViewer viewer = (AsynchronousTableViewer) getViewer();
        if (viewer != null) {
            viewer.insert(node.getElement(), node.getIndex());
            updateSelection(node.getElement(), node.getFlags());
        }
    }

    private void handleReplace(IModelDeltaNode node) {
        AsynchronousTableViewer viewer = (AsynchronousTableViewer) getViewer();
        if (viewer != null) {
            viewer.replace(node.getElement(), node.getNewElement());
            updateSelection(node.getNewElement(), node.getFlags());
        }
    }

    protected void handleChange(IModelDeltaNode node) {
        updateElementContent(node);
        updateSelection(node.getElement(), node.getFlags());
    }

    protected void handleAdd(IModelDeltaNode node) {
        ((AsynchronousTableViewer) getViewer()).add(node.getElement());
        updateSelection(node.getElement(), node.getFlags());
    }

    protected void handleRemove(IModelDeltaNode node) {
        ((AsynchronousTableViewer) getViewer()).remove(node.getElement());
    }
}
