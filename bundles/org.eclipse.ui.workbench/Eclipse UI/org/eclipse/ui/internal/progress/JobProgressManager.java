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
package org.eclipse.ui.internal.progress;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;

/**
 * JobProgressManager provides the progress monitor to the 
 * job manager and informs any ProgressContentProviders of changes.
 */
public class JobProgressManager
	extends JobChangeAdapter
	implements IProgressProvider {

	private ArrayList providers = new ArrayList();
	private static JobProgressManager singleton;
	private Map jobs = Collections.synchronizedMap(new HashMap());
	private Collection updates = Collections.synchronizedSet(new HashSet());
	private boolean updateAll = false;
	boolean debug = false;

	/**
	 * Get the progress manager currently in use.
	 * @return JobProgressManager
	 */
	public static JobProgressManager getInstance() {
		if (singleton == null)
			singleton = new JobProgressManager();
		return singleton;
	}

	/**
	 * The JobMonitor is the inner class that handles the IProgressMonitor 
	 * integration with the ProgressMonitor.
	 */
	private class JobMonitor implements IProgressMonitor {
		Job job;
		boolean cancelled = false;

		/**
		 * Create a monitor on the supplied job.
		 * @param newJob
		 */
		JobMonitor(Job newJob) {
			job = newJob;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
		 */
		public void beginTask(String taskName, int totalWork) {
			if (isNonDisplayableJob(job))
				return;
			JobInfo info = getJobInfo(job);
			info.beginTask(taskName, totalWork);
			refreshViewers(info);
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#done()
		 */
		public void done() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
		 */
		public void internalWorked(double work) {
			worked((int) work);

		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
		 */
		public boolean isCanceled() {
			return cancelled;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
		 */
		public void setCanceled(boolean value) {
			cancelled = value;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
		 */
		public void setTaskName(String taskName) {
			if (isNonDisplayableJob(job))
				return;

			JobInfo info = getJobInfo(job);
			if (info.hasTaskInfo())
				info.setTaskName(taskName);
			else {
				beginTask(taskName, 100);
				return;
			}

			info.clearChildren();
			refreshViewers(info);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
		 */
		public void subTask(String name) {
			if (isNonDisplayableJob(job))
				return;
			if (name.length() == 0)
				return;
			JobInfo info = getJobInfo(job);

			info.clearChildren();
			info.addSubTask(name);
			refreshViewers(info);

		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
		 */
		public void worked(int work) {
			if (isNonDisplayableJob(job))
				return;

			JobInfo info = getJobInfo(job);
			if (info.hasTaskInfo()) {
				info.addWork(work);
				refreshViewers(info);
			}
		}
	}

	/**
	 * Create a new instance of the receiver.
	 */
	JobProgressManager() {
		Platform.getJobManager().setProgressProvider(this);
		Platform.getJobManager().addJobChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressProvider#createMonitor(org.eclipse.core.runtime.jobs.Job)
	 */
	public IProgressMonitor createMonitor(Job job) {
		return new JobMonitor(job);
	}

	/**
	 * Add a progress content provider to listen to the changes.
	 * @param provider
	 */
	void addProvider(ProgressContentProvider provider) {
		providers.add(provider);
	}

	/**
	 * Remove the supplied provider from the list of providers.
	 * @param provider
	 */
	void removeProvider(ProgressContentProvider provider) {
		providers.remove(provider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void scheduled(IJobChangeEvent event) {
		if (!isNonDisplayableJob(event.getJob())) {
			jobs.put(event.getJob(), new JobInfo(event.getJob()));
			refreshViewers(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void aboutToRun(IJobChangeEvent event) {
		if (!isNonDisplayableJob(event.getJob())) {
			JobInfo info = getJobInfo(event.getJob());
			info.setRunning();
			refreshViewers(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void done(IJobChangeEvent event) {
		if (!isNonDisplayableJob(event.getJob())) {
			if (event.getResult().getCode() == IStatus.ERROR) {
				JobInfo info = getJobInfo(event.getJob());
				info.setError(event.getResult());
			} else {
				jobs.remove(event.getJob());
			}
			refreshViewers(null);
		}

	}

	/**
		 * Get the JobInfo for the job. If it does not exist
		 * create it.
		 * @param job
		 * @return
		 */
	private JobInfo getJobInfo(Job job) {
		JobInfo info = (JobInfo) jobs.get(job);
		if (info == null) {
			info = new JobInfo(job);
			jobs.put(job, info);
		}
		return info;
	}

	/**
	 * Refresh the content providers as a result of a change in info.
	 * @param info
	 */
	public void refreshViewers(JobInfo info) {
		Iterator iterator = providers.iterator();
		while (iterator.hasNext()) {
			ProgressContentProvider provider =
				(ProgressContentProvider) iterator.next();
			provider.refreshViewer(info);
		}

	}

	/**
	 * Return whether or not this job is displayable.
	 * @param job
	 * @return
	 */
	private boolean isNonDisplayableJob(Job job) {
		if (job == null)
			return true;
		if(debug) //Always display in debug mode
			return false;
		else
			return job.isSystem() || job.getState() == Job.SLEEPING;
	}

	/**
	 * Get the jobs currently being held onto.
	 * @return Object[]
	 */
	public Object[] getJobs() {
		return jobs.values().toArray();
	}

	/**
	 * Clear the job out of the list of those being displayed.
	 * Only do this for jobs that are an error.
	 * @param job
	 */
	void clearJob(Job job) {
		JobInfo info = (JobInfo) jobs.get(job);
		if (info != null && info.getStatus().getCode() == IStatus.ERROR) {
			jobs.remove(job);
			refreshViewers(null);
		}
	}
}
