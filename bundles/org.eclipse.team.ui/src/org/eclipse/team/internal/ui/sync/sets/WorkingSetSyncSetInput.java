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
package org.eclipse.team.internal.ui.sync.sets;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.ui.IWorkingSet;

public class WorkingSetSyncSetInput extends SyncSetInputFromSyncSet {

	private SyncInfoWorkingSetFilter workingSetFilter = new SyncInfoWorkingSetFilter();
	
	public WorkingSetSyncSetInput(SyncSet set) {
		super(set);
		setFilter(workingSetFilter);
	}
	
	public void setWorkingSet(IWorkingSet workSet) {
		workingSetFilter.setWorkingSet(workSet);
		try {
			reset(new NullProgressMonitor());
		} catch (TeamException e) {
			// TODO: ??
		}
	}
	
	public IWorkingSet getWorkingSet() {
		return workingSetFilter.getWorkingSet();
	}
	
	public IResource[] roots(TeamSubscriber subscriber) {
		return workingSetFilter.getRoots(subscriber);
	}
}
