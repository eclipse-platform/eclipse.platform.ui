/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;

/**
 * CutBreakpointsAction
 */
public class CutBreakpointsAction extends CopyBreakpointsAction {

    /**
     * Constructs a cut operation for breakpoints. Performs a move
     * on paste.
     * 
     * @param view
     * @param clipboard
     * @param pasteAction
     */
    public CutBreakpointsAction(BreakpointsView view, Clipboard clipboard, PasteBreakpointsAction pasteAction) {
        super(view, clipboard, pasteAction);
        setText(BreakpointGroupMessages.getString("CutBreakpointsAction.0")); //$NON-NLS-1$
        setToolTipText(BreakpointGroupMessages.getString("CutBreakpointsAction.1")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        super.run();
        IStructuredSelection selection = getStructuredSelection();
        // only cut from selected containers, not all
        Object[] objects = selection.toArray();
        BreakpointsViewer viewer = (BreakpointsViewer) breakpointsView.getViewer();
        Item[] items = viewer.getSelectedItems();
        for (int i = 0; i < objects.length; i++) {
            IBreakpoint breakpoint = (IBreakpoint) objects[i];
            BreakpointContainer[] containers = getSourceContainers(breakpoint, items);
            breakpointsView.performRemove(containers, new StructuredSelection(breakpoint));
        }
    }
    
    private BreakpointContainer[] getSourceContainers(IBreakpoint breakpoint, Item[] items) {
        List list = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            TreeItem item = (TreeItem) items[i];
            if (!item.isDisposed() && breakpoint.equals(item.getData())) {
                BreakpointContainer parent = getRemoveableParent(item, breakpoint);
                if (parent != null) {
                    list.add(parent);
                }
            }
        }
        return (BreakpointContainer[]) list.toArray(new BreakpointContainer[list.size()]);
    }
    
    private BreakpointContainer getRemoveableParent(TreeItem item, IBreakpoint breakpoint) {
        TreeItem parentItem = item.getParentItem();
        if (parentItem != null) {
            Object data = parentItem.getData();
            if (data instanceof BreakpointContainer) {
                BreakpointContainer container = (BreakpointContainer) data;
                if (container.getOrganizer().canRemove(breakpoint, container.getCategory())) {
                    return container;
                }
            }
            return getRemoveableParent(parentItem, breakpoint);
        }
        return null;
    }
}
