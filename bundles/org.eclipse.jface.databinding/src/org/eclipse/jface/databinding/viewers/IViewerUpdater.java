/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 238296)
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.jface.viewers.StructuredViewer;

/**
 * A strategy interface for updating the elements in a {@link StructuredViewer}.
 * Many structured viewer classes have similar methods for adding and removing
 * elements, few of which are defined in common ancestor classes. This interface
 * serves as a universal adapter for updating the elements in a viewer
 * 
 * @since 1.3
 * @see ObservableListContentProvider#ObservableListContentProvider(IViewerUpdater)
 * @see ObservableSetContentProvider#ObservableSetContentProvider(IViewerUpdater)
 */
public interface IViewerUpdater {
	/**
	 * Insert the element into the viewer at the specified position.
	 * 
	 * @param element
	 *            the element to add
	 * @param position
	 *            the position of the element
	 */
	public void insert(Object element, int position);

	/**
	 * Remove the element from the viewer
	 * 
	 * @param element
	 *            the element to remove
	 * @param position
	 *            the position of the element
	 */
	public void remove(Object element, int position);

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
	public void replace(Object oldElement, Object newElement, int position);

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
	public void move(Object element, int oldPosition, int newPosition);

	/**
	 * Adds the elements to the viewer.
	 * 
	 * @param elements
	 *            the elements to add
	 */
	public void add(Object[] elements);

	/**
	 * Removes the elements from the viewer
	 * 
	 * @param elements
	 *            the elements to remove
	 */
	public void remove(Object[] elements);
}