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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

/**
 * The MarkersChangeListener is IResourceChangeListener that waits for any
 * change in the markers in workspace that are of the view's interest. Schedules
 * and manages the gathering of markers and updating of UI.
 * 
 * @since 3.6
 */
class MarkersChangeListener implements IResourceChangeListener {

	private LinkedList markerEntryList;
	private List collectingtypes;

	private ExtendedMarkersView view;
	private CachedMarkerBuilder builder;
	private MarkerContentGenerator generator;

	private boolean incremental;
	private boolean updating;

	static final int SHORT_DELAY = 150;
	static final int LONG_DELAY = 10000;
	static final long TIME_OUT = 30000;

	// The time the build started. A -1 indicates no build in progress.
	private long preBuildTime;
	
	private MarkerUpdateTimer updateTimer;
	
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
		updateTimer = new MarkerUpdateTimer();
	}

	/**
	 * Initialize the listener.
	 */
	private void init() {
		markerEntryList = new LinkedList();
		collectingtypes = new ArrayList();
		updating = false;
		start();
	}

	/**
	 * Start listening for changes.
	 */
	private void start() {
		generator = builder.getGenerator();
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
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		if (markerEntryList != null) {
			markerEntryList.clear();
			markerEntryList = null;
		}
	}

	/**
	 * Schedule marker update.
	 */
	void scheduleUpdate(long delay, boolean cancelPrevious,
			boolean[] changeFlags) {
		if (cancelPrevious) {
			cancelQueuedUIUpdates();
			cancelUpdate();
		}
		indicateStatus(MarkerMessages.MarkerView_queueing_updates, true);
		builder.scheduleUpdateJob(delay, true, changeFlags);
		//updateTimer.reset();
	}
	
	/**
	 * Schedule marker update.
	 */
	void scheduleUpdate(long delay, boolean cancelPrevious) {
		if (cancelPrevious) {
			cancelQueuedUIUpdates();
			cancelUpdate();
		}
		indicateStatus(MarkerMessages.MarkerView_queueing_updates, true);
		builder.scheduleUpdateJob(delay, true);
		//updateTimer.reset();
	}
	
	/**
	 * Schedule marker update.
	 */
	void scheduleUpdate(long delay,boolean[] changeFlags) {
		scheduleUpdate(delay, true, changeFlags);
	}

	/**
	 * Schedule marker update.
	 */
	void scheduleUpdate(boolean[] changeFlags) {
		builder.updateChangeFlags(changeFlags);
		updateTimer.update();
	}
	
	/**
	 * Schedule marker update.
	 */
	
	void scheduleUpdate() {
		updateTimer.update();
	}

	/**
	 * Schedule only an UI update
	 * 
	 * @param delay
	 * 
	 */
	void scheduleUIUpdate(long delay) {
		view.scheduleUpdate(delay);
	}

	/**
	 * Indicate the status message on UI.
	 * 
	 * @param messsage
	 *            the status to display
	 */
	void indicateStatus(String messsage) {
		indicateStatus(messsage, false);
	}

	/**
	 * Indicate the status message on UI.
	 * 
	 * @param messsage
	 *            the status to display
	 * @param updateUI
	 *            <code>true</code> update label to show changing status
	 */
	void indicateStatus(String messsage, boolean updateUI) {
		view.indicateUpdating(messsage != null ? messsage
				: MarkerMessages.MarkerView_queueing_updates, updateUI);
	}

	/**
	 * Cancel any marker update if pending.
	 * 
	 */
	void cancelUpdate() {
		builder.cancelUpdate();
	}

	/**
	 * Cancel any UI update if pending.
	 * 
	 */
	void cancelQueuedUIUpdates() {
		view.cancelQueuedUpdates();
	}

	/**
	 * Checks if the workspace is building
	 * 
	 */
	boolean workspaceBuilding() {
			return preBuildTime > 0;
	}
	/**
	 * Tells the listener to become responsive to changes for the specified
	 * types of markers. Also collects them as a part of initial setup, in case
	 * a incremental updating is desired.
	 * 
	 * @param typeIds the ids of the IMarker types
	 * @param includeSubTypes true to include the sub-marker-types
	 * @param updateIncrementally true if incremental behavior is desired
	 */
	boolean startCollectingType(String[] typeIds, boolean includeSubTypes,
			boolean updateIncrementally, IProgressMonitor monitor) {
		stop();
		clearPendingDeltas();
		if (monitor.isCanceled()) {
			return false;
		}
		synchronized (this) {
			incremental = updateIncrementally;
			init();
			if (includeSubTypes) {
				HashSet superTypes = getMutuallyExclusiveSupers(typeIds);
				Iterator iterator = superTypes.iterator();
				while (iterator.hasNext()) {
					MarkerType type = (MarkerType) iterator.next();
					boolean success = startCollectingType(type.getId(),
							includeSubTypes, monitor);
					if (!success) {
						return success;
					}
					if (monitor.isCanceled()) {
						return false;
					}
				}
				HashSet subTypes = getAllSubTypes(typeIds);
				iterator = subTypes.iterator();
				while (iterator.hasNext()) {
					MarkerType type = (MarkerType) iterator.next();
					collectingtypes.add(type.getId());
				}
			} else {
				for (int i = 0; i < typeIds.length; i++) {
					boolean success = startCollectingType(typeIds[i],
							includeSubTypes, monitor);
					if (!success) {
						return success;
					}
					if (monitor.isCanceled()) {
						return false;
					}
					collectingtypes.add(typeIds[i]);
				}
			}
		}
		return true;
	}

	/**
	 * A helper to the above method
	 * @param typeId
	 * @param includeSubTypes
	 * @param monitor
	 */
	private boolean startCollectingType(String typeId, boolean includeSubTypes,
			IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return false;
		}
		setUpdating(true);
		try {
			synchronized (markerEntryList) {
				Iterator iterator = generator.getResourcesForBuild().iterator();
				while (iterator.hasNext()) {
					IResource resource = (IResource) iterator.next();
					IMarker[] markers = resource.findMarkers(typeId,
							includeSubTypes, IResource.DEPTH_INFINITE);

					for (int i = 0; i < markers.length; i++) {
						MarkerEntry entry = new MarkerEntry(markers[i]);
						if (generator.select(entry)) {
							markerEntryList.add(entry);
						}
					}
				}
			}
		} catch (Exception e) {
			return false;
		} finally {
			setUpdating(false);
		}
		return true;
	}
	
	/**
	 * Noop that triggers {@link IResourceChangeEvent},if any pending
	 */
	private void clearPendingDeltas() {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					// noop
				}
			}, null, IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
		}
	}

	/**
	 * @return Returns the markerEntryList.
	 */
	LinkedList getMarkerEntryList() {
		return markerEntryList;
	}

	/**
	 * @return the updating
	 */
	boolean isUpdating() {
		return updating;
	}

	/**
	 * @param updating
	 *            the updating to set
	 */
	void setUpdating(boolean updating) {
		this.updating = updating;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
	 * .eclipse.core.resources.IResourceChangeEvent)
	 */
	public synchronized void resourceChanged(IResourceChangeEvent event) {
		// boolean needsUpdate = false;
		try {
			setUpdating(true);
			if (event.getType() == IResourceChangeEvent.PRE_BUILD) {
				preBuild();
				return;
			}
			if (event.getType() == IResourceChangeEvent.POST_BUILD) {
				postBuild();
				//clear any pending updates
				updateTimer.clearUpdates();
				return;
			}
			IMarkerDelta[] markerDeltas = event.findMarkerDeltas(null, true);
			final List removed = new LinkedList();
			final List added = new LinkedList();
			final List changed = new LinkedList();
			for (int i = 0; i < markerDeltas.length; i++) {
				try {
					String typeId = markerDeltas[i].getType();
					if (!collectingtypes.contains(typeId)) {
						continue;
					}
					int kind = markerDeltas[i].getKind();
					IMarker marker = markerDeltas[i].getMarker();
					MarkerEntry markerEntry = new MarkerEntry(marker);
					// if (!needsUpdate) {
					// needsUpdate = isMarkerChangeOfInterest(markerEntry,
					// kind);
					// if (!needsUpdate) {
					// continue;
					// }
					// }
					if (kind == IResourceDelta.REMOVED) {
						removed.add(markerEntry);
					} else if (kind == IResourceDelta.ADDED) {
						added.add(markerEntry);
					} else if (kind == IResourceDelta.CHANGED) {
						changed.add(markerEntry);
					} else {
						// add(typeId, marker, removed);
					}
				} catch (Exception e) {
				}
			}
			// if (!needsUpdate) {
			// return;
			// }

			if (removed.size() > 0 || added.size() > 0 || changed.size() > 0) {
				handleMarkerChange(event, removed, added, changed);
			}else{
				handleNoMarkerChange();
			}
		} finally {
			setUpdating(false);
		}
	}

	/**
	 * Check if a marker change, removal, or addition is of interest to the view.
	 * 
	 * Note: This has been commented out intentionally as it performs bad when 
	 * very selective filters have been applied.
	 * 
	 * @param marker
	 * @param kind
	 * @return
	 */
	// private boolean isMarkerChangeOfInterest(MarkerEntry marker, int kind) {
	// boolean needsUpdate = false;
	// if (kind == IResourceDelta.REMOVED) {
	// needsUpdate = hasMarkerRemoval(marker);
	// } else if (kind == IResourceDelta.ADDED) {
	// needsUpdate = hasMarkerAdditionsofInterest(marker);
	// } else if (kind == IResourceDelta.CHANGED) {
	// needsUpdate = hasMarkerChanges(marker);
	// } else {
	// // add(typeId, marker, removed);
	// }
	// return needsUpdate;
	// }

	/**
	 * Returns whether or not the given marker addition is of interest to the
	 * view.
	 * 
	 * @param marker
	 *            the markerentry
	 * @return <code>true</code> if build is needed <code>false</code> if no
	 *         update needed
	 */
	// private boolean hasMarkerAdditionsofInterest(MarkerEntry marker) {
	// if (generator.select(marker)) {
	// return true;
	// }
	// return false;
	// }

	/**
	 * Returns whether or not markers were removed from the view.
	 * 
	 * @param marker
	 *              the markerentry
	 * @return <code>true</code> if build is needed <code>false</code> if no
	 *         update needed
	 */
	// private boolean hasMarkerRemoval(MarkerEntry marker) {
	// Iterator iterator = markerEntryList.iterator();
	// while (iterator.hasNext()) {
	// MarkerEntry entry = (MarkerEntry) iterator.next();
	// if (entry.marker.equals(marker.marker)) {
	// return true;
	// }
	// }
	// return false;
	// }

	/**
	 * Returns whether or not markers were removed from the view.
	 * 
	 * @param marker
	 *              the markerentry
	 * @return <code>true</code> if build is needed <code>false</code> if no
	 *         update needed
	 */
	// private boolean hasMarkerChanges(MarkerEntry marker) {
	// if (generator.select(marker)) {
	// return true;
	// }
	// Iterator iterator = markerEntryList.iterator();
	// while (iterator.hasNext()) {
	// MarkerEntry entry = (MarkerEntry) iterator.next();
	// if (entry.marker.equals(marker.marker)) {
	// return true;
	// }
	// }
	// return false;
	// }

	/**
	 * Markers have not changed
	 */
	private void handleNoMarkerChange() {
		//view.indicateUpdating(null, true, false);
	}

	/**
	 * Schedules marker updates at appropriate events and times.
	 * 
	 * @param event
	 * @param removed
	 * @param added
	 * @param changed
	 */
	private void handleMarkerChange(IResourceChangeEvent event, List removed,
			List added, List changed) {
		if (incremental) {
			synchronized (markerEntryList) {
				/**
				 * The following performs incremental updation of the markers
				 * that were gathered intially, and keeps them synched at any
				 * point with the markers of interest in Workspace.
				 * Unfortunately marker operations cannot be locked so locking
				 * between gathering of markers and marker deltas is not
				 * possible
				 * 
				 * We do not update incrementally.Anyway, we have this code
				 * for further investigation(*).
				 */
				handleRemovedEntries(removed);
				handleChangedEntries(changed);
				handleAddedEntries(added);
			}
		}
		boolean[] changeFlags = new boolean[] { added.size() > 0,
				removed.size() > 0, changed.size() > 0 };
		scheduleUpdate(changeFlags);
	}
	
	/**
	 * Post-build has happened.
	 */
	protected void postBuild() {
		preBuildTime = -1;
	}

	/**
	 * We are in a pre build state. Do not update until the post build happens.
	 */
	protected void preBuild() {
		preBuildTime = System.currentTimeMillis();
	}

	/**
	 * @param added
	 */
	private void handleAddedEntries(List added) {
		Iterator iterator = added.iterator();
		while (iterator.hasNext()) {
			MarkerEntry entry = (MarkerEntry) iterator.next();
			if (generator.select(entry)) {
				markerEntryList.add(entry);
			}
		}
	}

	/**
	 * @param changed
	 */
	private void handleChangedEntries(List changed) {
		Iterator iterator = changed.iterator();
		while (iterator.hasNext()) {
			MarkerEntry entry = (MarkerEntry) iterator.next();
			Iterator iterator2 = markerEntryList.iterator();
			while (iterator2.hasNext()) {
				MarkerEntry oldEntry = (MarkerEntry) iterator2.next();
				if (oldEntry.marker.equals(entry.marker)) {
					iterator2.remove();
				}
			}
			if (!generator.select(entry)) {
				iterator.remove();
			}
		}
		markerEntryList.addAll(changed);
	}

	/**
	 * @param removed
	 */
	private void handleRemovedEntries(List removed) {
		
//		Iterator iterator = markerEntryList.iterator();
//		while (iterator.hasNext()) {
//			MarkerEntry entry = (MarkerEntry) iterator.next();
//			if (entry.marker == null || !entry.marker.exists()) {
//				iterator.remove();
//			}
//		}
		
		Iterator iterator = removed.iterator();
		while (iterator.hasNext()) {
			MarkerEntry entry = (MarkerEntry) iterator.next();
			Iterator iterator2 = markerEntryList.iterator();
			while (iterator2.hasNext()) {
				MarkerEntry oldEntry = (MarkerEntry) iterator2.next();
				if (oldEntry.marker.equals(entry.marker)) {
					iterator2.remove();
					continue;
				}
			}
		}
	}

	// /////////helpers/////////////
	/**
	 * @param typeIds
	 */
	static String[] getAllSubTypesIds(String[] typeIds) {
		HashSet set = getAllSubTypes(typeIds);
		return toTypeStrings(set);
	}

	/**
	 * @param typeIds
	 */
	static HashSet getAllSubTypes(String[] typeIds) {
		HashSet set = new HashSet();
		MarkerTypesModel typesModel = MarkerTypesModel.getInstance();
		for (int i = 0; i < typeIds.length; i++) {
			MarkerType type = typesModel.getType(typeIds[i]);
			set.add(type);
			MarkerType[] subs = type.getAllSubTypes();
			for (int j = 0; j < subs.length; j++) {
				set.add(subs[j]);
			}
		}
		return set;
	}

	/**
	 * @param typeIds
	 */
	static String[] getMutuallyExclusiveSupersIds(String[] typeIds) {
		HashSet set = getMutuallyExclusiveSupers(typeIds);
		return toTypeStrings(set);
	}

	/**
	 * @param typeIds
	 */
	static HashSet getMutuallyExclusiveSupers(String[] typeIds) {
		HashSet set = new HashSet();
		MarkerTypesModel typesModel = MarkerTypesModel.getInstance();
		for (int i = 0; i < typeIds.length; i++) {
			MarkerType type = typesModel.getType(typeIds[i]);
			set.add(type);
		}
		for (int i = 0; i < typeIds.length; i++) {
			MarkerType type = typesModel.getType(typeIds[i]);
			MarkerType[] subs = type.getAllSubTypes();
			HashSet subsOnly = new HashSet(Arrays.asList(subs));
			subsOnly.remove(type);
			set.removeAll(subsOnly);
		}
		return set;
	}

	/**
	 * @param collection
	 */
	private static String[] toTypeStrings(Collection collection) {
		HashSet ids = new HashSet();
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			MarkerType type = (MarkerType) iterator.next();
			ids.add(type.getId());
		}
		return (String[]) ids.toArray(new String[ids.size()]);
	}

	/**
	 * @return Returns true if incremental.
	 */
	boolean isIncremental() {
		return incremental;
	}

	/**
	 * //Fix for Bug 294959.There is another patch(more exhaustive in terms of
	 * possibilities to cover) on the bug in which we keep scheduling updates
	 * with CANCEL_MARGIN_DELAY after a Post-Build event until we have actually
	 * finished an update. In case the current way has problems on a machine It
	 * would be worth looking at that.An optimization to ensure we do not update
	 * too often, yet be responsive and not miss any change.
	 * 
	 * Note that we re-schedule the update every time.This is to ensure we do
	 * not miss out an update even if another update was externally(UI)
	 * scheduled, and finished much earlier(The changes before that have been
	 * taken care of by the that update).Also we mandate updating once in
	 * TIME-OUT.To change behaviour, changes in the DELAY parameters will
	 * suffice.
	 * 
	 * @since 3.6
	 */
	private class MarkerUpdateTimer {
		/**
		 * This is to allow batching together any changes that may arrive in after a
		 * post-build, in a short interval.This controls how we update when we are
		 * receiving post-build events and change-events continously over a  short
		 * gap of time.
		 */
		private final long CANCEL_MARGIN_DELAY = (SHORT_DELAY * 3);

		private long timeB4Update;

		private long timerValidStart;

		MarkerUpdateTimer() {
		}

		void update() {
			long startTime = view.getLastUIRefreshTime();
			long currentTime = System.currentTimeMillis();
			long updateTimeGap = currentTime - startTime;
			// check if we can cancel a scheduled or a running update
			boolean cancelable = !(updateTimeGap > TIME_OUT);
			if (!cancelable) {
				cancelable = !isValidTimeOut(startTime, currentTime);
				if (timeB4Update != -1 && cancelable) {
					if (updateTimeGap < CANCEL_MARGIN_DELAY) {
						updateTimeGap = CANCEL_MARGIN_DELAY;
					}
				}
			}
			updateTimeGap = updateTimeGap % TIME_OUT;
			/* This is an optimization and may be removed.But, it
			 * is desirable that we schedule soon after a post-build.
			 */
			if (timeB4Update == -1) {
				// a Special Update request
				go(CANCEL_MARGIN_DELAY, true);
				return;
			}
			if (!workspaceBuilding()) {
				if (updateTimeGap + LONG_DELAY > TIME_OUT) {
					go(TIME_OUT - updateTimeGap, false);
				} else {
					go(LONG_DELAY, cancelable);
				}
			} else {
				// we are in build again
				if (updateTimeGap < LONG_DELAY) {
					go(TIME_OUT - updateTimeGap, cancelable);
				} else {
					go(TIME_OUT - updateTimeGap, false);
				}
			}
		}

		/**
		 * Checks if a time-out is valid,or if its just a period of inactivity.
		 * NOTE:This is PURELY an optimization and can be omitted.
		 */
		private boolean isValidTimeOut(long startTime, long currentTime) {
			//long updateTimeGap = currentTime - startTime;
			if (timeB4Update != -1 && startTime > timeB4Update) {
				/*
				 * The last scheduled update finished.This is not an actual
				 * TIME_OUT.Possible that we have not updated for a long
				 * interval.Lets make this update cancelable anyway.Reset timer.
				 */
				timerValidStart = currentTime;
				return false;
			} else if ((currentTime - timerValidStart) < (TIME_OUT)) {
				return false;
			} else {
				/*
				 * Do not update internal value we only use this for
				 * checking valid TIME_OUTs
				 */
				return true;
			}
		}
		
		/** 
		 * Schedules quickly if any update is pending,
		 * Or prepares for quick scheduling on next change
		 */
		void clearUpdates() {
			/* if we have a distant pending update
			 * schedule it with CANCEL_MARGIN_DELAY
			 */
			long diff = timeB4Update - System.currentTimeMillis();
			timeB4Update = -1;
			if (diff > CANCEL_MARGIN_DELAY) {
				update();
			} 
			/* Else wait for next change(Post-Change?), it 
			 * will be scheduled with CANCEL_MARGIN_DELAY
			 */
			timeB4Update = -1;
		}

		void go(long delay, boolean cancelPrevious) {
			timeB4Update = System.currentTimeMillis() + delay;
			scheduleUpdate(delay, cancelPrevious);
		}
	}
}
