/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Breakpoints viewer.
 */
public class BreakpointsViewer extends CheckboxTreeViewer {

    /**
     * Constructs a new breakpoints viewer with the given tree.
     * 
     * @param tree
     */
    public BreakpointsViewer(Tree tree) {
        super(tree);
    }
    
    /**
     * Returns the selected items.
     * 
     * @return seleted items
     */
    public Item[] getSelectedItems() {
        return getSelection(getControl());
    }
    
    /**
     * Returns the item assocaited with the given element, or <code>null</code>.
     * 
     * @param element element in breakpoints view
     * @return item assocaited with the given element, or <code>null</code>
     */
    public Widget searchItem(Object element) {
        return findItem(element);
    }
    
    /**
     * Refreshes the given item in the tree.
     * 
     * @param item item to refresh
     */
    public void refreshItem(TreeItem item) {
        updateItem(item, item.getData());
    }
    
    /**
     * Returns a collection of currently visible breakpoints.
     * 
     * @return collection of currently visible breakpoints
     */
    public IBreakpoint[] getVisibleBreakpoints() {
        IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
        Object[] elements= ((ITreeContentProvider)getContentProvider()).getElements(manager);
        List list = new ArrayList();
        for (int i = 0; i < elements.length; i++) {
            TreeItem item = (TreeItem) searchItem(elements[i]);
            if (item != null) {
                collectExpandedBreakpoints(item, list);
            }
        }
        return (IBreakpoint[]) list.toArray(new IBreakpoint[list.size()]);
    }

    /**
     * Adds expanded breakpoints to the list. Traverses children of the given
     * tree item if any.
     * 
     * @param item  
     * @param list collection of visible breakpoints
     */
    private void collectExpandedBreakpoints(TreeItem item, List list) {
        Object data = item.getData();
        if (data instanceof IBreakpoint) {
            list.add(data);
            return;
        }
        if (item.getExpanded()) {
            TreeItem[] items = item.getItems();
            for (int i = 0; i < items.length; i++) {
                collectExpandedBreakpoints(items[i], list);
            }
        }
    }

    /**
     * Sets the selection to a specific tree item
     * 
     * @param item
     */
    protected void setSelection(TreeItem item) {
    	getTree().setSelection(new TreeItem[]{item});
    	updateSelection(getSelection());
    }
}
