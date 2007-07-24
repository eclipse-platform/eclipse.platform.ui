/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.provisional.views.markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerType;

/**
 * The CachedMarkerBuilder is the object that generates the list of markers from
 * a generator.
 * 
 * @since 3.4
 * 
 */
public class CachedMarkerBuilder {

	private MarkerMap currentMap = null;

	private MarkerCategory[] categories;

	private boolean building = true;// Start with nothing until we have
	// something

	MarkerContentGenerator generator; // The MarkerContentGenerator we are
	// building for

	private Job markerProcessJob;
	private Job updateJob;

	private IWorkbenchSiteProgressService progressService;

	private static final int SHORT_DELAY = 100;// The 100 ms short delay for
	// scheduling

	private static final int TIME_OUT = 30000;// The 30s long delay to run

	private static final MarkerCategory[] EMPTY_CATEGORY_ARRAY = new MarkerCategory[0];

	// without a builder update

	/**
	 * Create a new instance of the receiver. Update using the updateJob.
	 * 
	 * @param contentGenerator
	 */
	CachedMarkerBuilder(MarkerContentGenerator contentGenerator) {
		this.generator = contentGenerator;
		createMarkerProcessJob();
		// Hook up to the resource changes after all widget have been created
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				getUpdateListener(),
				IResourceChangeEvent.POST_CHANGE
						| IResourceChangeEvent.PRE_BUILD
						| IResourceChangeEvent.POST_BUILD);
	}

	/**
	 * Return the resource listener for the builder
	 * 
	 * @return
	 */
	private IResourceChangeListener getUpdateListener() {
		return new IResourceChangeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
			 */
			public void resourceChanged(IResourceChangeEvent event) {
				if (!hasMarkerDelta(event))
					return;

				if (event.getType() == IResourceChangeEvent.POST_BUILD) {
					scheduleMarkerUpdate();
					return;
				}

				// After 30 seconds do updates anyways
				if (progressService == null)
					markerProcessJob.schedule(TIME_OUT);
				else
					progressService.schedule(markerProcessJob, TIME_OUT);

			}

			/**
			 * Returns whether or not the given even contains marker deltas for
			 * this view.
			 * 
			 * @param event
			 *            the resource change event
			 * @return <code>true</code> if the event contains at least one
			 *         relevant marker delta
			 * @since 3.3
			 */
			private boolean hasMarkerDelta(IResourceChangeEvent event) {
				Iterator markerTypes = generator.getMarkerTypes().iterator();
				while (markerTypes.hasNext()) {
					MarkerType type = (MarkerType) markerTypes.next();

					if (event.findMarkerDeltas(type.getId(), true).length > 0)
						return true;

				}
				return false;
			}

		};
	}

	/**
	 * Create the job for updating the markers.
	 */
	private void createMarkerProcessJob() {
		markerProcessJob = new Job(MarkerMessages.MarkerView_processUpdates) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				updateJob.cancel();
				buildAllMarkers(monitor);
				updateJob.schedule();
				return Status.OK_STATUS;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.WorkbenchJob#shouldRun()
			 */
			public boolean shouldRun() {
				// Do not run if the change came in before there is a viewer
				return PlatformUI.isWorkbenchRunning();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
			 */
			public boolean belongsTo(Object family) {
				return MarkerContentGenerator.CACHE_UPDATE_FAMILY == family;
			}
		};
		markerProcessJob.setSystem(true);

	}

	/**
	 * Build all of the markers in the receiver.
	 * 
	 * @param collector
	 * @param monitor
	 */
	public void buildAllMarkers(IProgressMonitor monitor) {
		building = true;
		MarkerMap newMarkers;
		try {

			monitor.beginTask(MarkerMessages.MarkerView_19, 60);

			monitor.subTask(MarkerMessages.MarkerView_waiting_on_changes);

			if (monitor.isCanceled())
				return;

			monitor.subTask(MarkerMessages.MarkerView_searching_for_markers);
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
			newMarkers = generator.generateFilteredMarkers(subMonitor);

			if (monitor.isCanceled())
				return;

			Arrays.sort(newMarkers.toArray(), generator.getComparator());

			monitor.worked(30);

			if (newMarkers.getSize() == 0) {
				categories = EMPTY_CATEGORY_ARRAY;
				currentMap = newMarkers;
				monitor.done();
				return;
			}

			monitor.subTask(MarkerMessages.MarkerView_queueing_updates);

			if (monitor.isCanceled())
				return;

			if (generator.isShowingHierarchy()) {
				MarkerCategory[] newCategories = buildHierarchy(newMarkers, 0,
						newMarkers.getSize() - 1, 0);
				if (monitor.isCanceled())
					return;
				categories = newCategories;
			}

			currentMap = newMarkers;
			monitor.done();
		} finally {
			building = false;
		}

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
	MarkerCategory[] buildHierarchy(MarkerMap markers, int start, int end,
			int sortIndex) {
		MarkerComparator sorter = generator.getComparator();

		if (sortIndex > 0) {
			return null;// Are we out of categories?
		}

		Collection categories = new ArrayList();

		Object previous = null;
		int categoryStart = start;

		Object[] elements = markers.toArray();

		for (int i = start; i <= end; i++) {

			if (previous != null) {
				// Are we at a category boundary?
				if (sorter.compareCategory(previous, elements[i]) != 0) {
					categories.add(new MarkerCategory(this, categoryStart,
							i - 1, generator.getCategoryField().getValue(
									markers.elementAt(categoryStart))));
					categoryStart = i;
				}
			}
			previous = elements[i];

		}

		if (end >= categoryStart) {
			categories.add(new MarkerCategory(this, categoryStart, end,
					generator.getCategoryField().getValue(
							markers.elementAt(categoryStart))));
		}

		MarkerCategory[] nodes = new MarkerCategory[categories.size()];
		categories.toArray(nodes);
		return nodes;

	}

	/**
	 * Return the current list of markers.
	 * 
	 * @return MarkerMap
	 */
	public MarkerMap getVisibleMarkers() {
		if (currentMap == null) {// First time?
			scheduleMarkerUpdate();
			building = true;
		}
		if (building) {
			return MarkerMap.EMPTY_MAP;
		}
		return currentMap;
	}

	/**
	 * Return the elements in the adapter.
	 * 
	 * @param root
	 * @return Object[]
	 */
	public MarkerItem[] getElements() {

		if (currentMap == null) {// First time?
			scheduleMarkerUpdate();
			building = true;
		}
		if (building) {
			return MarkerUtilities.EMPTY_MARKER_ITEM_ARRAY;
		}
		if (generator.isShowingHierarchy() && categories != null) {
			return categories;
		}
		return currentMap.toArray();
	}

	/**
	 * Return whether or not the receiver has markers without scheduling
	 * anything if it doesn't.
	 * 
	 * @return boolean <code>true</code> if the markers have not been
	 *         calculated.
	 */
	public boolean hasNoMarkers() {
		return currentMap == null;
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
	 * 
	 * @return boolean
	 */
	boolean isBuilding() {
		return building;
	}

	/**
	 * Schedule an update of the markers with a delay.
	 * 
	 */
	void scheduleMarkerUpdate() {
		cancelJobs();
		progressService.schedule(markerProcessJob, SHORT_DELAY);
	}

	/**
	 * Cancel the pending jobs in the receiver.
	 */
	private void cancelJobs() {
		markerProcessJob.cancel();
		updateJob.cancel();
	}

	/**
	 * Return the generator for the receiver.
	 * 
	 * @return MarkerContentGenerator
	 */
	MarkerContentGenerator getGenerator() {
		return generator;
	}

	/**
	 * Set the updateJob for the receiver.
	 * 
	 * @param job
	 */
	public void setUpdateJob(Job job) {
		updateJob = job;

	}

	/**
	 * @return
	 */
	public int getTotalMarkerCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Set the progress service for the receiver.
	 * 
	 * @param service
	 */
	public void setProgressService(IWorkbenchSiteProgressService service) {
		progressService = service;

	}

	/**
	 * Get the raw list of marker entries.
	 * 
	 * @return list of MarkerEntry
	 */
	public MarkerEntry[] getMarkerEntries() {
		return currentMap.toArray();
	}

	/**
	 * Set the generator and update the contents.
	 * 
	 * @param generator
	 */
	void setGenerator(MarkerContentGenerator generator) {
		this.generator = generator;
		scheduleMarkerUpdate();
	}

}
