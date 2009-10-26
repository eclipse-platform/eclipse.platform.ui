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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.osgi.framework.Bundle;

/**
 * The MarkerUpdateJob processes marker updates , the job is scheduled by
 * MarkersChangeListener. Once the processing is complete it schedules the an UI
 * update
 * 
 * @since 3.6
 * 
 */
class MarkerUpdateJob extends Job {

	CachedMarkerBuilder builder;
	private boolean building = false;
	private boolean clean;
	private boolean incremental;
	private long lastUpdateTime=-1;

	/**
	 * @param builder
	 */
	MarkerUpdateJob(CachedMarkerBuilder builder) {
		this(builder, true);
	}

	/**
	 * @param builder
	 * @param incremental
	 */
	MarkerUpdateJob(CachedMarkerBuilder builder, boolean incremental) {
		super(MarkerMessages.MarkerView_searching_for_markers);
		this.builder = builder;
		this.incremental = incremental;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		building = true;
		try {
			monitor.beginTask(MarkerMessages.MarkerView_searching_for_markers,
					IProgressMonitor.UNKNOWN);
			buildMarkers(monitor);
		} finally {
			building = false;
		}
		return Status.OK_STATUS;
	}

	/**
	 * gather all markers needed by the view.
	 * 
	 * @param monitor
	 */
	protected void buildMarkers(IProgressMonitor monitor) {
		MarkersChangeListener markersListener = builder.getMarkerListener();
		markersListener.cancelQueuedUIUpdates();
		markersListener
				.indicateStatus(
						MarkerMessages.MarkerView_searching_for_markers, false);
		if (clean || !isIncremental()) {
			clean = !clean(markersListener, monitor);
		}
		if (monitor.isCanceled()) {
			return;
		}
		List markers = markersListener.getMarkerEntryList();
		if (monitor.isCanceled()) {
			return;
		}
		monitor.setTaskName(MarkerMessages.MarkerView_processUpdates);
		
		markersListener.indicateStatus(
				MarkerMessages.MarkerView_processUpdates, true/* false */);
		processMarkerEntries(markers, monitor);
		if (monitor.isCanceled()) {
			return;
		}
		markersListener.scheduleUIUpdate(0L);
		lastUpdateTime=System.currentTimeMillis();
	}

	/**
	 * Collect the markers starting clean, all over again.
	 */
	protected boolean clean(MarkersChangeListener markersListener,
			IProgressMonitor monitor) {
		MarkerContentGenerator generator = builder.getGenerator();
		if (generator == null) {
			return false;
		}
		String[] types = generator.getTypes();
		if (monitor.isCanceled()) {
			return false;
		}
		return markersListener.startCollectingType(types, true,
				isIncremental(), monitor);
	}

	/**
	 * Process,sort and group the new marker entries in markerEntryList and
	 * update the Markers object
	 * 
	 * @param markerEntryList
	 *            the list of new MarkerEntry(s)
	 */
	private void processMarkerEntries(List markerEntryList,
			IProgressMonitor monitor) {
		synchronized (markerEntryList) {
			Markers markers = builder.getMarkers();
			if (monitor.isCanceled()) {
				return;
			}
			markers.updateWithNewMarkers(markerEntryList, true, monitor);
			if (monitor.isCanceled()) {
				return;
			}
			if (isIncremental()) {
				markerEntryList.clear();
				MarkerEntry[] entries = markers.getMarkerEntryArray();
				for (int i = 0; i < entries.length; i++) {
					markerEntryList.add(entries[i]);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
	 */
	public boolean shouldRun() {
		if (!builder.isActive()) {
			return false;
		}
		if (!PlatformUI.isWorkbenchRunning()) {
			return false;
		}
		MarkersChangeListener markersListener = builder.getMarkerListener();
		// Do not run if the change came in before there is a viewer
		return markersListener != null ? (markersListener.canUpdateNow())
				&& (IDEWorkbenchPlugin.getDefault().getBundle().getState() == Bundle.ACTIVE)
				: false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */
	public boolean belongsTo(Object family) {
		if (family.equals(builder.CACHE_UPDATE_FAMILY)) {
			return true;
		}
		if (family.equals(builder.MARKERSVIEW_UPDATE_JOB_FAMILY)) {
			return true;
		}
		return super.belongsTo(family);
	}

	/**
	 * @return Returns true when building.
	 */
	boolean isBuilding() {
		return building;
	}

	/**
	 * @return Returns if the a clean is requested.
	 */
	boolean isClean() {
		return clean;
	}

	/**
	 * Request a clean
	 */
	void setClean() {
		this.clean = true;
	}

	/**
	 * @return Returns true if collecting markers incrementally.
	 */
	boolean isIncremental() {
		return incremental;
	}

	/**
	 * @return last update time
	 */
	 long getLastUpdateTime() {
		return lastUpdateTime;
	}
}

/**
 * The SortingJob is used to resort the existing markers. Once the sorting is
 * complete it schedules the an UI update
 * 
 * @since 3.6
 * 
 */
class SortingJob extends MarkerUpdateJob {
	public SortingJob(CachedMarkerBuilder builder) {
		super(builder);
		this.builder = builder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.views.markers.MarkerUpdateJob#run(org.eclipse
	 * .core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(MarkerMessages.MarkerView_19,
				IProgressMonitor.UNKNOWN);
		MarkersChangeListener markersListener = builder.getMarkerListener();
		markersListener.cancelQueuedUIUpdates();
		markersListener.indicateStatus(
				MarkerMessages.MarkerView_19, true/* false */);
		builder.getMarkers().sortMarkerEntries(monitor);
		markersListener.scheduleUIUpdate(0L);
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerUpdateJob#shouldRun()
	 */
	public boolean shouldRun() {
		MarkersChangeListener markersListener = builder.getMarkerListener();
		// Do not run if the change came in before there is a viewer
		return markersListener != null ? (markersListener.canUpdateNow())
				&& (IDEWorkbenchPlugin.getDefault().getBundle().getState() == Bundle.ACTIVE)
				: false;
	}
}