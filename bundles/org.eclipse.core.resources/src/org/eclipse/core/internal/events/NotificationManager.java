/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import java.util.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

public class NotificationManager implements IManager, ILifecycleListener {
	class NotifyJob extends Job {
		private final IWorkspaceRunnable noop = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
			}
		};

		public NotifyJob() {
			super(ICoreConstants.MSG_RESOURCES_UPDATING);
			setSystem(true);
		}

		public IStatus run(IProgressMonitor monitor) {
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			notificationRequested = true;
			try {
				workspace.run(noop, null, IResource.NONE, null);
			} catch (CoreException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		}
	}

	private static final long NOTIFICATION_DELAY = 1500;
	/**
	 * The Threads that are currently avoiding notification.
	 */
	private Set avoidNotify = new HashSet();
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
	/**
	 * Indicates whether a notification is currently in progress. Used to avoid
	 * causing a notification to be requested as a result of another notification.
	 */
	protected boolean isNotifying;

	// if there are no changes between the current tree and the last delta state then we
	// can reuse the lastDelta (if any). If the lastMarkerChangeId is different then the current
	// one then we have to update that delta with new marker change info
	/**
	 * last delta we broadcast
	 */
	private ResourceDelta lastDelta;
	/**
	 * the marker change Id the last time we computed a delta
	 */
	private long lastDeltaId;
	/**
	 * tree the last time we computed a delta
	 */
	private ElementTree lastDeltaState;
	protected long lastNotifyDuration = 0L;
	/**
	 * the marker change id at the end of the last POST_AUTO_BUILD
	 */
	private long lastPostBuildId = 0;
	/**
	 * The state of the workspace at the end of the last POST_BUILD
	 * notification
	 */
	private ElementTree lastPostBuildTree;
	/**
	 * the marker change id at the end of the last POST_CHANGE
	 */
	private long lastPostChangeId = 0;
	/**
	 * The state of the workspace at the end of the last POST_CHANGE
	 * notification
	 */
	private ElementTree lastPostChangeTree;

	private ResourceChangeListenerList listeners;

	protected boolean notificationRequested = false;
	private Job notifyJob;
	Workspace workspace;

	public NotificationManager(Workspace workspace) {
		this.workspace = workspace;
		listeners = new ResourceChangeListenerList();
		notifyJob = new NotifyJob();
	}

	public void addListener(IResourceChangeListener listener, int eventMask) {
		listeners.add(listener, eventMask);
		EventStats.listenerAdded(listener);
	}

	/**
	 * Indicates the beginning of a block where periodic notifications should be avoided.
	 * Returns true if notification avoidance really started, and false for nested
	 * operations.
	 */
	public boolean beginAvoidNotify() {
		return avoidNotify.add(Thread.currentThread());
	}

	/**
	 * Signals the beginning of the notification phase at the end of a top level operation.
	 */
	public void beginNotify() {
		notifyJob.cancel();
		notificationRequested = false;
	}

	/**
	 * The main broadcast point for notification deltas 
	 */
	public void broadcastChanges(ElementTree lastState, int type, boolean lockTree) {
		try {
			// Do the notification if there are listeners for events of the given type.
			if (!listeners.hasListenerFor(type))
				return;
			isNotifying = true;
			ResourceDelta delta = getDelta(lastState, type);
			// if the delta is empty the root's change is undefined, there is nothing to do
			if (delta == null || delta.getKind() == 0)
				return;
			long start = System.currentTimeMillis();
			notify(getListeners(), new ResourceChangeEvent(workspace, type, delta), lockTree);
			lastNotifyDuration = System.currentTimeMillis() - start;
		} finally {
			// Update the state regardless of whether people are listening.
			isNotifying = false;
			cleanUp(lastState, type);
		}
	}

	/**
	 * Performs cleanup at the end of a resource change notification
	 */
	private void cleanUp(ElementTree lastState, int type) {
		// Remember the current state as the last notified state if requested.
		// Be sure to clear out the old delta
		boolean postChange = type == IResourceChangeEvent.POST_CHANGE;
		if (postChange || type == IResourceChangeEvent.POST_BUILD) {
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

	/**
	 * Helper method for the save participant lifecycle computation. */
	public void broadcastChanges(IResourceChangeListener listener, int type, IResourceDelta delta) {
		ResourceChangeListenerList.ListenerEntry[] entries;
		entries = new ResourceChangeListenerList.ListenerEntry[] {new ResourceChangeListenerList.ListenerEntry(listener, type)};
		notify(entries, new ResourceChangeEvent(workspace, type, delta), false);
	}

	/**
	 * Indicates the end of a block where periodic notifications should be avoided.
	 */
	public void endAvoidNotify() {
		avoidNotify.remove(Thread.currentThread());
	}

	/**
	 * Requests that a periodic notification be scheduled
	 */
	public void requestNotify() {
		//don't do intermediate notifications if the current thread doesn't want them
		if (isNotifying || avoidNotify.contains(Thread.currentThread()))
			return;
		//notifications must never take more than one tenth of operation time
		long delay = Math.max(NOTIFICATION_DELAY, lastNotifyDuration * 10);
		if (notifyJob.getState() == Job.NONE)
			notifyJob.schedule(delay);
	}

	/**
	 * Computes and returns the resource delta for the given event type and the 
	 * given current tree state.
	 */
	protected ResourceDelta getDelta(ElementTree tree, int type) {
		long id = workspace.getMarkerManager().getChangeId();
		// if we have a delta from last time and no resources have changed
		// since then, we can reuse the delta structure
		boolean postChange = type == IResourceChangeEvent.POST_CHANGE;
		if (lastDelta != null && !ElementTree.hasChanges(tree, lastDeltaState, ResourceComparator.getNotificationComparator(), true)) {
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
		return listeners.getListeners();
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

	private void notify(ResourceChangeListenerList.ListenerEntry[] resourceListeners, final IResourceChangeEvent event, final boolean lockTree) {
		int type = event.getType();
		boolean oldLock = workspace.isTreeLocked();
		if (lockTree)
			workspace.setTreeLocked(true);
		try {
			for (int i = 0; i < resourceListeners.length; i++) {
				if ((type & resourceListeners[i].eventMask) != 0) {
					final IResourceChangeListener listener = resourceListeners[i].listener;
					if (Policy.MONITOR_LISTENERS)
						EventStats.startNotify(listener);
					Platform.run(new ISafeRunnable() {
						public void handleException(Throwable e) {
							// exception logged in Platform#run
						}

						public void run() throws Exception {
							listener.resourceChanged(event);
						}
					});
					if (Policy.MONITOR_LISTENERS)
						EventStats.endNotify();
				}
			}
		} finally {
			if (lockTree)
				workspace.setTreeLocked(oldLock);
		}
	}

	public void removeListener(IResourceChangeListener listener) {
		listeners.remove(listener);
		EventStats.listenerRemoved(listener);
	}

	/**
	 * Returns true if a notification is needed. This happens if s
	 * ufficient time has elapsed since the last notification
	 * @return true if a notification is needed, and false otherwise
	 */
	public boolean shouldNotify() {
		return !isNotifying && notificationRequested;
	}

	public void shutdown(IProgressMonitor monitor) {
		//wipe out any existing listeners
		listeners = new ResourceChangeListenerList();
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