/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The job manager provides facilities for scheduling, querying, and maintaining jobs
 * and locks.  In particular, the job manager provides the following services:
 * <ul>
 * <li>Maintains a queue of jobs that are waiting to be run.  Items can be added to
 * the queue using the <code>schedule</code> method.</li>
 * <li>Allows manipulation of groups of jobs called job families.  Job families can
 * be canceled, put to sleep, or woken up atomically.  There is also a mechanism
 * for querying the set of known jobs in a given family.</li>
 * <li>Allows listeners to find out about progress on running jobs, and to find out
 * when jobs have changed states.</li>
 * <li>Provides a factory for creating lock objects.  Lock objects are smart monitors
 * that have strategies for avoiding deadlock.</li>
 * <li>Provide feedback to a client that is waiting for a given job or family of jobs
 * to complete.</li>
 * </ul>
 * 
 * @see Job
 * @see ILock
 * @since 3.0
 */
public interface IJobManager {
	/**
	 * Registers a job listener with the job manager.  
	 * Has no effect if an identical listener is already registered.
	 * 
	 * @param listener the listener to be added.
	 */
	public void addJobChangeListener(IJobChangeListener listener);
	/**
	 * Stops all jobs in the given job family.  Jobs in the family that are currently waiting
	 * will be removed from the queue.  Sleeping jobs will be discarded without having 
	 * a chance to wake up.  Currently executing jobs will be asked to cancel but there 
	 * is no guarantee that they will do so.
	 * 
	 * @param family the name of the job family to cancel
	 * @see Job#belongsTo(String)
	 */
	public void cancel(String family);
	/**
	 * Returns the job that is currently running in this thread, or null if there
	 * is no currently running job.
	 */
	public Job currentJob();
	/**
	 * Returns all waiting, executing and sleeping jobs belonging
	 * to the given family. 
	 * 
	 * If no jobs are found, an empty array is returned.
	 * 
	 * @param family the name of the job family to find
	 * @see Job#belongsTo(String)
	 */
	public Job[] find(String family);
	/**
	 * Creates a new lock object.  All lock objects supplied by the job manager
	 * know about each other and will always avoid circular deadlock amongst
	 * themselves.
	 * 
	 * @return the new lock object
	 */
	public ILock newLock();

	/**
	 * Removes a job listener from the job manager.  
	 * Has no effect if an identical listener is not already registered.
	 * 
	 * @param listener the listener to be removed.
	 */
	public void removeJobChangeListener(IJobChangeListener listener);
	/**
	 * Provides a hook that is notified whenever a thread is about to wait on a lock,
	 * or when a thread is about to release a lock.  This hook must only be set once.
	 * <p>
	 * This method is for internal use by the platform-related plug-ins.  
	 * Clients should not call this method.
	 * </p>
	 */
	public void setLockListener(ILockListener listener);
	/**
	 * Registers a progress provider with the job manager.  If there was a
	 * provider already registered, it is replaced.
	 * <p>
	 * This method is for internal use by the platform-related plug-ins.  
	 * Clients should not call this method.
	 * </p>
	 * 
	 * @param provider the new provider, or <code>null</code> if no progress
	 * is needed.
	 */
	public void setProgressProvider(IProgressProvider provider);
	/**
	 * Requests that all jobs in the given job family be suspended.  Jobs currently 
	 * waiting to be run will be removed from the queue and moved into the 
	 * <code>SLEEPING</code> state.  Jobs that have been put to sleep
	 * will remain in that state until either resumed or canceled.  This method has
	 * no effect on jobs that are not currently waiting to be run.
	 * 
	 * Sleeping jobs can be resumed using <code>wakeUp</code>.
	 * 
	 * @param family the name of the job family to put to sleep
	 * @see Job#belongsTo(String)
	 */
	public void sleep(String family);
	/**
	 * Waits until a job is finished.  This method will block the calling thread until the 
	 * job has finished executing.  Feedback on how the wait is progressing is provided 
	 * to a progress monitor.  If the job is not currently waiting, sleeping,
	 * or running, this method returns immediately.
	 * 
	 * <p>
	 * If the calling thread owns any locks, the locks may be released during the
	 * wait if necessary to prevent deadlock.  On return from the wait, the calling
	 * thread will once again have exclusive control of any locks that were owned
	 * upon entering the wait.
	 * </p>
	 * 
	 * @param job the job to wait for
	 * @param monitor Progress monitor for reporting progress on how the
	 * wait is progressing, or null if no progress monitoring is required.
	 * @exception InterruptedException if this thread is interrupted while waiting
	 * @see ILock
	 */
	public void wait(Job job, IProgressMonitor monitor) throws InterruptedException;
	/**
	 * Waits until all jobs of the given family are finished. 
	 * If a family of <code>null</code> is specified, waits until all waiting
	 * and executing jobs are finished.  This method will block the calling 
	 * thread until all such jobs have finished executing.  Feedback on how 
	 * the wait is progressing is provided to the given progress monitor.
	 * 
	 * <p>
	 * If the calling thread owns any locks, the locks may be released during the
	 * wait if necessary to prevent deadlock.  On return from the wait, the calling
	 * thread will once again have exclusive control of any locks that were owned
	 * upon entering the wait.
	 * </p>
	 * <p>
	 * Warning: this method can result in starvation of the current thread if
	 * another thread continues to add jobs of the given family.
	 * </p>
	 * 
	 * @param family the name of the job family to wait for
	 * @param monitor Progress monitor for reporting progress on how the
	 * wait is progressing, or null if no progress monitoring is required.
	 * @exception InterruptedException if this thread is interrupted while waiting
	 * @see Job#belongsTo(String)
	 */
	public void wait(String family, IProgressMonitor monitor) throws InterruptedException;
	/**
	 * Resumes scheduling of all sleeping jobs in the given family.  This method
	 * has no effect on jobs in the family that are not currently sleeping.
	 * @see Job#belongsTo(String)
	 */
	public void wakeUp(String family);

}