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

import java.util.*;

import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.viewers.ISynchronizeModelElement;
import org.eclipse.ui.progress.UIJob;

/**
 * It would be very useful to support showing changes grouped logically
 * instead of grouped physically. This could be used for showing incoming
 * changes and also for showing the results of comparisons.
 * 
 * + 2003-12-09 Tuesday 6:04 jlemieux
 *   + Bug 3456: this was changed last night
 *     + org/eclipse/com/Main.java
 *     + org/blah/this/Other.txt
 * 
 * {date/time, comment, user} -> {*files}
 */
public class ChangeLogModelProvider extends SynchronizeModelProvider {
	
	private Map commentRoots = new HashMap();
	private boolean shutdown = false;
	private FetchLogEntriesJob fetchLogEntriesJob;
	
	public static class DateComment {
		Date date;
		String comment;
		private String user;
		DateComment(Date date, String comment, String user) {
			this.date = date;
			this.comment = comment;
			this.user = user;	
		}

		public boolean equals(Object obj) {
			if(obj == this) return true;
			if(! (obj instanceof DateComment)) return false;
			DateComment other = (DateComment)obj;
			
			Calendar c1 = new GregorianCalendar();
			c1.setTime(date);
			int year = c1.get(Calendar.YEAR);
			int day = c1.get(Calendar.DAY_OF_YEAR);
			
			Calendar c2 = new GregorianCalendar();
			c2.setTime(other.date);
			int yearOther = c2.get(Calendar.YEAR);
			int dayOther = c2.get(Calendar.DAY_OF_YEAR);
			
			return year == yearOther && day == dayOther && comment.equals(other.comment) &&
				user.equals(other.user);
		}
		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return date.hashCode() + comment.hashCode() + user.hashCode();
		}
	}
	
	public static class FullPathSyncInfoElement extends SyncInfoModelElement {
		public FullPathSyncInfoElement(IDiffContainer parent, SyncInfo info) {
			super(parent, info);
		}
		public String getName() {
			return getResource().getFullPath().toString();
		}
	}
	
	private class FetchLogEntriesJob extends Job {
		private SyncInfoSet set;
		public FetchLogEntriesJob() {
			super("Fetching CVS logs");  //$NON-NLS-1$;
		}
		public void setSyncInfoSet(SyncInfoSet set) {
			this.set = set;
		}
		public IStatus run(IProgressMonitor monitor) {
			if (set != null && !shutdown) {
				final ISynchronizeModelElement[] nodes = calculateRoots(getSyncInfoSet(), monitor);				
				UIJob updateUI = new UIJob("updating change log viewers") {
					public IStatus runInUIThread(IProgressMonitor monitor) {
						StructuredViewer tree = getViewer();	
						tree.refresh();
						return Status.OK_STATUS;
					}
				};
				updateUI.setSystem(true);
				updateUI.schedule();				
			}
			return Status.OK_STATUS;
		}
	};
	
	public ChangeLogModelProvider(SyncInfoSet set) {
		super(set);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.HierarchicalModelProvider#buildModelObjects(org.eclipse.compare.structuremergeviewer.DiffNode)
	 */
	protected IDiffElement[] buildModelObjects(ISynchronizeModelElement node) {
		if(node == getModelRoot()) {
			if(fetchLogEntriesJob == null) {
				fetchLogEntriesJob = new FetchLogEntriesJob();
			}
			if(fetchLogEntriesJob.getState() != Job.NONE) {
				fetchLogEntriesJob.cancel();
				try {
					fetchLogEntriesJob.join();
				} catch (InterruptedException e) {
				}
			}
			fetchLogEntriesJob.setSyncInfoSet(getSyncInfoSet());
			fetchLogEntriesJob.schedule();						
		}
		return new IDiffElement[0];
	}

	private ISynchronizeModelElement[] calculateRoots(SyncInfoSet set, IProgressMonitor monitor) {
		commentRoots.clear();
		SyncInfo[] infos = set.getSyncInfos();
		monitor.beginTask("fetching from server", set.size() * 100);
		for (int i = 0; i < infos.length; i++) {
			if(monitor.isCanceled()) {
				break;
			}
			ILogEntry logEntry = getSyncInfoComment((CVSSyncInfo) infos[i], monitor);
			if(logEntry != null) {
				DateComment dateComment = new DateComment(logEntry.getDate(), logEntry.getComment(), logEntry.getAuthor());
				ChangeLogDiffNode changeRoot = (ChangeLogDiffNode) commentRoots.get(dateComment);
				if (changeRoot == null) {
					changeRoot = new ChangeLogDiffNode(getModelRoot(), logEntry);
					commentRoots.put(dateComment, changeRoot);
				}
				ISynchronizeModelElement element = new FullPathSyncInfoElement(changeRoot, infos[i]);
				associateDiffNode(element);
			}
			monitor.worked(100);
		}
		return (ChangeLogDiffNode[]) commentRoots.values().toArray(new ChangeLogDiffNode[commentRoots.size()]);
	}
	
	/**
	 * How do we tell which revision has the interesting log message? Use the later
	 * revision, since it probably has the most up-to-date comment.
	 */
	private ILogEntry getSyncInfoComment(CVSSyncInfo info, IProgressMonitor monitor) {
		try {
			if(info.getLocal().getType() != IResource.FILE) {
				return null;
			}
			
			ICVSRemoteResource remote = (ICVSRemoteResource)info.getRemote();
			ICVSRemoteResource base = (ICVSRemoteResource)info.getBase();
			ICVSRemoteResource local = (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(info.getLocal());
			
			String baseRevision = getRevisionString(base);
			String remoteRevision = getRevisionString(remote);
			String localRevision = getRevisionString(local);
			
			// TODO: handle new files where there is no local or remote	
			boolean useRemote = true;
			if(local != null && remote != null) {
				useRemote = ResourceSyncInfo.isLaterRevision(remoteRevision, localRevision);
			} else if(remote == null) {
				useRemote = false;
			}
			if (useRemote) {
				return ((RemoteFile) remote).getLogEntry(monitor);
			} else if (local != null){
				return ((RemoteFile) local).getLogEntry(monitor);
			}
			return null;
		} catch (CVSException e) {
			CVSUIPlugin.log(e);
			return null;
		}
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
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#getViewerSorter()
	 */
	public ViewerSorter getViewerSorter() {
		return new SynchronizeModelElementSorter();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#doAdd(org.eclipse.team.ui.synchronize.viewers.SynchronizeModelElement, org.eclipse.team.ui.synchronize.viewers.SynchronizeModelElement)
	 */
	protected void doAdd(ISynchronizeModelElement parent, ISynchronizeModelElement element) {
		AbstractTreeViewer viewer = (AbstractTreeViewer)getViewer();
		viewer.add(parent, element);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#doRemove(org.eclipse.team.ui.synchronize.viewers.SynchronizeModelElement)
	 */
	protected void doRemove(ISynchronizeModelElement element) {
		AbstractTreeViewer viewer = (AbstractTreeViewer)getViewer();
		viewer.remove(element);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#handleResourceAdditions(org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent)
	 */
	protected void handleResourceAdditions(ISyncInfoTreeChangeEvent event) {
		reset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#handleResourceChanges(org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent)
	 */
	protected void handleResourceChanges(ISyncInfoTreeChangeEvent event) {
		reset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#handleResourceRemovals(org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent)
	 */
	protected void handleResourceRemovals(ISyncInfoTreeChangeEvent event) {
		reset();
	}
}
