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
package org.eclipse.team.internal.ccvs.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ContentComparisonCriteria;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.core.subscribers.TeamProvider;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.RemoteSynchronizer;
import org.eclipse.team.internal.ccvs.core.syncinfo.RemoteTagSynchronizer;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSynchronizer;
import org.eclipse.team.internal.core.SaveContext;

/**
 * A CVSMergeSubscriber is responsible for maintaining the remote trees for a merge into
 * the workspace. The remote trees represent the CVS revisions of the start and end
 * points (version or branch) of the merge.
 * 
 * This subscriber stores the remote handles in the resource tree sync info slot. When
 * the merge is cancelled this sync info is cleared.
 * 
 * A merge can persist between workbench sessions and thus can be used as an
 * ongoing merge.
 * 
 * TODO: Is the merge subscriber interested in workspace sync info changes?
 * TODO: Do certain operations (e.g. replace with) invalidate a merge subscriber?
 * TODO: How to ensure that sync info is flushed when merge roots are deleted?
 */
public class CVSMergeSubscriber extends CVSSyncTreeSubscriber implements IResourceChangeListener {

	public static final String UNIQUE_ID_PREFIX = "merge-";
	
	private CVSTag start, end;
	private List roots;
	private RemoteTagSynchronizer remoteSynchronizer;
	private RemoteSynchronizer mergedSynchronizer;
	private RemoteTagSynchronizer baseSynchronizer;

	private static final byte[] NO_REMOTE = new byte[0];
	

	protected IResource[] refreshRemote(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		IResource[] remoteChanges = super.refreshRemote(resources, depth, monitor);
		adjustMergedResources(remoteChanges);
		return remoteChanges;
	}

	private void adjustMergedResources(IResource[] remoteChanges) throws CVSException {
		for (int i = 0; i < remoteChanges.length; i++) {
			IResource resource = remoteChanges[i];
			mergedSynchronizer.removeSyncBytes(resource, IResource.DEPTH_ZERO);			
		}	
	}

	private static QualifiedName getUniqueId() {
		String uniqueId = Long.toString(System.currentTimeMillis());
		return new QualifiedName(CVSSubscriberFactory.ID, UNIQUE_ID_PREFIX + uniqueId);
	}
	
	public CVSMergeSubscriber(IResource[] roots, CVSTag start, CVSTag end) {		
		this(getUniqueId(), roots, start, end);
	}
	
