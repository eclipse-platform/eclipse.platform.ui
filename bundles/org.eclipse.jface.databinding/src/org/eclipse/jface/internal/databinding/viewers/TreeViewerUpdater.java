/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207858)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * NON-API - An {@link ITreeViewerUpdater} implementation for
 * {@link AbstractTreeViewer} viewers.
 * 
 * @since 1.2
 */
class TreeViewerUpdater implements ITreeViewerUpdater {
	private final AbstractTreeViewer viewer;
	private final TreeViewer treeViewer;

	/**
	 * Constructs an ITreeViewerUpdater for updating the given viewer.
	 * 
	 * @param viewer
	 *            the viewer that will be updated
	 */
	public TreeViewerUpdater(AbstractTreeViewer viewer) {
		this.viewer = viewer;
		if (viewer instanceof TreeViewer)
			treeViewer = (TreeViewer) viewer;
		else
			treeViewer = null;
	}

	public void add(Object parent, Object[] elements) {
		viewer.add(parent, elements);
	}

	public void insert(Object parent, Object element, int position) {
		viewer.insert(parent, element, position);
	}

	public void replace(Object parent, Object oldElement, Object newElement,
			int position) {
		if (treeViewer != null && viewer.getComparator() == null
				&& viewer.getFilters().length == 0) {
			treeViewer.replace(parent, position, newElement);
		} else {
			remove(parent, oldElement, position);
			insert(parent, newElement, position);
		}

	}

	public void remove(Object parent, Object element, int position) {
		if (treeViewer != null && viewer.getComparator() == null
				&& viewer.getFilters().length == 0) {
			// Only TreeViewer has a remove-by-index method.
			treeViewer.remove(parent, position);
		} else {
			viewer.remove(parent, new Object[] { element });
		}
	}

	public void remove(Object parent, Object[] elements) {
		viewer.remove(parent, elements);
	}
}
