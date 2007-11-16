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

import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
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

	private static final MarkerCategory[] EMPTY_CATEGORY_ARRAY = new MarkerCategory[0];

	private static final int SHORT_DELAY = 100;// The 100 ms short delay for
	// scheduling

	private static final int TIME_OUT = 30000;// The 30s long delay to run

	private boolean building = true;// Start with nothing until we have
	// something

	private MarkerCategory[] categories;
	private MarkerMap currentMap = null;

	MarkerContentGenerator generator; // The MarkerContentGenerator we are
	// building for

	private Job markerProcessJob;

	private IWorkbenchSiteProgressService progressService;

	private Job updateJob;

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
	 * Build all of the markers in the receiver.
	 * 
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

			sortAndMakeCategories(new SubProgressMonitor(monitor, 30),
					newMarkers);
			monitor.done();
		} finally {
			building = false;
		}

	}

	/**
	 * Sort the newMarkers and build categories if required.
	 * 
	 * @param monitor
	 * @param newMarkers
	 */
	void sortAndMakeCategories(IProgressMonitor monitor, MarkerMap newMarkers) {
		Arrays.sort(newMarkers.toArray(), generator.getComparator());

		monitor.worked(50);

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

		monitor.worked(50);

		currentMap = newMarkers;
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
							i - 1, generator.getCategoryGroup()
									.getMarkerField().getValue(
											markers.elementAt(categoryStart))));
					categoryStart = i;
				}
			}
			previous = elements[i];

		}

		if (end >= categoryStart) {
			categories.add(new MarkerCategory(this, categoryStart, end,
					generator.getCategoryGroup().getMarkerField().getValue(
							markers.elementAt(categoryStart))));
		}

		MarkerCategory[] nodes = new MarkerCategory[categories.size()];
		categories.toArray(nodes);
		return nodes;

	}

	/**
	 * Cancel the pending jobs in the receiver.
	 */
	private void cancelJobs() {
		markerProcessJob.cancel();
		updateJob.cancel();
	}

	/**
	 * Create the job for updating the markers.
	 */
	private void createMarkerProcessJob() {
		markerProcessJob = new Job(MarkerMessages.MarkerView_processUpdates) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
			 */
			public boolean belongsTo(Object family) {
				return MarkerContentGenerator.CACHE_UPDATE_FAMILY == family;
			}

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
		};
		markerProcessJob.setSystem(true);

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
	 * Return the {@link MarkerGroup} being used for categorisation.
	 * 
	 * @return {@link MarkerGroup} or <code>null</code>.
	 */
	MarkerGroup getCategoryGroup() {
		return generator.getCategoryGroup();
	}

	/**
	 * Return the elements in the adapter.
	 * 
	 * @return Object[]
	 */
	public MarkerItem[] getElements() {

		if (currentMap == null) {// First time?
			scheduleMarkerUpdate();
			building = true;
		}
		if (building) {
			return MarkerSupportInternalUtilities.EMPTY_MARKER_ITEM_ARRAY;
		}
		if (generator.isShowingHierarchy() && categories != null) {
			return categories;
		}
		return currentMap.toArray();
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
	 * Get the raw list of marker entries.
	 * 
	 * @return list of MarkerEntry
	 */
	public MarkerEntry[] getMarkerEntries() {
		return currentMap.toArray();
	}

	/**
	 * Return the total number of markers.
	 * 
	 * @return int
	 */
	public int getTotalMarkerCount() {
		MarkerItem[] elements = getElements();
		if (elements.length == 0 || elements[0].isConcrete())
			return elements.length;
		int length = 0;
		for (int i = 0; i < elements.length; i++) {
			length += elements[i].getChildren().length;
		}

		return length;
	}

	/**
	 * Return the resource listener for the builder
	 * 
	 * @return IResourceChangeListener
	 */
	private IResourceChangeListener getUpdateListener() {
		return new IResourceChangeListener() {

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

		};
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
		currentMap = null;
		progressService.schedule(markerProcessJob, SHORT_DELAY);
	}

	/**
	 * Set the category group.
	 * 
	 * @param group
	 *            {@link MarkerGroup} or <code>null</code>.
	 */
	void setCategoryGroup(MarkerGroup group) {
		generator.setCategoryGroup(group);
		scheduleMarkerUpdate();

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

	/**
	 * Set the progress service for the receiver.
	 * 
	 * @param service
	 */
	public void setProgressService(IWorkbenchSiteProgressService service) {
		progressService = service;
		if (service != null) {
			service.showBusyForFamily(
					ResourcesPlugin.FAMILY_MANUAL_BUILD);
			service.showBusyForFamily(
					ResourcesPlugin.FAMILY_AUTO_BUILD);
			service.showBusyForFamily(MarkerContentGenerator.CACHE_UPDATE_FAMILY);
		}

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
	 * Toggle the enabled state of the filter for group.
	 * 
	 * @param group
	 */
	public void toggleFilter(MarkerFieldFilterGroup group) {
		getGenerator().toggleFilter(group);
		scheduleMarkerUpdate();

	}

	/**
	 * Update the receiver for a change in selection.
	 * 
	 * @param newElements
	 */
	public void updateForNewSelection(Object[] newElements) {
		if (generator.updateNeeded(newElements)) {
			generator.updateFocusElements(newElements);
			scheduleMarkerUpdate();
		}

	}

	/**
	 * Update the receiver from the dialog.
	 * 
	 * @param dialog
	 */
	void updateFrom(FiltersConfigurationDialog dialog) {
		generator.setAndFilters(dialog.andFilters());
		generator.setFilters(dialog.getFilters());
		scheduleMarkerUpdate();

	}

	/**
	 * Refresh the sort order and categories of the receiver.
	 * 
	 * @param service
	 *            The service to run the operation in.
	 */
	public void refreshContents(IWorkbenchSiteProgressService service) {
		try {
			service.busyCursorWhile(new IRunnableWithProgress() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public void run(IProgressMonitor monitor) {
					sortAndMakeCategories(monitor, currentMap);
				}
			});
		} catch (InvocationTargetException e) {
			StatusManager.getManager().handle(
					StatusUtil.newStatus(IStatus.ERROR,
							e.getLocalizedMessage(), e));
		} catch (InterruptedException e) {
			StatusManager.getManager().handle(
					StatusUtil.newStatus(IStatus.ERROR,
							e.getLocalizedMessage(), e));
		}

	}

	/**
	 * Save the state to the memento.
	 * @param memento
	 */
	void saveState(IMemento memento) {
		getGenerator().saveState(memento);
		
	}

}
