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
import org.eclipse.team.core.subscribers.ISubscriberChangeEvent;
import org.eclipse.team.core.subscribers.SubscriberChangeEvent;
import org.eclipse.team.core.synchronize.IResourceVariant;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.core.syncinfo.*;
import org.eclipse.team.internal.ccvs.core.util.ResourceStateChangeListeners;
import org.eclipse.team.internal.core.subscribers.caches.PersistantResourceVariantByteStore;
import org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * CVSWorkspaceSubscriber
 */
public class CVSWorkspaceSubscriber extends CVSSyncTreeSubscriber implements IResourceStateChangeListener {
	
	private CVSDescendantSynchronizationCache remoteSynchronizer;
	private ResourceVariantByteStore baseSynchronizer;
	
	// qualified name for remote sync info
	private static final String REMOTE_RESOURCE_KEY = "remote-resource-key"; //$NON-NLS-1$

	CVSWorkspaceSubscriber(QualifiedName id, String name, String description) {
		super(id, name, description);
		
		// install sync info participant
		baseSynchronizer = new CVSBaseSynchronizationCache();
		remoteSynchronizer = new CVSDescendantSynchronizationCache(
				baseSynchronizer, 
				new PersistantResourceVariantByteStore(new QualifiedName(SYNC_KEY_QUALIFIER, REMOTE_RESOURCE_KEY)));
		
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
					byte[] remoteBytes = remoteSynchronizer.getBytes(resource);
					if (remoteBytes == null) {
						if (remoteSynchronizer.isVariantKnown(resource)) {
							// The remote is known not to exist. If the local resource is
							// managed then this information is stale
							if (getBaseSynchronizationCache().getBytes(resource) != null) {
								if (canModifyWorkspace) {
									remoteSynchronizer.flushBytes(resource, IResource.DEPTH_ZERO);
								} else {
									// The revision  comparison will handle the stale sync bytes
									// TODO: Unless the remote is known not to exist (see bug 52936)
								}
							}
						}
					} else {
						byte[] localBytes = baseSynchronizer.getBytes(resource);
						if (localBytes == null || !isLaterRevision(remoteBytes, localBytes)) {
							if (canModifyWorkspace) {
								remoteSynchronizer.flushBytes(resource, IResource.DEPTH_ZERO);
							} else {
								// The getRemoteResource method handles the stale sync bytes
							}
						}
					}
				} else if (resource.getType() == IResource.FOLDER) {
					// If the base has sync info for the folder, purge the remote bytes
					if (getBaseSynchronizationCache().getBytes(resource) != null && canModifyWorkspace) {
						remoteSynchronizer.flushBytes(resource, IResource.DEPTH_ZERO);
					}
				}
			} catch (TeamException e) {
				CVSProviderPlugin.log(e);
			}
		}		
		
		fireTeamResourceChange(SubscriberChangeEvent.asSyncChangedDeltas(this, changedResources));
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
		SubscriberChangeEvent delta = new SubscriberChangeEvent(this, ISubscriberChangeEvent.ROOT_ADDED, project);
		fireTeamResourceChange(new SubscriberChangeEvent[] {delta});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#projectDeconfigured(org.eclipse.core.resources.IProject)
	 */
	public void projectDeconfigured(IProject project) {
		try {
			remoteSynchronizer.flushBytes(project, IResource.DEPTH_INFINITE);
		} catch (TeamException e) {
			CVSProviderPlugin.log(e);
		}
		SubscriberChangeEvent delta = new SubscriberChangeEvent(this, ISubscriberChangeEvent.ROOT_REMOVED, project);
		fireTeamResourceChange(new SubscriberChangeEvent[] {delta});
	}

	public void setRemote(IProject project, IResourceVariant remote, IProgressMonitor monitor) throws TeamException {
		// TODO: This exposes internal behavior to much
		IResource[] changedResources = 
			new CVSRefreshOperation(remoteSynchronizer, baseSynchronizer, null, false).collectChanges(project, remote, IResource.DEPTH_INFINITE, monitor);
		if (changedResources.length != 0) {
			fireTeamResourceChange(SubscriberChangeEvent.asSyncChangedDeltas(this, changedResources));
		}
	}
	
	protected IResource[] refreshBase(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		// TODO Ensure that file contents are cached for modified local files
		try {
			monitor.beginTask(null, 100);
			return new IResource[0];
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getRemoteTag()
	 */
	protected CVSTag getRemoteTag() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getBaseTag()
	 */
	protected CVSTag getBaseTag() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getBaseSynchronizationCache()
	 */
	protected ResourceVariantByteStore getBaseSynchronizationCache() {
		return baseSynchronizer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber#getRemoteSynchronizationCache()
	 */
	protected ResourceVariantByteStore getRemoteSynchronizationCache() {
		return remoteSynchronizer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.TeamSubscriber#getAllOutOfSync(org.eclipse.core.resources.IResource[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public SyncInfo[] getAllOutOfSync(IResource[] resources, final int depth, final IProgressMonitor monitor) throws TeamException {
		monitor.beginTask(null, IProgressMonitor.UNKNOWN);
		final List result = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			try {
				// We need to do a scheduling rule on the project to
				// avoid overly desctructive operations from occuring 
				// while we gather sync info
				Platform.getJobManager().beginRule(resource, monitor);
				resource.accept(new IResourceVisitor() {
					public boolean visit(IResource innerResource) throws CoreException {
						try {
							if (innerResource.getType() != IResource.FILE) {
								monitor.subTask(Policy.bind("CVSWorkspaceSubscriber.1", innerResource.getFullPath().toString())); //$NON-NLS-1$
							}
							if (isOutOfSync(innerResource, monitor)) {
								SyncInfo info = getSyncInfo(innerResource);
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
				monitor.done();
			}
		}
		monitor.done();
		return (SyncInfo[]) result.toArray(new SyncInfo[result.size()]);
	}
	
	/* internal use only */ boolean isOutOfSync(IResource resource, IProgressMonitor monitor) throws TeamException {
		return (hasIncomingChange(resource) || hasOutgoingChange(resource, monitor));
	}
	
	private boolean hasIncomingChange(IResource resource) throws TeamException {
		return remoteSynchronizer.isVariantKnown(resource);
	}
	
	private boolean hasOutgoingChange(IResource resource, IProgressMonitor monitor) throws CVSException {
		if (resource.getType() == IResource.PROJECT || resource.getType() == IResource.ROOT) {
			// a project (or the workspace root) cannot have outgoing changes
			return false;
		}
		int state = EclipseSynchronizer.getInstance().getModificationState(resource.getParent());
		if (state == ICVSFile.CLEAN) {
			// if the parent is known to be clean then the resource must also be clean
			return false;
		}
		if (resource.getType() == IResource.FILE) {
			// A file is an outgoing change if it is modified
			ICVSFile file = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
			return file.isModified(monitor);
		} else {
			// A folder is an outgoing change if it is not a CVS folder and not ignored
			ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)resource);
			return !folder.isCVSFolder() && !folder.isIgnored();
		}
	}

}
