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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * This class is reponsible for notifying listeners when jobs registered
 * with the handler start and stop. Start is invoked when the first registered job starts
 * anf finish is invoked when the last registered job finishes.
 */
public class JobStatusHandler extends JobChangeAdapter {
	
	private static Map handlers = new HashMap();
	
	private QualifiedName jobType;
	private Set jobs = new HashSet();
	private List listeners = new ArrayList();
	
	/*
	 * Private class used to safely notify listeners of job type starts and
	 * finishes. Subclass override the notify(IJobListener) method to
	 * fire specific events inside an ISafeRunnable.
	 */
	private abstract class Notification implements ISafeRunnable {
		private IJobListener listener;
		public void handleException(Throwable exception) {
			// don't log the exception....it is already being logged in Platform#run
		}
		public void run(IJobListener listener) {
			this.listener = listener;
			Platform.run(this);
		}
		public void run() throws Exception {
			notify(listener);
		}
		/**
		 * Subsclasses overide this method to send an event safely to a lsistener
		 * @param listener
		 */
		protected abstract void notify(IJobListener listener);
	}
	
	/**
	 * Associate the job with the given jobType and schedule the job for 
	 * immediate start.
	 * @param job
	 * @param jobType
	 */
	public static void schedule(Job job, QualifiedName jobType) {
		JobStatusHandler handler;
		synchronized (handlers) {
			 handler = getHandler(jobType);
			if (handler == null) {
				handler = createHandler(jobType);
			}			
		}
		handler.schedule(job);
	}

	/**
	 * Add a listener for the given job type.
	 * @param listener
	 * @param jobType
	 */
	public static void addJobListener(IJobListener listener, QualifiedName jobType) {
		synchronized (handlers) {
			JobStatusHandler handler = getHandler(jobType);
			if (handler == null) {
				handler = createHandler(jobType);
			}
			handler.addJobListener(listener);
		}
	}

	/**
	 * Remove a previously registered listener for the given job type.
	 * @param listener
	 * @param jobType
	 */
	public static void removeJobListener(IJobListener listener, QualifiedName jobType) {
		synchronized (handlers) {
			JobStatusHandler handler = getHandler(jobType);
			if (handler != null) {
				handler.removeJobListener(listener);
				checkStatus(jobType, handler);
			}
		}
	}

	/**
	 * Return whether a job of the given type is currently running.
	 * @param jobType
	 * @return
	 */
	public static boolean hasRunningJobs(QualifiedName jobType) {
		JobStatusHandler handler = getHandler(jobType);
		if (handler != null) {
			return handler.hasRunningJobs();
		}
		return false;
	}
	
	private static JobStatusHandler getHandler(QualifiedName jobType) {
		return (JobStatusHandler)handlers.get(jobType);
	}
	
	private static JobStatusHandler createHandler(QualifiedName jobType) {
		JobStatusHandler existing = getHandler(jobType);
		if (existing != null) return existing;
		JobStatusHandler newHandler = new JobStatusHandler(jobType);
		handlers.put(jobType, newHandler);
		return newHandler;
	}
	
	/*
	 * Check whether the handler can be removed.
     */
	private static void checkStatus(QualifiedName jobType, JobStatusHandler handler) {
		synchronized (handlers) {
			if (handler.isEmpty()) {
				// If a handler has no jobs or listeners, remove it
				handlers.remove(jobType);
			}
		}
	}

	public JobStatusHandler(QualifiedName jobType) {
		super();
		this.jobType = jobType;
	}
	
	public void schedule(Job job) {
		job.addJobChangeListener(this);
		job.schedule();
	}
	
	public void done(IJobChangeEvent event) {
		jobDone(event.getJob());

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
	
	private void fireNotification(Notification notification) {
		// Get a snapshot of the listeners so the list doesn't change while we're firing
		IJobListener[] listenerArray;
		synchronized (listeners) {
			listenerArray = (IJobListener[]) listeners.toArray(new IJobListener[listeners.size()]);
		}
		// Notify each listener in a safe manner (i.e. so their exceptions don't kill us)
		for (int i = 0; i < listenerArray.length; i++) {
			IJobListener listener = listenerArray[i];
			notification.run(listener);
		}
	}
	
	private void jobStarted(Job job) {
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
		fireNotification(new Notification() {
			public void notify(IJobListener listener) {
				listener.started(getJobType());
			}
		});
	}

	private void jobDone(Job job) {
		if (removeJob(job)) {
			fireEndNotification();
			checkStatus(jobType, this);
		}
	}

	private void fireEndNotification() {
		fireNotification(new Notification() {
			public void notify(IJobListener listener) {
				listener.finished(getJobType());
			}
		});
	}
	
	public boolean hasRunningJobs() {
		return !jobs.isEmpty();
	}

	/*
	 * Return true if this hanlder has no jobs and no listeners
	 */
	private boolean isEmpty() {
		return listeners.isEmpty() && jobs.isEmpty();
	}
	/**
	 * @return Returns the jobType.
	 */
	public QualifiedName getJobType() {
		return jobType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void aboutToRun(IJobChangeEvent event) {
		jobStarted(event.getJob());
	}
}
