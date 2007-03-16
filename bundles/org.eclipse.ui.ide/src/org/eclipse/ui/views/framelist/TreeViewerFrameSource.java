/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.framelist;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;

/** 
 * Frame source for tree viewers, which uses <code>TreeFrame</code> to capture
 * the state of the tree viewer.
 * 
 * @see TreeFrame
 */
public class TreeViewerFrameSource implements IFrameSource {

    private AbstractTreeViewer viewer;

    /**
     * Constructs a new tree viewer frame source for the specified tree viewer.
     * 
     * @param viewer the tree viewer
     */
    public TreeViewerFrameSource(AbstractTreeViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * Connects this source as a listener on the frame list,
     * so that when the current frame changes, the viewer is updated.
     */
    public void connectTo(FrameList frameList) {
        frameList.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                TreeViewerFrameSource.this.handlePropertyChange(event);
            }
        });
    }

    /**
     * Returns a new tree frame capturing the specified input element.
     * 
     * @param input the input element
     * @return the tree frame
     */
    protected TreeFrame createFrame(Object input) {
        return new TreeFrame(viewer, input);
    }

    /**
     * Updates the viewer in response to the current frame changing.
     * 
     * @param frame the new value for the current frame
     */
    protected void frameChanged(TreeFrame frame) {
        viewer.getControl().setRedraw(false);
        viewer.setInput(frame.getInput());
        viewer.setExpandedElements(frame.getExpandedElements());
        viewer.setSelection(frame.getSelection(), true);
        viewer.getControl().setRedraw(true);
    }

    /**
     * Returns the current frame.
     * 
     * @param flags a bit-wise OR of the frame source flag constants
     * @return the current frame
     */
    protected Frame getCurrentFrame(int flags) {
        Object input = viewer.getInput();
        TreeFrame frame = createFrame(input);
        if ((flags & IFrameSource.FULL_CONTEXT) != 0) {
            frame.setSelection(viewer.getSelection());
            frame.setExpandedElements(viewer.getExpandedElements());
        }
        return frame;
    }

    /* (non-Javadoc)
     * Method declared on IFrameSource.
     */
    public Frame getFrame(int whichFrame, int flags) {
        switch (whichFrame) {
        case IFrameSource.CURRENT_FRAME:
            return getCurrentFrame(flags);
        case IFrameSource.PARENT_FRAME:
            return getParentFrame(flags);
        case IFrameSource.SELECTION_FRAME:
            return getSelectionFrame(flags);
        default:
            return null;
        }
    }

    /**
     * Returns the parent frame, or <code>null</code> if there is no parent frame.
     * 
     * @param flags a bit-wise OR of the frame source flag constants
     * @return the parent frame, or <code>null</code>
     */
    protected Frame getParentFrame(int flags) {
        Object input = viewer.getInput();
        ITreeContentProvider provider = (ITreeContentProvider) viewer
                .getContentProvider();
        Object parent = provider.getParent(input);
        if (parent == null) {
            return null;
        } else {
            TreeFrame frame = createFrame(parent);
            if ((flags & IFrameSource.FULL_CONTEXT) != 0) {
                frame.setSelection(viewer.getSelection());
                // include current input in expanded set
                Object[] expanded = viewer.getExpandedElements();
                Object[] newExpanded = new Object[expanded.length + 1];
                System.arraycopy(expanded, 0, newExpanded, 0, expanded.length);
                newExpanded[newExpanded.length - 1] = input;
                frame.setExpandedElements(newExpanded);
            }
            return frame;
        }
    }

    /**
     * Returns the frame for the selection, or <code>null</code> if there is no
     * frame for the selection.
     * 
     * @param flags a bit-wise OR of the frame source flag constants
     * @return the selection frame, or <code>null</code>
     */
    protected Frame getSelectionFrame(int flags) {
        IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
        if (sel.size() == 1) {
            Object o = sel.getFirstElement();
            if (viewer.isExpandable(o)) {
                TreeFrame frame = createFrame(o);
                if ((flags & IFrameSource.FULL_CONTEXT) != 0) {
                    frame.setSelection(viewer.getSelection());
                    frame.setExpandedElements(viewer.getExpandedElements());
                }
                return frame;
            }
        }
        return null;
    }

    /**
     * Returns the tree viewer.
     * 
     * @return the tree viewer
     */
    public AbstractTreeViewer getViewer() {
        return viewer;
    }

    /**
     * Handles a property change event from the frame list.
     * Calls <code>frameChanged</code> when the current frame changes.
     */
    protected void handlePropertyChange(PropertyChangeEvent event) {
        if (FrameList.P_CURRENT_FRAME.equals(event.getProperty())) {
            frameChanged((TreeFrame) event.getNewValue());
        }
    }
}
