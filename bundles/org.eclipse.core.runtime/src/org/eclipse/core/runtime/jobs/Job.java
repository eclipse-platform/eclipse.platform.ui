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

import org.eclipse.core.internal.jobs.InternalJob;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Jobs are units of runnable work that can be scheduled to be run with the job
 * manager.  Once a job has completed, it can be scheduled to run again (jobs are
 * reusable).
 * 
 * Jobs have a state that indicates what they are currently doing.  When constructed,
 * jobs start with a state value of <code>NONE</code>.  When a job is scheduled
 * to be run, it moves into the <code>WAITING</code> state.  When a job starts
 * running, it moves into the <code>RUNNING</code>.  When execution finishes
 * (either normally or through cancelation), the state changes back to 
 * <code>NONE</code>.  
 * 
 * A job can also be in the <code>SLEEPING</code> state.  This happens if a user
 * calls Job.sleep() on a waiting job, or if a job is scheduled to run after a specified
 * delay.  Only jobs in the <code>WAITING</code> state can be put to sleep.  
 * Sleeping jobs can be woken at any time using Job.wakeUp(), which will put the
 * job back into the <code>WAITING</code> state.
 * 
 * Jobs can be assigned a priority that is used as a hint about how the job should
 * be scheduled.  There is no guarantee that jobs of one priority will be run before
 * all jobs of lower priority.  The javadoc for the various priority constants provide
 * more detail about what each priority means.  By default, jobs start in the 
 * <code>LONG</code> priority class.
 * 
 * @see IJobManager
 * @since 3.0
 */
public abstract class Job extends InternalJob {
	
	/**
	 * The job status return value that is be used to indicate asynchronous
	 * job completion.
	 * @see Job#run
	 */
	public static final IStatus ASYNC_FINISH = new Status(IStatus.OK, Platform.PI_RUNTIME, 1, "", null);//$NON-NLS-1$

	/* Job priorities */
	/** 
	 * Job priority constant (value 10) for interactive jobs.
	 * Interactive jobs generally have priority over all other jobs.
	 * Interactive jobs must be relatively fast running in order to avoid
	 * blocking other interactive jobs from running.
	 * 
	 * @see IJobManager#getPriority
	 * @see IJobManager#setPriority
	 * @see Job#run
	 */
	public static final int INTERACTIVE = 10;
	/** 
	 * Job priority constant (value 20) for short background jobs.
	 * Short background jobs are jobs that typically complete within a second,
	 * but may take longer in some cases.  Short jobs are given priority
	 * over all other jobs except interactive jobs.
	 * 
	 * @see IJobManager#getPriority
	 * @see IJobManager#setPriority
	 * @see Job#run
	 */
	public static final int SHORT = 20;
	/** 
	 * Job priority constant (value 30) for long-running background jobs.
	 * 
	 * see IJobManager#getPriority
	 * @see IJobManager#setPriority
	 * @see Job#run
	 */
	public static final int LONG = 30;
	/** 
	 * Job priority constant (value 40) for build jobs.  Build jobs are
	 * generally run after all other background jobs complete.
	 * 
	 * @see IJobManager#getPriority
	 * @see IJobManager#setPriority
	 * @see Job#run
	 */
	public static final int BUILD = 40;

	/** 
	 * Job priority constant (value 50) for decoration jobs.
	 * Decoration jobs have lowest priority.  Decoration jobs generally
	 * compute extra information that the user may be interested in seeing
	 * but is generally not waiting for.
	 * 
	 * @see IJobManager#getPriority
	 * @see IJobManager#setPriority
	 * @see Job#run
	 */
	public static final int DECORATE = 50;
	/** 
	 * Job state code (value -1) indicating that a job is not 
	 * currently sleeping, waiting, or running (i.e., the job manager doesn't know 
	 * anything about the job). 
	 * 
	 * @see IJobManager#getState
	 */
	public static final int NONE = -1;
	/** 
	 * Job state code (value 3) indicating that a job is sleeping.
	 * 
	 * @see Job#run
	 * @see IJobManager#getState
	 */
	public static final int SLEEPING = 1;
	/** 
	 * Job state code (value 4) indicating that a job is waiting to be run.
	 * 
	 * @see IJobManager#getState
	 */
	public static final int WAITING = 2;
	/** 
	 * Job state code (value 3) indicating that a job is currently running
	 * 
	 * @see IJobManager#getState
	 */
	public static final int RUNNING = 3;

