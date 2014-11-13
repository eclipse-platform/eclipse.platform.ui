/*******************************************************************************
 * Copyright (c) 2014 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thirumala Reddy Mutchukota - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.jobs;

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobGroup;

/**
 * Internal implementation class for job groups.
 *
 * @noextend This class is not intended to be extended by clients. All job groups
 * must be subclasses of the API <code>org.eclipse.core.runtime.jobs.JobGroup</code> class.
 */
public class InternalJobGroup {
	/**
	 * This lock will be held while performing state changes on this job group. It is
	 * also used as a notifier to wake up the threads waiting for this job group to complete.
	 *
	 * See also the lock ordering protocol explanation in JobManager's documentation.
	 *
	 * @GuardedBy("itself")
	 */
	Object jobGroupStateLock = new Object();

	private static final JobManager manager = JobManager.getInstance();

	private final String name;
	private final int maxThreads;

	private volatile int state = JobGroup.NONE;
	private volatile MultiStatus result;
	private Set<InternalJob> runningJobs = new HashSet<InternalJob>();
	private Set<InternalJob> otherActiveJobs = new HashSet<InternalJob>();
	private List<IStatus> results = new ArrayList<IStatus>();
	private boolean cancelingDueToError;
	private int failedJobsCount;
	private int canceledJobsCount;
	private int seedJobsCount;
	private int seedJobsRemainingCount;

	protected InternalJobGroup(String name, int maxThreads, int seedJobsCount) {
		Assert.isNotNull(name);
		Assert.isLegal(maxThreads >= 0);
		Assert.isLegal(seedJobsCount > 0);
		this.name = name;
		this.maxThreads = maxThreads;
		this.seedJobsCount = seedJobsCount;
		this.seedJobsRemainingCount = seedJobsCount;
	}

	/* (non-Javadoc)
	 * @see JobGroup#getName()
	 */
	protected String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see JobGroup#getMaxThreads()
	 */
	protected int getMaxThreads() {
		return maxThreads;
	}

	/* (non-Javadoc)
	 * @see JobGroup#getResult()
	 */
	protected MultiStatus getResult() {
		return result;
	}

	/* (non-Javadoc)
	 * @see JobGroup#getState()
	 */
	protected int getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see JobGroup#getActiveJobs()
	 */
	protected Job[] getActiveJobs() {
		return manager.find(this);
	}

	/* (non-Javadoc)
	 * @see JobGroup#cancel()
	 */
	protected void cancel() {
		manager.cancel(this);
	}

	/* (non-Javadoc)
	 * @see JobGroup#join(long, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected boolean join(long timeout, IProgressMonitor monitor) throws InterruptedException, OperationCanceledException {
		return manager.join(this, timeout, monitor);
	}

	/**
	 * Called by the JobManager when the state of a job belonging to this group has changed.
	 * Must be called from JobManager#updateJobGroup
	 *
	 * @param job a job belonging to this group
	 * @param oldState the old state of the job
	 * @param newState the new state of the job
	 * @GuardedBy("JobManager.lock")
	 */
	final void jobStateChanged(InternalJob job, int oldState, int newState) {
		switch (oldState) {
			case Job.NONE :
				break;
			case Job.SLEEPING :
			case Job.WAITING :
				otherActiveJobs.remove(job);
				break;
			case Job.RUNNING :
				runningJobs.remove(job);
				break;
			default :
				Assert.isLegal(false, "Invalid job state: " + job + ", state: " + oldState); //$NON-NLS-1$//$NON-NLS-2$
				break;
		}

		switch (newState) {
			case Job.NONE :
				break;
			case Job.SLEEPING :
			case Job.WAITING :
				otherActiveJobs.add(job);
				break;
			case Job.RUNNING :
				runningJobs.add(job);
				break;
			default :
				Assert.isLegal(false, "Invalid job state: " + job + ", state: " + newState); //$NON-NLS-1$//$NON-NLS-2$
				break;
		}

		/*
		 * We can determine if the job that is being scheduled is one of its job group's "seed"
		 * jobs by retrieving the currently running job for this thread. If there is a running
		 * job on this thread or that job's job group is the same as this job's job group, then
		 * this job is being scheduled in response to discovering more work, so it is not one
		 * of its group's seed jobs, otherwise it is.
		 */
		if (job.internalGetState() == InternalJob.ABOUT_TO_SCHEDULE && getGroupOfCurrentlyRunningJob() != job.getJobGroup()) {
			seedJobsRemainingCount--;
		}

		if (oldState == Job.RUNNING && newState == Job.NONE) {
			IStatus jobResult = job.getResult();
			Assert.isLegal(jobResult != null);
			if (cancelingDueToError && jobResult.getSeverity() == IStatus.CANCEL)
				return;

			results.add(jobResult);
			int jobResultSeverity = jobResult.getSeverity();
			if (jobResultSeverity == IStatus.ERROR) {
				failedJobsCount++;
			} else if (jobResultSeverity == IStatus.CANCEL) {
				canceledJobsCount++;
			}
		}
	}

