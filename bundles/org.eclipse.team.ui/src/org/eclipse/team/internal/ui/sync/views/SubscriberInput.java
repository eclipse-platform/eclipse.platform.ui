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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.sync.SyncInfoFilter;
import org.eclipse.ui.IWorkingSet;

/**
 * SubscriberInput encapsulates the UI model for synchronization changes associated
 * with a TeamSubscriber. 
 */
public class SubscriberInput implements IPropertyChangeListener {

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
	
	SubscriberInput(TeamSubscriber subscriber) {
		Assert.isNotNull(subscriber);
		subscriberInput = new SyncSetInputFromSubscriberWorkingSet(subscriber);
		filteredInput = new SyncSetInputFromSyncSet();		
		TeamUI.addPropertyChangeListener(this);
	}
	
	/*
	 * Initializes this input with the contents of the subscriber and installs the given set of filters. This
	 * is a long running operation.
	 */
	public void prepareInput(IProgressMonitor monitor) throws TeamException {
		monitor.beginTask(null, 100);
		try {			
			subscriberInput.initialize(Policy.subMonitorFor(monitor, 70));						
			filteredInput.setInputSyncSet(subscriberInput.getSyncSet(), Policy.subMonitorFor(monitor, 30));
		} finally {
			monitor.done();
		}
	}
	
	public TeamSubscriber getSubscriber() {
		return subscriberInput.getSubscriber();
	}
	
	public SyncSet getFilteredSyncSet() {
		return filteredInput.getSyncSet();
	}
	
	public SyncSet getSubscriberSyncSet() {
		return subscriberInput.getSyncSet();
	}

	public void setFilter(SyncInfoFilter filter, IProgressMonitor monitor) throws TeamException {
		filteredInput.setFilter(filter, monitor);
	}

	public void dispose() {
		subscriberInput.disconnect();
		filteredInput.disconnect();
		TeamUI.removePropertyChangeListener(this);		
	}

	public IWorkingSet getWorkingSet() {
		return subscriberInput.getWorkingSet();
	}

	public void setWorkingSet(IWorkingSet set) {
		subscriberInput.setWorkingSet(set);
	}

	public IResource[] roots() {
		return subscriberInput.getRoots();
	}

	/* (non-Javadoc)
	 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(TeamUI.GLOBAL_IGNORES_CHANGED)) {
			try {
				subscriberInput.reset(new NullProgressMonitor());
			} catch (TeamException e) {
				TeamUIPlugin.log(e);
			}
		}
	}	
}
