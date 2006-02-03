/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * 
 * Provides context sensitive sorting of elements based on their parent in the
 * tree.
 * 
 * <p>
 * Clients may extend this class.
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class TreeViewerSorter extends ViewerSorter {

	/**
	 * Sorts the given elements in-place taking into account the parent of the
	 * elements, modifying the given array.
	 * <p>
	 * The default implementation of this method uses the java.util.Arrays#sort
	 * algorithm on the given array, calling <code>compare</code> to compare
	 * elements, and the parent does not affect sorting algorithm.
	 * </p>
	 * <p>
	 * Subclasses may reimplement this method to provide a more optimized
	 * implementation.
	 * </p>
	 * <p>
	 * 
	 * 
	 * @param viewer
	 *            the viewer
	 * @param parent
	 *            The parent element in the tree of both children.
	 * @param elements
	 *            the elements to sort
	 */
	public void sort(final Viewer viewer, final Object parent, Object[] elements) {
		super.sort(viewer, elements);
	}

	/**
	 * Returns a negative, zero, or positive number depending on whether the
	 * first element is less than, equal to, or greater than the second element.
	 * <p>
	 * The default implementation of this method is based on comparing the
	 * elements' categories as computed by the <code>category</code> framework
	 * method. Elements within the same category are further subjected to a case
	 * insensitive compare of their label strings, either as computed by the
	 * content viewer's label provider, or their <code>toString</code> values
	 * in other cases. Subclasses may override.
	 * </p>
	 * 
	 * @param viewer
	 *            the viewer
	 * @param parent
	 *            The parent element in the tree of both children.
	 * @param e1
	 *            the first element
	 * @param e2
	 *            the second element
	 * @return a negative number if the first element is less than the second
	 *         element; the value <code>0</code> if the first element is equal
	 *         to the second element; and a positive number if the first element
	 *         is greater than the second element
	 */
	public int compare(Viewer viewer, Object parent, Object e1, Object e2) {
		return super.compare(viewer, e1, e2);
	}

}
