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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.*;
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
 * Disclamer:
 * This is a prototype layout using *internal* team classes. It is not meant
 * to be an example or sanctioned use of team. These classes and the classes
 * references here may change or be deleted in the future.
 * 
 * This provider groups changes into commit sets and fetches the log history for
 * files in the background. Changes that can't be grouped into commit sets (e.g. outgoing 
 * changes) are shown in a flat list.
 * 
 * @since 3.0
 */
public class ChangeLogModelProvider extends SynchronizeModelProvider {
	// Log operation that is used to fetch revision histories from the server. It also
	// provides caching so we keep it around.
	private RemoteLogOperation logOperation;
	
	// Job that builds the layout in the background.
	private boolean shutdown = false;
	private FetchLogEntriesJob fetchLogEntriesJob;
	
	// Sorters for the commit sets and resources
	private ChangeLogActionGroup sortGroup;
	
	// Tag ranges for fetching revision histories. If no tags are specified then
	// the history for the remote revision in the sync info is used.
	private CVSTag tag1;
	private CVSTag tag2;
	private Map multipleResourceMap;
	
	// Constants for persisting sorting options
	private final static String SORT_ORDER_GROUP = "changelog_sort"; //$NON-NLS-1$
	private static final String P_LAST_COMMENTSORT = TeamUIPlugin.ID + ".P_LAST_COMMENT_SORT"; //$NON-NLS-1$
	private static final String P_LAST_RESOURCESORT = TeamUIPlugin.ID + ".P_LAST_RESOURCE_SORT"; //$NON-NLS-1$
	
	/* *****************************************************************************
	 * Action that allows changing the model providers sort order.
	 */
	private class ToggleSortOrderAction extends Action {
		private int criteria;
		private int sortType;
		public final static int RESOURCE_NAME = 1;
		public final static int COMMENT = 2;
		protected ToggleSortOrderAction(String name, int criteria, int sortType, int defaultCriteria) {
			super(name, Action.AS_RADIO_BUTTON);
			this.criteria = criteria;
			this.sortType = sortType;
			setChecked(criteria == defaultCriteria);		
		}

		public void run() {
			StructuredViewer viewer = getViewer();
			if (viewer != null && !viewer.getControl().isDisposed()) {
				ChangeLogModelSorter sorter = (ChangeLogModelSorter) viewer.getSorter();
				if (isChecked() && sorter != null && getCriteria(sorter) != criteria) {
					viewer.setSorter(createSorter(sorter));
					String key = sortType == RESOURCE_NAME ? P_LAST_RESOURCESORT : P_LAST_COMMENTSORT;
					IDialogSettings pageSettings = getConfiguration().getSite().getPageSettings();
					if(pageSettings != null) {
						pageSettings.put(key, criteria);
					}
					update();
				}
			}
		}
		
		public void update() {
			StructuredViewer viewer = getViewer();
			if (viewer != null && !viewer.getControl().isDisposed()) {
				ChangeLogModelSorter sorter = (ChangeLogModelSorter) viewer.getSorter();
				if (sorter != null) {
					setChecked(getCriteria(sorter) == criteria);		
				}
			}	
		}
		
		protected ChangeLogModelSorter createSorter(ChangeLogModelSorter sorter) {
			if(sortType == COMMENT) {
				return new ChangeLogModelSorter(criteria, sorter.getResourceCriteria());
			}	else {
				return new ChangeLogModelSorter(sorter.getCommentCriteria(), criteria);
			}
		}
		
		protected int getCriteria(ChangeLogModelSorter sorter) {
			if(sortType == COMMENT)
				return sorter.getCommentCriteria();
			else
				return sorter.getResourceCriteria();
		}
	}
	
