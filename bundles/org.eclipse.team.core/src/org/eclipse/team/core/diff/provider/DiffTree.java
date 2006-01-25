/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.diff.provider;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.mapping.DiffChangeEvent;
import org.eclipse.team.internal.core.mapping.PathTree;
import org.eclipse.team.internal.core.subscribers.DiffTreeStatistics;

/**
 * Implementation of {@link IDiffTree}.
 * <p>
 * This class is not intended to be subclassed by clients
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public class DiffTree implements IDiffTree {
	
	private ListenerList listeners = new ListenerList();
	
	private PathTree pathTree = new PathTree();
	
	private ILock lock = Platform.getJobManager().newLock();
	
	private DiffTreeStatistics statistics = new DiffTreeStatistics();
	
	private DiffChangeEvent changes;

	private  boolean lockedForModification;

	/**
	 * Create an empty diff tree.
	 */
	public DiffTree() {
		resetChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDeltaTree#addSyncDeltaChangeListener(org.eclipse.team.core.synchronize.ISyncDeltaChangeListener)
	 */
	public void addDiffChangeListener(IDiffChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDeltaTree#removeSyncDeltaChangeListener(org.eclipse.team.core.synchronize.ISyncDeltaChangeListener)
	 */
	public void removeDiffChangeListener(IDiffChangeListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDeltaTree#accept(org.eclipse.core.runtime.IPath, org.eclipse.team.core.synchronize.ISyncDeltaVisitor)
	 */
	public void accept(IPath path, IDiffVisitor visitor, int depth)
			throws CoreException {
		IDiffNode delta = getDiff(path);
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
	public IDiffNode getDiff(IPath path) {
		return (IDiffNode)pathTree.get(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDeltaTree#getAffectedChildren(org.eclipse.core.runtime.IPath)
	 */
	public IPath[] getChildren(IPath path) {
		return pathTree.getChildren(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.delta.ISyncDeltaTree#isEmpty()
	 */
	public boolean isEmpty() {
		return pathTree.isEmpty();
	}

	/**
	 * Add the given {@link IDiffNode} to the tree. A change event will
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
	public void add(IDiffNode delta) {
		try {
			beginInput();
			boolean alreadyExists = getDiff(delta.getPath()) != null;
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
	 * Remove the given local resource from the set. A change event will
	 * be generated unless the call to this method is nested in between calls
	 * to <code>beginInput()</code> and <code>endInput(IProgressMonitor)</code>
	 * in which case the event for this removal and any other sync set
	 * change will be fired in a batched event when <code>endInput</code>
	 * is invoked.
	 * <p>
	 * Invoking this method outside of the above mentioned block will result
	 * in the <code>endInput(IProgressMonitor)</code> being invoked with a null
	 * progress monitor. If responsiveness is required, the client should always
	 * nest sync set modifications within <code>beginInput/endInput</code>.
	 * </p>
	 * 
	 * @param resource the local resource to remove
	 */
	public synchronized void remove(IPath path) {
		try {
			beginInput();
			IDiffNode delta = getDiff(path);
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
			statistics.clear();
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
		final DiffChangeEvent event;
		synchronized(this) {
			event = getChangeEvent();
			resetChanges();
		}
		if(event.isEmpty() && ! event.isReset()) return;
		Object[] listeners = this.listeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IDiffChangeListener listener = (IDiffChangeListener)listeners[i];
			Platform.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// don't log the exception....it is already being logged in Platform#run
				}
				public void run() throws Exception {
					try {
						lockedForModification = true;
						listener.diffChanged(event, Policy.subMonitorFor(monitor, 100));
					} finally {
						lockedForModification = false;
					}
				}
			});
		}
		monitor.done();
	}

	private DiffChangeEvent getChangeEvent() {
		return changes;
	}
	
	private void resetChanges() {
		changes = createEmptyChangeEvent();
	}

	private DiffChangeEvent createEmptyChangeEvent() {
		return new DiffChangeEvent(this);
	}

	private void internalAdd(IDiffNode delta) {
		Assert.isTrue(!lockedForModification);
		IDiffNode oldDiff = (IDiffNode)pathTree.get(delta.getPath());
		pathTree.put(delta.getPath(), delta);
		if(oldDiff == null) {
			statistics.add(delta);
		} else {
			statistics.remove(oldDiff);
			statistics.add(delta);
		}
	}
	
	private void internalRemove(IDiffNode delta) {
		Assert.isTrue(!lockedForModification);
		IDiffNode oldDiff = (IDiffNode)pathTree.get(delta.getPath());
		if(oldDiff == null) {
			statistics.remove(oldDiff);
		}
		pathTree.remove(delta.getPath());
	}
	
	private void internalAdded(IDiffNode delta) {
		changes.added(delta);
	}
	
	private void internalChanged(IDiffNode delta) {
		changes.changed(delta);
	}
	private void internalRemoved(IPath path, IDiffNode delta) {
		changes.removed(path, delta);
	}
	
	private void internalReset() {
		changes.reset();
	}

	/**
	 * Return the paths in this tree that contain diffs.
	 * @return the paths in this tree that contain diffs.
	 */
	public IPath[] getPaths() {
		return pathTree.getPaths();
	}
	
	/**
	 * Return all the diffs contained in this diff tree.
	 * @return all the diffs contained in this diff tree
	 */
	public IDiffNode[] getDiffs() {
		return (IDiffNode[]) pathTree.values().toArray(new IDiffNode[pathTree.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffTree#countFor(int, int)
	 */
	public long countFor(int state, int mask) {
		return statistics.countFor(state, mask);
	}

}
