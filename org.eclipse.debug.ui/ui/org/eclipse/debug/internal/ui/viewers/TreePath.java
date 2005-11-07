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
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A tree path denotes a model element in a tree viewer. Tree path
 * objects do have value semantics. A model element is represented by
 * a path of elements in the tree from the root element to the leaf
 * element.
 * <p>
 * Clients may instantiate this class. Not intended to be subclassed.
 * </p>
 * @since 3.2
 */
public final class TreePath {
    private Object[] fSegments;
    private int fHash;
    private TreeItem fItem;
    
    /**
     * Constructs a path identifying a leaf node in a tree.
     *  
     * @param segments path of elements to a leaf node in a tree, starting with the root element
     */
    public TreePath(Object[] segments) {
        Assert.isNotNull(segments);
        for (int i= 0; i < segments.length; i++) {
            Assert.isNotNull(segments[i]);
        }
        fSegments= segments;
    }
    
    /**
     * Returns the element at the specified index in this path.
     * 
     * @param index index of element to return
     * @return element at the specified index
     */
    public Object getSegment(int index) {
        return fSegments[index];
    }
    
    /**
     * Returns the number of elements in this path.
     * 
     * @return the number of elements in this path
     */
    public int getSegmentCount() {
        return fSegments.length;
    }
    
    /**
     * Returns the first element in this path.
     * 
     * @return the first element in this path
     */
    public Object getFirstSegment() {
        if (fSegments.length == 0)
            return null;
        return fSegments[0];
    }
    
    /**
     * Returns the last element in this path.
     * 
     * @return the last element in this path
     */
    public Object getLastSegment() {
        if (fSegments.length == 0)
            return null;
        return fSegments[fSegments.length - 1];
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        if (!(other instanceof TreePath))
            return false;
        return equals((TreePath)other, null);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (fHash != 0)
            return fHash;
        for (int i= 0; i < fSegments.length; i++) {
            fHash= fHash + fSegments[i].hashCode();
        }
        return fHash;
    }
    
    /**
     * Returns whether this path is equivalent to the given path using the
     * specified comparator to compare individual elements.
     * 
     * @param otherPath tree path to compare to
     * @param comparer compartor to use or <code>null</code> if default comparator 
     *  should be used
     * @return whether the paths are equal
     */
    public boolean equals(TreePath otherPath, IElementComparer comparer) {
        if (comparer == null)
            comparer= DefaultElementComparer.INSTANCE;
        if (otherPath == null)
            return false;
        if (fSegments.length != otherPath.fSegments.length)
            return false;
        for (int i= 0; i < fSegments.length; i++) {
            if (!comparer.equals(fSegments[i], otherPath.fSegments[i]))
                return false;
        }
        return true;
    }
    
    /**
     * Used internally to set the tree item associated with the leaf element.
     * 
     * @param item associated tree item for the leaf element
     */
    void setTreeItem(TreeItem item) {
        fItem = item;
    }
    
    /**
     * Used internally to return the tree item associated with the leaf element.
     * 
     * @return tree item associated with leaf element or <code>null</code>
     */
    TreeItem getTreeItem() {
        return fItem;
    }

    /**
     * Returns whether this path starts with the same elements in the given path.
     * This is indicates the given path is a prefix of this path, or is the same
     * as this path.
     * 
     * @param treePath path to compare to
     * @return whether the given path is a prefix of this path, or the same as this
     *  path
     */
    public boolean startsWith(TreePath treePath) {
    		if (treePath == null) {
    			return false;
    		}
    		
    		if (treePath.equals(this)) {
    			return true;
    		}
    		
        int segmentCount = treePath.getSegmentCount();
        if (segmentCount >= fSegments.length) {
            return false;
        }
        for (int i = 0; i < segmentCount; i++) {
            Object segment = treePath.getSegment(i);
            if (!segment.equals(fSegments[i])) {
                return false;
            }
        }
        return true;
    }
}

