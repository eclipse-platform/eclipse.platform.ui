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
package org.eclipse.team.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.core.Policy;

/**
 * Populates an output <code>SyncInfoSet</code> with the <code>SyncInfo</code> from an 
 * input <code>SyncInfoSet</code> which match a <code>SyncInfoFilter</code>. The collector
 * also dynamically updates the output set in reaction to changes in the input set.
 * <p>
 * This class is not intended to be subclassed by clients
 * 
 * @see SyncInfoSet
 * @see SyncInfoFilter
 * 
 * @since 3.0
 */
public final class FilteredSyncInfoCollector implements ISyncInfoSetChangeListener {

	private SyncInfoSet inputSet;
	private SyncInfoSet outputSet;
	private SyncInfoFilter filter;

	/**
	 * Create a filtered sync info collector that collects sync info from the input set.
	 * @param collector the collector that provides the source set
	 * @param inputSet the input set
	 * @param outputSet the output set
	 * @param filter the filter to be applied to the output set
	 */
	public FilteredSyncInfoCollector(SyncInfoSet inputSet, SyncInfoSet outputSet, SyncInfoFilter filter) {
		this.inputSet = inputSet;
		this.outputSet = outputSet;
		this.filter = filter;
	}

	/**
	 * Start the collector. After this method returns the output <code>SyncInfoSet</code>
	 * of the collector will be populated.
	 */
	public void start(IProgressMonitor monitor) {
		inputSet.connect(this, monitor);
	}
	
	/**
	 * Return the output <code>SyncInfoSet</code> that contains the filtered <code>SyncInfo</code>.
	 * @return the output <code>SyncInfoSet</code>
	 */
	public SyncInfoSet getSyncInfoSet() {
		return outputSet;
	}
	
	/**
	 * Return the filter used by this collector.
	 * @return the filter
	 */
	public SyncInfoFilter getFilter() {
		return filter;
	}
	
	public void setFilter(SyncInfoFilter filter, IProgressMonitor monitor) {
		this.filter = filter;
		start(monitor);
	}
	
	/**
	 * Dispose of the collector. The collector cannot be restarted after it has been disposed.
	 */
	public void dispose() {
		if (inputSet == null) return;
		inputSet.removeSyncSetChangedListener(this);
		inputSet = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetReset(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {
		SyncInfoSet syncSet = getSyncInfoSet();
		try {
			syncSet.beginInput();
			monitor.beginTask(null, 100);
			syncSet.clear();
			syncSetChanged(set.getSyncInfos(), Policy.subMonitorFor(monitor, 95));
		} finally {
			syncSet.endInput(Policy.subMonitorFor(monitor, 5));
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoChanged(org.eclipse.team.core.synchronize.ISyncInfoSetChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoChanged(ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
		SyncInfoSet syncSet = getSyncInfoSet();
		try {
			syncSet.beginInput();
			monitor.beginTask(null, 105);
			syncSetChanged(event.getChangedResources(), Policy.subMonitorFor(monitor, 50));			
			syncSetChanged(event.getAddedResources(), Policy.subMonitorFor(monitor, 50));
			remove(event.getRemovedResources());
		} finally {
			syncSet.endInput(Policy.subMonitorFor(monitor, 5));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetErrors(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.team.core.ITeamStatus[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
		SyncInfoSet syncSet = getSyncInfoSet();
		try {
			syncSet.beginInput();
			for (int i = 0; i < errors.length; i++) {
				ITeamStatus status = errors[i];
				syncSet.addError(status);
			}
		} finally {
			syncSet.endInput(monitor);
		}
	}
	
	private void remove(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			remove(resources[i]);
		}
	}
	
	private void remove(IResource resource)  {
		SyncInfoSet syncSet = getSyncInfoSet();
		SyncInfo oldInfo = syncSet.getSyncInfo(resource);
		if (oldInfo != null) {
			syncSet.remove(resource);
		}
	}
	
	private void syncSetChanged(SyncInfo[] infos, IProgressMonitor monitor) {
		for (int i = 0; i < infos.length; i++) {
			collect(infos[i], monitor);
		}
	}
	
	private void collect(SyncInfo info, IProgressMonitor monitor) {
		SyncInfoSet syncSet = getSyncInfoSet();
		boolean isOutOfSync = filter.select(info, monitor);
		SyncInfo oldInfo = syncSet.getSyncInfo(info.getLocal());
		boolean wasOutOfSync = oldInfo != null;
		if (isOutOfSync) {
			syncSet.add(info);
		} else if (wasOutOfSync) {
			syncSet.remove(info.getLocal());
		}
	}
}
