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

package org.eclipse.jface.internal.databinding.provisional.viewers;

/**
 * NON-API - Interface for sending updates to a tree viewer.
 * 
 * @since 1.2
 */
public interface ITreeViewerUpdater {
	/**
	 * Add the elements into the viewer as children of the specified parent
	 * element.
	 * 
	 * @param parent
	 *            the parent of the element being inserted
	 * @param elements
	 *            the elements to insert
	 */
	void add(Object parent, Object[] elements);

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
	void insert(Object parent, Object element, int position);

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
	void replace(Object parent, Object oldElement, Object newElement,
			int position);

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
	void remove(Object parent, Object element, int position);

	/**
	 * Remove the elements from the viewer wherever they appear as children of
	 * the specified parent element.
	 * 
	 * @param parent
	 *            the parent of the elements being removed
	 * @param elements
	 *            the elements to remove
	 */
	void remove(Object parent, Object[] elements);
}
