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

import java.util.*;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogEntry;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogListener;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Performs an rlog on the resources and caches the results.
 */
public class RemoteLogOperation extends RepositoryLocationOperation {
	
	private RLog rlog = new RLog();
	private CVSTag tag1;
	private CVSTag tag2;
	private LogEntryCache entryCache;
	
	/** 
	 * A log entry cache that can be shared by multiple instances of the
	 * remote log operation.
	 */
	public static class LogEntryCache {
	    
	    /*
	     * Map that is used for the single revision cases
	     */
		private Map entries = new HashMap(); /* Map ICVSRemoteFile->ILogEntry */
		
		/*
		 * Map that is used to cache multiple ILogEntry for a single resource
		 */
		private Map allEntries = new HashMap(); /* Map String->ILogEntry[] */
		
		/**
		 * Return the log entry that was fetch for the given resource
		 * or <code>null</code> if no entry was fetched.
		 * @param getFullPath(resource) the resource
		 * @return the fetched log entry or <code>null</code>
		 */
		public synchronized ILogEntry getLogEntry(ICVSRemoteResource resource) {
			return (ILogEntry)entries.get(resource);
		}
		
		/**
		 * Return the log entries that were fetched for the given resource
		 * or an empty list if no entry was fetched.
		 * @param getFullPath(resource) the resource
		 * @return the fetched log entries or an empty list is none were found
		 */
		public synchronized ILogEntry[] getLogEntries(ICVSRemoteResource resource) {
			return (ILogEntry[])allEntries.get(getFullPath(resource));
		}
		
		/*
         * Return the full path that uniquely identifies the resource
         * accross repositories. This path include the repository and
         * resource path but does not include the revision so that 
         * all log entries for a file can be retrieved.
         */
        private String getFullPath(ICVSRemoteResource resource) {
            return Util.appendPath(resource.getRepository().getLocation(), resource.getRepositoryRelativePath());
        }
		
		public synchronized void clearEntries() {
			entries.clear();
			allEntries.clear();
		}

	    public synchronized void cacheEntries(ICVSRemoteResource[] remotes, LogListener listener) {
	        // Record the log entries for the files we want
	        for (int i = 0; i < remotes.length; i++) {
	        	ICVSRemoteResource resource = remotes[i];
	        	if (!resource.isContainer()) {
	        		ICVSRemoteFile file = (ICVSRemoteFile) resource;
	        		ILogEntry entry = listener.getEntryFor(file);
	        		if (entry != null) {
	        			entries.put(file, entry);
	        		}
	        		ILogEntry allLogs[] = listener.getEntriesFor(file);
	        		allEntries.put(getFullPath(file), allLogs);
	        	}
	        }
	    }
	    
	    public synchronized ICVSRemoteFile getImmediatePredecessor(ICVSRemoteFile file) throws TeamException {
	        ILogEntry[] allLogs = (ILogEntry[])allEntries.get(getFullPath(file));
            String revision = file.getRevision();
            // First decrement the last digit and see if that revision exists
            String predecessorRevision = getPredecessorRevision(revision);
            ICVSRemoteFile predecessor = findRevison(allLogs, predecessorRevision);
            // If nothing was found, try to fond the base of a branch
            if (predecessor == null && isBrancheRevision(revision)) {
                predecessorRevision = getBaseRevision(revision);
                predecessor = findRevison(allLogs, predecessorRevision);
            }
            // If that fails, it is still possible that there is a revision.
            // This can happen if the revision has been manually set.
            if (predecessor == null) {
                // We don't search in this case since this is costly and would be done
                // for any file that is new as well.
            }
            return predecessor;
	    }
        
