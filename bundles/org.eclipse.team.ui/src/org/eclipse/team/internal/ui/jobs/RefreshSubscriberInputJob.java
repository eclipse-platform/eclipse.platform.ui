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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.subscribers.ITeamResourceChangeListener;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.sync.sets.SubscriberInput;

/**
 * Job to refresh a subscriber with its remote state.
 * 
 * There can be several refresh jobs created but they will be serialized.
 */
public class RefreshSubscriberInputJob extends RefreshSubscriberJob implements ITeamResourceChangeListener {
	
	/**
	 * The subscribers and roots to refresh. If these are changed when the job
	 * is running the job is cancelled.
	 */
	private SubscriberInput input;
	
	public RefreshSubscriberInputJob(String name) {
		super(name, null, null);
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
			cancel();
		}
		this.input = input;
	
		if(state == Job.NONE && input != null) {
			if(shouldReschedule()) {
				schedule(getScheduleDelay());
			}
		}
	}
		
	protected IResource[] getResources() {
		if(input != null) {
			return input.workingSetRoots();			
		}
		return null;
	}
	
	protected TeamSubscriber getSubscriber() {
		if(input != null) {
			return input.getSubscriber();
		}
		return null;
	}
}