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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.RemoteLogOperation;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.progress.UIJob;

/**
 * This is a prototype model provider using *internal* team classes. It is not meant
 * to be an example or sanctioned use of team. This provider groups changes 
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
	private ChangeLogActionGroup sortGroup;
	private CVSTag tag1;
	private CVSTag tag2;
	private final static String SORT_ORDER_GROUP = "changelog_sort"; //$NON-NLS-1$
	
	/**
	 * Action that allows changing the model providers sort order.
	 */
	private class ToggleModelProviderAction extends Action {
		private int criteria;
		protected ToggleModelProviderAction(String name, int criteria) {
			super(name, Action.AS_RADIO_BUTTON);
			this.criteria = criteria;
			update();
		}

		public void run() {
			StructuredViewer viewer = getViewer();
			if (viewer != null && !viewer.getControl().isDisposed()) {
				ChangeLogModelSorter sorter = (ChangeLogModelSorter) viewer.getSorter();
				if (sorter != null && sorter.getCriteria() != criteria) {
					viewer.setSorter(new ChangeLogModelSorter(criteria));
					update();
				}
			}
		}
		
		public void update() {
			StructuredViewer viewer = getViewer();
			if (viewer != null && !viewer.getControl().isDisposed()) {
				ChangeLogModelSorter sorter = (ChangeLogModelSorter) viewer.getSorter();
				if (sorter != null) {
					setChecked(sorter.getCriteria() == criteria);		
				}
			}	
		}
	}
	
	/**
	 * Actions for the compare particpant's toolbar
	 */
	public class ChangeLogActionGroup extends SynchronizePageActionGroup {
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			MenuManager sortBy = new MenuManager(Policy.bind("ChangeLogModelProvider.0"));	 //$NON-NLS-1$
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					SORT_ORDER_GROUP, 
					sortBy);
			
			sortBy.add(new ToggleModelProviderAction(Policy.bind("ChangeLogModelProvider.1"), ChangeLogModelSorter.COMMENT)); //$NON-NLS-1$
			sortBy.add(new ToggleModelProviderAction(Policy.bind("ChangeLogModelProvider.2"), ChangeLogModelSorter.DATE)); //$NON-NLS-1$
			Action a = new ToggleModelProviderAction(Policy.bind("ChangeLogModelProvider.3"), ChangeLogModelSorter.USER); //$NON-NLS-1$
			a.setChecked(true);
			sortBy.add(a);
		}
	}
	
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
			
			return comment.equals(other.comment) && user.equals(other.user);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return comment.hashCode() + user.hashCode();
		}
	}
	
	public static class FullPathSyncInfoElement extends SyncInfoModelElement {
		public FullPathSyncInfoElement(IDiffContainer parent, SyncInfo info) {
			super(parent, info);
		}
		public String getName() {
			IResource resource = getResource();
			return resource.getName() + " - " + resource.getFullPath().toString(); //$NON-NLS-1$
		}
	}
	
	private class FetchLogEntriesJob extends Job {
		private Set syncSets = new HashSet();
		public FetchLogEntriesJob() {
			super(Policy.bind("ChangeLogModelProvider.4"));  //$NON-NLS-1$
			setUser(true);
		}
		public boolean belongsTo(Object family) {
			return family == ISynchronizeManager.FAMILY_SYNCHRONIZE_OPERATION;
		}
		public IStatus run(IProgressMonitor monitor) {
			if (syncSets != null && !shutdown) {
				// Determine the sync sets for which to fetch comment nodes
				SyncInfoSet[] updates;
				synchronized(syncSets) {
					updates = (SyncInfoSet[])syncSets.toArray(new SyncInfoSet[syncSets.size()]);
					syncSets.clear();
				}
				
				for (int i = 0; i < updates.length; i++) {
					SyncInfoSet set = updates[i];
					calculateRoots(updates[i], monitor);
				}
								
				UIJob updateUI = new UIJob("") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						StructuredViewer tree = getViewer();	
						tree.refresh();
						ISynchronizeModelElement root = getModelRoot();
						if(root instanceof SynchronizeModelElement)
							((SynchronizeModelElement)root).fireChanges();
						return Status.OK_STATUS;
					}
				};
				updateUI.setSystem(true);
				updateUI.schedule();				
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
	
	public static class ChangeLogModelProviderDescriptor implements ISynchronizeModelProviderDescriptor {
		public static final String ID = TeamUIPlugin.ID + ".modelprovider_cvs_changelog"; //$NON-NLS-1$
		public String getId() {
			return ID;
		}		
		public String getName() {
			return Policy.bind("ChangeLogModelProvider.5"); //$NON-NLS-1$
		}		
		public ImageDescriptor getImageDescriptor() {
			return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_CHANGELOG);
		}
	};
	private static final ChangeLogModelProviderDescriptor descriptor = new ChangeLogModelProviderDescriptor();
	
	public ChangeLogModelProvider(ISynchronizePageConfiguration configuration, SyncInfoSet set, CVSTag tag1, CVSTag tag2) {
		super(configuration, set);
		this.tag1 = tag1;
		this.tag2 = tag2;
		configuration.addMenuGroup(ISynchronizePageConfiguration.P_CONTEXT_MENU, SORT_ORDER_GROUP);
		this.sortGroup = new ChangeLogActionGroup();
		configuration.addActionContribution(sortGroup);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.ISynchronizeModelProvider#getDescriptor()
	 */
	public ISynchronizeModelProviderDescriptor getDescriptor() {
		return descriptor;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.HierarchicalModelProvider#buildModelObjects(org.eclipse.compare.structuremergeviewer.DiffNode)
	 */
	protected IDiffElement[] buildModelObjects(ISynchronizeModelElement node) {
		if(node == getModelRoot()) {
			commentRoots.clear();
			startUpdateJob(getSyncInfoSet());
		}
		return new IDiffElement[0];
	}

	private void startUpdateJob(SyncInfoSet set) {
		if(fetchLogEntriesJob == null) {
			fetchLogEntriesJob = new FetchLogEntriesJob();
		}
		fetchLogEntriesJob.add(set);
	}
	
	private ISynchronizeModelElement[] calculateRoots(SyncInfoSet set, IProgressMonitor monitor) {
		try {
			SyncInfo[] infos = set.getSyncInfos();
			RemoteLogOperation logs = getSyncInfoComment(infos, monitor);
			if (logs != null) {
				for (int i = 0; i < infos.length; i++) {
					addSyncInfoToCommentNode(infos[i], logs);
				}
			}
			return (ChangeLogDiffNode[]) commentRoots.values().toArray(new ChangeLogDiffNode[commentRoots.size()]);
		} catch (CVSException e) {
			Utils.handle(e);
			return new ISynchronizeModelElement[0];
		} catch (InterruptedException e) {
			return new ISynchronizeModelElement[0];
		}
	}
	
	/**
	 * Create a node for the given sync info object. The logs should contain the log for this info.
	 * 
	 * @param info the info for which to create a node in the model
	 * @param log the cvs log for this node
	 */
	private void addSyncInfoToCommentNode(SyncInfo info, RemoteLogOperation logs) {
		ICVSRemoteResource remoteResource = getRemoteResource((CVSSyncInfo)info);
		if(tag1 != null && tag2 != null) {
			ILogEntry[] logEntries = logs.getLogEntries(remoteResource);
			for (int i = 0; i < logEntries.length; i++) {
				ILogEntry entry = logEntries[i];
				addNewElementFor(info, entry.getRemoteFile(), entry);
			}
		} else {
			ILogEntry logEntry = logs.getLogEntry(remoteResource);
		}
	}
	
	/**
	 * @param info
	 * @param remoteResource
	 * @param logEntry
	 */
	private void addNewElementFor(SyncInfo info, ICVSRemoteResource remoteResource, ILogEntry logEntry) {
		ISynchronizeModelElement element;	
		// If the element has a comment then group with common comment
		if(remoteResource != null && logEntry != null) {
			DateComment dateComment = new DateComment(logEntry.getDate(), logEntry.getComment(), logEntry.getAuthor());
			ChangeLogDiffNode changeRoot = (ChangeLogDiffNode) commentRoots.get(dateComment);
			if (changeRoot == null) {
				changeRoot = new ChangeLogDiffNode(getModelRoot(), logEntry);
				commentRoots.put(dateComment, changeRoot);
				try {
				setAllowRefreshViewer(false);
				addToViewer(changeRoot);
				} finally {
					setAllowRefreshViewer(true);
				}
			}
			element = new FullPathSyncInfoElement(changeRoot, info);
		} else {
			// For nodes without comments, simply parent with the root. These will be outgoing
			// additions.
			element = new FullPathSyncInfoElement(getModelRoot(), info);
		}	
		try {
			setAllowRefreshViewer(false);
			addToViewer(element);
		} finally {
			setAllowRefreshViewer(true);
		}
	}

	/**
	 * How do we tell which revision has the interesting log message? Use the later
	 * revision, since it probably has the most up-to-date comment.
	 */
	private RemoteLogOperation getSyncInfoComment(SyncInfo[] infos, IProgressMonitor monitor) throws CVSException, InterruptedException {
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
		if(! remotes.isEmpty()) {
			RemoteLogOperation op = new RemoteLogOperation(null, (ICVSRemoteResource[]) remotes.toArray(new ICVSRemoteResource[remotes.size()]), tag1, tag2);
			op.execute(monitor);
			return op;
		}
		return null;
	}
	
	private ICVSRemoteResource getRemoteResource(CVSSyncInfo info) {
		try {
			ICVSRemoteResource remote = (ICVSRemoteResource) info.getRemote();
			ICVSRemoteResource local = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(info.getLocal());

			String remoteRevision = getRevisionString(remote);
			String localRevision = getRevisionString(local);

			boolean useRemote = true;
			if (local != null && remote != null) {
				useRemote = ResourceSyncInfo.isLaterRevision(remoteRevision, localRevision);
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
		getConfiguration().removeActionContribution(sortGroup);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#getViewerSorter()
	 */
	public ViewerSorter getViewerSorter() {
		return new ChangeLogModelSorter(ChangeLogModelSorter.USER);
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
		startUpdateJob(new SyncInfoSet(event.getAddedResources()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#handleResourceChanges(org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent)
	 */
	protected void handleResourceChanges(ISyncInfoTreeChangeEvent event) {
		//	Refresh the viewer for each changed resource
		SyncInfo[] infos = event.getChangedResources();
		for (int i = 0; i < infos.length; i++) {
			SyncInfo info = infos[i];
			IResource local = info.getLocal();
			ISynchronizeModelElement diffNode = getModelObject(local);
			if (diffNode != null) {
				if(diffNode instanceof SyncInfoModelElement) {
					((SyncInfoModelElement)diffNode).update(info);
					calculateProperties(diffNode, false);
				}
			}
		}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#handleResourceRemovals(org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent)
	 */
	protected void handleResourceRemovals(ISyncInfoTreeChangeEvent event) {
		IResource[] removedRoots = event.getRemovedSubtreeRoots();
		for (int i = 0; i < removedRoots.length; i++) {
			removeFromViewer(removedRoots[i]);
		}
		// We have to look for folders that may no longer be in the set
		// (i.e. are in-sync) but still have descendants in the set
		IResource[] removedResources = event.getRemovedResources();
		for (int i = 0; i < removedResources.length; i++) {
			removeFromViewer(removedResources[i]);
		}
	}
}
