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

import org.eclipse.core.internal.resources.IManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class NotificationManager implements IManager, ILifecycleListener {

	// if there are no changes between the current tree and the last delta state then we 
	// can reuse the lastDelta (if any).  If the lastMarkerChangeId is different then the current
	// one then we have to update that delta with new marker change info
	protected ResourceDelta lastDelta; // last delta we broadcast
	protected ElementTree lastDeltaState; // tree the last time we broadcast a change
	protected long lastMarkerChangeId = 0; // the marker change id the last time we broadcast
	/**
	 * The state of the workspace at the end of the last POST_BUILD notification
	 */
	protected ElementTree lastPostBuild;
	/**
	 * The state of the workspace at the end of the last POST_CHANGE notification
	 */
	protected ElementTree lastPostChange;
	protected ResourceChangeListenerList listeners;
	protected Workspace workspace;

	public NotificationManager(Workspace workspace) {
		this.workspace = workspace;
		listeners = new ResourceChangeListenerList();
	}
	public void addListener(IResourceChangeListener listener, int eventMask) {
		synchronized (listeners) {
			listeners.add(listener, eventMask);
		}
		EventStats.listenerAdded(listener);
	}
	/**
	 * The main broadcast point for notification deltas
	 */
	public void broadcastChanges(ElementTree lastState, int type, boolean lockTree) throws CoreException {
		// Do the notification if there are listeners for events of the given type.
		// Be sure to update the state if requested.  This needs to happen regardless of 
		// whether people are listening.
		try {
			IResourceDelta delta = null;
			if (listeners.hasListenerFor(type))
				delta = getDelta(lastState, type);
			// if the delta is empty the root's change is undefined, there is nothing to do
			if ((delta == null || delta.getKind() == 0))
				return;
			notify(getListeners(), new ResourceChangeEvent(workspace, type, delta), lockTree);
			// Remember the current state as the last notified state if requested.
			// Be sure to clear out the old delta
		} finally {
			if (type != IResourceChangeEvent.PRE_AUTO_BUILD) {
				workspace.getMarkerManager().resetMarkerDeltas();
				lastState.immutable();
				if (type == IResourceChangeEvent.POST_CHANGE)
					lastPostChange = lastState;
				else
					lastPostBuild = lastState;
				lastDelta = null;
				lastDeltaState = lastState;
				lastMarkerChangeId = 0;
			}
		}
	}
	/**
	 * Helper method for the save participant lifecycle computation.  
	 */
	public void broadcastChanges(IResourceChangeListener listener, int type, IResourceDelta delta) {
		ResourceChangeListenerList.ListenerEntry[] entries;
		entries = new ResourceChangeListenerList.ListenerEntry[] { new ResourceChangeListenerList.ListenerEntry(listener, type)};
		notify(entries, new ResourceChangeEvent(workspace, type, delta), false);
	}
	protected ResourceDelta getDelta(ElementTree tree, int type) {
		long id = workspace.getMarkerManager().getChangeId();
		// if we have a delta from last time and no resources have changed since then, we
		// can reuse the delta structure
		if (lastDelta != null && !ElementTree.hasChanges(tree, lastDeltaState, ResourceComparator.getComparator(true), true)) {
			// Markers may have changed since the delta was generated.  If so, get the new
			// marker state and insert it in to the delta which is being reused.
			if (id != lastMarkerChangeId)
				lastDelta.updateMarkers(workspace.getMarkerManager().getMarkerDeltas());
		} else {
			// We don't have a delta or something changed so recompute the whole deal.
			ElementTree oldTree = type == IResourceChangeEvent.POST_CHANGE ? lastPostChange : lastPostBuild;
			lastDelta = ResourceDeltaFactory.computeDelta(workspace, oldTree, tree, Path.ROOT, true);
		}
		// remember the state of the world when this delta was consistent
		lastMarkerChangeId = id;
		lastDeltaState = tree;
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
				//only notify deletion on move if old project handle is going away
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
							// don't log the exception....it is already being logged in Platform#run
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
	public void removeListener(IResourceChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
		EventStats.listenerRemoved(listener);
	}
	public void shutdown(IProgressMonitor monitor) {
	}
	public void startup(IProgressMonitor monitor) {
		// get the current state of the workspace as the starting point and
		// tell the workspace to track changes from there.  This gives the
		// notificaiton manager an initial basis for comparison.
		lastPostBuild = lastPostChange = workspace.getElementTree();
		workspace.addLifecycleListener(this);
	}
}