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
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.sync.SyncInfoFilter;
import org.eclipse.ui.IWorkingSet;

/**
 * SubscriberInput encapsulates the UI model for synchronization changes associated
 * with a TeamSubscriber. 
 */
public class SubscriberInput {

	/*
	 * The subscriberInput manages a sync set that contains all of the out-of-sync elements
	 * of a subscriber.  
	 */
	private SyncSetInputFromSubscriberWorkingSet subscriberInput;
	
	/*
	 * The filteredInput manages a sync set that contains a filtered list of the out-of-sync
	 * elements from another sync set. This is an optimization to allow filters to be applied
	 * to the subscriber input and is the input for a UI model.
	 */
	private SyncSetInputFromSyncSet filteredInput;
	
	/*
	 * The subscriber 
	 */
	private TeamSubscriber subscriber;
	
	SubscriberInput(TeamSubscriber subscriber) {
		this.subscriber = subscriber;
		subscriberInput = new SyncSetInputFromSubscriberWorkingSet();
		filteredInput = new SyncSetInputFromSyncSet(); 
	}
	
	/*
	 * Initializes this input with the contents of the subscriber and installs the given set of filters. This
	 * is a long running operation.
	 */
	public void prepareInput(IProgressMonitor monitor) throws TeamException {
		monitor.beginTask(null, 100);
		try {			
			subscriberInput.setSubscriber(getSubscriber(), Policy.subMonitorFor(monitor, 70));						
			filteredInput.setInputSyncSet(subscriberInput.getSyncSet(), Policy.subMonitorFor(monitor, 30));
		} finally {
			monitor.done();
		}
	}
	
	public TeamSubscriber getSubscriber() {
		return subscriber;
	}
	
	public SyncSet getSyncSet() {
		return filteredInput.getSyncSet();
	}
	
	public void setFilter(SyncInfoFilter filter, IProgressMonitor monitor) throws TeamException {
		filteredInput.setFilter(filter, monitor);
	}

	public void dispose() {
		subscriberInput.disconnect();
		filteredInput.disconnect();		
	}

	public IWorkingSet getWorkingSet() {
		return subscriberInput.getWorkingSet();
	}

	public void setWorkingSet(IWorkingSet set) {
		subscriberInput.setWorkingSet(set);
	}

	public IResource[] roots() throws TeamException {
		return subscriberInput.getRoots();
	}
	
	public SyncSet getSubscriberInputSyncSet() {
		return subscriberInput.getSyncSet();
	}
}
