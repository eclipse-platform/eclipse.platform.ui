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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Policy;

/**
 * Job to periodically refresh the registered subscribers with their remote state. 
 * 
 * When the user explicitly requests a refresh the current background refreshes are
 * cancelled and the subscriber and resources that the user asked to refresh are processed.
 * Upon completion of the user initiated refresh, the scheduled background refreshes
 * will resume.
 */
public class RefreshSubscribersJob extends Job implements ITeamResourceChangeListener,IJobChangeListener {
	
	private final static boolean DEBUG = Policy.DEBUG_REFRESH_JOB;
	private static long REFRESH_DELAY = 10000; //5 /* minutes */ * (60 * 1000); 
	private Map subscribers = Collections.synchronizedMap(new  HashMap());
	
	public RefreshSubscribersJob() {
		TeamProvider.addListener(this);
		Platform.getJobManager().addJobChangeListener(this);
		if(! subscribers.isEmpty()) {
			if(DEBUG) System.out.println("refreshJob: starting job in constructor");
			schedule();
		}
	}
	
	synchronized public void refreshNow(IResource[] resources, TeamSubscriber subscriber) {
	}
	
	public IStatus run(IProgressMonitor monitor) {		
		monitor.beginTask("", subscribers.size() * 100);
		try {		
			for (Iterator it = subscribers.values().iterator(); it.hasNext();) {
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

	public void teamResourceChanged(TeamDelta[] deltas) {
		for (int i = 0; i < deltas.length; i++) {
			TeamDelta delta = deltas[i];
			if(delta.getFlags() == TeamDelta.SUBSCRIBER_CREATED) {				
				TeamSubscriber s = delta.getSubscriber();
				subscribers.put(s.getId(), s);
				if(DEBUG) System.out.println("refreshJob: adding subscriber " + s.getName());
				if(this.getState() == Job.NONE) {
					if(DEBUG) System.out.println("refreshJob: starting job after adding " + s.getName());
					schedule(REFRESH_DELAY);
				}				
			} else if(delta.getFlags() == TeamDelta.SUBSCRIBER_DELETED) {
				// cancel current refresh just to make sure that the subscriber being deleted can
				// be properly shutdown
				cancel();
				TeamSubscriber s = delta.getSubscriber();
				subscribers.remove(s.getId());
				if(DEBUG) System.out.println("refreshJob: removing subscriber " + s.getName());
				if(! subscribers.isEmpty()) {
					schedule();
				}
			}
		}
	}

	public void aboutToRun(Job job) {
	}

	public void awake(Job job) {
	}

	public void done(Job job, IStatus result) {
		if(job == this) {
			if(DEBUG) System.out.println("refreshJob: restarting job");
			schedule(REFRESH_DELAY);
		}
	}

	public void running(Job job) {
	}

	public void scheduled(Job job) {
	}

	public void sleeping(Job job) {
	}
}
