package org.eclipse.core.internal.events;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.ElementTree;
import java.util.*;

public class NotificationManager implements IManager {
	protected ResourceChangeListenerList listeners;
	protected Workspace workspace;
	protected ElementTree oldState;

	public final static String extensionId = Platform.PI_RUNTIME + ".notification";
public NotificationManager(Workspace workspace) {
	this.workspace = workspace;
	listeners = new ResourceChangeListenerList();
}
public void addListener(IResourceChangeListener listener, int eventMask) {
	synchronized (listeners) {
		listeners.add(listener, eventMask);
	}
}
private void broadcastChanges(ResourceChangeListenerList.ListenerEntry[] resourceListeners, int type, IResourceDelta delta, boolean lockTree) {
	// if the delta is empty the root's change is undefined, there is nothing to do
	if (delta == null || delta.getKind() == 0)
		return;
	notify(resourceListeners, new ResourceChangeEvent(workspace, type, delta), lockTree);
}
public void broadcastChanges(IResourceChangeListener listener, int type, IResourceDelta delta, boolean lockTree) {
	ResourceChangeListenerList.ListenerEntry[] listeners;
	listeners = new ResourceChangeListenerList.ListenerEntry[] { new ResourceChangeListenerList.ListenerEntry(listener, type)};
	broadcastChanges(listeners, type, delta, lockTree);
}
public IResourceDelta broadcastChanges(IResourceDelta delta, ElementTree lastState, int type, boolean lockTree, boolean updateState) {
	try {
		// Do the notification if there are listeners for events of the given type.
		// Be sure to do all of this inside the try/finally as the finally will update the state 
		// if requested.  This needs to happen regardless of whether people are listening.
		if (listeners.hasListenerFor(type)) {
			delta = delta == null ? getDelta(lastState) : delta;
			broadcastChanges(getListeners(), type, delta, lockTree);
		}
	} finally {
		// Remember the current state as the last notified state if requested.
		// Even if there are problems during the notification there is no need to abort.
		if (updateState) {
			if (!lastState.isImmutable())
				lastState.immutable();
			oldState = lastState;
		}
	}
	return delta;
}
public void changing(IProject project) {
}
public void closing(IProject project) {
	if (!listeners.hasListenerFor(IResourceChangeEvent.PRE_CLOSE))
		return;
	notify(getListeners(), new ResourceChangeEvent(workspace, IResourceChangeEvent.PRE_CLOSE, project), true);
}
public void deleting(IProject project) {
	if (!listeners.hasListenerFor(IResourceChangeEvent.PRE_DELETE))
		return;
	notify(getListeners(), new ResourceChangeEvent(workspace, IResourceChangeEvent.PRE_DELETE, project), true);
}
protected ResourceDelta getDelta(ElementTree tree) {
	return ResourceDeltaFactory.computeDelta(workspace, oldState, tree, Path.ROOT, true);
}
protected ResourceChangeListenerList.ListenerEntry[] getListeners() {
	ResourceChangeListenerList.ListenerEntry[] result;
	synchronized (listeners) {
		result = listeners.getListeners();
	}
	return result;
}
private void notify(ResourceChangeListenerList.ListenerEntry[] resourceListeners, final IResourceChangeEvent event, boolean lockTree) {
	String message = Policy.bind("notification.2", null);
	int type = event.getType();
	for (int i = 0; i < resourceListeners.length; i++) {
		if ((type & resourceListeners[i].eventMask) != 0) {
			final IResourceChangeListener listener = resourceListeners[i].listener;
			ResourceStats.startNotify(listener.toString());
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.resourceChanged(event);
				}
				public void handleException(Throwable e) {
					//ResourceStats.notifyException(e);
					// don't log the exception....it is already being logged in Workspace#run
				}
			};
			boolean oldLock = workspace.isTreeLocked();
			if (lockTree)
				workspace.setTreeLocked(true);
			try {
				Platform.run(code);
			} finally {
				if (lockTree)
					workspace.setTreeLocked(oldLock);
			}
			ResourceStats.endNotify();
		}
	}
}
public void opening(IProject project) {
}
public void removeListener(IResourceChangeListener listener) {
	synchronized (listeners) {
		listeners.remove(listener);
	}
}
public void shutdown(IProgressMonitor monitor) {
}
public void startup(IProgressMonitor monitor) {
	// get the current state of the workspace as the starting point and
	// tell the workspace to track changes from there.  This gives the
	// notificaiton manager an initial basis for comparison.
	oldState = workspace.getElementTree();
}
}
