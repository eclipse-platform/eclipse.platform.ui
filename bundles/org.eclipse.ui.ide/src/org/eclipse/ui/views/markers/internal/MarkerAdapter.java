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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.internal.MarkerView.MarkerRefreshRecord;

/**
 * The MarkerAdapter is the adapter for the deferred update of markers.
 * 
 * @since 3.2
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

			IField field = adapter.getCategorySorter().getCategoryField(
					fieldIndex, getType());

			name = field
					.getValue(markerAdapter.lastMarkers.toArray()[startIndex]);
		}

		/**
		 * Return the type of the markers in this category.
		 * 
		 * @return String
		 */
		private String getType() {
			ConcreteMarker marker = markerAdapter.lastMarkers.toArray()[start];
			return marker.getType();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getChildren()
		 */
		public MarkerNode[] getChildren() {

			if (children == null) {

				if (isIntermediateCategory()) {
					children = buildHierarchy(markerAdapter.lastMarkers, start,
							end, fieldIndex + 1, this);
					if (children.length > 0)// Did we find any?
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

		/**
		 * Return whether or not there is a field below this one.
		 * 
		 * @return boolean
		 */
		private boolean isIntermediateCategory() {
			return getCategorySorter().hasField(fieldIndex + 1, getType());
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#isStale()
		 */
		public boolean isStale() {
			return false;
		}

		public void removeChild(MarkerNode marker) {

		}

		/**
		 * Return whether or not this category has the same fields as the
		 * receiver.
		 * 
		 * @param category
		 * @return boolean
		 */
		public boolean isSameAs(MarkerCategory category) {
			return category.fieldIndex == fieldIndex && category.start == start
					&& category.end == end;
		}

		/**
		 * Return the children if they are categories
		 * 
		 * @return MarkerCategory[]
		 */
		public MarkerCategory[] getCategoryChildren() {
			
			if (!isIntermediateCategory())
				return EMPTY_MARKER_CATEGORIES;
			MarkerNode[] children = getChildren();
			if(children.length == 0 || children[0].isConcrete())
				return EMPTY_MARKER_CATEGORIES;
			MarkerCategory[] categories = new MarkerCategory[children.length];
			System.arraycopy(children, 0, categories, 0, children.length);
			return categories;

		}

	}

	MarkerView view;

	private MarkerList lastMarkers = null;

	private MarkerCategory[] categories = new MarkerCategory[0];

	private Job markerJob;

	private static final MarkerCategory[] EMPTY_MARKER_CATEGORIES = new MarkerCategory[0];

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
	private void buildAllMarkers(IProgressMonitor monitor) {
		int markerLimit = view.getMarkerLimit();
		monitor.beginTask(MarkerMessages.MarkerView_19, markerLimit == -1 ? 60
				: 100);
		try {
			monitor.subTask(MarkerMessages.MarkerView_waiting_on_changes);

			if (monitor.isCanceled()) {
				return;
			}

			monitor.subTask(MarkerMessages.MarkerView_searching_for_markers);
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
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

		if (monitor.isCanceled())
			return;

		view.getViewer().getSorter().sort(view.getViewer(),
				lastMarkers.toArray());

		monitor.subTask(MarkerMessages.MarkerView_queueing_updates);

		if (view.isHierarchalMode())
			categories = buildHierarchy(lastMarkers, 0,
					lastMarkers.getSize() - 1, 0, null);
		if (monitor.isCanceled())
			return;

		monitor.done();

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
	 * @return MarkerCategory[] 
	 */
	MarkerCategory[] buildHierarchy(MarkerList markers, int start, int end,
			int sortIndex, MarkerCategory parent) {

		if (end < start)
			return EMPTY_MARKER_CATEGORIES;

		CategorySorter sorter = getCategorySorter();

		if (sortIndex >= sorter.getCategoryFieldCount(markers.getMarker(start)
				.getType())) {
			return EMPTY_MARKER_CATEGORIES;// Are we out of categories?
		}

		Collection newCategories = new ArrayList();

		Object previous = null;
		int categoryStart = start;

		Object[] elements = markers.getArray();

		for (int i = start; i <= end; i++) {

			if (previous != null) {
				// Are we at a category boundary?
				if (sorter.compare(previous, elements[i], sortIndex, false) != 0) {
					newCategories.add(new MarkerCategory(this, categoryStart,
							i - 1, sortIndex, parent));
					categoryStart = i;
				}
			}
			previous = elements[i];

		}

		if (end >= categoryStart) {
			newCategories.add(new MarkerCategory(this, categoryStart, end,
					sortIndex, parent));
		}

		// Flatten single categories
		if (newCategories.size() == 1) {
			return buildHierarchy(markers, start, end, sortIndex + 1, parent);
		}
		MarkerCategory[] nodes = new MarkerCategory[newCategories.size()];
		newCategories.toArray(nodes);
		return nodes;

	}

	/**
	 * Get the children of o.
	 * 
	 * @param o
	 * @return Object[]
	 */
	public Object[] getChildren(Object o) {

		if (lastMarkers == null) {
			scheduleMarkerCalculation();
			return new Object[0];
		}
		if (o instanceof MarkerNode)
			return ((MarkerNode) o).getChildren();
		if (view.isHierarchalMode() && categories != null && categories.length > 0)
			return categories;
		int markerLimit = view.getMarkerLimit();
		if (markerLimit == -1 || markerLimit >= lastMarkers.getSize())
			return lastMarkers.toArray();

		Object[] returnValue = new Object[markerLimit];
		System.arraycopy(lastMarkers.toArray(), 0, returnValue, 0, markerLimit);
		return returnValue;
	}

	/**
	 * Schedule the marker calculation.
	 */
	public void scheduleMarkerCalculation() {
		if (markerJob == null) {
			markerJob = new Job(MarkerMessages.Calculate_Markers_Job) {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				protected IStatus run(IProgressMonitor monitor) {
					buildAllMarkers(monitor);
					MarkerAdapter.this.view.scheduleRefresh();
					return Status.OK_STATUS;
				}

			};
			markerJob.setSystem(true);
		}
		markerJob.schedule();

	}

	/**
	 * Get the parent of the object
	 * 
	 * @param o
	 * @return Object
	 */
	public Object getParent(Object o) {
		return ((MarkerNode) o).getParent();
	}

	/**
	 * Return the current list of markers.
	 * 
	 * @return MarkerList
	 */
	public MarkerList getCurrentMarkers() {
		return lastMarkers;
	}

	/**
	 * Refresh the markers that have changes in record.
	 * 
	 * @param record
	 * @param monitor
	 */
	public void refreshMarkers(MarkerRefreshRecord record,
			IProgressMonitor monitor) {

		if (monitor.isCanceled())
			return;
		
		MarkerList removed = lastMarkers.removeMarkers(record
				.getRemovedMarkers());

		MarkerList added = lastMarkers.addMarkers(record.getAddedMarkers());

		// Resort just in case of issues with marker limits
		view.getViewer().getSorter().sort(view.getViewer(),
				lastMarkers.toArray());

		if (monitor.isCanceled())
			return;

		MarkerList changed = lastMarkers.findMarkers(record.changedMarkers);
		changed.refreshAll();// Be sure to re-read the marker info

		MarkerNodeRefreshRecord refreshRecord = new MarkerNodeRefreshRecord(
				added.asList(), removed.asList(), changed.asList());

		if (view.isHierarchalMode()) {
			MarkerCategory[] oldCategories = categories;
			categories = buildHierarchy(lastMarkers, 0,
					lastMarkers.getSize() - 1, 0, null);
			refreshStaleCategories(oldCategories, refreshRecord, categories);
		}

		view.refreshMarkerCounts(monitor);
		view.refreshNodes(refreshRecord);
	}

	private void refreshStaleCategories(MarkerCategory[] oldCategories,
			MarkerNodeRefreshRecord refreshRecord,
			MarkerCategory[] newCategories) {

		int newCategoryIndex = 0;
		for (int i = 0; i < oldCategories.length; i++) {
			int maxCategories = Math.min(oldCategories.length,
					newCategories.length);
			for (int j = newCategoryIndex; j < maxCategories; j++) {
				if (newCategories[j].isSameAs(oldCategories[i])) {
					newCategories[j] = oldCategories[i];
					newCategoryIndex = j + 1;
					refreshStaleCategories(oldCategories[i]
							.getCategoryChildren(), refreshRecord,
							newCategories[j].getCategoryChildren());
					break;
				}
				refreshRecord.remove(oldCategories[i]);

			}
		}

		for (int i = newCategoryIndex; i < newCategories.length; i++) {
			refreshRecord.add(newCategories[i]);
		}

	}

	/**
	 * Mark all of the displayed markers as stale.
	 */
	public void markAllStale() {
		if (lastMarkers == null)
			return;

		ConcreteMarker[] markers = lastMarkers.toArray();
		for (int i = 0; i < markers.length; i++) {
			markers[i].markStale();
		}

		lastMarkers = null;
		scheduleMarkerCalculation();		

	}
}
