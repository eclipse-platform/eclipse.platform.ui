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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;

/**
 * Job to refresh a subscriber with its remote state.
 * 
 * There can be several refresh jobs created but they will be serialized.
 */
public class RefreshSubscriberJob extends WorkspaceJob {
	
	/**
	 * Uniquely identifies this type of job. This is used for cancellation.
	 */
	private final static Object FAMILY_ID = new Object();

	/**
	 * If true this job will be restarted when it completes 
	 */
	private boolean reschedule = false;
	
	/**
	 * If true a rescheduled refresh job should be retarted when cancelled
	 */
	private boolean restartOnCancel = true; 

	/**
	 * The schedule delay used when rescheduling a completed job 
	 */
	private static long scheduleDelay = 20000; //5 /* minutes */ * (60 * 1000); 
	
	/**
	 * Time the job was run last in milliseconds.
	 */
	private long lastTimeRun = 0; 
	
	/**
	 * The subscribers and roots to refresh. If these are changed when the job
	 * is running the job is cancelled.
	 */
	private IResource[] resources;
	private TeamSubscriber subscriber;
	
	private class BatchSimilarSchedulingRule implements ISchedulingRule {
		public String id;
		public BatchSimilarSchedulingRule(String id) {
			this.id = id;
		}		
		public boolean isConflicting(ISchedulingRule rule) {
			if(rule instanceof BatchSimilarSchedulingRule) {
				return ((BatchSimilarSchedulingRule)rule).id.equals(id);
			}
			return false;
		}
		public boolean contains(ISchedulingRule rule) {
			return isConflicting(rule);
		}
	}
	
	public RefreshSubscriberJob(String name, IResource[] resources, TeamSubscriber subscriber) {
		super(name);
		
		this.resources = resources;
		this.subscriber = subscriber;
		
		setPriority(Job.DECORATE);
		setRule(new BatchSimilarSchedulingRule("org.eclipse.team.core.refreshsubscribers")); //$NON-NLS-1$
		
		addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if(shouldReschedule()) {
					if(event.getResult().getSeverity() == IStatus.CANCEL && ! restartOnCancel) {					
						return;
					}
					RefreshSubscriberJob.this.schedule(scheduleDelay);
					restartOnCancel = true;
				}
			}
		});
	}

	public boolean shouldRun() {
		return getSubscriber() != null;
	}
		
	public boolean belongsTo(Object family) {		
		return family == getFamily();
	}

	public static Object getFamily() {
		return FAMILY_ID;
	}
	
	/**
	 * This is run by the job scheduler. A list of subscribers will be refreshed, errors will not stop the job 
	 * and it will continue to refresh the other subscribers.
	 */
	public IStatus runInWorkspace(IProgressMonitor monitor) {		
		MultiStatus status = new MultiStatus(TeamUIPlugin.ID, TeamException.UNABLE, Policy.bind("RefreshSubscriberJob.0"), null); //$NON-NLS-1$
		TeamSubscriber subscriber = getSubscriber();
		IResource[] roots = getResources();
		
		// if there are no resources to refresh, just return
		if(subscriber == null || roots == null) {
			return Status.OK_STATUS;
		}
				
		monitor.beginTask(getTaskName(subscriber, roots), 100);
		try {
			lastTimeRun = System.currentTimeMillis();
			if(monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			try {					
				monitor.setTaskName(subscriber.getName());
				subscriber.refresh(roots, IResource.DEPTH_INFINITE, Policy.subMonitorFor(monitor, 100));
			} catch(TeamException e) {
				status.merge(e.getStatus());
			}
		} catch(OperationCanceledException e2) {
			return Status.CANCEL_STATUS;
		} finally {
			monitor.done();
		}
		return status.isOK() ? Status.OK_STATUS : (IStatus) status;
	}

	protected String getTaskName(TeamSubscriber subscriber, IResource[] roots) {
		// Don't return a task name as the job name contains the subscriber and resource count
		return null;
	}

	protected IResource[] getResources() {
		return resources;
	}

	protected TeamSubscriber getSubscriber() {
		return subscriber;
	}
	
	protected long getScheduleDelay() {
		return scheduleDelay;
	}

	/**
	 * Specify the interval in seconds at which this job is scheduled.
	 * @param seconds delay specified in seconds
	 */
	public void setRefreshInterval(long seconds) {
		scheduleDelay = seconds * 1000;
	}

	/**
	 * Returns the interval of this job in seconds. 
	 * @return
	 */
	public long getRefreshInterval() {
		return scheduleDelay / 1000;
	}

	public void setRestartOnCancel(boolean restartOnCancel) {
		this.restartOnCancel = restartOnCancel;
	}
	
	public void setReschedule(boolean reschedule) {
		this.reschedule = reschedule;
	}
	
	public boolean shouldReschedule() {
		return reschedule;
	}
	
	public long getLastTimeRun() {
		return lastTimeRun;
	}
}