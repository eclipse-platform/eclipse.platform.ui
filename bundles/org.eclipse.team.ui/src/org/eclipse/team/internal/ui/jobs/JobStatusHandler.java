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
package org.eclipse.team.internal.ui.jobs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * This class is reponsible for notifying listeners when jobs registered
 * with the handler start and stop. Start is invoked when the first registered job starts
 * anf finish is invoked when the last registered job finishes.
 */
public class JobStatusHandler {
	
	private QualifiedName jobType;
	private Set jobs = new HashSet();
	private List listeners = new ArrayList();
	
	public JobStatusHandler(QualifiedName jobType) {
		super();
		this.jobType = jobType;
	}
	
	public void schedule(Job job) {
		job.addJobChangeListener(getJobChangeListener());
		// indicate that the job has started since it will be schdulued immediatley
		jobStarted(job);
		job.schedule();
	}

	public void schedule(Job job, long delay) {
		job.addJobChangeListener(getJobChangeListener());
		job.schedule(delay);
	}
	
	private JobChangeAdapter getJobChangeListener() {
		return new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				jobDone(event.getJob());

			}
			public void running(IJobChangeEvent event) {
				jobStarted(event.getJob());
			}
		};
	}

	public void addJobListener(IJobListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	public void removeJobListener(IJobListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private IJobListener[] getJobListeners() {
		synchronized (listeners) {
			return (IJobListener[]) listeners.toArray(new IJobListener[listeners.size()]);
		}
	}
	
	/* internal use only */ void jobStarted(Job job) {
		if (recordJob(job)) {
			fireStartNotification();
		}
	}

	/*
	 * Record the job and return true if it's the first job of that type
	 */
	private boolean recordJob(Job job) {
		if (!jobs.add(job)) {
			// The job was already in the set.
			return false;
		}
		return jobs.size() == 1;
	}

	/*
	 * Remove the job and return true if it is the last job for the type
	 */
	private boolean removeJob(Job job) {
		if (!jobs.remove(job)) {
			// The job wasn't in the list.
			return false;
		}
		return jobs.isEmpty();
	}
	
	private void fireStartNotification() {
		IJobListener[] listenerArray = getJobListeners();
		for (int i = 0; i < listenerArray.length; i++) {
			IJobListener listener = listenerArray[i];
			listener.started(jobType);
		}
	}

	/* internal use only */ void jobDone(Job job) {
		if (removeJob(job)) {
			fireEndNotification();
		}
	}

	private void fireEndNotification() {
		IJobListener[] listenerArray = getJobListeners();
		for (int i = 0; i < listenerArray.length; i++) {
			IJobListener listener = listenerArray[i];
			listener.finished(jobType);
		}
	}
	
	public boolean hasRunningJobs() {
		return !jobs.isEmpty();
	}

}
