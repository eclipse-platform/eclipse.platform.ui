/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
	 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// this.viewer = (MarkersTreeViewer) viewer;
		this.input = newInput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java
	 * .lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		MarkerSupportItem[] children = ((MarkerSupportItem) parentElement)
				.getChildren();

		return getLimitedChildren(children);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements
	 * (java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#getParent(
	 * java.lang.Object)
	 */
	public Object getParent(Object element) {
		Object parent = ((MarkerSupportItem) element).getParent();
		if (parent == null)
			return input;
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java
	 * .lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return ((MarkerSupportItem) element).getChildren().length > 0;
	}
}