	    /*
         * Find the given revision in the list of log entries.
         * Return null if the revision wasn't found.
         */
        private ICVSRemoteFile findRevison(ILogEntry[] allLogs, String predecessorRevision) throws TeamException {
            for (int i = 0; i < allLogs.length; i++) {
                ILogEntry entry = allLogs[i];
                ICVSRemoteFile file = entry.getRemoteFile();
                if (file.getRevision().equals(predecessorRevision)) {
                    return file;
                }
            }
            return null;
        }
        /*
	     * Decrement the trailing digit by one.
	     */
        private String getPredecessorRevision(String revision) {
            int digits[] = Util.convertToDigits(revision);
            digits[digits.length -1]--;
            StringBuffer buffer = new StringBuffer(revision.length());
            for (int i = 0; i < digits.length; i++) {
                buffer.append(Integer.toString(digits[i]));
                if (i < digits.length - 1) {
                    buffer.append('.');
                }
            }
            return buffer.toString();
        }
        
        /*
         * Return true if there are more than 2 digits in the revision number
         * (i.e. the revision is on a branch)
         */
        private boolean isBrancheRevision(String revision) {
            return Util.convertToDigits(revision).length > 2;
        }
        
        /*
         * Remove the trailing revision digits such that the
         * returned revision is shorter than the given revision 
         * and is an even number of digits long
         */
        private String getBaseRevision(String revision) {
            int digits[] = Util.convertToDigits(revision);
            int length = digits.length - 1;
            if (length % 2 == 1) {
                length--;
            }
            StringBuffer buffer = new StringBuffer(revision.length());
            for (int i = 0; i < length; i++) {
                buffer.append(Integer.toString(digits[i]));
                if (i < length - 1) {
                    buffer.append('.');
                }
            }
            return buffer.toString();
        }
        /**
         * Remove any entries for the remote resources
         * @param resource the remote resource
         */
        public synchronized void clearEntries(ICVSRemoteResource resource) {
            String remotePath = getFullPath(resource);
            LogEntry[] entries = (LogEntry[])allEntries.remove(remotePath);
            if (entries != null) {
                for (int i = 0; i < entries.length; i++) {
                    LogEntry entry = entries[i];
                    this.entries.remove(entry.getRemoteFile());
                }
            }
        }
	}
	
	public RemoteLogOperation(IWorkbenchPart part, ICVSRemoteResource[] remoteResources, CVSTag tag1, CVSTag tag2, LogEntryCache cache) {
		super(part, remoteResources);
		this.tag1 = tag1;
		this.tag2 = tag2;
		this.entryCache = cache;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryLocationOperation#execute(org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation, org.eclipse.team.internal.ccvs.core.ICVSRemoteResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(ICVSRepositoryLocation location, ICVSRemoteResource[] remoteResources, IProgressMonitor monitor) throws CVSException {
		monitor.beginTask(Policy.bind("RemoteLogOperation.0", location.getHost()), 100); //$NON-NLS-1$
		Session s = new Session(location, CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot()), false /* do not output to console */);
		LogListener listener = new LogListener();
		
		ICVSRemoteResource[] remotes = remoteResources;
		Command.LocalOption[] localOptions;
		if(tag1 != null && tag2 != null) {
			localOptions  = new Command.LocalOption[] {RLog.NO_TAGS, RLog.makeTagOption(tag1, tag2)};
		} else {
			localOptions  = new Command.LocalOption[] {RLog.NO_TAGS};
			// Optimize the cases were we are only fetching the history for a single revision. If it is
			// already cached, don't fetch it again.
			ArrayList unCachedRemotes = new ArrayList();
			for (int i = 0; i < remoteResources.length; i++) {
				ICVSRemoteResource r = remoteResources[i];
				if(entryCache.getLogEntry(r) == null) {
					unCachedRemotes.add(r);
				}
			}
			remotes = (ICVSRemoteResource[]) unCachedRemotes.toArray(new ICVSRemoteResource[unCachedRemotes.size()]);
		}
		if (remotes.length > 0) {
			try {
				s.open(Policy.subMonitorFor(monitor, 10));
				IStatus status = rlog.execute(s, Command.NO_GLOBAL_OPTIONS, localOptions, remotes, listener, Policy.subMonitorFor(monitor, 90));
				collectStatus(status);
			} finally {
				s.close();
			}

			entryCache.cacheEntries(remotes, listener);
		}
	}

    /* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("RemoteLogOperation.1"); //$NON-NLS-1$
	}
}
