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
import org.eclipse.team.ui.sync.SyncInfoFilter;

/**
 * This is the superclass for all SyncSet input providers
 */
public abstract class SyncSetInput {
	
	private SyncSet syncSet = new SyncSet();
	private SyncInfoFilter filter = new SyncInfoFilter();
	
	public SyncSet getSyncSet() {
		return syncSet;
	}

	protected void log(TeamException e) {
		// TODO: log or throw or communicate to the view that an error has occured
	}
	
	/**
	 * The input is no longer being used. Disconnect it from its source.
	 */
	public abstract void disconnect();

	/**
	 * Reset the input. This will clear the current contents of the sync set and
	 * obtain the contents from the input source.
	 * 
	 * @param monitor
	 * @throws TeamException
	 */
	protected void reset(IProgressMonitor monitor) throws TeamException {
		try {
			syncSet.beginInput();
			syncSet.reset();
			fetchInput(monitor);
		} finally {
			getSyncSet().endInput();
		}
	}

	/**
	 * This method is invoked from reset to get all the sync information from
	 * the input source.
	 * 
	 * @param monitor
	 */
	protected abstract void fetchInput(IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Collect the change in the provided sync info.
	 * 
	 * @param info
	 */
	protected void collect(SyncInfo info) {
		boolean isOutOfSync = filter.select(info);
		SyncInfo oldInfo = syncSet.getSyncInfo(info.getLocal());
		boolean wasOutOfSync = oldInfo != null;
		if (isOutOfSync) {
			if (wasOutOfSync) {
				syncSet.changed(info);
			} else {
				syncSet.add(info);
			}
		} else if (wasOutOfSync) {
			syncSet.remove(info.getLocal());
		}
	}

	protected void remove(IResource resource)  {
		SyncInfo oldInfo = syncSet.getSyncInfo(resource);
		boolean wasOutOfSync = oldInfo != null;
		if (oldInfo != null) {
			syncSet.remove(resource);
		}
	}
	
	/**
	 * @return
	 */
	public SyncInfoFilter getFilter() {
		return filter;
	}

	/**
	 * @param filter
	 */
	public void setFilter(SyncInfoFilter filter, IProgressMonitor monitor) throws TeamException {
		this.filter = filter;
		reset(monitor);
	}
}
