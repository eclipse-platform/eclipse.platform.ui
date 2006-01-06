package org.eclipse.ui.views.markers.internal;

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * The MarkerAdapter is the adapter for the deferred update of markers.
 * 
 * @since 3.1
 * 
 */
public class MarkerAdapter {

	private class MarkerCategory extends MarkerNode {

		MarkerAdapter markerAdapter;

		int start;

		int end;

		int fieldIndex = 0;

		MarkerCategory parent;

		private MarkerNode[] children;

		private String name;

		/**
		 * Create a new instance of the receiver that has the markers between
		 * startIndex and endIndex showing.
		 * 
		 * @param adapter
		 * @param startIndex
		 * @param endIndex
		 * @param fieldNumber
		 * @param parentCategory
		 */
		MarkerCategory(MarkerAdapter adapter, int startIndex, int endIndex,
				int fieldNumber, MarkerCategory parentCategory) {
			markerAdapter = adapter;
			start = startIndex;
			end = endIndex;
			fieldIndex = fieldNumber;
			parent = parentCategory;

			IField field = adapter.getCategorySorter().getCategoryField();

			name = field
					.getValue(markerAdapter.lastMarkers.toArray()[startIndex]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getChildren()
		 */
		public MarkerNode[] getChildren() {

			if (children == null) {

				if (view.getTableSorter().getFields().length >= fieldIndex) {
					children = buildHierarchy(markerAdapter.lastMarkers, start,
							end, fieldIndex + 1, this);
					if (children != null)// De we find any?
						return children;
				}

				// We are at the leaf

				ConcreteMarker[] allMarkers = markerAdapter.lastMarkers
						.toArray();
				children = new MarkerNode[end - start + 1];

				System.arraycopy(allMarkers, start, children, 0, end - start
						+ 1);
				// Sort them locally now
				view.getTableSorter().sort(view.getViewer(), children);

				for (int i = 0; i < children.length; i++) {
					((ConcreteMarker) children[i]).setCategory(this);
				}
			}
			return children;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getParent()
		 */
		public MarkerNode getParent() {
			return parent;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getDescription()
		 */
		public String getDescription() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#isConcrete()
		 */
		public boolean isConcrete() {
			return false;
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
	public CategorySorter getCategorySorter() {
		return (CategorySorter) view.getViewer().getSorter();
	}

	/**
	 * Build all of the markers in the receiver.
	 * 
	 * @param collector
	 * @param monitor
	 */
	public void buildAllMarkers(IProgressMonitor monitor) {
		building = true;
		try {
			int markerLimit = view.getMarkerLimit();
			monitor.beginTask(MarkerMessages.MarkerView_19,
					markerLimit == -1 ? 60 : 100);
			try {
				monitor.subTask(MarkerMessages.MarkerView_waiting_on_changes);

				if (monitor.isCanceled()) {
					return;
				}

				monitor
						.subTask(MarkerMessages.MarkerView_searching_for_markers);
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
						10);
				lastMarkers = MarkerList.compute(view.getEnabledFilters(),
						subMonitor, true);

				if (monitor.isCanceled())
					return;

				view.refreshMarkerCounts(monitor);

			} catch (CoreException e) {
				IDEWorkbenchPlugin.getDefault().getLog().log(e.getStatus());
				lastMarkers = new MarkerList();
				return;
			}

			if (monitor.isCanceled()) {
				return;
			}

			ViewerSorter sorter = view.getViewer().getSorter();

			if (markerLimit == -1) {
				sorter.sort(view.getViewer(), lastMarkers.toArray());
			} else {

				monitor.subTask(MarkerMessages.MarkerView_18);
				SubProgressMonitor mon = new SubProgressMonitor(monitor, 40);

				lastMarkers = SortUtil.getFirst(lastMarkers,
						(Comparator) sorter, markerLimit, mon);
				if (monitor.isCanceled())
					return;
				sorter.sort(view.getViewer(), lastMarkers.toArray());
			}

			if (lastMarkers.getSize() == 0) {
				monitor.done();
				return;
			}

			monitor.subTask(MarkerMessages.MarkerView_queueing_updates);

			if (isShowingHierarchy()) {
				categories = buildHierarchy(lastMarkers, 0, lastMarkers
						.getSize() - 1, 0, null);
			}

			if (monitor.isCanceled())
				return;

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
	private boolean isShowingHierarchy() {

		ViewerSorter sorter = view.getViewer().getSorter();
		if (sorter instanceof CategorySorter)
			return ((CategorySorter) sorter).getCategoryField() != null;
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
			int sortIndex, MarkerCategory parent) {
		CategorySorter sorter = getCategorySorter();

		if (sortIndex > 0)
			return null;// Are we out of categories?

		Collection categories = new ArrayList();

		Object previous = null;
		int categoryStart = start;

		Object[] elements = markers.getArray();

		for (int i = start; i <= end; i++) {

			if (previous != null) {
				// Are we at a category boundary?
				if (sorter.compare(previous, elements[i], sortIndex, false) != 0) {
					categories.add(new MarkerCategory(this, categoryStart,
							i - 1, sortIndex, parent));
					categoryStart = i;
				}
			}
			previous = elements[i];

		}

		if (end >= categoryStart) {
			categories.add(new MarkerCategory(this, categoryStart, end,
					sortIndex, parent));
		}

		// Flatten single categories
		if (categories.size() == 1) {
			return buildHierarchy(markers, start, end, sortIndex + 1, parent);
		}
		MarkerCategory[] nodes = new MarkerCategory[categories.size()];
		categories.toArray(nodes);
		return nodes;

	}

	/**
	 * Return the current list of markers.
	 * 
	 * @return MarkerList
	 */
	public MarkerList getCurrentMarkers() {
		if (lastMarkers == null) {// First time?
			view.scheduleMarkerUpdate();
			building = true;
		}	
		if (building)
			return new MarkerList();
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
			view.scheduleMarkerUpdate();
			building = true;
		}	
		if (building)
			return Util.EMPTY_MARKER_ARRAY;
		if (isShowingHierarchy() && categories != null)
			return categories;
		return lastMarkers.toArray();
	}

}
