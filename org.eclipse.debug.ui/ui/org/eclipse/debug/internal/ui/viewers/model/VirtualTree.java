/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.SWT;


/**
 * Tree of virtual items that is analogous to SWT's tree control. 
 * 
 * @since 3.5
 */
class VirtualTree extends VirtualItem {

    /**
     * Lazy virtual tree does not retrieve elements or labels,
     * except for the selected elements.
     */
    private boolean fLazy;
    
    /**
     * The item currently at the top of the "virtual" view-port.
     */
    private VirtualItem fTopItem;
    
    /**
     * Interface for listeners that need to be notified when items 
     * are disposed or revealed.  It  should be implemented by the viewer.
     */
    public static interface IVirtualItemListener {
        
        /**
         * Called when the item has been shown in the virtual viewer's 
         * view-port.  This indicates to the viewer that it should check
         * the item's status and request needed data.
         * 
         * @param item The item that was revealed.
         */
        public void revealed(VirtualItem item);
        
        /**
         * Called when an item is disposed.  It tells the viewer to
         * clean up any remaining mappings and cached data of this item.
         * 
         * @param item The itam that was disposed.
         */
        public void disposed(VirtualItem item);
    }
    
    /**
     * Set of listeners of the virtual tree.
     */
    private Set fVirtualItemListeners = new HashSet(1);

    /**
     * The currently selected items.  This array contains only
     * the leaf items which are selected.
     */
    private VirtualItem[] fSelection = new VirtualItem[0];
    
    VirtualTree(int style) {
        super(null, new VirtualItem.Index(0));
        fLazy = (style & SWT.VIRTUAL) != 0;
        clearNeedsLabelUpdate();
        clearNeedsDataUpdate();
    }
    
    void dispose() {
        super.dispose();
        fVirtualItemListeners.clear();
    }

    void setNeedsCountUpdate() {
        super.setNeedsCountUpdate();
        clearNeedsLabelUpdate();
        clearNeedsDataUpdate();
    }
    
    void setNeedsLabelUpdate() {
        // no-op
    }

    void setData(String key, Object data) {
        super.setData(key, data);
        if (data == null) {
            clearNeedsDataUpdate();
        }
    }
    
    void addItemListener(IVirtualItemListener listener) {
        fVirtualItemListeners.add(listener);
    }

    void removeItemListener(IVirtualItemListener listener) {
        fVirtualItemListeners.remove(listener);
    }

    VirtualItem[] getSelection() {
        return fSelection;
    }
    
    void setSelection(VirtualItem[] items) {
        fSelection = items;
    }
    
    void showItem(VirtualItem item) {
        setTopItem(item);
    }
    
    void fireItemDisposed(VirtualItem item) {
        for (Iterator itr = fVirtualItemListeners.iterator(); itr.hasNext();) {
            ((IVirtualItemListener)itr.next()).disposed(item);
        }
    }
    
    void fireItemRevealed(VirtualItem item) {
        for (Iterator itr = fVirtualItemListeners.iterator(); itr.hasNext();) {
            ((IVirtualItemListener)itr.next()).revealed(item);
        }        
    }

    void setData(Object data) {
        super.setData(data);
        // The root item always has children as long as the input is non-null, 
        // so that it should be expanded.
        setHasItems(data != null);
    }

    void setTopItem(VirtualItem item) {
        fTopItem = item;
    }
    
    VirtualItem getTopItem() {
        return fTopItem;
    }
    
    void setHasItems(boolean hasChildren) {
        super.setHasItems(hasChildren);
        // The root item is always expanded as long as it has children. 
        if (hasChildren) {
            setExpanded(true);
        }
    }
    
    boolean isItemVisible(VirtualItem item) {
        if (!fLazy) {
            // If not in lazy mode, all items are visible.
            return true;
        } else {
            // TODO: use top item and visible item count to determine list of 
            // visible items.  For now only mark the selected items as visible.
            for (int i = 0; i < fSelection.length; i++) {
                VirtualItem selectionItem = fSelection[i]; 
                while (selectionItem != null) {
                    if (item.equals(selectionItem)) {
                        return true;
                    }
                    selectionItem = selectionItem.getParent();
                }
            }
            return false;
        }
    }

    /**
     * Validates the entire tree.
     */
    void validate() {
        validate(VirtualTree.this);
    }
    
    /**
     * Validates the item and its children, identifying children which were 
     * revealed and need to be updated.
     * 
     * @param item The item which to validate.
     */
    void validate(VirtualItem item) {
        if (item.needsDataUpdate()) {
            if (isItemVisible(item)) {
                fireItemRevealed(item);
            }
        } else if (item.getData() != null) {
            if ( item.needsLabelUpdate() || (item.needsCountUpdate() && item.hasItems() && item.getExpanded()) ) {
                if (isItemVisible(item)) {
                    fireItemRevealed(item);
                }
            }
            
            if (item.getData() != null && item.getItemCount() > 0 && item.getExpanded()) {
                for (int i = 0; i < item.getItemCount(); i++) {
                    validate(item.getItem(new Index(i)));
                }
            }
        }
    }
}
