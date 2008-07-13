/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @since 1.2
 */
class TableViewerUpdater extends ViewerUpdater {
	private AbstractTableViewer viewer;

	TableViewerUpdater(AbstractTableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	public void insert(Object element, int position) {
		viewer.insert(element, position);
	}

	public void remove(Object element, int position) {
		viewer.remove(element);
	}

	public void replace(Object oldElement, Object newElement, int position) {
		if (isElementOrderPreserved())
			viewer.replace(newElement, position);
		else {
			super.replace(oldElement, newElement, position);
		}
	}

	public void add(Object[] elements) {
		viewer.add(elements);
	}

	public void remove(Object[] elements) {
		viewer.remove(elements);
	}
}
