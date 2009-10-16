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

	private static final int NO_DELAY = 10;
	static final int SHORT_DELAY = 100;// The 100 ms short delay for scheduling
	static final int LONG_DELAY = 10000;
	static final long TIME_OUT = 30000;

	// The time the build started. A -1 indicates no build in progress.
	private long preBuildTime;
	
	private long lastUpdateTime=-1;

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
	void scheduleUpdate(long delay,boolean[] changeFlags) {
		scheduleUpdate(delay, true, changeFlags);
	}
	/**
	 * Schedule marker update.
	 */
	void scheduleUpdate(long delay, boolean cancelPrevious,
			boolean[] changeFlags) {
		if (cancelPrevious) {
			cancelQueuedUIUpdates(false);
			cancelUpdate(false);
		}
		indicateStatus(MarkerMessages.MarkerView_queueing_updates, true, false);
		builder.scheduleUpdateJob(delay, true, changeFlags);
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
	 * @param block
	 *            <code>true</code> return after showing
	 */
	void indicateStatus(String messsage, boolean block) {
		indicateStatus(messsage, false, block);
	}

	/**
	 * Indicate the status message on UI.
	 * 
	 * @param messsage
	 *            the status to display
	 * @param updateUI
	 *            <code>true</code> update label to show changing status
	 * @param block
	 *            <code>true</code> return after showing
	 */
	void indicateStatus(String messsage, boolean updateUI, boolean block) {
		view.indicateUpdating(messsage != null ? messsage
				: MarkerMessages.MarkerView_queueing_updates, updateUI, block);
	}

	/**
	 * Cancel any marker update if pending.
	 * 
	 * @param block
	 *            <code>true</code> return after showing
	 */
	void cancelUpdate(boolean block) {
		builder.cancelUpdate(block);
	}

	/**
	 * Cancel any UI update if pending.
	 * 
	 * @param block
	 *            <code>true</code> return after showing
	 */
	void cancelQueuedUIUpdates(boolean block) {
		view.cancelQueuedUpdates(block);
	}

	/**
	 * check if it is a good time to for update jobs to run now
	 */
	 boolean canUpdateNow() {
		// Hold off while everything is active
		if (preBuildTime > 0
				&& System.currentTimeMillis() - preBuildTime < TIME_OUT){
			return false;
		}
		if (preBuildTime > 0) {
			preBuildTime = System.currentTimeMillis();
		}
		return true;
	}

	/**
	 * Checks if the workspace is building
	 * 
	 * We are in a pre build state. Do not update until the post build happens.
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
				/*indicateStatus(MarkerMessages.MarkerView_waiting_on_changes,
						true, false);*/
				return;
			}
			if (event.getType() == IResourceChangeEvent.POST_BUILD) {
				postBuild();
				// the post change event should do the update for us
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
				 * point with the markers of interest in Workspace
				 */
				// unfortunately marker operations cannot be locked
				// so locking between gathering of markers and
				// marker deltas is not possible
				handleRemovedEntries(removed);
				handleChangedEntries(changed);
				handleAddedEntries(added);
			}
		}
		boolean[] changeFlags = new boolean[] { added.size() > 0,
				removed.size() > 0, changed.size() > 0 };
		scheduleUpdate(getScheduleDelay(), !workspaceBuilding(), changeFlags);
	}
	
	/**
	 * Calculate a schedule delay to optimize updating frequency
	 * @return delay
	 */
	private int getScheduleDelay() {
		if (lastUpdateTime == -1) {
			lastUpdateTime=System.currentTimeMillis();
			return SHORT_DELAY;
		}
		lastUpdateTime = view.getLastUIRefreshTime();
		lastUpdateTime = lastUpdateTime > builder.getLastUpdateTime() ? builder
				.getLastUpdateTime() : lastUpdateTime;
		if (lastUpdateTime == -1
				|| System.currentTimeMillis() - lastUpdateTime > TIME_OUT) {
			return NO_DELAY;
		}
		return LONG_DELAY;
	}

	/**
	 * Post build has happened. Let it all run.
	 */
	protected void postBuild() {
		preBuildTime = -1;
		//we want to update immediately
		lastUpdateTime=-1;
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

}
