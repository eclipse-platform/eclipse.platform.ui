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
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * Job to periodically refresh the registered subscribers with their remote state. 
 */
public class RefreshAllRegisteredSubscribersJob extends RefreshSubscriberJob implements ITeamResourceChangeListener {
	
	private static long refreshInterval = 20000; //5 /* minutes */ * (60 * 1000); 
	private Map subscribers = Collections.synchronizedMap(new  HashMap());
	
	public RefreshAllRegisteredSubscribersJob() {
		super();
		TeamProvider.addListener(this);
		addJobChangeListener(new JobChangeAdapter() {
			public void done(Job job, IStatus result) {
				startup();
			}
		});
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
		if(DEBUG) System.out.println("refreshJob: scheduling job in " + (refreshInterval / 3600) + " seconds");
		schedule(refreshInterval);
	}
	
	protected IResource[] getResources(TeamSubscriber s) {
		try {
			return s.roots();
		} catch (TeamException e) {
			TeamPlugin.log(e);
		}
		return new IResource[0];
	}

	protected TeamSubscriber[] getSubscribers() {
		return (TeamSubscriber[]) subscribers.values().toArray(new TeamSubscriber[subscribers.size()]);
	}
}