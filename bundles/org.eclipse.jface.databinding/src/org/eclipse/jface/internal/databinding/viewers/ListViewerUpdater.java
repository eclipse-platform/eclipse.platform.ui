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
 *     Matthew Hall - bug 226765
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.jface.viewers.AbstractListViewer;

/**
 * NON-API - A {@link ViewerUpdater} that updates {@link AbstractListViewer}
 * instances.
 *
 * @since 1.2
 */
class ListViewerUpdater<E> extends ViewerUpdater<E> {
	private AbstractListViewer viewer;

	ListViewerUpdater(AbstractListViewer viewer) {
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
	public void add(E[] elements) {
		viewer.add(elements);
	}

	@Override
	public void remove(E[] elements) {
		viewer.remove(elements);
	}
}
