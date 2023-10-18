/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The ContentProvider to the TreeViewer used in Markers View.
 *
 * @since 3.6
 *
 */
class MarkerViewerContentProvider implements ITreeContentProvider {

	// private MarkersTreeViewer viewer;
	private Object input;
	private final ExtendedMarkersView markersView;

	/**
	 * @param extendedMarkersView
	 */
	public MarkerViewerContentProvider(ExtendedMarkersView extendedMarkersView) {
		this.markersView = extendedMarkersView;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// this.viewer = (MarkersTreeViewer) viewer;
		this.input = newInput;
	}

	@Override
	public void dispose() {

	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof MarkerSupportItem markerItem) {
			MarkerSupportItem[] children = markerItem.getChildren();
			return getLimitedChildren(children);
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object inputElement) {
		//use clone
		return getLimitedChildren(((Markers) input).getElements());
	}

	/**
	 * Get the children limited by the marker limits.
	 *
	 * @param children
	 * @return Object[]
	 */
	private Object[] getLimitedChildren(Object[] children) {

		boolean limitsEnabled = markersView.getGenerator().isMarkerLimitsEnabled();
		int limits = markersView.getGenerator().getMarkerLimits();

		if (!limitsEnabled || limits <= 0 || limits > children.length)
			return children;

		Object[] newChildren = new Object[limits];
		System.arraycopy(children, 0, newChildren, 0, limits);
		return newChildren;
	}

	@Override
	public Object getParent(Object element) {
		Object parent = null;
		if (element instanceof MarkerSupportItem markerItem) {
			parent = markerItem.getParent();
		}
		if (parent == null)
			return input;
		return parent;
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof MarkerSupportItem markerItem ? markerItem.getChildren().length > 0 : false;
	}
}
