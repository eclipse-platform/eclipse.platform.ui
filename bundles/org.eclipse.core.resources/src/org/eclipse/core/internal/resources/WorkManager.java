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
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;

/**
 * Used to track operation state for each thread that is involved in an operation.
 * This includes prepared and running operation depth, auto-build strategy and
 * cancel state.
 */
class WorkManager implements IManager {
	/**
	 * Scheduling rule for use during resource change notification.  This
	 * rule must always be allowed to nest within a resource rule of any granularity
	 * since it is used from within the scope of all resource changing operations.
	 */
	class NotifyRule implements ISchedulingRule {
		public boolean contains(ISchedulingRule rule) {
			return (rule instanceof IResource) || rule.getClass().equals(NotifyRule.class);
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return contains(rule);
		}
	}
	/**
	 * Indicates whether any operations have run that may require a build.
	 */
	private boolean hasBuildChanges = false;
	private IJobManager jobManager;
	private final ILock lock;
	private int nestedOperations = 0;
	protected NotifyRule notifyRule = new NotifyRule();
	private boolean operationCanceled = false;
	private int preparedOperations = 0;

	public WorkManager(Workspace workspace) {
		this.jobManager = Platform.getJobManager();
		this.lock = jobManager.newLock();
	}
	/**
	 * Begins a resource change notification.
	 */
	public void beginNotify() {
		jobManager.beginRule(notifyRule);
	}
	/**
	 * Releases the workspace lock without changing the nested operation depth.
	 * Must be followed eventually by endUnprotected.  Any 
	 * beginUnprotected/endUnprotected pair must be done entirely within the scope
	 * of a checkIn/checkOut pair.  Returns the old lock depth.
	 * @see endUnprotected
	 */
	public int beginUnprotected() {
		int depth = lock.getDepth();
		for (int i = 0; i < depth; i++)
			lock.release();
		return depth;
	}
	/**
	 * An operation calls this method and it only returns when the operation
	 * is free to run.
	 */
	public void checkIn(ISchedulingRule rule) {
		try {
			jobManager.beginRule(rule);
		} finally {
			//must increment regardless of failure because checkOut is always in finally
			lock.acquire();
			incrementPreparedOperations();
		}
	}
	/**
	 * Inform that an operation has finished.
	 */
	public synchronized void checkOut(ISchedulingRule rule) throws CoreException {
		decrementPreparedOperations();
		rebalanceNestedOperations();
		//reset state if this is the end of a top level operation
		if (preparedOperations == 0) {
			operationCanceled = false;
			hasBuildChanges = false;
		}
		lock.release();
		jobManager.endRule(rule);
	}
	/**
	 * This method can only be safelly called from inside a workspace
	 * operation. Should NOT be called from outside a 
	 * prepareOperation/endOperation block.
	 */
	private void decrementPreparedOperations() {
		preparedOperations--;
	}
	public void endNotify() {
		jobManager.endRule(notifyRule);
	}
	/**
	 * Re-acquires the workspace lock that was temporarily released during an
	 * operation, and restores the old lock depth.
	 * @see beginUnprotected
	 */
	public void endUnprotected(int depth) {
		for (int i = 0; i < depth; i++)
			lock.acquire();
	}
	/**
	 * This method can only be safelly called from inside a workspace
	 * operation. Should NOT be called from outside a 
	 * prepareOperation/endOperation block.
	 */
	public synchronized int getPreparedOperationDepth() {
		return preparedOperations;
	}

	/**
	 * This method can only be safelly called from inside a workspace
	 * operation. Should NOT be called from outside a 
	 * prepareOperation/endOperation block.
	 */
	void incrementNestedOperations() {
		nestedOperations++;
	}
	/**
	 * This method can only be safelly called from inside a workspace
	 * operation. Should NOT be called from outside a 
	 * prepareOperation/endOperation block.
	 */
	private void incrementPreparedOperations() {
		preparedOperations++;
	}
	/**
	 * Returns true if the nested operation depth is the same
	 * as the prepared operation depth, and false otherwise.
	 *
	 * This method can only be safelly called from inside a workspace
	 * operation. Should NOT be called from outside a 
	 * prepareOperation/endOperation block.
	 */
	boolean isBalanced() {
		return nestedOperations == preparedOperations;
	}
	/**
	 * This method can only be safelly called from inside a workspace
	 * operation. Should NOT be called from outside a 
	 * prepareOperation/endOperation block.
	 */
	public void operationCanceled() {
		operationCanceled = true;
	}
	/**
	 * Used to make things stable again after an operation has failed between
	 * a workspace.prepareOperation() and workspace.beginOperation().
	 * 
	 * This method can only be safelly called from inside a workspace
	 * operation. Should NOT be called from outside a 
	 * prepareOperation/endOperation block.
	 */
	public void rebalanceNestedOperations() {
		nestedOperations = preparedOperations;
	}
	/**
	 * Indicates if the operation that has just completed may potentially 
	 * require a build.
	 */
	public void setBuild(boolean hasChanges) {
		hasBuildChanges = hasBuildChanges || hasChanges;
	}

	/**
	 * This method can only be safely called from inside a workspace operation.
	 * Should NOT be called from outside a prepareOperation/endOperation block.
	 */
	public boolean shouldBuild() {
		if (hasBuildChanges) {
			if (operationCanceled)
				return Policy.buildOnCancel;
			return true;
		}
		return false;
	}
	public void shutdown(IProgressMonitor monitor) {
	}
	public void startup(IProgressMonitor monitor) {
	}
	/**
	 * Returns true if the workspace lock has already been acquired by this thread,
	 * and false otherwise.
	 */
	public boolean isLockAlreadyAcquired() {
		boolean result = false;
		try {
			boolean success = lock.acquire(0L);
			if (success) {
				//if lock depth is greater than one, then we already owned it before
				result = lock.getDepth() > 1;
				lock.release();
			}
		} catch (InterruptedException e) {
		}
		return result;
	}
}