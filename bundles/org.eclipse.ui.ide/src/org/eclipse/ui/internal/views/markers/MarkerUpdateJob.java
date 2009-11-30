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

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.osgi.framework.Bundle;

/**
 * The MarkerUpdateJob processes marker updates.
 * Once the processing is complete it schedules an UI
 * update.
 * 
 * @since 3.6
 * 
 */
class MarkerUpdateJob extends Job {

	CachedMarkerBuilder builder;
	private boolean clean;
	private long lastUpdateTime=-1;

	/**
	 * @param builder
	 */
	MarkerUpdateJob(CachedMarkerBuilder builder) {
		super(MarkerMessages.MarkerView_searching_for_markers);
		this.builder = builder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(MarkerMessages.MarkerView_searching_for_markers,
				IProgressMonitor.UNKNOWN);
		buildMarkers(monitor);
		return Status.OK_STATUS;
	}

	/**
	 * gather all markers needed by the view.
	 * 
	 * @param monitor
	 */
	void buildMarkers(IProgressMonitor monitor) {
		//check for cancellation before we start
		if (monitor.isCanceled()) {
			return;
		}
		// builder.getUpdateScheduler().cancelQueuedUIUpdates();
		// builder.getUpdateScheduler().indicateStatus(
		// MarkerMessages.MarkerView_searching_for_markers, false);

		Collection markerEntries = new LinkedList();
		//this is not incremental clean every time
		clean = !clean(markerEntries, monitor);
		if (monitor.isCanceled()) {
			return;
		}
		// builder.getUpdateScheduler().indicateStatus(
		// MarkerMessages.MarkerView_processUpdates, false);
		
		monitor.setTaskName(MarkerMessages.MarkerView_processUpdates);
		if (!processMarkerEntries(markerEntries, monitor)) {
			return;
		}
		if (monitor.isCanceled()) {
			return;
		}
		builder.getUpdateScheduler().scheduleUIUpdate(
				MarkerUpdateScheduler.SHORT_DELAY);
		if (monitor.isCanceled()) {
			return;
		}
		builder.setBuilding(false);
		updateDone();
	}

	/**
	 * Capture the current time into as lastupdate time
	 */
	void updateDone() {
		lastUpdateTime = System.currentTimeMillis();
	}

	/**
	 * Collect the markers starting clean, all over again.
	 * @param markerEntries 
	 */
	boolean clean(Collection markerEntries, IProgressMonitor monitor) {
		MarkerContentGenerator generator = builder.getGenerator();
		if (monitor.isCanceled() || generator == null) {
			return false;
		}
		builder.registerTypesToListener();
		return generator.generateMarkerEntries(markerEntries, monitor);
	}

	/**
	 * Process,sort and group the new marker entries in markerEntryList and
	 * update the Markers object
	 * 
	 * @param markerEntries
	 *            the collection of new MarkerEntry(s)
	 */
	boolean processMarkerEntries(Collection markerEntries,
			IProgressMonitor monitor) {
		Markers markers = builder.getMarkers();
		if (monitor.isCanceled()) {
			return false;
		}
		return markers.updateWithNewMarkers(markerEntries, true, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
	 */
	public boolean shouldRun() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return false;
		}
		// Do not run if the change came in before there is a viewer
		return (IDEWorkbenchPlugin.getDefault().getBundle().getState() == Bundle.ACTIVE)
				&& builder.isActive();
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
		builder.getUpdateScheduler().cancelQueuedUIUpdates();
		// builder.getUpdateScheduler().indicateStatus(
		// MarkerMessages.MarkerView_19, false);
		builder.getMarkers().sortMarkerEntries(monitor);
		builder.getUpdateScheduler().scheduleUIUpdate(0L);
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}
}