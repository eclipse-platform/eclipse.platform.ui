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
package org.eclipse.team.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Policy;

/**
 * Job to refresh a subscriber with its remote state.
 * 
 * There can be several refresh jobs created but they will be serialized.
 */
public class RefreshSubscriberJob extends Job {
	
	protected final static boolean DEBUG = Policy.DEBUG_REFRESH_JOB;
	
	private TeamSubscriber subscriber;
	private IResource[] roots;
	
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
	
	protected RefreshSubscriberJob() {
		setPriority(Job.DECORATE);
		setRule(new BatchSimilarSchedulingRule("org.eclipse.team.core.refreshsubscribers"));
	}
	
	public RefreshSubscriberJob(TeamSubscriber subscriber, IResource[] roots) {
		this.subscriber = subscriber;
		this.roots = roots;
	}
	
	/**
	 * This is run by the job scheduler. A list of subscribers will be refreshed, errors will not stop the job 
	 * and it will continue to refresh the other subscribers.
	 */
	public IStatus run(IProgressMonitor monitor) {		
		monitor.beginTask(null, getSubscribers().length * 100);
		try {
			TeamSubscriber[] subscribers = getSubscribers();
			if(DEBUG) System.out.println("refreshJob: running with " + subscribers.length + " subscribers");
			for (int i = 0; i < subscribers.length; i++) {
				TeamSubscriber s = subscribers[i];
				if(monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				try {
					if(DEBUG) System.out.println("refreshJob: starting refresh for " + s.getName());
					s.refresh(getResources(s), IResource.DEPTH_INFINITE, Policy.subMonitorFor(monitor, 100));
					if(DEBUG) System.out.println("refreshJob: finished refresh for " + s.getName());
				} catch(TeamException e) {
					if(DEBUG) System.out.println("refreshJob: exception in refresh " + s.getName() + ":" + e.getMessage());
					//TeamPlugin.log(e);
					// keep going'
				}
			}
		} catch(OperationCanceledException e2) {
			if(DEBUG) System.out.println("refreshJob: run cancelled");
			return Status.CANCEL_STATUS;
		} finally {
			monitor.done();
		}
		if(DEBUG) System.out.println("refreshJob: run completed");
		return Status.OK_STATUS;
	}

	protected TeamSubscriber[] getSubscribers() {
		return new TeamSubscriber[] {subscriber};		
	}
	
	protected IResource[] getResources(TeamSubscriber s) {
		return roots;		
	}
}