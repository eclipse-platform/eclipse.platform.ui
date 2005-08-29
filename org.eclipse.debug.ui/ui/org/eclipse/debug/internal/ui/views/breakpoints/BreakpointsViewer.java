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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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
    
    
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#refresh()
	 */
	public void refresh() {
		super.refresh();
		initializeCheckedState();
	}

	/**
	 * Sets the initial checked state of the items in the viewer.
	 */
	private void initializeCheckedState() {
		TreeItem[] items = getTree().getItems();
		for (int i = 0; i < items.length; i++) {
			updateCheckedState(items[i]);
		}
	}    
    
    /**
     * Update the checked state up the given element and all of its children.
     * 
     * @param element
     */
	public void updateCheckedState(Object element) {
        Widget widget = searchItem(element);
        if (widget != null) {
            updateCheckedState((TreeItem)widget);
        }
	}
    
    /**
     * Update the checked state up the given element and all of its children.
     * 
     * @param element
     */
    public void updateCheckedState(TreeItem item) {
        Object element = item.getData();
        if (element instanceof IBreakpoint) {
            try {
                item.setChecked(((IBreakpoint) element).isEnabled());
                refreshItem(item);
            } catch (CoreException e) {
                DebugUIPlugin.log(e);
            }
        } else if (element instanceof BreakpointContainer) {
            IBreakpoint[] breakpoints = ((BreakpointContainer) element).getBreakpoints();
            int enabledChildren= 0;
            for (int i = 0; i < breakpoints.length; i++) {
                IBreakpoint breakpoint = breakpoints[i];
                try {
                    if (breakpoint.isEnabled()) {
                        enabledChildren++;
                    }
                } catch (CoreException e) {
                    DebugUIPlugin.log(e);
                }
            }
            if (enabledChildren == 0) {
                // Uncheck the container node if no children are enabled
                item.setGrayed(false);
                item.setChecked(false);
            } else if (enabledChildren == breakpoints.length) {
                // Check the container if all children are enabled
                item.setGrayed(false);
                item.setChecked(true);
            } else {
                // If some but not all children are enabled, gray the container node
                item.setGrayed(true);
                item.setChecked(true);
            }
            // Update any children (breakpoints and containers)
            TreeItem[] items = item.getItems();
            for (int i = 0; i < items.length; i++) {
                updateCheckedState(items[i]);
            }
        }
    }        
}
