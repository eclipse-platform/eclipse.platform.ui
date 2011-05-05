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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;

/**
 * Virtual item, which is analogous to the SWT's tree item.
 * 
 * @since 3.5
 */
class VirtualItem {

    // Data keys for display attributes of an item.
    static String LABEL_KEY = "LABEL_KEY"; //$NON-NLS-1$
    static String IMAGE_KEY = "IMAGE_KEY"; //$NON-NLS-1$
    static String FONT_KEY = "FONT_KEY"; //$NON-NLS-1$
    static String FOREGROUND_KEY = "FOREGROUND_KEY"; //$NON-NLS-1$
    static String BACKGROUND_KEY = "BACKGROUND_KEY"; //$NON-NLS-1$
    
    static String ELEMENT_DATA_KEY = "element"; //$NON-NLS-1$
    
    /**
     * Index object of a tree item. It allows the indexes to be modified
     * as items are inserted and removed.
     */
    public static class Index implements Comparable {
        private Integer fIndexValue;
        
        public Index(int index) {
            fIndexValue = new Integer(index);
        }
        
        public boolean equals(Object obj) {
            return obj instanceof Index && ((Index)obj).fIndexValue.equals(fIndexValue);
        }
        
        public int hashCode() {
            return fIndexValue.hashCode();
        }
        
        public void increment() {
            fIndexValue = new Integer(fIndexValue.intValue() + 1);
        }
        
        public void decrement() {
            fIndexValue = new Integer(fIndexValue.intValue() - 1);
        }

        public int intValue() {
            return fIndexValue.intValue();
        }
        
        public int compareTo(Object obj) {
            return obj instanceof Index ? fIndexValue.compareTo(((Index)obj).fIndexValue) : 0; 
        }
        
        public String toString() {
            return fIndexValue.toString();
        }
    }
    
    /**
     * Parent items of this item.
     */
    final private VirtualItem fParent;

    /**
     * The index of this item.  
     */
    final private Index fIndex;
    
    /**
     * Map of child items.  The key to the map is the item's index, which 
     * must be the same object instance as the index in the item.  The tree map
     * keeps the items sorted while allowing indexes (keys) to be modified as 
     * child items are inserted and removed.   
     */
    private Map fItems = new TreeMap();
    
    /**
     * Flag indicating whether this item has child items.
     */
    private boolean fHasItems = false;

    /**
     * Indicates that this item has been expanded.  It should only
     * be set to <code>true</code> if fHasItems is <code>true</code>.
     */
    private boolean fExpanded = false;

    /**
     * The cound of child items.  <code>-1</code> indicates that the count 
     * is not known.
     */
    private int fItemCount = -1;
    
    /**
     * The data held by this item.  It includes the element as well as the item
     * display attributes. 
     */
    private Map fData = new HashMap(1);

    /**
     * Flag indicating that the item needs to have it's label updated.
     */
    private boolean fNeedsLabelUpdate = true;
    
    /**
     * Flag indicating that the item's count needs to be updated.
     */
    private boolean fNeedsCountUpdate = true;
    
    /**
     * Flag indicating that the item's element needs to be updated.
     */
    private boolean fNeedsDataUpdate = true;
    
    /**
     * Indicates that this item has been disposed.
     */
    private boolean fDisposed = false;
    
    
    VirtualItem(VirtualItem parent, Index index) {
        fParent = parent;
        fIndex = index;
    }

    void setNeedsCountUpdate() {
        fNeedsCountUpdate = true;
        fItemCount = -1;
    }

    void setNeedsLabelUpdate() {
        fNeedsLabelUpdate = true;
    }

    void setNeedsDataUpdate() {
        fNeedsDataUpdate = true;
    }
    
    void clear(Index index) {
        VirtualItem item = (VirtualItem)fItems.remove(index);
        if (item != null) {
            item.dispose();
        }
    }
    
    VirtualItem getParent() {
        return fParent;
    }
    
    Index getIndex() {
        return fIndex;
    }
    
    VirtualItem findItem(Object element) {
        for (Iterator itr = fItems.values().iterator(); itr.hasNext();) {
            VirtualItem next = (VirtualItem)itr.next();
            Object nextData = next.getData();
            if ( (element != null && element.equals(nextData)) || (element == null && nextData == null) ) {
                return next;
            }
        }
        return null;
    }
    
    boolean needsDataUpdate() {
        return fNeedsDataUpdate;
    }

    void clearNeedsDataUpdate() {
        fNeedsDataUpdate = false;
    }

    boolean needsCountUpdate() {
        return fNeedsCountUpdate;
    }

    void clearNeedsCountUpdate() {
        fNeedsCountUpdate = false;
    }

    boolean needsLabelUpdate() {
        return fNeedsLabelUpdate;
    }
    
    void clearNeedsLabelUpdate() {
        fNeedsLabelUpdate = false;
    }
    
    boolean isDisposed() {
        return fDisposed;
    }
    
