/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The MarkersChangeListener is IResourceChangeListener that waits for any
 * change in the markers in workspace that are of the view's interest. Schedules
 * an update if we have a change that affects the view.
 *
 * @since 3.6
 */
class MarkersChangeListener implements IResourceChangeListener {

	private final ExtendedMarkersView view;
	private final CachedMarkerBuilder builder;

	private String[] listeningTypes;
	private boolean receiving;

	// The time the build started. A -1 indicates no build in progress.
	private long preBuildTime;

	/**
	 *
	 * @param view
	 *            the marker view the listener is listening for
	 * @param builder
	 *            the builder for the view
	 */
	MarkersChangeListener(ExtendedMarkersView view, CachedMarkerBuilder builder) {
		this.view = view;
		this.builder = builder;
		listeningTypes = new String[0];
	}

	/**
	 * Start listening for changes.
	 */
	synchronized void start() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				this,
				IResourceChangeEvent.POST_CHANGE
						| IResourceChangeEvent.PRE_BUILD
						| IResourceChangeEvent.POST_BUILD);
	}

	/**
	 * Stop listening for changes.
	 */
	synchronized void stop() {
		if (listeningTypes != null) {
			listeningTypes = new String[0];
		}
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * Checks if the workspace is building
	 */
	boolean workspaceBuilding() {
		return preBuildTime > 0;
	}

	/**
	 * Tells the listener to become responsive to changes for the specified
	 * types of markers.
	 *
	 * @param typeIds
	 *            the ids of the IMarker types
	 * @param includeSubTypes
	 *            true to include the sub-marker-types
	 */
	void listenToTypes(String[] typeIds, boolean includeSubTypes) {
		try {
			// register marker types being gathering
			if (includeSubTypes) {
				listeningTypes = MarkerResourceUtil.getAllSubTypesIds(typeIds);
			} else {
				// register marker types being gathering
				listeningTypes = typeIds;
			}
		} catch (Exception e) {
			MarkerSupportInternalUtilities.logViewError(e);
		}
	}

	@Override
	public synchronized void resourceChanged(IResourceChangeEvent event) {
		/* We can now consider removing synchronized for
		 * this method.Only the start and stop need to be
		 * synchronize on the listener
		 */
		setReceivingChange(true);
		try {
			if (event.getType() == IResourceChangeEvent.PRE_BUILD) {
				preBuild();
				return;
			}
			if (event.getType() == IResourceChangeEvent.POST_BUILD) {
				postBuild();
				// clear any pending updates
				builder.getUpdateScheduler().speedUpPendingUpdates();
				return;
			}
			if(!hasApplicableTypes(event)){
				return;
			}

			if (!builder.isIncremental()) {
				builder.getUpdateScheduler().scheduleUpdate();
				return;
			}
			handleIncrementalChange(event);
		} finally {
			setReceivingChange(false);
		}
	}

	/**
	 * @return the receiving
	 */
	boolean isReceivingChange() {
		return receiving;
	}

	/**
	 * @param receiving
	 *            the receiving to set
	 */
	void setReceivingChange(boolean receiving) {
		this.receiving = receiving;
	}

	/**
	 * Handle changes incrementally.
	 * The following performs incremental updation
	 * of the markers that were gathered initially, and keeps them synched at
	 * any point with the markers of interest in Workspace. Unfortunately marker
	 * operations cannot be locked so locking between gathering of markers and
	 * marker deltas is not possible.
	 *
	 * Note : this method of updating is NOT used and tested yet and has holes
	 * but left out SOLELY for further investigation(*).
	 */
	private void handleIncrementalChange(IResourceChangeEvent event) {
		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(null, true);
		if (markerDeltas.length == 0) {
			return;
		}
		Collection<MarkerEntry> removed = new LinkedList<>(), added = new LinkedList<>(), changed = new LinkedList<>();
		String[] types = listeningTypes;
		for (IMarkerDelta markerDelta : markerDeltas) {
			try {
				String typeId = markerDelta.getType();
				if (!isApplicableType(types, typeId)) {
					continue;
				}
				IMarker marker = markerDelta.getMarker();
				MarkerEntry markerEntry = new MarkerEntry(marker);
				switch (markerDelta.getKind()) {
				case IResourceDelta.REMOVED: {
					removed.add(markerEntry);
					break;
				}
				case IResourceDelta.ADDED: {
					added.add(markerEntry);
					break;
				}
				case IResourceDelta.CHANGED: {
					changed.add(markerEntry);
					break;
				}
				default:{
					break;
				}
				}
			} catch (Exception e) {
				// log exception
				MarkerSupportInternalUtilities.logViewError(e);
			}
		}
		if (removed.size() > 0 || added.size() > 0 || changed.size() > 0) {
			MarkerUpdate update = new MarkerUpdate(added, removed, changed);
			builder.incrementalUpdate(update);
			builder.getUpdateScheduler().scheduleUpdate();
		}
	}

	/**
	 * @return true if the marker delta has a change in an applicable marker
	 *         type else false.
	 */
	private boolean hasApplicableTypes(IResourceChangeEvent event) {
		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(null, true);
		String[] types = listeningTypes;
		if (types.length == 0) {
			return false;
		}
		for (IMarkerDelta markerDelta : markerDeltas) {
			if (isApplicableType(types, markerDelta.getType())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Helper to {@link #hasApplicableTypes(IResourceChangeEvent)}
	 */
	private boolean isApplicableType(String[] types, String typeId) {
		for (String type : types) {
			if (type.equals(typeId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * We are in a pre-build state.
	 */
	private void preBuild() {
		preBuildTime = System.currentTimeMillis();
	}

	/**
	 * Post-build has happened.
	 */
	private void postBuild() {
		preBuildTime = -1;
	}

	/**
	 * @return Returns the view.
	 */
	ExtendedMarkersView getView() {
		return view;
	}

	/**
	 * @return Returns the builder.
	 */
	CachedMarkerBuilder getBuilder() {
		return builder;
	}
}

///////////helpers/////////////

/**
 * For Incremental updating
 * @since 3.6
 */
class MarkerUpdate {
	Collection<MarkerEntry> added;
	Collection<MarkerEntry> removed;
	Collection<MarkerEntry> changed;

	MarkerUpdate(Collection<MarkerEntry> added, Collection<MarkerEntry> removed, Collection<MarkerEntry> changed) {
		this.added = added;
		this.removed = removed;
		this.changed = changed;
	}
}

/**
 * Manages scheduling of marker updates and the view ,also various other methods
 * related to scheduling updates.This class should be used for update
 * scheduling to avoid confusion.
 *
 * Note: the reason for keeping this class is because the update scheduling is
 * so closely related to Marker change events.
 *
 * @since 3.6
 */
class MarkerUpdateScheduler {

	static final int SHORT_DELAY = 150;
	static final int LONG_DELAY = 10000;
	static final long TIME_OUT = 30000;

	private final CachedMarkerBuilder builder;
	private final ExtendedMarkersView view;

	private MarkerUpdateJob updateJob;
	private UIUpdateJob uiUpdateJob;

	private final Object schedulingLock;

	private final MarkerUpdateTimer updateTimer;

	public MarkerUpdateScheduler(ExtendedMarkersView view, CachedMarkerBuilder builder) {
		this.view = view;
		this.builder = builder;
		schedulingLock = new Object();
		updateTimer = new MarkerUpdateTimer();
	}

	/**
	 * Always use this to schedule update job
	 * @return Returns the schedulingLock.
	 */
	Object getSchedulingLock() {
		return schedulingLock;
	}

	/**
	 * Schedule marker update.
	 */
	void scheduleUpdate(long delay, boolean cancelPrevious,
			boolean[] changeFlags) {
		//we do not need to make this atomic (?)
		builder.setBuilding(true);
		if (cancelPrevious) {
			cancelQueuedUIUpdates();
			cancelUpdate();
		}
		// indicateStatus(MarkerMessages.MarkerView_queueing_updates, true);
		updateJob = builder.scheduleUpdateJob(delay, true, changeFlags);
		// updateTimer.reset();
	}

	/**
	 * Schedule marker update.
	 */
	void scheduleUpdate(long delay, boolean cancelPrevious) {
		//we do not need to make this atomic (?)
		builder.setBuilding(true);
		if (cancelPrevious) {
			cancelQueuedUIUpdates();
			cancelUpdate();
		}
		// indicateStatus(MarkerMessages.MarkerView_queueing_updates, true);
		updateJob = builder.scheduleUpdateJob(delay, true);
		// updateTimer.reset();
	}

	/**
	 * Schedule marker update.
	 */
	void scheduleUpdate(long delay, boolean[] changeFlags) {
		scheduleUpdate(delay, true, changeFlags);
	}

	/**
	 * Schedule marker update.
	 */
	void scheduleUpdate(boolean[] changeFlags) {
		synchronized (updateTimer) {
			builder.updateChangeFlags(changeFlags);
			updateTimer.update();
		}
	}

	/**
	 * Schedule marker update.
	 */

	void scheduleUpdate() {
		synchronized (updateTimer) {
			updateTimer.update();
		}
	}

	/**
	 * Schedule pending updates to happen quickly.
	 */
	void speedUpPendingUpdates() {
		synchronized (updateTimer) {
			updateTimer.speedUpPendingUpdates();
		}
	}

	/**
	 * Returns true if updates have been scheduled and not finished,else false.
	 */
	boolean updatesPending() {
		synchronized (updateTimer) {
			if (builder.isBuilding()) {
				return true;
			}
			boolean pending = false;
			if (updateJob != null) {
				pending = updateJob.getState() != Job.NONE;
			}
			if (!pending) {
				if (uiUpdateJob != null) {
					pending = uiUpdateJob.getState() != Job.NONE;
				}
			}
			if (!pending) {
				// No need to come till here
				pending = updateTimer.updatesPending();
			}
			return pending;
		}
	}

	/**
	 * Schedule only an UI update
	 */
	void scheduleUIUpdate(long delay) {
		uiUpdateJob = view.scheduleUpdate(delay);
	}

	/**
	 * Cancel any marker update if pending.
	 */
	void cancelUpdate() {
		builder.cancelUpdate();
	}

	/**
	 * Cancel any UI update if pending.
	 */
	void cancelQueuedUIUpdates() {
		view.cancelQueuedUpdates();
	}

	/**
	 * //Fix for Bug 294959.There is another patch(more exhaustive in terms
	 * of possibilities to cover) on the bug in which we keep scheduling
	 * updates with CANCEL_MARGIN_DELAY after a Post-Build event until we
	 * have actually finished an update. In case the current way has
	 * problems on a machine It would be worth looking at that.An
	 * optimization to ensure we do not update too often, yet be responsive
	 * and not miss any change.
	 *
	 * Note that we re-schedule the update every time.This is to ensure we
	 * do not miss out an update even if another update was externally(UI)
	 * scheduled, and finished much earlier(The changes before that have
	 * been taken care of by the that update).Also we mandate updating once
	 * in TIME-OUT.To change behaviour, changes in the DELAY parameters will
	 * suffice. For example, setting TIME_OUT much larger value, and so on.
	 *
	 * @since 3.6
	 */
	class MarkerUpdateTimer {

		/**
		 * This is to allow batching together any changes that may arrive in
		 * after a post-build, in a short interval.This controls how we
		 * update when we are receiving post-build events and change-events
		 * continuously over a short gap of time.
		 */
		private static final long CANCEL_MARGIN_DELAY = (SHORT_DELAY * 3);
		private static final long NO_CANCEL_TIME_OUT = (LONG_DELAY * 3);
		//this to account for an ordinary change that may come in
		//after post build
		private static final long AFTER_MARGIN = 2;

		private long timeB4Update;

		private long timerValidStart;

		void update() {
			long startTime = view.getLastUIRefreshTime();
			long currentTime = System.currentTimeMillis();
			long updateTimeGap = currentTime - startTime;
			// check if we can cancel a scheduled or a running update
			boolean cancelable = !(updateTimeGap > TIME_OUT);
			updateTimeGap = updateTimeGap % TIME_OUT;
			if (!cancelable) {
				cancelable = !isValidTimeOut(startTime, currentTime, TIME_OUT);
				if (timeB4Update != -1 && cancelable) {
					if (updateTimeGap < CANCEL_MARGIN_DELAY) {
						updateTimeGap = CANCEL_MARGIN_DELAY;
					}
				}
			}

			if (timeB4Update == -1) {
				/*
				 * This is an optimization and may be removed.But, it is
				 * desirable that we schedule soon after a post-build.
				 */
				// a Special Update request
				go(CANCEL_MARGIN_DELAY, cancelable);
				return;
			}

			long delay = TIME_OUT - updateTimeGap;
			if ((delay + updateTimeGap) > NO_CANCEL_TIME_OUT) {
				if (delay > NO_CANCEL_TIME_OUT) {
					// rectify the delay
					delay = LONG_DELAY;
				}
				if (isValidTimeOut(startTime, currentTime, NO_CANCEL_TIME_OUT)) {
					cancelable = false;
				}
			}
			if (!builder.getMarkerListener().workspaceBuilding()) {
				if (updateTimeGap + LONG_DELAY > TIME_OUT) {
					if (updateTimeGap + (CANCEL_MARGIN_DELAY) >= TIME_OUT) {
						go(delay, false);
					} else {
						go(delay, cancelable);
					}
				} else {
					go(LONG_DELAY, cancelable);
				}
			} else {
				// we are in build again
				go(delay, cancelable);
			}
		}

		/**
		 * Schedules quickly if any update is pending, Or prepares for quick
		 * scheduling on next change
		 */
		void speedUpPendingUpdates() {
			/*
			 * if we have a distant pending update schedule it with
			 * CANCEL_MARGIN_DELAY
			 */
			if (updatesPending()) {
				timeB4Update = -1;
				update();
			}
			/*
			 * Else wait for next change(Post-Change?), it will be scheduled
			 * with CANCEL_MARGIN_DELAY
			 */
			timeB4Update = -1;
		}

		/**
		 * Checks if we have a pending update
		 */
		boolean updatesPending() {
			long diff = timeB4Update - System.currentTimeMillis();
			return diff > CANCEL_MARGIN_DELAY;
		}

		/**
		 * Checks if a time-out is valid,or if its just a period of
		 * inactivity. NOTE:This is PURELY an optimization and can be
		 * omitted.
		 */
		private boolean isValidTimeOut(long startTime, long currentTime, long timeOut) {
			// long updateTimeGap = currentTime - startTime;
			if (timeB4Update != -1 && startTime > timeB4Update) {
				/*
				 * The last scheduled update finished.This is not an actual
				 * TIME_OUT.Possible that we have not updated for a long
				 * interval.Lets make this update cancelable anyway.Reset
				 * timer.
				 */
				timerValidStart = currentTime;
				return false;
			} else if ((currentTime - timerValidStart) < timeOut ) {
				return false;
			} else {
				/*
				 * Do not update internal value we only use this for
				 * checking valid TIME_OUTs
				 */
				return true;
			}
		}

		private void go(long delay, boolean cancelPrevious) {
			timeB4Update = System.currentTimeMillis() + delay;
			scheduleUpdate(delay + AFTER_MARGIN, cancelPrevious);
		}
	}
}
