/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jface.databinding.viewers.IViewerUpdater;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
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

	@Override
	public abstract void insert(Object element, int position);

	@Override
	public abstract void remove(Object element, int position);

	@Override
	public void replace(final Object oldElement, final Object newElement, final int position) {
		final List<Object> selectedElements = new ArrayList<>(viewer.getStructuredSelection().toList());

		remove(oldElement, position);
		insert(newElement, position);

		// Preserve selection
		selectionContains(selectedElements, oldElement).ifPresent(iter -> {
			iter.remove();
			selectedElements.add(newElement);
			viewer.setSelection(new StructuredSelection(selectedElements));
		});
	}

	@Override
	public void move(Object element, int oldPosition, int newPosition) {
		if (isElementOrderPreserved()) {
			IStructuredSelection selection = viewer.getStructuredSelection();

			remove(element, oldPosition);
			insert(element, newPosition);

			// Preserve selection
			selectionContains(selection.toList(), element).ifPresent(i -> viewer.setSelection(selection));
		}
	}

	boolean isElementOrderPreserved() {
		return viewer.getComparator() == null
				&& viewer.getFilters().length == 0;
	}

	private Optional<Iterator<?>> selectionContains(List<?> selection, Object element) {
		if (!selection.isEmpty()) {
			IElementComparer comparer = viewer.getComparer();
			for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
				Object selectionElement = iter.next();
				if (comparer == null ? Objects.equals(element, selectionElement)
						: comparer.equals(element, selectionElement)) {
					return Optional.of(iter);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public abstract void add(Object[] elements);

	@Override
	public abstract void remove(Object[] elements);
}
