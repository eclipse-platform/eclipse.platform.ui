/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.subscribers.Subscriber;

public class WorkingSetSyncSetInput extends SyncSetInputFromSyncSet {

	private SyncInfoWorkingSetFilter workingSetFilter = new SyncInfoWorkingSetFilter();
	
	public WorkingSetSyncSetInput(SubscriberSyncInfoSet set, SubscriberEventHandler handler) {
		super(set, handler);
		setFilter(workingSetFilter);
	}
	
	public void setWorkingSet(IResource[] resources) {
		workingSetFilter.setWorkingSet(resources);
	}
	
	public IResource[] getWorkingSet() {
		return workingSetFilter.getWorkingSet();
	}
	
	public IResource[] roots(Subscriber subscriber) {
		return workingSetFilter.getRoots(subscriber);
	}
}
