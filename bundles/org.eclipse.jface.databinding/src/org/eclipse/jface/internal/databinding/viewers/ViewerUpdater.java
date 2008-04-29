/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 226765)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * NON-API - An interface for updating a viewer's elements.
 * 
 * @since 1.2
 */
public abstract class ViewerUpdater {
	private final StructuredViewer viewer;

	/**
	 * Constructs a ViewerUpdater for updating the specified viewer.
	 * 
	 * @param viewer
	 *            the viewer which will be updated through this instance.
	 */
	protected ViewerUpdater(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Insert the element into the viewer at the specified position.
	 * 
	 * @param element
	 *            the element to add
	 * @param position
	 *            the position of the element
	 */
	public abstract void insert(Object element, int position);

	/**
	 * Remove the element from the viewer
	 * 
	 * @param element
	 *            the element to remove
	 * @param position
	 *            the position of the element
	 */
	public abstract void remove(Object element, int position);

	/**
	 * Replace the specified element at the given position with the new element.
	 * 
	 * @param oldElement
	 *            the element being replaced
	 * @param newElement
	 *            the element that replaces <code>oldElement</code>
	 * @param position
	 *            the position of the element being replaced.
	 */
	public void replace(Object oldElement, Object newElement, int position) {
		remove(oldElement, position);
		insert(newElement, position);
	}

	/**
	 * Moves the specified element from the specified old position to the
	 * specified new position. No action is taken if the viewer has a sorter or
	 * filter(s).
	 * 
	 * @param element
	 *            the element being moved
	 * @param oldPosition
	 *            the position of the element before it is moved
	 * @param newPosition
	 *            the position of the element after it is moved
	 */
	public void move(Object element, int oldPosition, int newPosition) {
		if (viewer.getComparator() == null && viewer.getFilters().length == 0) {
			IStructuredSelection selection = (IStructuredSelection) viewer
					.getSelection();

			remove(element, oldPosition);
			insert(element, newPosition);

			// Preserve selection
			if (selection.isEmpty()) {
				IElementComparer comparer = viewer.getComparer();
				Object[] selectedElements = selection.toArray();
				for (int i = 0; i < selectedElements.length; i++) {
					if (comparer == null ? Util.equals(element,
							selectedElements[i]) : comparer.equals(element,
							selectedElements[i])) {
						viewer.setSelection(selection);
						break;
					}
				}
			}
		}
	}

	/**
	 * Adds the elements to the viewer.
	 * 
	 * @param elements
	 *            the elements to add
	 */
	public abstract void add(Object[] elements);

	/**
	 * Removes the elements from the viewer
	 * 
	 * @param elements
	 *            the elements to remove
	 */
	public abstract void remove(Object[] elements);
}
