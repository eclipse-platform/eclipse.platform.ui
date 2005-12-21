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
package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A concrete implementation of the <code>ITreeSelection</code> interface,
 * suitable for instantiating.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * 
 * @since 3.2
 */
public class TreeSelection implements ITreeSelection {

	private TreePath[] paths = null;

	/**
	 * The canonical empty selection. This selection should be used instead of
	 * <code>null</code>.
	 */
	public static final TreeSelection EMPTY = new TreeSelection();

	/**
	 * Constructs a selection based on the elements identified by the given tree
	 * paths.
	 * 
	 * @param paths
	 *            tree paths
	 */
	public TreeSelection(TreePath[] paths) {
		this.paths = paths;
	}

	/**
	 * Constructs a selection based on the elements identified by the given tree
	 * path.
	 * 
	 * @param treePath
	 *            tree path
	 */
	public TreeSelection(TreePath treePath) {
		this(treePath != null ? new TreePath[] { treePath } : null);
	}

	/**
	 * Creates a new empty selection. See also the static field
	 * <code>EMPTY</code> which contains an empty selection singleton.
	 * <p>
	 * Note that TreeSelection.EMPTY is not equals() to StructuredViewer.EMPTY.
	 * </p>
	 * 
	 * @see #EMPTY
	 */
	public TreeSelection() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredSelection#getFirstElement()
	 */
	public Object getFirstElement() {
		if (paths != null && paths.length > 0) {
			return paths[0].getLastSegment();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredSelection#iterator()
	 */
	public Iterator iterator() {
		return toList().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredSelection#size()
	 */
	public int size() {
		if (paths != null) {
			return paths.length;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredSelection#toArray()
	 */
	public Object[] toArray() {
		int size = size();
		Object[] selection = new Object[size];
		for (int i = 0; i < size; i++) {
			selection[i] = paths[i].getLastSegment();
		}
		return selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredSelection#toList()
	 */
	public List toList() {
		int size = size();
		List selection = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			selection.add(paths[i].getLastSegment());
		}
		return selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof TreeSelection) {
			TreeSelection selection = (TreeSelection) obj;
			int size = size();
			if (selection.size() == size) {
				if (size > 0) {
					for (int i = 0; i < paths.length; i++) {
						if (!paths[i].equals(selection.paths[i])) {
							return false;
						}
					}
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int code = getClass().hashCode();
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				code += paths[i].hashCode();
			}
		}
		return code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeSelection#getPaths()
	 */
	public TreePath[] getPaths() {
		return paths==null ? new TreePath[0] : (TreePath[]) paths.clone();
	}
}
