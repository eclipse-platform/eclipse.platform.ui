/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import java.util.Map;

import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

public class NotificationManager implements IManager, ILifecycleListener {
	private static final long NOTIFICATION_DELAY = 1500;
	/**
	 * The marker change stamp that was last used to update the build marker
	 * deltas.
	 */
	private long buildMarkerChangeId;
	/**
	 * With background autobuild, the marker deltas from POST_CHANGE
	 * notifications must be accumulated so that they can be reused for the
	 * autobuild notifications.
	 */
	private Map buildMarkerDeltas;

	// if there are no changes between the current tree and the last delta
	// state then we
	// can reuse the lastDelta (if any). If the lastMarkerChangeId is different
	// then the current
	// one then we have to update that delta with new marker change info
	private ResourceDelta lastDelta; // last delta we broadcast
	private ElementTree lastDeltaState; // tree the last time
														  // we computed a
														  // delta
	private long lastDeltaId; //the marker change Id the last time
										// we computed a delta

	/**
	 * The state of the workspace at the end of the last POST_BUILD
	 * notification
	 */
	private ElementTree lastPostBuildTree;
	private long lastPostBuildId = 0; // the marker change id at
													// the end of the last
													// POST_AUTO_BUILD
	/**
	 * The state of the workspace at the end of the last POST_CHANGE
	 * notification
	 */
	private ElementTree lastPostChangeTree;
	private long lastPostChangeId = 0; // the marker change
														 // id at the end of
														 // the last
														 // POST_CHANGE

	private ResourceChangeListenerList listeners;
	private Workspace workspace;

	protected boolean notificationRequested = false;
	private Job notifyJob;
	protected long lastNotifyDuration = 0L;

