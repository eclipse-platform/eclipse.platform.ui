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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * A selection in a async tree viewer. Selections in an async tree viewer
 * are identified by associated tree paths. As there can be duplicate elements
 * in a tree viewer, paths are required to uniquely identify which leaf
 * elements are selected.
 * <p>
 * Clients may instantiate this class. Not intended to be subclassed.
 * </p>
 * @since 3.2
 */
public class TreeSelection implements IStructuredSelection {
    
    private TreePath[] fPaths = null;

    /**
     * Constructs a selection based on the elements identified by the
     * given tree paths.
     * 
     * @param paths tree paths where <code>null</code> indicates an
     *  empty selection
     */
    public TreeSelection(TreePath[] paths) {
        fPaths = paths;
    }

    /**
     * Constructs a selection based on the elements identified by the 
     * given tree path.
     *
     * @param treePath tree path where <code>null</code> indicates an 
     * empty selection.
     */
    public TreeSelection(TreePath treePath) {
    		this(treePath != null ? new TreePath[] {treePath} : null);
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredSelection#getFirstElement()
     */
    public Object getFirstElement() {
        if (fPaths != null && fPaths.length > 0) {
            return fPaths[0].getLastSegment();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredSelection#iterator()
     */
    public Iterator iterator() {
        return toList().iterator();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredSelection#size()
     */
    public int size() {
        if (fPaths != null) {
            return fPaths.length;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredSelection#toArray()
     */
    public Object[] toArray() {
        int size = size();
        Object[] selection = new Object[size];
        for (int i = 0; i < size; i++) {
            selection[i] = fPaths[i].getLastSegment();
        }
        return selection;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredSelection#toList()
     */
    public List toList() {
        int size = size();
        List selection = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            selection.add(fPaths[i].getLastSegment());
        }
        return selection;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelection#isEmpty()
     */
    public boolean isEmpty() {
        return size() == 0;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof TreeSelection) {
            TreeSelection selection = (TreeSelection) obj;
            int size = size();
            if (selection.size() == size) {
                if (size > 0) {
                    for (int i = 0; i < fPaths.length; i++) {
                        if (!fPaths[i].equals(selection.fPaths[i])) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } else if (obj instanceof IStructuredSelection) {
        	if (isEmpty() && ((IStructuredSelection)obj).isEmpty()) {
        		return true;
        	}
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int code = getClass().hashCode();
        if (fPaths != null) {
            for (int i = 0; i < fPaths.length; i++) {
                code+=fPaths[i].hashCode();
            }
        }
        return code;
    }
    
    /**
     * Returns the paths in this selection
     * 
     * @return the paths in this selection
     */
    public TreePath[] getPaths() {
        return (TreePath[]) fPaths.clone();
    }
}
