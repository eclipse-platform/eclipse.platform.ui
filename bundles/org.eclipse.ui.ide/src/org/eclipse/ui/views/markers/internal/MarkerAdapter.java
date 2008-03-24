/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ide.Policy;

/**
 * The MarkerAdapter is the adapter for the deferred update of markers.
 * 
 * @since 3.1
 * 
 */
public class MarkerAdapter {

	class MarkerCategory extends MarkerNode {

		MarkerAdapter markerAdapter;

		int start;

		int end;

		private ConcreteMarker[] children;

		private String name;

		/**
		 * Create a new instance of the receiver that has the markers between
		 * startIndex and endIndex showing.
		 * 
		 * @param adapter
		 * @param startIndex
		 * @param endIndex
		 */
		MarkerCategory(MarkerAdapter adapter, int startIndex, int endIndex,
				String categoryName) {
			markerAdapter = adapter;
			start = startIndex;
			end = endIndex;
			name = categoryName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getChildren()
		 */
		public MarkerNode[] getChildren() {

			if (children == null) {

				// Return nothing while a build is going on as this could be
				// stale
				if (building) {
					return Util.EMPTY_MARKER_ARRAY;
				}

				ConcreteMarker[] allMarkers = markerAdapter.lastMarkers
						.toArray();

				int totalSize = getDisplayedSize();
				children = new ConcreteMarker[totalSize];

				System.arraycopy(allMarkers, start, children, 0, totalSize);
				// Sort them locally now
				view.getTableSorter().sort(view.getViewer(), children);

				for (int i = 0; i < children.length; i++) {
					children[i].setCategory(this);
				}
			}
			return children;

		}

		/**
		 * Return the number of errors being displayed.
		 * 
		 * @return int
		 */
		int getDisplayedSize() {
			if (view.getMarkerLimit() > 0) {
				return Math.min(getTotalSize(), view.getMarkerLimit());
			}
			return getTotalSize();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getParent()
		 */
		public MarkerNode getParent() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getDescription()
		 */
		public String getDescription() {

			int size = end - start + 1;

			if (size <= view.getMarkerLimit()) {

				if (size == 1)
					return NLS.bind(MarkerMessages.Category_One_Item_Label,
							new Object[] { name });

				return NLS.bind(MarkerMessages.Category_Label, new Object[] {
						name, String.valueOf(getDisplayedSize()) });
			}
			return NLS.bind(MarkerMessages.Category_Limit_Label, new Object[] {
					name, String.valueOf(getDisplayedSize()),
					String.valueOf(getTotalSize()) });
		}

		/**
		 * Get the total size of the receiver.
		 * 
		 * @return int
		 */
		private int getTotalSize() {
			return end - start + 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#isConcrete()
		 */
		public boolean isConcrete() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getConcreteRepresentative()
		 */
		public ConcreteMarker getConcreteRepresentative() {
			return markerAdapter.lastMarkers.getMarker(start);
		}

		/**
		 * Return the name of the receiver.
		 * 
		 * @return String
		 */
		public String getName() {
			return name;
		}
	}

	MarkerView view;

	private MarkerList lastMarkers;

	private MarkerCategory[] categories;

	private boolean building = true;// Start with nothing until we have

	// something

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param markerView
	 */
	MarkerAdapter(MarkerView markerView) {
		view = markerView;
	}

	/**
	 * Return the category sorter for the receiver. This should only be called
	 * in hierarchal mode or there will be a ClassCastException.
	 * 
	 * @return CategorySorter
	 */
	public CategoryComparator getCategorySorter() {
		return (CategoryComparator) view.getViewer().getComparator();
	}

	/**
	 * Build all of the markers in the receiver.
	 * 
	 * @param collector
	 * @param monitor
	 */
	public void buildAllMarkers(IProgressMonitor monitor) {
		building = true;
		MarkerList newMarkers;
		try {
			int markerLimit = view.getMarkerLimit();
			monitor.beginTask(MarkerMessages.MarkerView_19,
					markerLimit == -1 ? 60 : 100);
			try {
				monitor.subTask(MarkerMessages.MarkerView_waiting_on_changes);

				if (monitor.isCanceled())
					return;

				monitor
						.subTask(MarkerMessages.MarkerView_searching_for_markers);
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
						10);
				MarkerFilter[] filters = view.getEnabledFilters();
				if (filters.length > 0)
					newMarkers = MarkerList.compute(filters, subMonitor, true);
				else
					// Grab any filter as a disabled filter gives all of them
					newMarkers = MarkerList.compute(new MarkerFilter[] { view
							.getAllFilters()[0] }, subMonitor, true);

				if (monitor.isCanceled())
					return;

				view.refreshMarkerCounts(monitor);

			} catch (CoreException e) {
				Policy.handle(e);
				newMarkers = new MarkerList();
				return;
			}

			if (monitor.isCanceled())
				return;

			ViewerComparator sorter = view.getViewer().getComparator();

			if (markerLimit == -1 || isShowingHierarchy()) {
				sorter.sort(view.getViewer(), newMarkers.toArray());
			} else {

				monitor.subTask(MarkerMessages.MarkerView_18);
				SubProgressMonitor mon = new SubProgressMonitor(monitor, 40);

				newMarkers = SortUtil.getFirst(newMarkers, (Comparator) sorter,
						markerLimit, mon);
				if (monitor.isCanceled()) 
					return;
				
				sorter.sort(view.getViewer(), newMarkers.toArray());
			}

			if (newMarkers.getSize() == 0) {
				categories = new MarkerCategory[0];
				lastMarkers = newMarkers;
				monitor.done();
				return;
			}

			monitor.subTask(MarkerMessages.MarkerView_queueing_updates);

			if (monitor.isCanceled())
				return;

			if (isShowingHierarchy()) {
				MarkerCategory[] newCategories = buildHierarchy(newMarkers, 0,
						newMarkers.getSize() - 1, 0);
				if (monitor.isCanceled())
					return;
				categories = newCategories;
			}

			lastMarkers = newMarkers;
			monitor.done();
		} finally {
			building = false;
		}

	}

	/**
	 * Return whether or not a hierarchy is showing.
	 * 
	 * @return boolean
	 */
	boolean isShowingHierarchy() {

		ViewerComparator sorter = view.getViewer().getComparator();
		if (sorter instanceof CategoryComparator) {
			return ((CategoryComparator) sorter).getCategoryField() != null;
		}
		return false;
	}

	/**
	 * Break the marker up into categories
	 * 
	 * @param markers
	 * @param start
	 *            the start index in the markers
	 * @param end
	 *            the last index to check
	 * @param sortIndex -
	 *            the parent of the field
	 * @param parent
	 * @return MarkerCategory[] or <code>null</code> if we are at the bottom
	 *         of the tree
	 */
	MarkerCategory[] buildHierarchy(MarkerList markers, int start, int end,
			int sortIndex) {
		CategoryComparator sorter = getCategorySorter();

		if (sortIndex > 0) {
			return null;// Are we out of categories?
		}

		Collection categories = new ArrayList();

		Object previous = null;
		int categoryStart = start;

		Object[] elements = markers.getArray();

		for (int i = start; i <= end; i++) {

			if (previous != null) {
				// Are we at a category boundary?
				if (sorter.compare(previous, elements[i], sortIndex, false) != 0) {
					categories.add(new MarkerCategory(this, categoryStart,
							i - 1, getNameForIndex(markers, categoryStart)));
					categoryStart = i;
				}
			}
			previous = elements[i];

		}

		if (end >= categoryStart) {
			categories.add(new MarkerCategory(this, categoryStart, end,
					getNameForIndex(markers, categoryStart)));
		}

		// Flatten single categories
		// if (categories.size() == 1) {
		// return buildHierarchy(markers, start, end, sortIndex + 1, parent);
		// }
		MarkerCategory[] nodes = new MarkerCategory[categories.size()];
		categories.toArray(nodes);
		return nodes;

	}

	/**
	 * Get the name for the category from the marker at categoryStart in
	 * markers.
	 * 
	 * @param markers
	 * @param categoryStart
	 * @return String
	 */
	private String getNameForIndex(MarkerList markers, int categoryStart) {
		return getCategorySorter().getCategoryField().getValue(
				markers.toArray()[categoryStart]);
	}

	/**
	 * Return the current list of markers.
	 * 
	 * @return MarkerList
	 */
	public MarkerList getCurrentMarkers() {
		if (lastMarkers == null) {// First time?
			view.scheduleMarkerUpdate(Util.SHORT_DELAY);
			building = true;
		}
		if (building) {
			return new MarkerList();
		}
		return lastMarkers;
	}

	/**
	 * Return the elements in the adapter.
	 * 
	 * @param root
	 * @return Object[]
	 */
	public Object[] getElements() {

		if (lastMarkers == null) {// First time?
			view.scheduleMarkerUpdate(Util.SHORT_DELAY);
			building = true;
		}
		if (building) {
			return Util.EMPTY_MARKER_ARRAY;
		}
		if (isShowingHierarchy() && categories != null) {
			return categories;
		}
		return lastMarkers.toArray();
	}

	/**
	 * Return whether or not the receiver has markers without scheduling
	 * anything if it doesn't.
	 * 
	 * @return boolean <code>true</code> if the markers have not been
	 *         calculated.
	 */
	public boolean hasNoMarkers() {
		return lastMarkers == null;
	}

	/**
	 * Return the categories for the receiver.
	 * 
	 * @return MarkerCategory[] or <code>null</code> if there are no
	 *         categories.
	 */
	public MarkerCategory[] getCategories() {
		if (building) {
			return null;
		}
		return categories;
	}

	/**
	 * Return whether or not the receiver is building.
	 * @return boolean
	 */
	boolean isBuilding() {
		return building;
	}

}