	/**
	 * Returns the job group of the job currently running in this thread. Will return null if
	 * either this thread is not running a job or the job is not part of a job group.
	 */
	private JobGroup getGroupOfCurrentlyRunningJob() {
		Job job = manager.currentJob();
		return job == null ? null : job.getJobGroup();
	}

	/**
	 * Called by the JobManager to notify the group when the first job belonging
	 * to the group is scheduled to run. Must be called from JobManager#updateJobGroup.
	 *
	 * @GuardedBy("JobManager.lock")
	 * @GuardedBy("jobGroupStateLock")
	 */
	final void startJobGroup() {
		state = JobGroup.ACTIVE;
	}

	/**
	 * Called by the JobManager to signify that the group is getting canceled.
	 * Must be called from JobManager#updateJobGroup.
	 *
	 * @param cancelDueToError <code>true</code> if the group is getting canceled because
	 * the <code>shouldCancel(IStatus, int, int)</code> method returned <code>true</code>,
	 * <code>false</code> otherwise.
	 * @GuardedBy("JobManager.lock")
	 * @GuardedBy("jobGroupStateLock")
	 */
	final void cancelJobGroup(boolean cancelDueToError) {
		state = JobGroup.CANCELING;
		cancelingDueToError = cancelDueToError;
		if (!cancelDueToError) {
			// add a dummy cancel status to the results to make sure the combined status
			// will be of severity CANCEL.
			results.add(Status.CANCEL_STATUS);
		}
	}

	/**
	 * Called by the JobManager to notify the group when the last job belonging
	 * to the group has finished execution. Must be called from JobManager#updateJobGroup.
	 *
	 * @param groupResult the combined status of the job group
	 * @GuardedBy("JobManager.lock")
	 * @GuardedBy("jobGroupStateLock")
	 */
	final void endJobGroup(MultiStatus groupResult) {
		if (seedJobsRemainingCount > 0 && !groupResult.matches(IStatus.CANCEL))
			throw new IllegalStateException("Invalid initial jobs remaining count"); //$NON-NLS-1$
		state = JobGroup.NONE;
		result = groupResult;
		results.clear();
		cancelingDueToError = false;
		failedJobsCount = 0;
		canceledJobsCount = 0;
		seedJobsRemainingCount = seedJobsCount;
	}

	final Job[] internalGetActiveJobs() {
		Job[] activeJobs = new Job[runningJobs.size() + otherActiveJobs.size()];
		int i = 0;
		for (InternalJob job : runningJobs)
			activeJobs[i++] = (Job) job;
		for (InternalJob job : otherActiveJobs)
			activeJobs[i++] = (Job) job;
		return activeJobs;
	}

	/**
	 * Called by the JobManager when updating the job group status. Prevents the job group
	 * from entering the NONE state prematurely, which can happen if all scheduled jobs
	 * run to completion while the master thread still has more "seed" jobs to schedule.
	 *
	 * @return the count of initial jobs remaining to be scheduled
	 */
	final int getseedJobsRemainingCount() {
		return seedJobsRemainingCount;
	}

	final int getActiveJobsCount() {
		return runningJobs.size() + otherActiveJobs.size();
	}

	final int getRunningJobsCount() {
		return runningJobs.size();
	}

	final int getFailedJobsCount() {
		return failedJobsCount;
	}

	final int getCanceledJobsCount() {
		return canceledJobsCount;
	}

	final IStatus[] getCompletedJobResults() {
		return results.toArray(new IStatus[results.size()]);
	}

	/* (non-Javadoc)
	 * @see JobGroup#shouldCancel(IStatus, int, int)
	 */
	protected boolean shouldCancel(IStatus lastCompletedJobResult, int numberOfFailedJobs, int numberOfCanceledJobs) {
		return numberOfFailedJobs > 0;
	}

	/* (non-Javadoc)
	 * @see JobGroup#computeGroupResult(IStatus[])
	 */
	protected MultiStatus computeGroupResult(IStatus[] jobResults) {
		List<IStatus> importantResults = new ArrayList<IStatus>();
		for (IStatus jobResult : jobResults) {
			if (jobResult.getSeverity() != IStatus.OK)
				importantResults.add(jobResult);
		}
		if (importantResults.isEmpty())
			return new MultiStatus("org.eclipse.core.jobs", 0, name, null); //$NON-NLS-1$

		String pluginId = importantResults.get(0).getPlugin();
		IStatus[] groupResults = importantResults.toArray(new IStatus[importantResults.size()]);
		return new MultiStatus(pluginId, 0, groupResults, name, null);
	}
}
