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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.text.DateFormat;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.*;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.*;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.RemoteLogOperation;
import org.eclipse.team.internal.ccvs.ui.operations.RemoteLogOperation.LogEntryCache;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.*;

/**
 * Collector that fetches the log for incoming CVS change sets
 */
public class CVSChangeSetCollector extends SyncInfoSetChangeSetCollector {

    /*
     * Constant used to add the collector to the configuration of a page so
     * it can be accessed by the CVS custom actions
     */
    public static final String CVS_CHECKED_IN_COLLECTOR = CVSUIPlugin.ID + ".CVSCheckedInCollector"; //$NON-NLS-1$
    
	// Log operation that is used to fetch revision histories from the server. It also
	// provides caching so we keep it around.
    private LogEntryCache logs;
	
	// Job that builds the layout in the background.
	private boolean shutdown = false;
	private FetchLogEntriesJob fetchLogEntriesJob;

    private static final String DEFAULT_INCOMING_SET_NAME = "Unassigned Remote Changes";

	/* *****************************************************************************
	 * Special sync info that has its kind already calculated.
	 */
	public class CVSUpdatableSyncInfo extends CVSSyncInfo {
		public int kind;
		public CVSUpdatableSyncInfo(int kind, IResource local, IResourceVariant base, IResourceVariant remote, Subscriber s) {
			super(local, base, remote, s);
			this.kind = kind;
		}

		protected int calculateKind() throws TeamException {
			return kind;
		}
	}
	
	/* *****************************************************************************
	 * Background job to fetch commit comments and update view
	 */
	private class FetchLogEntriesJob extends Job {
		private Set syncSets = new HashSet();
		public FetchLogEntriesJob() {
			super(Policy.bind("CVSChangeSetCollector.4"));  //$NON-NLS-1$
			setUser(false);
		}
		public boolean belongsTo(Object family) {
			return family == ISynchronizeManager.FAMILY_SYNCHRONIZE_OPERATION;
		}
		public IStatus run(IProgressMonitor monitor) {
			
				if (syncSets != null && !shutdown) {
					// Determine the sync sets for which to fetch comment nodes
					SyncInfoSet[] updates;
					synchronized (syncSets) {
						updates = (SyncInfoSet[]) syncSets.toArray(new SyncInfoSet[syncSets.size()]);
						syncSets.clear();
					}
					for (int i = 0; i < updates.length; i++) {
						calculateRoots(updates[i], monitor);
					}
				}
				return Status.OK_STATUS;
		
		}
		public void add(SyncInfoSet set) {
			synchronized(syncSets) {
				syncSets.add(set);
			}
			schedule();
		}
		public boolean shouldRun() {
			return !syncSets.isEmpty();
		}
	};
	
	private class DefaultCheckedInChangeSet extends CheckedInChangeSet {

	    private Date date = new Date();
	    