	public NotificationManager(Workspace workspace) {
		this.workspace = workspace;
		listeners = new ResourceChangeListenerList();
		notifyJob = new Job(ICoreConstants.MSG_RESOURCES_UPDATING) {
			public IStatus run(IProgressMonitor monitor) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				notificationRequested = true;
				return Status.OK_STATUS;
			}
		};
		notifyJob.setSystem(true);
	}
	public void addListener(IResourceChangeListener listener, int eventMask) {
		synchronized (listeners) {
			listeners.add(listener, eventMask);
		}
		EventStats.listenerAdded(listener);
	}
	/**
	 * Indicates that a notification phase is beginning. */
	public void beginNotify() {
		notifyJob.cancel();
		notificationRequested = false;
	}
	/**
	 * The main broadcast point for notification deltas */
	public void broadcastChanges(ElementTree lastState, int type, boolean lockTree) throws CoreException {
		// Do the notification if there are listeners for events of the given type.
		// Update the state regardless of whether people are listening.
		ResourceDelta delta = null;
		try {
			if (listeners.hasListenerFor(type))
				delta = getDelta(lastState, type);
			// if the delta is empty the root's change is undefined, there is
			// nothing to do
			if ((delta == null || delta.getKind() == 0))
				return;
			long start = System.currentTimeMillis();
			notify(getListeners(), new ResourceChangeEvent(workspace, type, delta), lockTree);
			lastNotifyDuration = System.currentTimeMillis() - start;
		} finally {
			// Remember the current state as the last notified state if requested.
			// Be sure to clear out the old delta
			boolean postChange = type == IResourceChangeEvent.POST_CHANGE;
			if (postChange || type == IResourceChangeEvent.POST_AUTO_BUILD) {
				long id = workspace.getMarkerManager().getChangeId();
				lastState.immutable();
				if (postChange) {
					lastPostChangeTree = lastState;
					lastPostChangeId = id;
				} else {
					lastPostBuildTree = lastState;
					lastPostBuildId = id;
				}
				workspace.getMarkerManager().resetMarkerDeltas(Math.min(lastPostBuildId, lastPostChangeId));
				lastDelta = null;
				lastDeltaState = lastState;
			}
		}
	}
	/**
	 * Helper method for the save participant lifecycle computation. */
	public void broadcastChanges(IResourceChangeListener listener, int type, IResourceDelta delta) {
		ResourceChangeListenerList.ListenerEntry[] entries;
		entries = new ResourceChangeListenerList.ListenerEntry[] { new ResourceChangeListenerList.ListenerEntry(listener, type)};
		notify(entries, new ResourceChangeEvent(workspace, type, delta), false);
	}
	protected ResourceDelta getDelta(ElementTree tree, int type) {
		long id = workspace.getMarkerManager().getChangeId();
		// if we have a delta from last time and no resources have changed
		// since then, we can reuse the delta structure
		boolean postChange = type == IResourceChangeEvent.POST_CHANGE;
		if (lastDelta != null && !ElementTree.hasChanges(tree, lastDeltaState, ResourceComparator.getComparator(true), true)) {
			// Markers may have changed since the delta was generated. If so, get the new
			// marker state and insert it in to the delta which is being reused.
			if (id != lastDeltaId) {
				Map markerDeltas = workspace.getMarkerManager().getMarkerDeltas(lastPostBuildId);
				lastDelta.updateMarkers(markerDeltas);
			}
		} else {
			// We don't have a delta or something changed so recompute the whole deal.
			ElementTree oldTree = postChange ? lastPostChangeTree : lastPostBuildTree;
			long markerId = postChange ? lastPostChangeId : lastPostBuildId;
			lastDelta = ResourceDeltaFactory.computeDelta(workspace, oldTree, tree, Path.ROOT, markerId + 1);
		}
		// remember the state of the world when this delta was consistent
		lastDeltaState = tree;
		lastDeltaId = id;
		return lastDelta;
	}
	protected ResourceChangeListenerList.ListenerEntry[] getListeners() {
		ResourceChangeListenerList.ListenerEntry[] result;
		synchronized (listeners) {
			result = listeners.getListeners();
		}
		return result;
	}
	public void handleEvent(LifecycleEvent event) {
		switch (event.kind) {
			case LifecycleEvent.PRE_PROJECT_CLOSE :
				if (!listeners.hasListenerFor(IResourceChangeEvent.PRE_CLOSE))
					return;
				IProject project = (IProject) event.resource;
				notify(getListeners(), new ResourceChangeEvent(workspace, IResourceChangeEvent.PRE_CLOSE, project), true);
				break;
			case LifecycleEvent.PRE_PROJECT_MOVE :
				//only notify deletion on move if old project handle is going
				// away
				if (event.resource.equals(event.newResource))
					return;
				//fall through
			case LifecycleEvent.PRE_PROJECT_DELETE :
				if (!listeners.hasListenerFor(IResourceChangeEvent.PRE_DELETE))
					return;
				project = (IProject) event.resource;
				notify(getListeners(), new ResourceChangeEvent(workspace, IResourceChangeEvent.PRE_DELETE, project), true);
				break;
		}
	}
	private void notify(ResourceChangeListenerList.ListenerEntry[] resourceListeners, final IResourceChangeEvent event, boolean lockTree) {
		int type = event.getType();
		for (int i = 0; i < resourceListeners.length; i++) {
			if ((type & resourceListeners[i].eventMask) != 0) {
				final IResourceChangeListener listener = resourceListeners[i].listener;
				if (Policy.MONITOR_LISTENERS)
					EventStats.startNotify(listener);
				boolean oldLock = workspace.isTreeLocked();
				if (lockTree)
					workspace.setTreeLocked(true);
				try {
					Platform.run(new ISafeRunnable() {
						public void handleException(Throwable e) {
							//ResourceStats.notifyException(e);
							// don't log the exception....it is already being
							// logged in Platform#run
						}
						public void run() throws Exception {
							listener.resourceChanged(event);
						}
					});
				} finally {
					if (lockTree)
						workspace.setTreeLocked(oldLock);
				}

				if (Policy.MONITOR_LISTENERS)
					EventStats.endNotify();
			}
		}
	}
	public void endOperation() {
		//notifications must never take more than one tenth of operation time
		long delay = Math.max(NOTIFICATION_DELAY, lastNotifyDuration * 10);
		if (notifyJob.getState() == Job.NONE)
			notifyJob.schedule(delay);
	}
	public void removeListener(IResourceChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
		EventStats.listenerRemoved(listener);
	}
	public boolean shouldNotify() {
		return notificationRequested;
	}
	public void shutdown(IProgressMonitor monitor) {
	}
	public void startup(IProgressMonitor monitor) {
		// get the current state of the workspace as the starting point and
		// tell the workspace to track changes from there. This gives the
		// notification manager an initial basis for comparison.
		lastPostBuildTree = lastPostChangeTree = workspace.getElementTree();
		workspace.addLifecycleListener(this);
	}
	/**
	 * Build delta listeners need to receive marker deltas that are accumulated
	 * over several post change notifications. This method keeps the set of
	 * marker deltas for auto-build deltas up to date.
	 * @param newDeltas the most recently computed marker deltas
	 * @param changeId the generation id of this new set of marker deltas
	 */
	protected void updateMarkerDeltas(Map newDeltas, long changeId) {
		//just return if we have already seen these changes
		if (changeId == buildMarkerChangeId)
			return;
		buildMarkerChangeId = changeId;
		buildMarkerDeltas = MarkerDelta.merge(buildMarkerDeltas, lastDelta.getDeltaInfo().getMarkerDeltas());
	}
}