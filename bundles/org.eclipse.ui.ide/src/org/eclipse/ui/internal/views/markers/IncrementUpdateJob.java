/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * The job that performs incremental update. Once the processing is complete it
 * schedules an UI update. If it'll be possible and beneficial switch to
 * incremental updatation, this has been left out for further investigation(*).
 * only, and not used currently.
 *
 * @since 3.6
 *
 */
class IncrementUpdateJob extends MarkerUpdateJob {

	private LinkedList<MarkerEntry> incrementEntryList;
	private LinkedList<MarkerUpdate> updateQueue;

	/**
	 * @param builder
	 */
	public IncrementUpdateJob(CachedMarkerBuilder builder) {
		super(builder);
		incrementEntryList = new LinkedList<>();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(MarkerMessages.MarkerView_processUpdates, IProgressMonitor.UNKNOWN);
		boolean clean= isClean();
		if (clean) {
			clearEntries();
		}
		Collection<MarkerEntry> markerEntries = incrementalEntries();
		if (clean) {
			/*
			 * Unfortunately we cannot lock marker operations between gathering
			 * and updation. We have this code in place only for further
			 * investigation
			 */
			clean = !clean(markerEntries, monitor);
			LinkedList<MarkerUpdate> queue = getUpdatesQueue();
			synchronized (queue) {
				queue.clear();
			}
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		if (!clean) {
			builder.registerTypesToListener();
			if (!processUpdates(monitor)) {
				return Status.CANCEL_STATUS;
			}
		}
		if (!processMarkerEntries(markerEntries, monitor)) {
			return Status.CANCEL_STATUS;
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		// update with sorted entries
		updateIncrementalList(markerEntries);

		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		builder.getUpdateScheduler().scheduleUIUpdate(MarkerUpdateScheduler.SHORT_DELAY);
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		builder.setBuilding(false);
		updateDone();
		return Status.OK_STATUS;
	}

	/**
	 * Update the list
	 *
	 * @param markerEntries
	 */
	private boolean updateIncrementalList(Collection<MarkerEntry> markerEntries) {
		markerEntries.clear();
		Markers clone = builder.getMarkers();
		synchronized (clone) {
			clone = clone.getClone();
		}
		for (MarkerEntry entry : clone.getMarkerEntryArray()) {
			markerEntries.add(entry);
		}
		return true;
	}

	/**
	 * Process the incremental updates
	 *
	 * @param monitor
	 */
	private boolean processUpdates(IProgressMonitor monitor) {
		Collection<MarkerEntry> markerEntries = incrementalEntries();
		int addCount = 0, removedCount = 0, changedCount = 0, size = 0, newSize = 0;
		LinkedList<MarkerUpdate> queue = getUpdatesQueue();
		MarkerUpdate next = null;
		do {
			synchronized (queue) {
				if (!queue.isEmpty()) {
					next = queue.removeFirst();
				} else {
					next = null;
				}
			}
			if (monitor.isCanceled() || next == null) {
				break;
			}
			/**
			 * The following performs incremental updation of the markers that
			 * were gathered intially, and keeps them synched at any point with
			 * the markers of interest in Workspace
			 */
			// unfortunately marker operations cannot be locked
			// so locking between gathering of markers and
			// marker deltas is not possible
			size = markerEntries.size();
			handleRemovedEntries(markerEntries, next.removed, monitor);
			newSize = markerEntries.size();
			removedCount += size - newSize;

			handleChangedEntries(markerEntries, next.changed, monitor);
			changedCount += next.changed.size();

			size = newSize;
			handleAddedEntries(markerEntries, next.added, monitor);
			newSize = markerEntries.size();
			removedCount += newSize - size;
		} while (next != null);
		boolean[] changeFlags = new boolean[] { addCount > 0, removedCount > 0,
				changedCount > 0 };
		for (boolean changeFlag : changeFlags) {
			if (changeFlag) {
				builder.updateChangeFlags(changeFlags);
				return true;
			}
		}
		return false;
	}

	/**
	 * @param added
	 * @param monitor
	 */
	private void handleAddedEntries(Collection<MarkerEntry> markerEntries, Collection<MarkerEntry> added,
			IProgressMonitor monitor) {
		MarkerContentGenerator generator = builder.getGenerator();
		Iterator<MarkerEntry> iterator = added.iterator();
		while (iterator.hasNext()) {
			MarkerEntry entry = iterator.next();
			if (generator.select(entry)) {
				markerEntries.add(entry);
			}
		}
	}

	/**
	 * @param changed
	 * @param monitor
	 */
	private void handleChangedEntries(Collection<MarkerEntry> markerEntries,
			Collection<MarkerEntry> changed, IProgressMonitor monitor) {
		MarkerContentGenerator generator = builder.getGenerator();
		Iterator<MarkerEntry> iterator = changed.iterator();
		while (iterator.hasNext()) {
			MarkerEntry entry = iterator.next();
			Iterator<MarkerEntry> iterator2 = markerEntries.iterator();
			while (iterator2.hasNext()) {
				MarkerEntry oldEntry = iterator2.next();
				if (oldEntry.getMarker().equals(entry.getMarker())) {
					iterator2.remove();
				}
			}
			if (!generator.select(entry)) {
				iterator.remove();
			}
		}
		markerEntries.addAll(changed);
	}

	/**
	 * @param removed
	 * @param monitor
	 */
	private void handleRemovedEntries(Collection<MarkerEntry> markerEntries,
			Collection<MarkerEntry> removed, IProgressMonitor monitor) {
		boolean found = false;
		Iterator<MarkerEntry> iterator = markerEntries.iterator();
		while (iterator.hasNext()) {
			MarkerEntry entry = iterator.next();
			found = entry.getStaleState();
			if (found) {
				iterator.remove();
			}
			if (removed.isEmpty()) {
				continue;
			}
			Iterator<MarkerEntry> iterator2 = removed.iterator();
			while (iterator2.hasNext()) {
				MarkerEntry stale = iterator2.next();
				if (stale.getMarker().equals(entry.getMarker())) {
					iterator2.remove();
					if (!found) {
						iterator.remove();
					}
					break;
				}
			}
		}
		if (removed.isEmpty()) {
			// TODO: do we check for residuals?
			return;
		}
		// TODO: do we check for residuals?
		iterator = markerEntries.iterator();
		while (iterator.hasNext()) {
			MarkerEntry entry = iterator.next();
			if (entry.getMarker() != null && !entry.getMarker().exists()) {
				iterator.remove();
			}
		}
	}

	/**
	 * Clean
	 */
	void clearEntries() {
		incrementEntryList = new LinkedList<>();
	}

	/**
	 * @return Returns the incrementEntryies.
	 */
	Collection<MarkerEntry> incrementalEntries() {
		return incrementEntryList;
	}

	/**
	 * @return the updateQueue that holds the updates and maintains ordering
	 */
	LinkedList<MarkerUpdate> getUpdatesQueue() {
		synchronized (builder.MARKER_INCREMENTAL_UPDATE_FAMILY) {
			if (updateQueue == null) {
				updateQueue = new LinkedList<>();
			}
			return updateQueue;
		}
	}

	/**
	 * Add update to the list
	 *
	 * @param update
	 */
	void addUpdate(MarkerUpdate update) {
		LinkedList<MarkerUpdate> updateList = getUpdatesQueue();
		synchronized (updateList) {
			updateList.addLast(update);
		}
	}

	@Override
	public boolean belongsTo(Object family) {
		if (family.equals(builder.MARKER_INCREMENTAL_UPDATE_FAMILY)) {
			return true;
		}
		return super.belongsTo(family);
	}
}