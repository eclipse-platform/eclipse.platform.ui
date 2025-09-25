/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207858)
 *     Matthew Hall - bugs 226765, 230296, 226292, 312926
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Objects;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * NON-API - An interface for sending updates to an {@link AbstractTreeViewer}.
 *
 * @since 1.2
 */
public class TreeViewerUpdater {
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
		if (viewer instanceof TreeViewer) {
			treeViewer = (TreeViewer) viewer;
		} else {
			treeViewer = null;
		}
	}

	/**
	 * Insert the element into the viewer as a child of the specified parent
	 * element, at the specified position.
	 *
	 * @param parent
	 *            the parent of the element being inserted
	 * @param element
	 *            the element to insert
	 * @param position
	 *            the position where the element is inserted
	 */
	public void insert(Object parent, Object element, int position) {
		viewer.insert(parent, element, position);
	}

	/**
	 * Replaces the specified element whenever it appears as a child of the
	 * specified parent element, at the given position with the new element.
	 *
	 * @param parent
	 *            the parent of the element being replaced
	 * @param oldElement
	 *            the element being replaced
	 * @param newElement
	 *            the element that replaces <code>oldElement</code>
	 * @param position
	 *            the position of the element being replaced.
	 */
	public void replace(Object parent, Object oldElement, Object newElement,
			int position) {
		if (treeViewer != null && isElementOrderPreserved()) {
			treeViewer.replace(parent, position, newElement);
			treeViewer.refresh(newElement);
		} else {
			ITreeSelection selection = viewer.getStructuredSelection();

			remove(parent, oldElement, position);
			insert(parent, newElement, position);

			// don't call selectionContains(...) here, because we have to
			// iterate all selected paths anyway.
			replaceElementInSelection(getPathParent(parent), oldElement, newElement, selection);
		}
	}

	/**
	 * In the TreePath, the viewers input (root) appears as null.
	 */
	private Object getPathParent(Object parent) {
		return parent == viewer.getInput() ? null : parent;
	}

	/**
	 * TODO: if the oldElement was expanded and no child element selected, the
	 * newElement will appear collapsed, but selected
	 */
	private void replaceElementInSelection(final Object parent, final Object oldElement, final Object newElement,
			final ITreeSelection selection) {
		IElementComparer comparer = viewer.getComparer();
		TreePath[] paths = selection.getPaths();
		for (int i = 0; i < paths.length; i++) {
			TreePath path = paths[i];
			Object[] segments = new Object[path.getSegmentCount()];
			boolean replacePath = false;
			for (int j = 0; j < path.getSegmentCount(); j++) {
				segments[j] = path.getSegment(j);
				Object pathParent = j > 0 ? path.getSegment(j - 1) : null;
				Object pathElement = path.getSegment(j);
				if (!replacePath && eq(comparer, parent, pathParent) && eq(comparer, oldElement, pathElement)) {
					segments[j] = newElement;
					replacePath = true;
				}
			}
			if (replacePath) {
				paths[i] = new TreePath(segments);
			}
		}
		viewer.setSelection(new TreeSelection(paths, viewer.getComparer()));
	}

	boolean isElementOrderPreserved() {
		return viewer.getComparator() == null
				&& viewer.getFilters().length == 0;
	}

	/**
	 * Moves the specified element from the specified old position to the
	 * specified new position, whenever it appears as a child of the specified
	 * parent element. No action is taken if the viewer has a sorter or
	 * filter(s).
	 *
	 * @param parent
	 *            the parent of the element being moved
	 * @param element
	 *            the element being moved
	 * @param oldPosition
	 *            the position of the element before it is moved
	 * @param newPosition
	 *            the position of the element after it is moved
	 */
	public void move(Object parent, Object element, int oldPosition,
			int newPosition) {
		if (isElementOrderPreserved()) {
			ITreeSelection selection = (ITreeSelection) viewer.getSelection();

			remove(parent, element, oldPosition);
			insert(parent, element, newPosition);

			// If the moved element is selected (or is an ancestor of a selected
			// element), restore the selection.
			if (selectionContains(selection, getPathParent(parent), element)) {
				viewer.setSelection(selection);
			}
		}
	}

	private boolean selectionContains(ITreeSelection selection, Object parent,
			Object element) {
		if (!selection.isEmpty()) {
			IElementComparer comparer = viewer.getComparer();
			TreePath[] paths = selection.getPaths();
			for (TreePath path : paths) {
				for (int j = 0; j < path.getSegmentCount() - 1; j++) {
					Object pathParent = path.getSegment(j);
					Object pathElement = path.getSegment(j + 1);
					if (eq(comparer, parent, pathParent)
							&& eq(comparer, element, pathElement)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean eq(IElementComparer comparer, Object o1, Object o2) {
		return comparer == null ? Objects.equals(o1, o2) : comparer.equals(o1, o2);
	}

	/**
	 * Removes the element from the from whenever it appears as a child of the
	 * specified parent element, at the specified position.
	 *
	 * @param parent
	 *            the parent of the element being removed
	 * @param element
	 *            the element to remove
	 * @param position
	 *            the position where the element is located
	 */
	public void remove(Object parent, Object element, int position) {
		if (treeViewer != null && viewer.getComparator() == null
				&& viewer.getFilters().length == 0) {
			// Only TreeViewer has a remove-by-index method.
			treeViewer.remove(parent, position);
		} else {
			viewer.remove(parent, new Object[] { element });
		}
	}

	/**
	 * Add the elements into the viewer as children of the specified parent
	 * element.
	 *
	 * @param parent
	 *            the parent of the element being inserted
	 * @param elements
	 *            the elements to insert
	 */
	public void add(Object parent, Object[] elements) {
		viewer.add(parent, elements);
	}

	/**
	 * Remove the elements from the viewer wherever they appear as children of
	 * the specified parent element.
	 *
	 * @param parent
	 *            the parent of the elements being removed
	 * @param elements
	 *            the elements to remove
	 */
	public void remove(Object parent, Object[] elements) {
		viewer.remove(parent, elements);
	}
}
