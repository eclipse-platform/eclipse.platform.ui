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

import java.util.Hashtable;

import org.eclipse.core.internal.jobs.OrderedLock;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
/**
 * Used to track operation state for each thread that is involved in an operation.
 * This includes prepared and running operation depth, auto-build strategy and
 * cancel state.
 */
public class WorkManager implements IManager {
	/**
	 * Each thread that is modifying the workspace must have a job associated
	 * with it.  If clients call workspace changing API from outside a job, we
	 * must create an implicit one.  This object also stores the operation depth
	 * for a particular job, so it is known when "done" should be called on the job.
	 */
	class ThreadJob extends Job {
		// the operation depth within this thread
		int depth;
		private boolean running = false;
		ThreadJob() {
			super(""); //$NON-NLS-1$
			setSystem(true);
			setPriority(INTERACTIVE);
			depth = 1;
		}
		int decrement() {
			return --depth;
		}
		void increment() {
			depth++;
		}
		/**
		 * Block the calling thread until the job starts running
		 */
		synchronized void joinRun() {
			while (!running) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}
		public IStatus run(IProgressMonitor monitor) {
			synchronized (this) {
				running = true;
				notifyAll();
			}
			return Job.ASYNC_FINISH;
		}
	}
	private static final String MSG_VIOLATE_RULE = "IWorkspaceRunnable is attempting to modify a portion of the workspace that violates its scheduliing rule"; //$NON-NLS-1$
	public static final int OPERATION_EMPTY = 0;
	public static final int OPERATION_NONE = -1;
	/**
	 * Indicates whether any operations have run that may require a build.
	 */
	private boolean hasBuildChanges = false;
	private final Hashtable jobs = new Hashtable(20);
	private final OrderedLock lock;
	private int nestedOperations = 0;
	private boolean operationCanceled = false;
	private int preparedOperations = 0;
	private final ISchedulingRule rootRule;

	public WorkManager(Workspace workspace) {
		this.lock = (OrderedLock) Platform.getJobManager().newLock();
		this.rootRule = new ResourceSchedulingRule(workspace.getRoot());
	}
	private void assertCompatible(ISchedulingRule runningRule, ISchedulingRule waitingRule) {
		if (runningRule == waitingRule)
			return;
		IResource[] waiting = extractResources(waitingRule);
		//if the waiting job has no resource rule, there can be no conflict
		if (waitingRule == null || waiting == null)
			return;
		//if the running job has no resource rule, then any rule on the waiting job is a conflict
		IResource[] running = extractResources(runningRule);
		if (runningRule == null || running == null) {
			Assert.isLegal(false, MSG_VIOLATE_RULE);
			return;
		}
		//all waiting rules must be children of running rules
		for (int i = 0; i < waiting.length; i++) {
			boolean foundParent = false;
			for (int j = 0; j < running.length && !foundParent; j++)
				foundParent = running[j].getFullPath().isPrefixOf(waiting[i].getFullPath());
			Assert.isLegal(foundParent, MSG_VIOLATE_RULE);
		}
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
			lock.acquire();
			checkInJob(rule);
		} finally {
			//must increment regardless of failure because checkOut is always in finally
			incrementPreparedOperations();
		}
	}
	/**
	 * Ensures that the job running in this thread is compatible with the
	 * rule.  Creates an implicit job for this thread if necessary.
	 */
	private void checkInJob(ISchedulingRule rule) {
		ThreadJob running = (ThreadJob) jobs.get(Thread.currentThread());
		if (running == null) {
			//create an implicit job for this thread
			running = new ThreadJob();
			jobs.put(Thread.currentThread(), running);
			//run the implicit job if there is no real job running
			Job realJob = Platform.getJobManager().currentJob();
			if (realJob == null) {
				running.setRule(rule);
				running.schedule();
				running.joinRun();
				return;
			}
			//use the real job rule if it has a resource rule
			running.setRule(hasResourceRule(realJob) ? realJob.getRule() : rule);
		} else {
			running.increment();
		}
		assertCompatible(running.getRule(), rule);
	}
	/**
	 * Returns true if the given job has any resource rule, and false otherwise.
	 */
	private boolean hasResourceRule(Job realJob) {
		ISchedulingRule realRule = realJob.getRule();
		if (realRule == null)
			return false;
		if (realRule instanceof MultiRule) {
			ISchedulingRule[] children = ((MultiRule) realRule).getChildren();
			for (int i = 0; i < children.length; i++)
				if (rootRule.isConflicting(children[i]))
					return true;
			return false;
		}
		return rootRule.isConflicting(realRule);
	}
	private void checkOutJob() {
		ThreadJob running = (ThreadJob) jobs.get(Thread.currentThread());
		if (running.decrement() == 0) {
			running.done(Status.OK_STATUS);
			jobs.remove(Thread.currentThread());
		}
	}
	/**
	 * Inform that an operation has finished.
	 */
	public synchronized void checkOut() throws CoreException {
		decrementPreparedOperations();
		rebalanceNestedOperations();
		checkOutJob();
		//reset state if this is the end of a top level operation
		if (preparedOperations == 0) {
			operationCanceled = false;
			hasBuildChanges = false;
		}
		lock.release();
	}
	/**
	 * This method can only be safelly called from inside a workspace
	 * operation. Should NOT be called from outside a 
	 * prepareOperation/endOperation block.
	 */
	private void decrementPreparedOperations() {
		preparedOperations--;
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
	 * Returns the resources associated with the given scheduling rule,
	 * or null if there are no resource rules.
	 */
	private IResource[] extractResources(ISchedulingRule rule) {
		if (rule == null)
			return null;
		if (rule instanceof ResourceSchedulingRule)
			return new IResource[] {((ResourceSchedulingRule) rule).getResource()};
		if (rule instanceof MultiRule) {
			ISchedulingRule[] children = ((MultiRule) rule).getChildren();
			for (int i = 0; i < children.length; i++) {

			}
		}
		return null;
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
	 * This method is synchronized with checkIn() and checkOut() that use blocks
	 * like synchronized (this) { ... }.
	 */
	public synchronized boolean isCurrentOperation() {
		return lock.getCurrentOperationThread() == Thread.currentThread();
	}
	protected ISchedulingRule newSchedulingRule(IResource resource) {
		if (resource.getFullPath().isRoot())
			return rootRule;
		return new ResourceSchedulingRule(resource);
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
	public void setBuild(boolean hasChanges)  {
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
}