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
public class TreeSelection extends StructuredSelection implements ITreeSelection {

	/* Implementation note.  This class extends StructuredSelection because many pre-existing
	 * JFace viewer clients assumed that the only implementation of IStructuredSelection 
	 * was StructuredSelection.  By extending StructuredSelection rather than implementing
	 * ITreeSelection directly, we avoid this problem.
	 * For more details, see Bug 121939 [Viewers] TreeSelection should subclass StructuredSelection. 
	 */
	
	private TreePath[] paths = null;

	/**
	 * The canonical empty selection. This selection should be used instead of
	 * <code>null</code>.
	 */
	public static final TreeSelection EMPTY = new TreeSelection();

	/**
	 * Internal - Extracts the last segments of the given paths into a list.
	 * 
	 * @param paths the paths
	 * @return the list of last segments
	 */
	private static List lastSegments(TreePath[] paths) {
		int size = paths.length;
		List selection = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			selection.add(paths[i].getLastSegment());
		}
		return selection;
	}
	
	/**
	 * Constructs a selection based on the elements identified by the given tree
	 * paths.
	 * 
	 * @param paths
	 *            tree paths
	 */
	public TreeSelection(TreePath[] paths) {
		super(lastSegments(paths));
        this.paths = new TreePath[paths.length];
        System.arraycopy(paths, 0, this.paths, 0, paths.length);
	}

	/**
	 * Constructs a selection based on the elements identified by the given tree
	 * path.
	 * 
	 * @param treePath
	 *            tree path, or <code>null</code> for an empty selection
	 */
	public TreeSelection(TreePath treePath) {
		this(treePath != null ? new TreePath[] { treePath } : new TreePath[0]);
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
		super();
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
				code = code * 17 + paths[i].hashCode();
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
