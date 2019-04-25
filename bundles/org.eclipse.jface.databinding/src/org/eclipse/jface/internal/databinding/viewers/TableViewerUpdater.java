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
 *     Matthew Hall - initial API and implementation (bug 215531)
 *     Matthew Hall - bugs 226765, 230296
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.jface.viewers.AbstractTableViewer;

/**
 * NON-API - A {@link ViewerUpdater} that updates {@link AbstractTableViewer}
 * instances.
 *
 * @param <E> type of the elements in the updated viewer
 *
 * @since 1.2
 */
class TableViewerUpdater<E> extends ViewerUpdater<E> {
	private AbstractTableViewer viewer;

	TableViewerUpdater(AbstractTableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	public void insert(E element, int position) {
		viewer.insert(element, position);
	}

	@Override
	public void remove(E element, int position) {
		viewer.remove(element);
	}

	@Override
	public void replace(E oldElement, E newElement, int position) {
		if (isElementOrderPreserved())
			viewer.replace(newElement, position);
		else {
			super.replace(oldElement, newElement, position);
		}
	}

	@Override
	public void add(Object[] elements) {
		viewer.add(elements);
	}

	@Override
	public void remove(Object[] elements) {
		viewer.remove(elements);
	}
}
