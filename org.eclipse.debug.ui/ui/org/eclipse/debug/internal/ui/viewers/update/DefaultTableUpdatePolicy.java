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
import org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Default update policy updates a viewer based on model deltas.
 * 
 * @since 3.2
 */
public class DefaultTableUpdatePolicy extends AbstractUpdatePolicy implements IModelChangedListener {

    public void modelChanged(IModelDelta delta) {
        IModelDelta[] nodes = delta.getNodes();
        updateNodes(nodes);
    }

    private void handleState(IModelDelta node) {
        AsynchronousViewer viewer = getViewer();
        if (viewer != null) {
            Object element = node.getElement();
			viewer.update(element);
            updateSelection(element, node.getFlags());
        }
    }
    private void handleContent(IModelDelta node) {
    	AsynchronousViewer viewer = getViewer();
        if (viewer != null) {
        	Object element = node.getElement();
			viewer.refresh(element);
        	updateSelection(element, node.getFlags());
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

    protected void updateNodes(IModelDelta[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            IModelDelta node = nodes[i];
            int flags = node.getFlags();

            if ((flags & IModelDelta.STATE) != 0) {
                handleState(node);
            }
            if ((flags & IModelDelta.CONTENT) != 0) {
                handleContent(node);
            }
            if ((flags & IModelDelta.ADDED) != 0) {
                handleAdd(node);
            } 
            if ((flags & IModelDelta.REMOVED) != 0) {
                handleRemove(node);
            }
            if ((flags & IModelDelta.REPLACED) != 0) {
                handleReplace(node);
            }
            if ((flags & IModelDelta.INSERTED) != 0) {
                handleInsert(node);
            }

            IModelDelta[] childNodes = node.getNodes();
            updateNodes(childNodes);
        }
    }

    private void handleInsert(IModelDelta node) {
        AsynchronousTableViewer viewer = (AsynchronousTableViewer) getViewer();
        if (viewer != null) {
            viewer.insert(node.getElement(), node.getIndex());
            updateSelection(node.getElement(), node.getFlags());
        }
    }

    private void handleReplace(IModelDelta node) {
        AsynchronousTableViewer viewer = (AsynchronousTableViewer) getViewer();
        if (viewer != null) {
            viewer.replace(node.getElement(), node.getReplacementElement());
            updateSelection(node.getReplacementElement(), node.getFlags());
        }
    }

    protected void handleAdd(IModelDelta node) {
        ((AsynchronousTableViewer) getViewer()).add(node.getElement());
        updateSelection(node.getElement(), node.getFlags());
    }

    protected void handleRemove(IModelDelta node) {
        ((AsynchronousTableViewer) getViewer()).remove(node.getElement());
    }
}
