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
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.*;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.OptimizedRemoteSynchronizer;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.ResourceStateChangeListeners;

/**
 * CVSWorkspaceSubscriber
 */
public class CVSWorkspaceSubscriber extends CVSSyncTreeSubscriber implements IResourceStateChangeListener {
	
	private OptimizedRemoteSynchronizer remoteSynchronizer;
	
	// qualified name for remote sync info
	private static final String REMOTE_RESOURCE_KEY = "remote-resource-key"; //$NON-NLS-1$

	CVSWorkspaceSubscriber(QualifiedName id, String name, String description) {
		super(id, name, description);
		
		// install sync info participant
		remoteSynchronizer = new OptimizedRemoteSynchronizer(REMOTE_RESOURCE_KEY);
		
		ResourceStateChangeListeners.getListener().addResourceStateChangeListener(this); 
	}

	/* 
	 * Return the list of projects shared with a CVS team provider.
	 * 
	 * [Issue : this will have to change when folders can be shared with
	 * a team provider instead of the current project restriction]
	 * (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ISyncTreeSubscriber#roots()
	 */
	public IResource[] roots() {
		List result = new ArrayList();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if(project.isOpen()) {
				RepositoryProvider provider = RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
				if(provider != null) {
					result.add(project);
				}
			}
		}
		return (IProject[]) result.toArray(new IProject[result.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#resourceSyncInfoChanged(org.eclipse.core.resources.IResource[])
	 */
	public void resourceSyncInfoChanged(IResource[] changedResources) {
		internalResourceSyncInfoChanged(changedResources, true); 
	}

	private void internalResourceSyncInfoChanged(IResource[] changedResources, boolean canModifyWorkspace) {
		// IMPORTANT NOTE: This will throw exceptions if performed during the POST_CHANGE delta phase!!!
		for (int i = 0; i < changedResources.length; i++) {
			IResource resource = changedResources[i];
			try {
				if (resource.getType() == IResource.FILE
						&& (resource.exists() || resource.isPhantom())) {
					byte[] remoteBytes = remoteSynchronizer.getSyncBytes(resource);
					if (remoteBytes == null) {
						if (remoteSynchronizer.isRemoteKnown(resource)) {
							// The remote is known not to exist. If the local resource is
							// managed then this information is stale
							if (getBaseSynchronizer().hasRemote(resource)) {
								if (canModifyWorkspace) {
									remoteSynchronizer.removeSyncBytes(resource, IResource.DEPTH_ZERO);
								} else {
									// The revision  comparison will handle the stale sync bytes
								}
							}
						}
					} else {
						byte[] localBytes = remoteSynchronizer.getBaseSynchronizer().getSyncBytes(resource);
						if (localBytes == null || !isLaterRevision(remoteBytes, localBytes)) {
							if (canModifyWorkspace) {
								remoteSynchronizer.removeSyncBytes(resource, IResource.DEPTH_ZERO);
							} else {
								// The getRemoteResource method handles the stale sync bytes
							}
						}
					}
				} else if (resource.getType() == IResource.FOLDER) {
					// If the base has sync info for the folder, purge the remote bytes
					if (getBaseSynchronizer().hasRemote(resource) && canModifyWorkspace) {
						remoteSynchronizer.removeSyncBytes(resource, IResource.DEPTH_ZERO);
					}
				}
			} catch (TeamException e) {
				CVSProviderPlugin.log(e);
			}
		}		
		
		fireTeamResourceChange(TeamDelta.asSyncChangedDeltas(this, changedResources));
	}

	private boolean isLaterRevision(byte[] remoteBytes, byte[] localBytes) {
		try {
			return ResourceSyncInfo.isLaterRevisionOnSameBranch(remoteBytes, localBytes);
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#externalSyncInfoChange(org.eclipse.core.resources.IResource[])
	 */
	public void externalSyncInfoChange(IResource[] changedResources) {
		internalResourceSyncInfoChanged(changedResources, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#resourceModified(org.eclipse.core.resources.IResource[])
	 */
	public void resourceModified(IResource[] changedResources) {
		// This is only ever called from a delta POST_CHANGE
		// which causes problems since the workspace tree is closed
		// for modification and we flush the sync info in resourceSyncInfoChanged
		
		// Since the listeners of the Subscriber will also listen to deltas
		// we don't need to propogate this.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#projectConfigured(org.eclipse.core.resources.IProject)
	 */
	public void projectConfigured(IProject project) {
		TeamDelta delta = new TeamDelta(this, TeamDelta.PROVIDER_CONFIGURED, project);
		fireTeamResourceChange(new TeamDelta[] {delta});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#projectDeconfigured(org.eclipse.core.resources.IProject)
	 */
	public void projectDeconfigured(IProject project) {
		try {
			remoteSynchronizer.removeSyncBytes(project, IResource.DEPTH_INFINITE);
		} catch (TeamException e) {
			CVSProviderPlugin.log(e);
		}
		TeamDelta delta = new TeamDelta(this, TeamDelta.PROVIDER_DECONFIGURED, project);
		fireTeamResourceChange(new TeamDelta[] {delta});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getRemoteSynchronizer()
	 */
	protected RemoteSynchronizer getRemoteSynchronizer() {
		return remoteSynchronizer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getBaseSynchronizer()
	 */
	protected RemoteSynchronizer getBaseSynchronizer() {
		return remoteSynchronizer.getBaseSynchronizer();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#getAllOutOfSync(org.eclipse.core.resources.IResource[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public SyncInfo[] getAllOutOfSync(IResource[] resources, final int depth, IProgressMonitor monitor) throws TeamException {
		monitor.beginTask(null, resources.length * 100);
		final List result = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			final IProgressMonitor infinite = Policy.infiniteSubMonitorFor(monitor, 100);
			try {
				// We need to do a scheduling rule on the project to
				// avoid overly desctructive operations from occuring 
				// while we gather sync info
				infinite.beginTask(null, 512);
				Platform.getJobManager().beginRule(resource, Policy.subMonitorFor(infinite, 1));
				resource.accept(new IResourceVisitor() {
					public boolean visit(IResource innerResource) throws CoreException {
						try {
							if (isOutOfSync(innerResource, infinite)) {
								SyncInfo info = getSyncInfo(innerResource, infinite);
								if (info != null && info.getKind() != 0) {
									result.add(info);
								}
							}
							return true;
						} catch (TeamException e) {
							// TODO:See bug 42795
							throw new CoreException(e.getStatus());
						}
					}
				}, depth, true /* include phantoms */);
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			} finally {
				Platform.getJobManager().endRule(resource);
				infinite.done();
			}
		}
		monitor.done();
		return (SyncInfo[]) result.toArray(new SyncInfo[result.size()]);
	}
	
	/* internal use only */ boolean isOutOfSync(IResource resource, IProgressMonitor monitor) throws TeamException {
		return (hasIncomingChange(resource) || hasOutgoingChange(CVSWorkspaceRoot.getCVSResourceFor(resource), monitor));
	}
	
	private boolean hasOutgoingChange(ICVSResource resource, IProgressMonitor monitor) throws CVSException {
		if (resource.isFolder()) {
			// A folder is an outgoing change if it is not a CVS folder and not ignored
			ICVSFolder folder = (ICVSFolder)resource;
			// OPTIMIZE: The following checks load the CVS folder information
			if (folder.getParent().isModified(monitor)) {
				return !folder.isCVSFolder() && !folder.isIgnored();
			}
		} else {
			// A file is an outgoing change if it is modified
			ICVSFile file = (ICVSFile)resource;
			// The parent caches the dirty state so we only need to check
			// the file if the parent is dirty
			// OPTIMIZE: Unfortunately, the modified check on the parent still loads
			// the CVS folder information so not much is gained
			if (file.getParent().isModified(monitor)) {
				return file.isModified(monitor);
			}
		}
		return false;
	}
	
	private boolean hasIncomingChange(IResource resource) throws TeamException {
		return remoteSynchronizer.isRemoteKnown(resource);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.TeamSubscriber#getRemoteResource(org.eclipse.core.resources.IResource)
	 */
	public IRemoteResource getRemoteResource(IResource resource) throws TeamException {
		IRemoteResource remote =  super.getRemoteResource(resource);
		if (resource.getType() == IResource.FILE && remote instanceof ICVSRemoteFile) {
			byte[] remoteBytes = ((ICVSRemoteFile)remote).getSyncBytes();
			byte[] localBytes = CVSWorkspaceRoot.getCVSFileFor((IFile)resource).getSyncBytes();
			if (localBytes != null && remoteBytes != null) {
				if (!ResourceSyncInfo.isLaterRevisionOnSameBranch(remoteBytes, localBytes)) {
					// The remote bytes are stale so ignore the remote and use the base
					return getBaseResource(resource);
				}
			}
		}
		return remote;
	}

}
