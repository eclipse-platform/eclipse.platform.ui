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
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogListener;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Performs an rlog on the resources
 */
public class RemoteLogOperation extends RepositoryLocationOperation {
	
	private RLog rlog = new RLog();
	private Map entries = new HashMap();
	private Map allEntries = new HashMap();
	private CVSTag tag1;
	private CVSTag tag2;
	
	public RemoteLogOperation(IWorkbenchPart part, ICVSRemoteResource[] remoteResources) {
		this(part, remoteResources, null, null);
	}
	
	public RemoteLogOperation(IWorkbenchPart part, ICVSRemoteResource[] remoteResources, CVSTag tag1, CVSTag tag2) {
		super(part, remoteResources);
		this.tag1 = tag1;
		this.tag2 = tag2;
	}
	
	/**
	 * Return the log entry that was fetch for the given resource
	 * or <code>null</code> if no entry was fetched.
	 * @param resource the resource
	 * @return the fetched log entry or <code>null</code>
	 */
	public ILogEntry getLogEntry(ICVSRemoteResource resource) {
		return (ILogEntry)entries.get(resource);
	}
	
	/**
	 * Return the log entries that were fetched for the given resource
	 * or an empty list if no entry was fetched.
	 * @param resource the resource
	 * @return the fetched log entries or an empty list is none were found
	 */
	public ILogEntry[] getLogEntries(ICVSRemoteResource resource) {
		return (ILogEntry[])allEntries.get(resource);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryLocationOperation#execute(org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation, org.eclipse.team.internal.ccvs.core.ICVSRemoteResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(ICVSRepositoryLocation location, ICVSRemoteResource[] remoteResources, IProgressMonitor monitor) throws CVSException {
		monitor.beginTask("Fetching log information from {0}" + location.getHost(), 100);
		Session s = new Session(location, CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot()));
		LogListener listener = new LogListener();
		
		Command.LocalOption[] localOptions;
		if(tag1 != null && tag2 != null) {
			localOptions  = new Command.LocalOption[] {RLog.NO_TAGS, RLog.makeTagOption(tag1, tag2)};
		} else {
			localOptions  = new Command.LocalOption[] {RLog.NO_TAGS};
		}
		
		try {
			s.open(Policy.subMonitorFor(monitor, 10));
			IStatus status = rlog.execute(s,
					Command.NO_GLOBAL_OPTIONS,
					localOptions,
					remoteResources,
					listener,
					Policy.subMonitorFor(monitor, 90));
			collectStatus(status);
		} finally {
			s.close();
		}
		
		// Record the log entries for the files we want
		for (int i = 0; i < remoteResources.length; i++) {
			ICVSRemoteResource resource = remoteResources[i];
			if (!resource.isContainer()) {
				ICVSRemoteFile file = (ICVSRemoteFile)resource;
				ILogEntry entry = listener.getEntryFor(file);
				if (entry != null) {
					entries.put(file, entry);
				}
				ILogEntry allLogs[] = listener.getEntriesFor(file);
				allEntries.put(file, allLogs);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return "Fetching log information";
	}
}
