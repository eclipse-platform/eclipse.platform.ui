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

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.ui.synchronize.viewers.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
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
public class ChangeLogViewerInput extends HierarchicalModelProvider {
	
	private Map commentRoots = new HashMap();
	private PendingUpdateAdapter pendingItem;
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
	
	/**
	 * The PendingUpdateAdapter is a convenience object that can be used
	 * by a BaseWorkbenchContentProvider that wants to show a pending update.
	 */
	public static class PendingUpdateAdapter implements IWorkbenchAdapter, IAdaptable {

		/**
		 * Create a new instance of the receiver.
		 */
		public PendingUpdateAdapter() {
			//No initial behavior
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			if (adapter == IWorkbenchAdapter.class)
				return this;
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object o) {
			return new Object[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
		 */
		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
		 */
		public String getLabel(Object o) {
			return "Fetching logs from server. Please wait...";
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
		 */
		public Object getParent(Object o) {
			return null;
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
				final SyncInfoModelElement[] nodes = calculateRoots(getSyncInfoTree(), monitor);				
				UIJob updateUI = new UIJob("updating change log viewers") {
					public IStatus runInUIThread(IProgressMonitor monitor) {
						AbstractTreeViewer tree = getTreeViewer();	
						if(pendingItem != null && tree != null && !tree.getControl().isDisposed()) {									
							tree.remove(pendingItem);
						}
						for (int i = 0; i < nodes.length; i++) {
							addToViewer(nodes[i]);
							buildModelObjects(nodes[i]);				
						}
						return Status.OK_STATUS;
					}
				};
				updateUI.setSystem(true);
				updateUI.schedule();				
			}
			return Status.OK_STATUS;
		}
	};
	
	public ChangeLogViewerInput(SyncInfoTree set) {
		super(set);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.HierarchicalModelProvider#buildModelObjects(org.eclipse.compare.structuremergeviewer.DiffNode)
	 */
	protected IDiffElement[] buildModelObjects(SynchronizeModelElement node) {
		/*if(node == this) {
			UIJob job = new UIJob("") {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					AbstractTreeViewer tree = getTreeViewer();			
					if (tree != null && !tree.getControl().isDisposed()) {
						if(pendingItem == null) {
							pendingItem = new PendingUpdateAdapter();
						}
						IDiffElement[] elements = getChildren();
						for (int i = 0; i < elements.length; i++) {
							tree.remove(elements[i]);
						}
						tree.add(ChangeLogViewerInput.this, pendingItem);
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
			
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
			fetchLogEntriesJob.setSyncInfoSet(getSyncInfoTree());
			fetchLogEntriesJob.schedule();						
		} else {
			return super.buildModelObjects(node);
		}*/
		return new IDiffElement[0];
	}

	private SyncInfoModelElement[] calculateRoots(SyncInfoSet set, IProgressMonitor monitor) {
		commentRoots.clear();
		/*SyncInfo[] infos = set.getSyncInfos();
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
					changeRoot = new ChangeLogDiffNode(this, logEntry);
					commentRoots.put(dateComment, changeRoot);
				}
				changeRoot.add(infos[i]);
			}
			monitor.worked(100);
		}*/		
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
	 * @see org.eclipse.team.ui.synchronize.views.HierarchicalModelProvider#syncSetChanged(org.eclipse.team.core.subscribers.ISyncInfoSetChangeEvent)
	 */
	protected void syncSetChanged(ISyncInfoSetChangeEvent event) {
		reset();
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
}