	/* *****************************************************************************
	 * Action group for this layout. It is added and removed for this layout only.
	 */
	public class ChangeLogActionGroup extends SynchronizePageActionGroup {
		private MenuManager sortByComment;
		private MenuManager sortByResource;
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			sortByComment = new MenuManager(Policy.bind("ChangeLogModelProvider.0"));	 //$NON-NLS-1$
			sortByResource = new MenuManager(Policy.bind("ChangeLogModelProvider.6"));	 //$NON-NLS-1$
			
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					SORT_ORDER_GROUP, 
					sortByComment);
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					SORT_ORDER_GROUP, 
					sortByResource);
			
			ChangeLogModelSorter sorter = (ChangeLogModelSorter)getViewerSorter();
			
			sortByComment.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.1"), ChangeLogModelSorter.COMMENT, ToggleSortOrderAction.COMMENT, sorter.getCommentCriteria())); //$NON-NLS-1$
			sortByComment.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.2"), ChangeLogModelSorter.DATE, ToggleSortOrderAction.COMMENT, sorter.getCommentCriteria())); //$NON-NLS-1$
			sortByComment.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.3"), ChangeLogModelSorter.USER, ToggleSortOrderAction.COMMENT, sorter.getCommentCriteria())); //$NON-NLS-1$

			sortByResource.add( new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.8"), ChangeLogModelSorter.PATH, ToggleSortOrderAction.RESOURCE_NAME, sorter.getResourceCriteria())); //$NON-NLS-1$
			sortByResource.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.7"), ChangeLogModelSorter.NAME, ToggleSortOrderAction.RESOURCE_NAME, sorter.getResourceCriteria())); //$NON-NLS-1$
			sortByResource.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.9"), ChangeLogModelSorter.PARENT_NAME, ToggleSortOrderAction.RESOURCE_NAME, sorter.getResourceCriteria())); //$NON-NLS-1$
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#dispose()
		 */
		public void dispose() {
			sortByComment.dispose();
			sortByResource.dispose();
			sortByComment.removeAll();
			sortByResource.removeAll();
			super.dispose();
		}
	}
	
	/* *****************************************************************************
	 * Model element for the resources in this layout. They are displayed with filename and path
	 * onto the same line.
	 */
	public static class FullPathSyncInfoElement extends SyncInfoModelElement {
		public FullPathSyncInfoElement(IDiffContainer parent, SyncInfo info) {
			super(parent, info);
		}
		public String getName() {
			IResource resource = getResource();
			return resource.getName() + " - " + resource.getFullPath().toString(); //$NON-NLS-1$
		}
	}
	
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
	 * Action group for this layout. It is added and removed for this layout only.
	 */
	
	private class FetchLogEntriesJob extends Job {
		private Set syncSets = new HashSet();
		public FetchLogEntriesJob() {
			super(Policy.bind("ChangeLogModelProvider.4"));  //$NON-NLS-1$
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
					refreshViewer();
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
	
	/* *****************************************************************************
	 * Descriptor for this model provider
	 */
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
		if (node == getModelRoot()) {
			// Cancel any existing fetching jobs
			try {
				if (fetchLogEntriesJob != null && fetchLogEntriesJob.getState() != Job.NONE) {
					fetchLogEntriesJob.cancel();
					fetchLogEntriesJob.join();
				}
			} catch (InterruptedException e) {
			}

			// Start building the model from scratch
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
	
	private void refreshViewer() {
		UIJob updateUI = new UIJob("") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				BusyIndicator.showWhile(getDisplay(), new Runnable() {
					public void run() {
						StructuredViewer tree = getViewer();	
						tree.refresh();
						ISynchronizeModelElement root = getModelRoot();
						if(root instanceof SynchronizeModelElement)
							((SynchronizeModelElement)root).fireChanges();
					}
				});

				return Status.OK_STATUS;
			}
		};
		updateUI.setSystem(true);
		updateUI.schedule();
	}
	
	private void calculateRoots(SyncInfoSet set, IProgressMonitor monitor) {
		try {
			monitor.beginTask(null, 100);
			// Decide which nodes we have to fetch log histories
			SyncInfo[] infos = set.getSyncInfos();
			ArrayList commentNodes = new ArrayList();
			ArrayList resourceNodes = new ArrayList();
			for (int i = 0; i < infos.length; i++) {
				SyncInfo info = infos[i];
				if(isInterestingChange(info)) {
					commentNodes.add(info);
				} else {
					resourceNodes.add(info);
				}
			}	
			// Show elements that don't need their log histories retreived
			for (Iterator it = resourceNodes.iterator(); it.hasNext();) {
				SyncInfo info = (SyncInfo) it.next();
				addNewElementFor(info, null, null);
			}
			if(! resourceNodes.isEmpty())
				refreshViewer();
			
			// Fetch log histories then add elements
			SyncInfo[] commentInfos = (SyncInfo[]) commentNodes.toArray(new SyncInfo[commentNodes.size()]);
			RemoteLogOperation logs = getSyncInfoComment(commentInfos, Policy.subMonitorFor(monitor, 80));
			addLogEntries(commentInfos, logs, Policy.subMonitorFor(monitor, 20));
		} catch (CVSException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Add the followinf sync info elements to the viewer. It is assumed that these elements have associated
	 * log entries cached in the log operation.
	 */
	private void addLogEntries(SyncInfo[] commentInfos, RemoteLogOperation logs, IProgressMonitor monitor) {
		try {
			monitor.beginTask(null, commentInfos.length * 10);
			if (logs != null) {
				for (int i = 0; i < commentInfos.length; i++) {
					addSyncInfoToCommentNode(commentInfos[i], logs);
					monitor.worked(10);
				}
				// Don't cache log entries when in two way mode.
				if (getConfiguration().getComparisonType().equals(ISynchronizePageConfiguration.TWO_WAY)) {
					logs.clearEntries();
				}
			}
		} finally {
			monitor.done();
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
			addMultipleRevisions(info, logs, remoteResource);
		} else {
			addSingleRevision(info, logs, remoteResource);
		}
	}
	
	/**
	 * Add multiple log entries to the model.
	 * 
	 * @param info
	 * @param logs
	 * @param remoteResource
	 */
	private void addMultipleRevisions(SyncInfo info, RemoteLogOperation logs, ICVSRemoteResource remoteResource) {
		ILogEntry[] logEntries = logs.getLogEntries(remoteResource);
		if(logEntries == null || logEntries.length == 0) {
			// If for some reason we don't have a log entry, try the latest
			// remote.
			addNewElementFor(info, null, null);
		} else {
			for (int i = 0; i < logEntries.length; i++) {
				ILogEntry entry = logEntries[i];
				addNewElementFor(info, remoteResource, entry);
			}
		}
	}

	/**
	 * Add a single log entry to the model.
	 * 
	 * @param info
	 * @param logs
	 * @param remoteResource
	 */
	private void addSingleRevision(SyncInfo info, RemoteLogOperation logs, ICVSRemoteResource remoteResource) {
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
		addNewElementFor(info, remoteResource, logEntry);
	}

	private boolean isDeletedRemotely(SyncInfo info) {
		int kind = info.getKind();
		if(kind == (SyncInfo.INCOMING | SyncInfo.DELETION)) return true;
		if(SyncInfo.getDirection(kind) == SyncInfo.CONFLICTING && info.getRemote() == null) return true;
		return false;
	}
	
	private void addNewElementFor(SyncInfo info, ICVSRemoteResource remoteResource, ILogEntry logEntry) {
		ISynchronizeModelElement element;	
		// If the element has a comment then group with common comment
		if(remoteResource != null && logEntry != null && isInterestingChange(info)) {
			ChangeLogDiffNode changeRoot = getChangeLogDiffNodeFor(logEntry);
			if (changeRoot == null) {
				changeRoot = new ChangeLogDiffNode(getModelRoot(), logEntry);
				addToViewer(changeRoot);
			}
			if(requiresCustomSyncInfo(info, remoteResource, logEntry)) {
				info = new CVSUpdatableSyncInfo(info.getKind(), info.getLocal(), info.getBase(), (RemoteResource)logEntry.getRemoteFile(), ((CVSSyncInfo)info).getSubscriber());
				try {
					info.init();
				} catch (TeamException e) {
					// this shouldn't happen, we've provided our own calculate kind
				}
			}
			element = new FullPathSyncInfoElement(changeRoot, info);
		} else {
			// For nodes without comments, simply parent with the root. These will be outgoing
			// additions.
			element = new FullPathSyncInfoElement(getModelRoot(), info);
		}	
		addToViewer(element);
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

	/*
	 * Find an existing comment set
	 * TODO: we could do better than a linear lookup?
	 */
	private ChangeLogDiffNode getChangeLogDiffNodeFor(ILogEntry entry) {
		IDiffElement[] elements = getModelRoot().getChildren();
		for (int i = 0; i < elements.length; i++) {
			IDiffElement element = elements[i];
			if(element instanceof ChangeLogDiffNode) {
				ChangeLogDiffNode other = (ChangeLogDiffNode)element;
				ILogEntry thisLog = other.getComment();
				if(thisLog.getComment().equals(entry.getComment()) && thisLog.getAuthor().equals(entry.getAuthor())) {
					return other;
				}
			}
		}
		return null;
	}
	
	/*
	 * Return if this sync info should be considered as part of a commit set.
	 */
	private boolean isInterestingChange(SyncInfo info) {
		int kind = info.getKind();
		if(info.getLocal().getType() != IResource.FILE) return false;
		if(info.getComparator().isThreeWay()) {
			return (kind & SyncInfo.DIRECTION_MASK) != SyncInfo.OUTGOING;
		}
		return true;
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
		ICVSRemoteResource[] remoteResources = (ICVSRemoteResource[]) remotes.toArray(new ICVSRemoteResource[remotes.size()]);
		if(logOperation == null) {
			logOperation = new RemoteLogOperation(null, remoteResources, tag1, tag2);
		}
		if(! remotes.isEmpty()) {
			logOperation.setRemoteResources(remoteResources);
			logOperation.execute(monitor);
		}
		return logOperation;
	}
	
	private ICVSRemoteResource getRemoteResource(CVSSyncInfo info) {
		try {
			ICVSRemoteResource remote = (ICVSRemoteResource) info.getRemote();
			ICVSRemoteResource local = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(info.getLocal());
			if(local == null) {
				local = (ICVSRemoteResource)info.getBase();
			}

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
		sortGroup.dispose();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#getViewerSorter()
	 */
	public ViewerSorter getViewerSorter() {
		int commentSort = ChangeLogModelSorter.DATE;
		int resourceSort = ChangeLogModelSorter.PATH;
		try {
			IDialogSettings pageSettings = getConfiguration().getSite().getPageSettings();
			if(pageSettings != null) {
				commentSort = pageSettings.getInt(P_LAST_COMMENTSORT);
				resourceSort = pageSettings.getInt(P_LAST_RESOURCESORT);
			}
		} catch(NumberFormatException e) {
			// ignore and use the defaults.
		}
		return new ChangeLogModelSorter(commentSort, resourceSort);
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
			removeFromViewer(local);
		}
		startUpdateJob(new SyncInfoSet(event.getChangedResources()));
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelProvider#removeFromViewer(org.eclipse.core.resources.IResource)
	 */
	protected void removeFromViewer(IResource resource) {
		// First clear the log history cache for the remote element
		if (logOperation != null) {
			ISynchronizeModelElement element = getModelObject(resource);
			if (element instanceof FullPathSyncInfoElement) {
				CVSSyncInfo info = (CVSSyncInfo) ((FullPathSyncInfoElement) element).getSyncInfo();
				if (info != null) {
					ICVSRemoteResource remote = getRemoteResource(info);
					logOperation.clearEntriesFor(remote);
				}
			}
		}
		// Clear the multiple element cache
		if(multipleResourceMap != null) {
			List elements = (List)multipleResourceMap.get(resource);
			if(elements != null) {
				for (Iterator it = elements.iterator(); it.hasNext();) {
					ISynchronizeModelElement element = (ISynchronizeModelElement) it.next();
					super.removeFromViewer(element);			
				}
				multipleResourceMap.remove(resource);
			}
		}	
		// Remove the object now
		super.removeFromViewer(resource);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelProvider#addToViewer(org.eclipse.team.ui.synchronize.ISynchronizeModelElement)
	 */
	protected void addToViewer(ISynchronizeModelElement node) {
		// Save model elements in our own mapper so that we
		// can support multiple elements for the same resource.
		IResource r = node.getResource();
		if(r != null) {
			if(multipleResourceMap == null) {
				multipleResourceMap = new HashMap(5);
			}
			List elements = (List)multipleResourceMap.get(r);
			if(elements == null) {
				elements = new ArrayList(2);
				multipleResourceMap.put(r, elements);
			}
			elements.add(node);
		}
		// The super class will do all the interesting work.
		super.addToViewer(node);
	}
}