    void dispose() {
        fData.clear();
        for (Iterator itr = fItems.values().iterator(); itr.hasNext();) {
            ((VirtualItem)itr.next()).dispose();
        }
        fItems.clear();
        
        fDisposed = true;
        findTree().fireItemDisposed(this);
    }

    Object getData (String key) {
        return fData.get(key);
    }
    
    void setData(String key, Object data) {
        fData.put(key, data);
    }

    void setData(Object data) {
        fData.put(ELEMENT_DATA_KEY, data);
    }
    
    Object getData () {
        return fData.get(ELEMENT_DATA_KEY);
    }
    
    void setExpanded(boolean expanded) {
        if (fExpanded == expanded) {
            return;
        }
        fExpanded = expanded;

        if (fExpanded && getItemCount() == -1) {
            setNeedsCountUpdate();
        }

        
        Assert.isTrue(!fExpanded || hasItems());        

        // If collapsed, make sure that all the children are collapsed as well.
        if (!fExpanded) {
            for (Iterator itr = fItems.values().iterator(); itr.hasNext();) {
                ((VirtualItem)itr.next()).setExpanded(expanded);
            }
        }
    }
    
    boolean getExpanded() {
        return fExpanded;
    }

    void setHasItems(boolean hasChildren) {
        fHasItems = hasChildren;
        if (!fHasItems) {
            if (getItemCount() != 0) {
                setItemCount(0);
            }
        } else if (getItemCount() == 0) {
            setItemCount(-1);
        }
    }
    
    boolean hasItems() {
        return fHasItems;
    }
    
    void setItemCount(int count) {
        fItemCount = count;
        for (Iterator itr = fItems.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry)itr.next();
            int index = ((Index)entry.getKey()).intValue();
            if (index >= count) {
                itr.remove();
                VirtualItem item = (VirtualItem)entry.getValue(); 
                item.dispose();
            }
        }
        if (fItemCount == 0) {
            if (hasItems()) {
                setHasItems(false);
            }
            if (getExpanded()) {
                setExpanded(false);
            }
        } else {
            if (!hasItems()) {
                setHasItems(true);
            }
        }
    }
    
    int getItemCount() {
        return fItemCount;
    }
    
    VirtualItem getItem(Index index) {
        ensureItems();
        
        VirtualItem item = (VirtualItem)fItems.get(index); 
        if (item == null) {
            item = new VirtualItem(this, index);
            fItems.put(index, item);
        }
        return item;
    }
    
    boolean childrenNeedDataUpdate() {
        if (getItemCount() == 0) {
            return false;
        }
        if (fItems == null || fItems.size() != fItemCount) {
            return true;
        }
        for (Iterator itr = fItems.values().iterator(); itr.hasNext();) {
            VirtualItem child = (VirtualItem)itr.next();
            if (child.needsDataUpdate()) {
                return true;
            }
        }
        return false;
    }
    
    VirtualItem[] getItems() {
        return (VirtualItem[]) fItems.values().toArray(new VirtualItem[fItems.size()]);
    }
    
    VirtualItem addItem(int position) {
        if (!fHasItems) {
            fHasItems = true;
        }
        if (fItemCount < 0) {
            fItemCount = 0;
        }
        
        // Increment all items with an index higher than the given position.
        fItemCount++;
        ensureItems();
        for (Iterator itr = fItems.keySet().iterator(); itr.hasNext();) {
            Index childIndex = (Index)itr.next();
            if (childIndex.intValue() >= position) {
                childIndex.increment();
            }
        }
        
        // Note: the same index object used to create the item has to 
        // be used as the key into the map.  
        Index childIndex = new Index(position);
        VirtualItem newChild = new VirtualItem(this, childIndex);
        fItems.put(childIndex, newChild);
        return newChild;
    }
    
    void remove(Index position) {
        fItemCount--;
        if (fItemCount < 0) {
            fHasItems = false;
        }
         
        ensureItems();

        VirtualItem removedItem = null;
        for (Iterator itr = fItems.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry)itr.next();
            Index childIndex = (Index)entry.getKey();
            if (childIndex.intValue() > position.intValue()) {
                childIndex.decrement();
            } else if (childIndex.intValue() == position.intValue()) {
                removedItem = (VirtualItem)entry.getValue();
                removedItem.dispose();
                itr.remove();
            }
        }
    }
    
    private void ensureItems() {
        if (fItems == null) {
            fItems = new HashMap( Math.max(1, Math.min(fItemCount, 16)) );
        }
    }
    
    private VirtualTree findTree() {
        VirtualItem item = this;
        while (!(item instanceof VirtualTree)) {
            item = item.fParent;
        }
        return (VirtualTree)item;
    }
    
    public String toString() {
        String[] label = (String[])fData.get(LABEL_KEY);
        if (label != null && label.length != 0) {
            return label[0];
        } 
        Object data = fData.get(ELEMENT_DATA_KEY);
        if (data != null) {
            return data.toString();
        }
        return super.toString();
    }
}
