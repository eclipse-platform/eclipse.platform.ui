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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Policy;

/**
 * Job to periodically refresh the registered subscribers with their remote state. 
 * 
 * When the user explicitly requests a refresh the current background refreshes are
 * cancelled and the subscriber and resources that the user asked to refresh are processed.
 * Upon completion of the user initiated refresh, the scheduled background refreshes
 * will resume.
 * 
 * There can be several refresh jobs created but they will be serialized.
 * 
 * [Note: this job currently updates all roots of every subscriber. It may be better to have API 
 * to specify a more constrained set of resources and subscribers to refresh.] 
 */
public class RefreshSubscribersJob extends Job implements ITeamResourceChangeListener {
	
	private boolean rescheduled  = false;
	private final static boolean DEBUG = Policy.DEBUG_REFRESH_JOB;
	private static long refreshInterval = 20000; //5 /* minutes */ * (60 * 1000); 
	
	private Map subscribers = Collections.synchronizedMap(new  HashMap());
	
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
	
	public RefreshSubscribersJob() {
		TeamProvider.addListener(this);
		addJobChangeListener(new JobChangeAdapter() {
			public void done(Job job, IStatus result) {
				if(rescheduled) {
					startup();
				}
			}
		});
		setPriority(Job.DECORATE);
		setRule(new BatchSimilarSchedulingRule("org.eclipse.team.core.refreshsubscribers"));
	}
	
	/**
	 * Specify the interval in seconds at which this job is scheduled.
	 * @param seconds delay specified in seconds
	 */
	synchronized public void setRefreshInterval(long seconds) {
		refreshInterval = seconds * 1000;
	}
	
	/**
	 * Returns the interval of this job in seconds. 
	 * @return
	 */
	synchronized public long getRefreshInterval() {
		return refreshInterval / 1000;
	}
	
	/**
	 * This is run by the job scheduler. A list of subscribers will be refreshed, errors will not stop the job 
	 * and it will continue to refresh the other subscribers.
	 */
	public IStatus run(IProgressMonitor monitor) {		
		monitor.beginTask("", subscribers.size() * 100);
		try {		
			Iterator it = subscribers.values().iterator();
			while (it.hasNext()) {
				if(monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				TeamSubscriber s = (TeamSubscriber) it.next();
				try {
					if(DEBUG) System.out.println("refreshJob: starting refresh for " + s.getName());
					s.refresh(s.roots(), IResource.DEPTH_INFINITE, Policy.subMonitorFor(monitor, 100));
					if(DEBUG) System.out.println("refreshJob: finished refresh for " + s.getName());
				} catch(TeamException e) {
					if(DEBUG) System.out.println("refreshJob: exception in refresh " + s.getName() + ":" + e.getMessage());
					//TeamPlugin.log(e);
					// keep going'
				}
			}
		} catch(OperationCanceledException e2) {
			return Status.CANCEL_STATUS;
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	/**
	 * This job will update its list of subscribers to refresh based on the create/delete 
	 * subscriber events. 
	 * 
	 * If a new subscriber is created it will be added to the list of subscribers
	 * to refresh and the job will be started if it isn't already.
	 * 
	 * If a subscriber is deleted, the job is cancelled to ensure that the subscriber being 
	 * deleted can be properly shutdown. After removing the subscriber from the list the
	 * job is restarted if there are any subscribers left.  
	 */
	public void teamResourceChanged(TeamDelta[] deltas) {
		for (int i = 0; i < deltas.length; i++) {
			TeamDelta delta = deltas[i];
			if(delta.getFlags() == TeamDelta.SUBSCRIBER_CREATED) {				
				TeamSubscriber s = delta.getSubscriber();
				subscribers.put(s.getId(), s);
				if(DEBUG) System.out.println("refreshJob: adding subscriber " + s.getName());
				if(this.getState() == Job.NONE) {
					startup();
				}				
			} else if(delta.getFlags() == TeamDelta.SUBSCRIBER_DELETED) {
				// cancel current refresh just to make sure that the subscriber being deleted can
				// be properly shutdown
				cancel();
				TeamSubscriber s = delta.getSubscriber();
				subscribers.remove(s.getId());
				if(DEBUG) System.out.println("refreshJob: removing subscriber " + s.getName());
				if(! subscribers.isEmpty()) {
					startup();
				}
			}
		}
	}

	protected void startup() {
		if(DEBUG) System.out.println("refreshJob: scheduling job");
		schedule(refreshInterval);
	}

	/**
	 * @param b
	 */
	public void setRescheduled(boolean rescheduled) {
		this.rescheduled = rescheduled;		
	}
}