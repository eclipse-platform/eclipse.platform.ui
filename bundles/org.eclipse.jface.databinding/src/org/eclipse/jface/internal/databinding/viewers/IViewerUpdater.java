/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

/**
 * NON-API - Interface for updating a viewer's elements.
 * 
 * @since 1.2
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
	void insert(Object element, int position);

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
	void replace(Object oldElement, Object newElement, int position);

	/**
	 * Remove the element from the viewer
	 * 
	 * @param element
	 *            the element to remove
	 * @param position
	 *            the position of the element
	 */
	void remove(Object element, int position);

	/**
	 * Add the elements to the viewer.
	 * 
	 * @param elements
	 *            the elements to add
	 */
	void add(Object[] elements);

	/**
	 * Remove the elements from the viewer
	 * 
	 * @param elements
	 *            the elements to remove
	 */
	void remove(Object[] elements);
}