	/**
	 * Registers a job listener with this job
	 * Has no effect if an identical listener is already registered.
	 * 
	 * @param listener the listener to be added.
	 */
	public final void addJobChangeListener(IJobChangeListener listener) {
		super.addJobChangeListener(listener);
	}
	/**
	 * Returns whether this job belongs to the given family.  Job families are
	 * represented as strings that are not interpreted or specified in any way
	 * by the job manager.  Thus, a job can choose to belong to any number of
	 * families.
	 * 
	 * <p>Clients may override this method.  This default implementation always returns
	 * <code>false</code>.
	 * </p>
	 * 
	 * @return true if this job belongs to the given family, and false otherwise.
	 */
	public boolean belongsTo(String family) {
		return false;
	}
	/**
	 * Stops the job.  If the job is currently waiting,
	 * it will be removed from the queue.  If the job is sleeping,
	 * it will be discarded without having a chance to resume and its sleeping state
	 * will be cleared.  If the job is currently executing, it will be asked to
	 * stop but there is no guarantee that it will do so.
	 * 
	 * @return <code>false</code> if the job is currently running (and thus may not
	 * respond to cancelation), and <code>true</code> in all other cases.
	 */
	public final boolean cancel() {
		return super.cancel();
	}
	/**
	 * Jobs that complete their execution asynchronously must indicate when they
	 * are finished by calling this method.  This method has no effect if called on
	 * a job that has not indicated that it is executing asynchronously.
	 * 
	 * @param result a status object indicating the result of the job's execution.
	 * @see #run
	 */
	protected final void done(IStatus result) {
		super.done(result);
	}
	/**
	 * Returns the priority of this job.  The priority is used as a hint when the job
	 * is scheduled to be run.
	 * 
	 * @return the priority of the job.  One of INTERACTIVE, SHORT, LONG, BUILD, 
	 * 	or DECORATE.
	 */
	public final int getPriority() {
		return super.getPriority();
	}
	/**
	 * Returns the scheduling rule for this job.  Returns <code>null</code> if this job has no
	 * scheduling rule.
	 * 
	 * @return the scheduling rule for this job, or <code>null</code>.
	 * @see ISchedulingRule
	 */
	public final ISchedulingRule getRule() {
		return super.getRule();
	}
	/**
	 * Returns the state of the job. Result will be one of:
	 * <ul>
	 * <li>Job.RUNNING - if the job is currently running.</li>
	 * <li>Job.WAITING - if the job is waiting to be run.</li>
	 * <li>Job.SLEEPING - if the job is sleeping.</li>
	 * <li>Job.NONE - in all other cases.</li>
	 * </ul>
	 * Return the job state
	 */
	public final int getState() {
		return super.getState();
	}
	/**
	 * Removes a job listener from this job.
	 * Has no effect if an identical listener is not already registered.
	 * 
	 * @param listener the listener to be removed.
	 */
	public final void removeJobChangeListener(IJobChangeListener listener) {
		super.removeJobChangeListener(listener);
	}
	/**
	 * Executes the current job.  Returns the result of the execution.
	 * <p>
	 * The provided monitor can be used to report progress and respond to 
	 * cancellation.  If the progress monitor has been cancelled, the job
	 * should finish its execution at the earliest convenience. 
	 * <p>
	 * Jobs can optionally finish their execution asynchronously (in another thread) by 
	 * returning a result status of <code>Job.ASYNC_FINISH</code>.  Jobs that finish
	 * asynchronously <b>must</b> indicate when they are finished by calling
	 * the method <code>Job.finished</code>.
	 * 
	 * @param monitor the monitor to be used for reporting progress, or <code>
	 * null</code> if progress monitoring is not required.
	 * @return the job result.
	 * @see #ASYNC_FINISH
	 * @see #finish
	 */
	public abstract IStatus run(IProgressMonitor monitor);
	/**
	 * Schedules this job to be run.  The job is added to a queue of waiting
	 * jobs, and will be run when it arrives at the beginning of the queue.
	 * <p>
	 * This is a convenience method, fully equivalent to 
	 * <code>schedule(0L)</code>.
	 * </p>
	 * 
	 * @param job the job to add to the queue
	 */
	public final void schedule() {
		super.schedule(0L);
	}
	/**
	 * Schedules this job to be run after a specified delay.  After the specified delay,
	 * the job is added to a queue of waiting jobs, and will be run when it arrives at the 
	 * beginning of the queue.	
	 * <p>
	 * Scheduling a job that is already waiting, sleeping, or running has no effect
	 * 
	 * @param job the job to add to the queue
	 */
	public final void schedule(long delay) {
		super.schedule(delay);
	}
	/**
	 * Sets the priority of the job.  This will not affect the execution of
	 * a running job, but it will affect how the job is scheduled while
	 * it is waiting to be run.
	 * 
	 * @param priority the new job priority.  One of
	 * INTERACTIVE, SHORT, LONG, BUILD, or DECORATE.
	 */
	public final void setPriority(int i) {
		super.setPriority(i);
	}
	/**
	 * Sets the scheduling rule to be used when scheduling this job.  This method
	 * must be called before the job is scheduled.
	 * 
	 * @param rule the new scheduling rule, or <code>null</code> if the job
	 * should have no scheduling rule
	 */
	public final void setRule(ISchedulingRule rule) {
		super.setRule(rule);
	}
	/**
	 * Returns whether this job should be run.
	 * If <code>false</code> is returned, this job will be discarded by the job manager
	 * without running.
	 * 
	 * <p>This method is called immediately prior to calling the job's
	 * run method, so it can be used for last minute pre-condition checking before
	 * a job is run.</p>
	 * 
	 * <p>Clients may override this method.  This default implementation always returns
	 * <code>true</code>.
	 * </p>
	 */
	public boolean shouldRun() {
		return true;
	}
	/**
	 * Requests that this job be suspended.  If the job is currently waiting to be run, it 
	 * will be removed from the queue move into the <code>SLEEPING</code> state.
	 * The job will remain asleep until either resumed or canceled.  If this job is not
	 * currently waiting to be run, this method has no effect.
	 * 
	 * Sleeping jobs can be resumed using <code>wakeUp</code>.
	 * 
	 * @return false if the job is currently running (and thus cannot
	 * be put to sleep), and true in all other cases.
	 */
	public final boolean sleep() {
		return super.sleep();
	}
	/**
	 * Resumes execution of the given job.  If the job is not currently
	 * sleeping, this request is ignored.
	 * @param job
	 */
	public final void wakeUp() {
		super.wakeUp();
	}
}