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
import org.eclipse.team.core.subscribers.ITeamResourceChangeListener;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.ui.sync.views.SubscriberInput;

/**
 * Job to refresh a subscriber with its remote state.
 * 
 * There can be several refresh jobs created but they will be serialized.
 */
public class RefreshSubscriberJob extends Job implements ITeamResourceChangeListener {
	
	protected final static boolean DEBUG = Policy.DEBUG_REFRESH_JOB;		
	
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
	private boolean restartOnCancel = false; 

	/**
	 * The schedule delay used when rescheduling a completed job 
	 */
	private static long scheduleDelay = 20000; //5 /* minutes */ * (60 * 1000); 
	
	/**
	 * The subscribers and roots to refresh. If these are changed when the job
	 * is running the job is cancelled.
	 */
	private SubscriberInput input;
	
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
	}
	
	public RefreshSubscriberJob(String name) {
		super(name);
		setPriority(Job.DECORATE);
		setRule(new BatchSimilarSchedulingRule("org.eclipse.team.core.refreshsubscribers"));
		
		addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if(shouldReschedule()) {
					if(event.getResult().getSeverity() != IStatus.CANCEL || restartOnCancel) {					
						RefreshSubscriberJob.this.schedule(scheduleDelay);
						restartOnCancel = false;
					}
				}
			}
		});
	}

	public boolean shouldRun() {
		return input != null;
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
	public IStatus run(IProgressMonitor monitor) {		
		MultiStatus status = new MultiStatus(TeamPlugin.ID, TeamException.UNABLE, Policy.bind("Team.errorRefreshingSubscribers"), null);
		TeamSubscriber subscriber = input.getSubscriber();
		IResource[] roots = input.roots();		
		monitor.beginTask(Policy.bind("RefreshSubscriber.runTitle", subscriber.getName()), 100);
		try {
			TeamSubscriber[] subscribers = new TeamSubscriber[] {subscriber};
			for (int i = 0; i < subscribers.length; i++) {
				TeamSubscriber s = subscribers[i];
				if(monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				try {					
					monitor.setTaskName(Policy.bind("RefreshSubscriber.runTitleSubscriber", s.getName()));
					s.refresh(roots, IResource.DEPTH_INFINITE, Policy.subMonitorFor(monitor, 100));
				} catch(TeamException e) {
					status.merge(e.getStatus());
				}
			}
		} catch(OperationCanceledException e2) {
			return Status.CANCEL_STATUS;
		} finally {
			monitor.done();
		}
		return status.isOK() ? Status.OK_STATUS : (IStatus) status;
	}

	public void teamResourceChanged(TeamDelta[] deltas) {
		for (int i = 0; i < deltas.length; i++) {
			TeamDelta delta = deltas[i];
			if(delta.getFlags() == TeamDelta.SUBSCRIBER_DELETED) {
				// cancel current refresh just to make sure that the subscriber being deleted can
				// be properly shutdown
				cancel();
				setSubscriberInput(null);
			}
		}
	}
	
	public void setSubscriberInput(SubscriberInput input) {
		int state = getState();
		if(state == Job.RUNNING) {
			if(shouldReschedule()) {
				setRestartOnCancel(true);
			}
			cancel();
		}
		this.input = input;
	
		if(state == Job.NONE && input != null) {
			if(reschedule) {
				schedule(scheduleDelay);
			}
		}
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
}