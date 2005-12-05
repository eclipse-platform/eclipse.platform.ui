/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.delta;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.team.core.delta.*;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.Policy;

/**
 * Implementation of {@link IDeltaTree}.
 */
public class DeltaTree implements IDeltaTree {
	
	private ListenerList listeners = new ListenerList();
	
	private PathTree pathTree = new PathTree();
	
	private ILock lock = Platform.getJobManager().newLock();
	
	DeltaChangeEvent changes;

	protected boolean lockedForModification;

	/**
	 * Create a sync delta set
	 */
	public DeltaTree() {
		resetChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDeltaTree#addSyncDeltaChangeListener(org.eclipse.team.core.synchronize.ISyncDeltaChangeListener)
	 */
	public void addSyncDeltaChangeListener(IDeltaChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDeltaTree#removeSyncDeltaChangeListener(org.eclipse.team.core.synchronize.ISyncDeltaChangeListener)
	 */
	public void removeSyncDeltaChangeListener(IDeltaChangeListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDeltaTree#accept(org.eclipse.core.runtime.IPath, org.eclipse.team.core.synchronize.ISyncDeltaVisitor)
	 */
	public void accept(IPath path, IDeltaVisitor visitor, int depth)
			throws CoreException {
		IDelta delta = getDelta(path);
		if (delta == null || visitor.visit(delta)) {
			if (depth == IResource.DEPTH_ZERO)
				return;
			IPath[] children = getChildren(path);
			for (int i = 0; i < children.length; i++) {
				IPath child = children[i];
				accept(child, visitor, depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDeltaTree#findMember(org.eclipse.core.runtime.IPath)
	 */
	public IDelta getDelta(IPath path) {
		return (IDelta)pathTree.get(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDeltaTree#getAffectedChildren(org.eclipse.core.runtime.IPath)
	 */
	public IPath[] getChildren(IPath path) {
		return pathTree.getChildren(path);
	}


	/**
	 * Add the given {@link IDelta} to the tree. A change event will
	 * be generated unless the call to this method is nested in between calls
	 * to <code>beginInput()</code> and <code>endInput(IProgressMonitor)</code>
	 * in which case the event for this addition and any other sync set
	 * change will be fired in a batched event when <code>endInput</code>
	 * is invoked.
	 * <p>
	 * Invoking this method outside of the above mentioned block will result
	 * in the <code>endInput(IProgressMonitor)</code> being invoked with a null
	 * progress monitor. If responsiveness is required, the client should always
	 * nest sync set modifications within <code>beginInput/endInput</code>.
	 * </p>
	 * @param delta the delta to be added to this set.
	 */
	public void add(IDelta delta) {
		try {
			beginInput();
			boolean alreadyExists = getDelta(delta.getPath()) != null;
			internalAdd(delta);
			if (alreadyExists) {
				internalChanged(delta);
			} else {
				internalAdded(delta);
			}
		} finally {
			endInput(null);
		}
	}

	/**
	 * Remove the given local resource from the set.
	 * 
	 * @param resource the local resource to remove
	 */
	public synchronized void remove(IPath path) {
		try {
			beginInput();
			IDelta delta = getDelta(path);
			if (delta != null) {
				internalRemove(delta);
				internalRemoved(path, delta);
			}
		} finally {
			endInput(null);
		}
	}

	/**
	 * Clear the contents of the set
	 */
	public synchronized void clear() {
		try {
			beginInput();
			pathTree.clear();
			internalReset();
		} finally {
			endInput(null);
		}
	}
	
	/**
	 * This method is used to obtain a lock on the set which ensures thread safety
	 * and batches change notification. If the set is locked by another thread, 
	 * the calling thread will block until the lock 
	 * becomes available. This method uses an <code>org.eclipse.core.runtime.jobs.ILock</code>.
	 * <p>
	 * It is important that the lock is released after it is obtained. Calls to <code>endInput</code>
	 * should be done in a finally block as illustrated in the following code snippet.
	 * <pre>
	 *   try {
	 *       set.beginInput();
	 *       // do stuff
	 *   } finally {
	 *      set.endInput(progress);
	 *   }
	 * </pre>
	 * </p><p>
	 * Calls to <code>beginInput</code> and <code>endInput</code> can be nested and must be matched.
	 * </p>
	 */
	public void beginInput() {
		lock.acquire();
	}

	/**
	 * This method is used to release the lock on this set. The progress monitor is needed to allow
	 * listeners to perform long-running operations is response to the set change. The lock is held
	 * while the listeners are notified so listeners must be cautious in order to avoid deadlock.
	 * @see #beginInput()
	 */
	public void endInput(IProgressMonitor monitor) {
		try {
			if (lock.getDepth() == 1) {
				// Remain locked while firing the events so the handlers 
				// can expect the set to remain constant while they process the events
				fireChanges(Policy.monitorFor(monitor));
			}
		} finally {
			lock.release();
		}
	}

	private void fireChanges(final IProgressMonitor monitor) {
		// Use a synchronized block to ensure that the event we send is static
		final DeltaChangeEvent event;
		synchronized(this) {
			event = getChangeEvent();
			resetChanges();
		}
		if(event.isEmpty() && ! event.isReset()) return;
		Object[] listeners = this.listeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IDeltaChangeListener listener = (IDeltaChangeListener)listeners[i];
			Platform.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// don't log the exception....it is already being logged in Platform#run
				}
				public void run() throws Exception {
					try {
						lockedForModification = true;
						if (event.isReset()) {
							listener.syncDeltaTreeReset(DeltaTree.this, Policy.subMonitorFor(monitor, 100));
						} else {
							listener.syncDeltaTreeChanged(event, Policy.subMonitorFor(monitor, 100));
						}
					} finally {
						lockedForModification = false;
					}
				}
			});
		}
		monitor.done();
	}

	private DeltaChangeEvent getChangeEvent() {
		return changes;
	}
	
	private void resetChanges() {
		changes = createEmptyChangeEvent();
	}

	private DeltaChangeEvent createEmptyChangeEvent() {
		return new DeltaChangeEvent(this);
	}

	private void internalAdd(IDelta delta) {
		Assert.isTrue(!lockedForModification);
		pathTree.put(delta.getPath(), delta);
	}
	
	private void internalRemove(IDelta delta) {
		Assert.isTrue(!lockedForModification);
		pathTree.remove(delta.getPath());
	}
	
	private void internalAdded(IDelta delta) {
		changes.added(delta);
	}
	
	private void internalChanged(IDelta delta) {
		changes.changed(delta);
	}
	private void internalRemoved(IPath path, IDelta delta) {
		changes.removed(path, delta);
	}
	
	private void internalReset() {
		changes.reset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.delta.ISyncDeltaTree#isEmpty()
	 */
	public boolean isEmpty() {
		return pathTree.isEmpty();
	}

}