        public DefaultCheckedInChangeSet(String name) {
            setName(name);
        }
        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.CheckedInChangeSet#getAuthor()
         */
        public String getAuthor() {
            return ""; //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.CheckedInChangeSet#getDate()
         */
        public Date getDate() {
            return date;
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.ChangeSet#getComment()
         */
        public String getComment() {
            return ""; //$NON-NLS-1$
        }
	    
	}
	
	private class CVSCheckedInChangeSet extends CheckedInChangeSet {

        private final ILogEntry entry;

        public CVSCheckedInChangeSet(ILogEntry entry) {
            this.entry = entry;
    		String date = DateFormat.getDateTimeInstance().format(entry.getDate());
    		String comment = HistoryView.flattenText(entry.getComment());
    		setName("["+entry.getAuthor()+ "] (" + date +") " + comment); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.CheckedInChangeSet#getAuthor()
         */
        public String getAuthor() {
            return entry.getAuthor();
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.CheckedInChangeSet#getDate()
         */
        public Date getDate() {
            return entry.getDate();
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.ChangeSet#getComment()
         */
        public String getComment() {
            return entry.getComment();
        }
	}
	
    public CVSChangeSetCollector(ISynchronizePageConfiguration configuration) {
        super(configuration);
        configuration.setProperty(CVSChangeSetCollector.CVS_CHECKED_IN_COLLECTOR, this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.core.subscribers.SyncInfoSetChangeSetCollector#add(org.eclipse.team.core.synchronize.SyncInfo[])
     */
    protected void add(SyncInfo[] infos) {
        startUpdateJob(new SyncInfoSet(infos));
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.SyncInfoSetChangeSetCollector#reset(org.eclipse.team.core.synchronize.SyncInfoSet)
     */
    public void reset(SyncInfoSet seedSet) {
        // Cancel any currently running job
        if (fetchLogEntriesJob != null) {
	        try {
	            fetchLogEntriesJob.cancel();
	            fetchLogEntriesJob.join();
	        } catch (InterruptedException e) {
	        }
        }
        super.reset(seedSet);
    }
    
	private synchronized void startUpdateJob(SyncInfoSet set) {
		if(fetchLogEntriesJob == null) {
			fetchLogEntriesJob = new FetchLogEntriesJob();
		}
		fetchLogEntriesJob.add(set);
	}
	
	private void calculateRoots(SyncInfoSet set, IProgressMonitor monitor) {
		try {
			monitor.beginTask(null, 100);
			// Decide which nodes we have to fetch log histories
			SyncInfo[] infos = set.getSyncInfos();
			ArrayList remoteChanges = new ArrayList();
			for (int i = 0; i < infos.length; i++) {
				SyncInfo info = infos[i];
				if(isRemoteChange(info)) {
					remoteChanges.add(info);
				}
			}	
			handleRemoteChanges((SyncInfo[]) remoteChanges.toArray(new SyncInfo[remoteChanges.size()]), monitor);
		} catch (CVSException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
		} finally {
			monitor.done();
		}
	}
	
	/*
	 * Return if this sync info should be considered as part of a remote change
	 * meaning that it can be placed inside an incoming commit set (i.e. the
	 * set is determined using the comments from the log entry of the file). 
	 */
	private boolean isRemoteChange(SyncInfo info) {
		int kind = info.getKind();
		if(info.getLocal().getType() != IResource.FILE) return false;
		if(info.getComparator().isThreeWay()) {
			return (kind & SyncInfo.DIRECTION_MASK) != SyncInfo.OUTGOING;
		}
		// For two-way, the change is only remote if it has a remote or has a base locally
		if (info.getRemote() != null) return true;
		ICVSFile file = CVSWorkspaceRoot.getCVSFileFor((IFile)info.getLocal());
		try {
            return file.getSyncBytes() != null;
        } catch (CVSException e) {
            // Log the error and exclude the file from consideration
            CVSUIPlugin.log(e);
            return false;
        }
	}
	
	/**
	 * Fetch the log histories for the remote changes and use this information
	 * to add each resource to an appropriate commit set.
     */
    private void handleRemoteChanges(final SyncInfo[] infos, final IProgressMonitor monitor) throws CVSException, InterruptedException {
        monitor.beginTask(null, 100);
        final LogEntryCache logs = getSyncInfoComment(infos, Policy.subMonitorFor(monitor, 80));
        performUpdate(new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) {
                addLogEntries(infos, logs, monitor);
            }
        }, true  /* preserver expansion */, Policy.subMonitorFor(monitor, 20));
        monitor.done();
    }
    
	/**
	 * How do we tell which revision has the interesting log message? Use the later
	 * revision, since it probably has the most up-to-date comment.
	 */
	private LogEntryCache getSyncInfoComment(SyncInfo[] infos, IProgressMonitor monitor) throws CVSException, InterruptedException {
		if (logs == null) {
		    logs = new LogEntryCache();
		}
	    if (isTagComparison()) {
	        CVSTag tag = getCompareSubscriber().getTag();
            if (tag != null) {
	            // This is a comparison against a single tag
                // TODO: The local tags could be different per root or even mixed!!!
                fetchLogs(infos, logs, getLocalResourcesTag(infos), tag, monitor);
	        } else {
	            // Perform a fetch for each root in the subscriber
	            Map rootToInfosMap = getRootToInfosMap(infos);
	            monitor.beginTask(null, 100 * rootToInfosMap.size());
	            for (Iterator iter = rootToInfosMap.keySet().iterator(); iter.hasNext();) {
                    IResource root = (IResource) iter.next();
                    List infoList = ((List)rootToInfosMap.get(root));
                    SyncInfo[] infoArray = (SyncInfo[])infoList.toArray(new SyncInfo[infoList.size()]);
                    fetchLogs(infoArray, logs, getLocalResourcesTag(infoArray), getCompareSubscriber().getTag(root), Policy.subMonitorFor(monitor, 100));
                }
	            monitor.done();
	        }
	        
	    } else {
	        // Run the log command once with no tags
			fetchLogs(infos, logs, null, null, monitor);
	    }
		return logs;
	}
	
	private void fetchLogs(SyncInfo[] infos, LogEntryCache cache, CVSTag localTag, CVSTag remoteTag, IProgressMonitor monitor) throws CVSException, InterruptedException {
	    ICVSRemoteResource[] remoteResources = getRemotes(infos);
	    if (remoteResources.length > 0) {
			RemoteLogOperation logOperation = new RemoteLogOperation(getConfiguration().getSite().getPart(), remoteResources, localTag, remoteTag, cache);
			logOperation.execute(monitor);
	    }    
	}
	
	private ICVSRemoteResource[] getRemotes(SyncInfo[] infos) {
		List remotes = new ArrayList();
		for (int i = 0; i < infos.length; i++) {
			CVSSyncInfo info = (CVSSyncInfo)infos[i];
			if (info.getLocal().getType() != IResource.FILE) {
				continue;
			}	
			ICVSRemoteResource remote = getRemoteResource(info);
			if(remote != null) {
				remotes.add(remote);
			}
		}
		return (ICVSRemoteResource[]) remotes.toArray(new ICVSRemoteResource[remotes.size()]);
	}
	
    private boolean isTagComparison() {
        return getCompareSubscriber() != null;
    }
    
	/*
     * Return a map of IResource -> List of SyncInfo where the resource
     * is a root of the compare subscriber and the SyncInfo are children
     * of that root
     */
    private Map getRootToInfosMap(SyncInfo[] infos) {
        Map rootToInfosMap = new HashMap();
        IResource[] roots = getCompareSubscriber().roots();
        for (int i = 0; i < infos.length; i++) {
            SyncInfo info = infos[i];
            IPath localPath = info.getLocal().getFullPath();
            for (int j = 0; j < roots.length; j++) {
                IResource resource = roots[j];
                if (resource.getFullPath().isPrefixOf(localPath)) {
                    List infoList = (List)rootToInfosMap.get(resource);
                    if (infoList == null) {
                        infoList = new ArrayList();
                        rootToInfosMap.put(resource, infoList);
                    }
                    infoList.add(info);
                    break; // out of inner loop
                }
            }
            
        }
        return rootToInfosMap;
    }

    private CVSTag getLocalResourcesTag(SyncInfo[] infos) {
		try {
			for (int i = 0; i < infos.length; i++) {
				IResource local = infos[i].getLocal();
                ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(local);
				CVSTag tag = null;
				if(cvsResource.isFolder()) {
					FolderSyncInfo info = ((ICVSFolder)cvsResource).getFolderSyncInfo();
					if(info != null) {
						tag = info.getTag();									
					}
					if (tag != null && tag.getType() == CVSTag.BRANCH) {
						tag = Util.getAccurateFolderTag(local, tag);
					}
				} else {
					tag = Util.getAccurateFileTag(cvsResource);
				}
				if(tag == null) {
					tag = new CVSTag();
				}
				return tag;
			}
			return new CVSTag();
		} catch (CVSException e) {
			return new CVSTag();
		}
	}
	
    private CVSCompareSubscriber getCompareSubscriber() {
        ISynchronizeParticipant participant = getConfiguration().getParticipant();
        if (participant instanceof CompareParticipant) {
            return ((CompareParticipant)participant).getCVSCompareSubscriber();
        }
        return null;
    }

    private ICVSRemoteResource getRemoteResource(CVSSyncInfo info) {
		try {
			ICVSRemoteResource remote = (ICVSRemoteResource) info.getRemote();
			ICVSRemoteResource local = CVSWorkspaceRoot.getRemoteResourceFor(info.getLocal());
			if(local == null) {
				local = (ICVSRemoteResource)info.getBase();
			}
			
			boolean useRemote = true;
			if (local != null && remote != null) {
				String remoteRevision = getRevisionString(remote);
				String localRevision = getRevisionString(local);
				useRemote = useRemote(localRevision, remoteRevision);
			} else if (remote == null) {
				useRemote = false;
			}
			if (useRemote) {
				return remote;
			} else if (local != null) {
				return local;
			}
			return null;
		} catch (CVSException e) {
			CVSUIPlugin.log(e);
			return null;
		}
	}
	
    private boolean useRemote(String localRevision, String remoteRevision) {
        boolean useRemote;
        if (remoteRevision == null && localRevision == null) {
            useRemote = true;
        } else if (localRevision == null) {
            useRemote = true;
        } else if (remoteRevision == null) {
            useRemote = false;
        } else {
            useRemote = ResourceSyncInfo.isLaterRevision(remoteRevision, localRevision);
        }
        return useRemote;
    }

    private String getRevisionString(ICVSRemoteResource remoteFile) {
		if(remoteFile instanceof RemoteFile) {
			return ((RemoteFile)remoteFile).getRevision();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.views.HierarchicalModelProvider#dispose()
	 */
	public void dispose() {
		shutdown = true;
		if(fetchLogEntriesJob != null && fetchLogEntriesJob.getState() != Job.NONE) {
			fetchLogEntriesJob.cancel();
		}
		if (logs != null) {
		    logs.clearEntries();
		}
		getConfiguration().setProperty(CVSChangeSetCollector.CVS_CHECKED_IN_COLLECTOR, null);
		super.dispose();
	}
	
    /*
	 * Add the following sync info elements to the viewer. It is assumed that these elements have associated
	 * log entries cached in the log operation.
	 */
	private void addLogEntries(SyncInfo[] commentInfos, LogEntryCache logs, IProgressMonitor monitor) {
		try {
			monitor.beginTask(null, commentInfos.length * 10);
			if (logs != null) {
				for (int i = 0; i < commentInfos.length; i++) {
					addSyncInfoToCommentNode(commentInfos[i], logs);
					monitor.worked(10);
				}
			}
		} finally {
			monitor.done();
		}
	}
	
	/*
	 * Create a node for the given sync info object. The logs should contain the log for this info.
	 * 
	 * @param info the info for which to create a node in the model
	 * @param log the cvs log for this node
	 */
	private void addSyncInfoToCommentNode(SyncInfo info, LogEntryCache logs) {
		ICVSRemoteResource remoteResource = getRemoteResource((CVSSyncInfo)info);
		if(isTagComparison() && remoteResource != null) {
			addMultipleRevisions(info, logs, remoteResource);
		} else {
			addSingleRevision(info, logs, remoteResource);
		}
	}
	
	/*
	 * Add a single log entry to the model.
	 * 
	 * @param info
	 * @param logs
	 * @param remoteResource
	 */
	private void addSingleRevision(SyncInfo info, LogEntryCache logs, ICVSRemoteResource remoteResource) {
		ILogEntry logEntry = logs.getLogEntry(remoteResource);
		// For incoming deletions grab the comment for the latest on the same branch
		// which is now in the attic.
		try {
			String remoteRevision = ((ICVSRemoteFile) remoteResource).getRevision();
			if (isDeletedRemotely(info)) {
				ILogEntry[] logEntries = logs.getLogEntries(remoteResource);
				for (int i = 0; i < logEntries.length; i++) {
					ILogEntry entry = logEntries[i];
					String revision = entry.getRevision();
					if (entry.isDeletion() && ResourceSyncInfo.isLaterRevision(revision, remoteRevision)) {
						logEntry = entry;
					}
				}
			}
		} catch (TeamException e) {
			// continue and skip deletion checks
		}
		addRemoteChange(info, remoteResource, logEntry);
	}
	
    /*
	 * Add multiple log entries to the model.
	 * 
	 * @param info
	 * @param logs
	 * @param remoteResource
	 */
	private void addMultipleRevisions(SyncInfo info, LogEntryCache logs, ICVSRemoteResource remoteResource) {
		ILogEntry[] logEntries = logs.getLogEntries(remoteResource);
		if(logEntries == null || logEntries.length == 0) {
			// If for some reason we don't have a log entry, try the latest
			// remote.
			addRemoteChange(info, null, null);
		} else {
			for (int i = 0; i < logEntries.length; i++) {
				ILogEntry entry = logEntries[i];
				addRemoteChange(info, remoteResource, entry);
			}
		}
	}
	
	private boolean isDeletedRemotely(SyncInfo info) {
		int kind = info.getKind();
		if(kind == (SyncInfo.INCOMING | SyncInfo.DELETION)) return true;
		if(SyncInfo.getDirection(kind) == SyncInfo.CONFLICTING && info.getRemote() == null) return true;
		return false;
	}
	
    /*
     * Add the remote change to an incoming commit set
     */
    private void addRemoteChange(SyncInfo info, ICVSRemoteResource remoteResource, ILogEntry logEntry) {
        if(remoteResource != null && logEntry != null && isRemoteChange(info)) {
	        if(requiresCustomSyncInfo(info, remoteResource, logEntry)) {
	        	info = new CVSUpdatableSyncInfo(info.getKind(), info.getLocal(), info.getBase(), (RemoteResource)logEntry.getRemoteFile(), ((CVSSyncInfo)info).getSubscriber());
	        	try {
	        		info.init();
	        	} catch (TeamException e) {
	        		// this shouldn't happen, we've provided our own calculate kind
	        	}
	        }
	        // Only add the info if the base and remote differ
	        IResourceVariant base = info.getBase();
	        IResourceVariant remote = info.getRemote();
	        if ((base == null && remote != null) || (remote == null && base != null) || !base.equals(remote)) {
	            synchronized(this) {
			        ChangeSet set = getChangeSetFor(logEntry);
			        if (set == null) {
			            set = createChangeSetFor(logEntry);
			        	add(set);
			        }
			        set.add(info);
	            }
	        }
        } else {
            // The info was not retrieved for the remote change for some reason.
            // Add the node to the root
            addToDefaultSet(DEFAULT_INCOMING_SET_NAME, info);
        }
    }
    
    private void addToDefaultSet(String name, SyncInfo info) {
        ChangeSet set;
        synchronized(this) {
	        set = getChangeSetFor(name);
	        if (set == null) {
	            set = createDefaultChangeSet(name);
	        	add(set);
	        }
	        set.add(info);
        }
    }
    
    private ChangeSet createDefaultChangeSet(String name) {
        return new DefaultCheckedInChangeSet(name);
    }

    private ChangeSet createChangeSetFor(ILogEntry logEntry) {
        return new CVSCheckedInChangeSet(logEntry);
    }

    private ChangeSet getChangeSetFor(ILogEntry logEntry) {
        ChangeSet[] sets = getSets();
        for (int i = 0; i < sets.length; i++) {
            ChangeSet set = sets[i];
            if (set instanceof CheckedInChangeSet &&
                    set.getComment().equals(logEntry.getComment()) &&
                    ((CheckedInChangeSet)set).getAuthor().equals(logEntry.getAuthor())) {
                return set;
            }
        }
        return null;
    }

    private ChangeSet getChangeSetFor(String name) {
        ChangeSet[] sets = getSets();
        for (int i = 0; i < sets.length; i++) {
            ChangeSet set = sets[i];
            if (set.getName().equals(name)) {
                return set;
            }
        }
        return null;
    }
    
    private boolean requiresCustomSyncInfo(SyncInfo info, ICVSRemoteResource remoteResource, ILogEntry logEntry) {
		// Only interested in non-deletions
		if (logEntry.isDeletion() || !(info instanceof CVSSyncInfo)) return false;
		// Only require a custom sync info if the remote of the sync info
		// differs from the remote in the log entry
		IResourceVariant remote = info.getRemote();
		if (remote == null) return true;
		return !remote.equals(remoteResource);
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.SyncInfoSetChangeSetCollector#waitUntilDone(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void waitUntilDone(IProgressMonitor monitor) {
        super.waitUntilDone(monitor);
		monitor.worked(1);
		// wait for the event handler to process changes.
		while(fetchLogEntriesJob.getState() != Job.NONE) {
			monitor.worked(1);
			try {
				Thread.sleep(10);		
			} catch (InterruptedException e) {
			}
			Policy.checkCanceled(monitor);
		}
		monitor.worked(1);
    }
    public LogEntryCache getLogs() {
        return logs;
    }
}
