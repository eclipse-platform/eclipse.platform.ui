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

/**
 * Callback interface for clients interested in being notified when jobs change state.
 * <p>
 * A single job listener instance can be added either to the job manager, for notification
 * of all scheduled jobs, or to any set of individual jobs.  A single listener instance should
 * not be added to both the job manager, and to individual jobs (such a listener may
 * receive duplicate notifications).
 * </p>
 * 
 * @see JobChangeAdapter
 * @see IJobManager#addJobChangeListener(IJobChangeListener)
 * @see IJobManager#removeJobChangeListener(IJobChangeListener)
 * @see Job#addJobChangeListener(IJobChangeListener)
 * @see Job#removeJobChangeListener(IJobChangeListener)
 * @since 3.0
 */
public interface IJobChangeListener {
	/**
	 * Notification that a job is about to be run. Listeners are allowed to sleep, cancel, 
	 * or change the priority of the job before it is started (and as a result may prevent
	 * the run from actually occurring).
	 * 
	 * @param event the event details
	 */
	public void aboutToRun(IJobChangeEvent event);
	/**
	 * Notification that a job was previously sleeping and has now been rescheduled
	 * to run.
	 * 
	 * @param event the event details
	 */
	public void awake(IJobChangeEvent event);
	/**
	 * Notification that a job has completed execution, either due to cancelation, successful
	 * completion, or failure.  The event status object indicates how the job finished,
	 * and the reason for failure, if applicable.
	 * 
	 * @param event the event details
	 */
	public void done(IJobChangeEvent event);
	/**
	 * Notification that a job has started running.
	 * 
	 * @param event the event details
	 */
	public void running(IJobChangeEvent event);
	/**
	 * Notification that a job has been added to the queue of scheduled jobs.  
	 * The event details includes the scheduling delay before the event should start 
	 * running.
	 * 
	 * @param event the event details, including the job instance and the scheduling
	 * delay
	 */
	public void scheduled(IJobChangeEvent event);
	/**
	 * Notification that a job was waiting to run and has now been put in the
	 * sleeping state.
	 * 
	 * @param event the event details
	 */
	public void sleeping(IJobChangeEvent event);
}