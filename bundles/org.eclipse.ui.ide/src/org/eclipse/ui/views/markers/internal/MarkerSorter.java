package org.eclipse.ui.views.markers.internal;

import java.util.Comparator;

import org.eclipse.jface.viewers.TreeViewer;

abstract class MarkerSorter implements Comparator {

	/**
	 * Sort the array of markers in lastMarkers in place.
	 * 
	 * @param viewer
	 * @param markers
	 */
	public abstract void sort(TreeViewer viewer, MarkerList markers);
}