	public CVSMergeSubscriber(QualifiedName id, IResource[] roots, CVSTag start, CVSTag end) {		
		super(id, "CVS Merge: " + start.getName() + " to " + end.getName(), "CVS Merge");
		this.start = start;
		this.end = end;
		this.roots = new ArrayList(Arrays.asList(roots));
		initialize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSWorkspaceSubscriber#initialize()
	 */
	private void initialize() {				
		QualifiedName id = getId();
		String syncKeyPrefix = id.getLocalName();
		remoteSynchronizer = new RemoteTagSynchronizer(syncKeyPrefix + end.getName(), end);
		baseSynchronizer = new RemoteTagSynchronizer(syncKeyPrefix + start.getName(), start);
		mergedSynchronizer = new RemoteSynchronizer(syncKeyPrefix + "0merged");
		
		// TODO: must use revision comparison until bugs 40035 and 40129 are addressed
//		try {
//			setCurrentComparisonCriteria(ContentComparisonCriteria.ID_IGNORE_WS);
//		} catch (TeamException e) {
//			// use the default but log an exception because the content comparison should
//			// always be available.
//			CVSProviderPlugin.log(e);
//		}
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	protected SyncInfo getSyncInfo(IResource local, IRemoteResource base, IRemoteResource remote, IProgressMonitor monitor) throws TeamException {
		return new CVSMergeSyncInfo(local, base, remote, this, monitor);
	}

	public void merged(IResource[] resources) throws CVSException {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			byte[] remoteBytes = remoteSynchronizer.getSyncBytes(resource);
			if (remoteBytes == null) {
				// If there is no remote, use a place holder to indicate the resouce was merged
				remoteBytes = NO_REMOTE;
			}
			mergedSynchronizer.setSyncBytes(resource, remoteBytes);
		}
		fireTeamResourceChange(TeamDelta.asSyncChangedDeltas(this, resources));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#cancel()
	 */
	public void cancel() {
		super.cancel();		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);		
		TeamProvider.deregisterSubscriber(this);		
		remoteSynchronizer.dispose();
		baseSynchronizer.dispose();
		mergedSynchronizer.dispose();		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#isCancellable()
	 */
	public boolean isCancellable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#roots()
	 */
	public IResource[] roots() {
		return (IResource[]) roots.toArray(new IResource[roots.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getRemoteSynchronizer()
	 */
	protected ResourceSynchronizer getRemoteSynchronizer() {
		return remoteSynchronizer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getBaseSynchronizer()
	 */
	protected ResourceSynchronizer getBaseSynchronizer() {
		return baseSynchronizer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#isSupervised(org.eclipse.core.resources.IResource)
	 */
	public boolean isSupervised(IResource resource) throws TeamException {
		return getBaseSynchronizer().getSyncBytes(resource) != null || getRemoteSynchronizer().getSyncBytes(resource) != null; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#saveState(org.eclipse.team.internal.core.SaveContext)
	 */
	public SaveContext saveState() {
		// start and end tags
		SaveContext state = new SaveContext();
		state.setName("merge");
		state.putString("startTag", start.getName());
		state.putInteger("startTagType", start.getType());
		state.putString("endTag", end.getName());
		state.putInteger("endTagType", end.getType());
		
		// resources roots
		SaveContext[] ctxRoots = new SaveContext[roots.size()];
		int i = 0;
		for (Iterator it = roots.iterator(); it.hasNext(); i++) {
			IResource element = (IResource) it.next();
			ctxRoots[i] = new SaveContext();
			ctxRoots[i].setName("resource");
			ctxRoots[i].putString("fullpath", element.getFullPath().toString());			
		}
		state.setChildren(ctxRoots);
		return state;
	}
	
	public static CVSMergeSubscriber restore(QualifiedName id, SaveContext saveContext) throws CVSException {
		String name = saveContext.getName(); 
		if(! name.equals("merge")) {
			throw new CVSException("error restoring merge subscriber: " + name + " is an invalid save context.");
		}
		
		CVSTag start = new CVSTag(saveContext.getString("startTag"), saveContext.getInteger("startTagType"));
		CVSTag end = new CVSTag(saveContext.getString("endTag"), saveContext.getInteger("endTagType"));
		
		SaveContext[] ctxRoots = saveContext.getChildren();
		if(ctxRoots == null || ctxRoots.length == 0) {
			throw new CVSException("error restoring merge subscriber: there are no roots in the save context.");
		}
		
		List resources = new ArrayList();
		for (int i = 0; i < ctxRoots.length; i++) {
			SaveContext context = ctxRoots[i];
			IPath path = new Path(context.getString("fullpath"));
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path, true /* include phantoms */);
			if(resource != null) {
				resources.add(resource);
			} else {
				// log that a resource previously in the merge set is no longer in the workspace
				CVSProviderPlugin.log(CVSStatus.INFO, "ignoring root resource not found in current workspace", null);
			}
		}
		if(resources.isEmpty()) {
			throw new CVSException("error restoring merge subscriber: there are no existing roots in the save context.");
		}
		IResource[] roots = (IResource[]) resources.toArray(new IResource[resources.size()]);
		return new CVSMergeSubscriber(id, roots, start, end);
	}
	
	public CVSTag getStartTag() {
		return start;
	}
	
	public CVSTag getEndTag() {
		return end;
	}
	
	public boolean isReleaseSupported() {
		// you can't release changes to a merge
		return false;
	}

	/*
	 * What to do when a root resource for this merge changes?
	 * Deleted, Move, Copied
	 * Changed in a CVS way (tag changed, revision changed...)
	 * Contents changed by user
	 * @see IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResourceDelta delta = event.getDelta();
			if(delta != null) {
				delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
			
					if (resource.getType()==IResource.PROJECT) {
						IProject project = (IProject)resource;
						if (!project.isAccessible()) {
							return false;
						}
						if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
							return false;
						} 
						if (RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId()) == null) {
							return false;
						}
					}
			
					if (roots.contains(resource)) {
						if (delta.getKind() == IResourceDelta.REMOVED || delta.getKind() == IResourceDelta.MOVED_TO) {
							cancel();
						}
						// stop visiting children
						return false;
					}
					// keep visiting children
					return true;
				}
			});
			}
		} catch (CoreException e) {
			CVSProviderPlugin.log(e.getStatus());
		}
	}

	public boolean isMerged(IResource resource) throws CVSException {
		return mergedSynchronizer.getSyncBytes(resource) != null;
	}		
}