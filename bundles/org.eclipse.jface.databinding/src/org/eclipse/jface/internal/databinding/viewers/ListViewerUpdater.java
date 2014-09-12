/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
class ListViewerUpdater extends ViewerUpdater {
	private AbstractListViewer viewer;

	ListViewerUpdater(AbstractListViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	public void insert(Object element, int position) {
		viewer.insert(element, position);
	}

	@Override
	public void remove(Object element, int position) {
		viewer.remove(element);
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
