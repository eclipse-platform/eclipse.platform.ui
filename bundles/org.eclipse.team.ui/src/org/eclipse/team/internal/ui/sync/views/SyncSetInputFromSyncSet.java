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
import org.eclipse.team.core.subscribers.SyncInfo;

/**
 * Ths class uses the contents of one sync set as the input of another.
 */
public class SyncSetInputFromSyncSet extends SyncSetInput implements ISyncSetChangedListener {

	SyncSet inputSyncSet;
	
	/**
	 * @param syncSet
	 */
	protected SyncSetInputFromSyncSet() {
	}

	/**
	 * @return
	 */
	public SyncSet getInputSyncSet() {
		return inputSyncSet;
	}

	private void connect(SyncSet set) {
		if (this.inputSyncSet != null) return;
		this.inputSyncSet = set;
		inputSyncSet.addSyncSetChangedListener(this);
	}
	
	public void disconnect() {
		if (inputSyncSet == null) return;
		inputSyncSet.removeSyncSetChangedListener(this);
		inputSyncSet = null;
	}
	
	/**
	 * @param set
	 */
	public void setInputSyncSet(SyncSet set, IProgressMonitor monitor) throws TeamException {
		if (inputSyncSet != null) disconnect();
		connect(set);
		reset(monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.AbstractSyncSet#initialize(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void fetchInput(IProgressMonitor monitor) throws TeamException {
		if (inputSyncSet == null) return;
		SyncInfo[] infos = inputSyncSet.allMembers();
		for (int i = 0; i < infos.length; i++) {
			collect(infos[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.ISyncSetChangedListener#syncSetChanged(org.eclipse.team.ccvs.syncviews.views.SyncSetChangedEvent)
	 */
	public void syncSetChanged(SyncSetChangedEvent event) {
		SyncSet syncSet = getSyncSet();
		try {
			syncSet.beginInput();
			if (event.isReset()) {
				syncSet.reset();
			}
			syncSetChanged(event.getChangedResources());			
			syncSetChanged(event.getAddedResources());
			
			remove(event.getRemovedResources());
		} finally {
			getSyncSet().endInput();
		}
	}

	private void syncSetChanged(SyncInfo[] infos) {
		for (int i = 0; i < infos.length; i++) {
			collect(infos[i]);
		}
	}
	
	private void remove(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			remove(resources[i]);
		}
	}
}
