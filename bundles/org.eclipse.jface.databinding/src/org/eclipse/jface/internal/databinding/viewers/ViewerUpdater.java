/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 226765)
 *     Matthew Hall - bug 230296, 238296
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Iterator;

import org.eclipse.jface.databinding.viewers.IViewerUpdater;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * NON-API - An interface for updating a viewer's elements.
 * 
 * @since 1.2
 */
public abstract class ViewerUpdater implements IViewerUpdater {
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

	public abstract void insert(Object element, int position);

	public abstract void remove(Object element, int position);

	public void replace(Object oldElement, Object newElement, int position) {
		remove(oldElement, position);
		insert(newElement, position);
	}

	public void move(Object element, int oldPosition, int newPosition) {
		if (isElementOrderPreserved()) {
			IStructuredSelection selection = (IStructuredSelection) viewer
					.getSelection();

			remove(element, oldPosition);
			insert(element, newPosition);

			// Preserve selection
			if (selectionContains(selection, element)) {
				viewer.setSelection(selection);
			}
		}
	}

	boolean isElementOrderPreserved() {
		return viewer.getComparator() == null
				&& viewer.getFilters().length == 0;
	}

	private boolean selectionContains(IStructuredSelection selection,
			Object element) {
		if (!selection.isEmpty()) {
			IElementComparer comparer = viewer.getComparer();
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object selectionElement = iter.next();
				if (comparer == null ? Util.equals(element, selectionElement)
						: comparer.equals(element, selectionElement)) {
					return true;
				}
			}
		}
		return false;
	}

	public abstract void add(Object[] elements);

	public abstract void remove(Object[] elements);
}
