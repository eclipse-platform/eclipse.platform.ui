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
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

/**
 * The MarkerAdapter is the adapter for the deferred update of markers.
 * 
 * @since 3.1
 * 
 */
public class MarkerAdapter implements IDeferredWorkbenchAdapter {

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
			
			TableSorter sorter = view.getTableSorter();
			int prioritySlot = sorter.getPriorities()[fieldIndex];
			IField field = sorter.getFields()[prioritySlot];
			
			name = field.getCategoryValue(markerAdapter.lastMarkers.toArray()[startIndex]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getChildren()
		 */
		public MarkerNode[] getChildren() {

			if (children == null) {

				ConcreteMarker[] allMarkers = markerAdapter.lastMarkers
						.toArray();
				if (getDisplayedDepth() <= fieldIndex) {
					// Are we at the leaf?

					children = new MarkerNode[end - start + 1];
					System.arraycopy(allMarkers, start, children, 0, end
							- start + 1);
					for (int i = 0; i < children.length; i++) {
						((ConcreteMarker) children[i]).setCategory(this);

					}
				} else {
					children = buildHierarchy(allMarkers, start, end,
							fieldIndex + 1, this);

				}
			}
			return children;

		}

		/**
		 * Return how deep we want to categorize.
		 * 
		 * @return int
		 */
		private int getDisplayedDepth() {
			return 2;
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

	private MarkerList lastMarkers = new MarkerList();

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param markerView
	 */
	MarkerAdapter(MarkerView markerView) {
		view = markerView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object,
	 *      org.eclipse.ui.progress.IElementCollector,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object object,
			IElementCollector collector, IProgressMonitor monitor) {

		if (!view.isHierarchalMode() || object.equals(view.getViewerInput())) {

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

			// Exit immediately if the markers have changed in the meantime.

			Collection markers = Arrays.asList(lastMarkers.toArray());

			if (markerLimit != -1) {

				monitor.subTask(MarkerMessages.MarkerView_18);
				SubProgressMonitor mon = new SubProgressMonitor(monitor, 40);

				markers = SortUtil.getFirst(markers, (TableSorter) view
						.getViewer().getSorter(), markerLimit, mon);
				if (monitor.isCanceled())
					return;
			}

			monitor.subTask(MarkerMessages.MarkerView_queueing_updates);
			ConcreteMarker[] nodes = new ConcreteMarker[markers.size()];
			markers.toArray(nodes);

			if (view.isHierarchalMode()) {
				view.getTableSorter().sort(view.getViewer(), nodes);
				collector.add(buildHierarchy(nodes, 0, nodes.length - 1, 1,
						null), new SubProgressMonitor(monitor, 50));// skip the
																	// first
																	// field
																	// (category)
			} else
				collector.add(markers.toArray(), new SubProgressMonitor(
						monitor, 50));

			if (monitor.isCanceled())
				return;

			monitor.done();
			view.scheduleCountUpdate();
		} else
			addChildren(object, collector, monitor);
	}

	/**
	 * Add the children of object to collector.
	 * 
	 * @param object
	 * @param collector
	 * @param monitor
	 */
	private void addChildren(Object object, IElementCollector collector,
			IProgressMonitor monitor) {
		MarkerCategory category = (MarkerCategory) object;
		collector.add(category.getChildren(), monitor);

	}

	/**
	 * Break the marker up into categories
	 * 
	 * @param markers
	 * @param start
	 *            the start index in the markers
	 * @param end
	 *            the last index to check
	 * @param fieldIndex -
	 *            the parent of the field
	 * @param parent
	 * @return Object[]
	 */
	MarkerNode[] buildHierarchy(ConcreteMarker[] markers, int start, int end,
			int fieldIndex, MarkerCategory parent) {
		TableSorter sorter = view.getTableSorter();
		Collection categories = new ArrayList();

		Object previous = null;
		int categoryStart = start;
		for (int i = start; i <= end; i++) {

			if (previous != null) {
				MarkerNode next = markers[i];

				// Are we at a category boundary?
				if (sorter.compare(previous, next, fieldIndex, false) != 0) {
					categories.add(new MarkerCategory(this, categoryStart,
							i - 1, fieldIndex, parent));
					categoryStart = i;
				}
			}
			previous = markers[i];

		}

		if(end > categoryStart){
			categories.add(new MarkerCategory(this, start, end, fieldIndex,
					parent));
		}
		
		MarkerNode[] nodes = new MarkerNode[categories.size()];
		categories.toArray(nodes);
		return nodes;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
	 */
	public boolean isContainer() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
	 */
	public ISchedulingRule getRule(Object object) {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		return ((MarkerNode) o).getChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return MarkerMessages.MarkerList_0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return ((MarkerNode) o).getParent();
	}

	/**
	 * Get the current list of markers.
	 * 
	 * @return MarkerList
	 */
	MarkerList getCurrentMarkers() {
		return lastMarkers;

	}
}
