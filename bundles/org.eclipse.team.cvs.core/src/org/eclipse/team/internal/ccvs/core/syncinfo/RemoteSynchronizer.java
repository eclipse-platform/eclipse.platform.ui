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
package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * A remote resource sychronizer caches the remote sync bytes that can be 
 * used to create remote handles
 */
public class RemoteSynchronizer extends ResourceSynchronizer {
	
	public static final String SYNC_KEY_QUALIFIER = "org.eclipse.team.cvs";
	
	private static final byte[] NO_REMOTE = new byte[0];
	
	private QualifiedName syncName;
	private Set changedResources = new HashSet();
	private CVSTag tag;
	
	public RemoteSynchronizer(String id, CVSTag tag) {
		syncName = new QualifiedName(SYNC_KEY_QUALIFIER, id);
		getSynchronizer().add(syncName);
		this.tag = tag;
	}
	
	/**
	 * Dispose of any cached remote sync info.
	 */
	public void dispose() {
		getSynchronizer().remove(getSyncName());
	}
	
	private ISynchronizer getSynchronizer() {
		return ResourcesPlugin.getWorkspace().getSynchronizer();
	}
	
	private QualifiedName getSyncName() {
		return syncName;
	}
	
	/*
	 * Get the sync bytes for the remote resource
	 */
	public byte[] getSyncBytes(IResource resource) throws CVSException {
		try {
			return getSynchronizer().getSyncInfo(getSyncName(), resource);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	public void setSyncBytes(IResource resource, byte[] bytes) throws CVSException {
		byte[] oldBytes = getSyncBytes(resource);
		if (oldBytes != null && Util.equals(oldBytes, bytes)) return;
		try {
			getSynchronizer().setSyncInfo(getSyncName(), resource, bytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
		changedResources.add(resource);
	}
	
	public void removeSyncBytes(IResource resource, int depth) throws CVSException {
		if (resource.exists() || resource.isPhantom()) {
			try {
				getSynchronizer().flushSyncInfo(getSyncName(), resource, depth);
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			}
			changedResources.add(resource);
		}
	}

	/**
	 * @param resource
	 */
	public void collectChanges(IResource local, ICVSRemoteResource remote, int depth, IProgressMonitor monitor) throws TeamException {
		byte[] remoteBytes;
		if (remote != null) {
			remoteBytes = ((RemoteResource)remote).getSyncBytes();
		} else {
			remoteBytes = NO_REMOTE;
		}
		setSyncBytes(local, remoteBytes);
		if (depth == IResource.DEPTH_ZERO) return;
		Map children = mergedMembers(local, remote, monitor);	
		for (Iterator it = children.keySet().iterator(); it.hasNext();) {
			IResource localChild = (IResource) it.next();
			ICVSRemoteResource remoteChild = (ICVSRemoteResource)children.get(localChild);
			collectChanges(localChild, remoteChild, 
				depth == IResource.DEPTH_INFINITE ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, 
				monitor);
		}
	}
	
	protected Map mergedMembers(IResource local, IRemoteResource remote, IProgressMonitor progress) throws TeamException {
	
		// {IResource -> IRemoteResource}
		Map mergedResources = new HashMap();
		
		IRemoteResource[] remoteChildren = getRemoteChildren(remote, progress);
		
		IResource[] localChildren = getLocalChildren(local);		

		if (remoteChildren.length > 0 || localChildren.length > 0) {
			List syncChildren = new ArrayList(10);
			Set allSet = new HashSet(20);
			Map localSet = null;
			Map remoteSet = null;

			if (localChildren.length > 0) {
				localSet = new HashMap(10);
				for (int i = 0; i < localChildren.length; i++) {
					IResource localChild = localChildren[i];
					String name = localChild.getName();
					localSet.put(name, localChild);
					allSet.add(name);
				}
			}

			if (remoteChildren.length > 0) {
				remoteSet = new HashMap(10);
				for (int i = 0; i < remoteChildren.length; i++) {
					IRemoteResource remoteChild = remoteChildren[i];
					String name = remoteChild.getName();
					remoteSet.put(name, remoteChild);
					allSet.add(name);
				}
			}
		
			Iterator e = allSet.iterator();
			while (e.hasNext()) {
				String keyChildName = (String) e.next();

				if (progress != null) {
					if (progress.isCanceled()) {
						throw new OperationCanceledException();
					}
					// XXX show some progress?
				}

				IResource localChild =
					localSet != null ? (IResource) localSet.get(keyChildName) : null;

				IRemoteResource remoteChild =
					remoteSet != null ? (IRemoteResource) remoteSet.get(keyChildName) : null;
				
				if (localChild == null) {
					// there has to be a remote resource available if we got this far
					Assert.isTrue(remoteChild != null);
					boolean isContainer = remoteChild.isContainer();				
					localChild = getResourceChild(local /* parent */, keyChildName, isContainer);
				}
				mergedResources.put(localChild, remoteChild);				
			}
		}
		return mergedResources;
	}
	
	private IRemoteResource[] getRemoteChildren(IRemoteResource remote, IProgressMonitor progress) throws TeamException {
		return remote != null ? remote.members(progress) : new IRemoteResource[0];
	}

	private IResource[] getLocalChildren(IResource local) throws TeamException {
		IResource[] localChildren;			
		if( local.getType() != IResource.FILE && local.exists() ) {
			// TODO: This should be a list of all non-ignored resources including outgoing deletions
			ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)local);
			ICVSResource[] cvsChildren = cvsFolder.members(ICVSFolder.MANAGED_MEMBERS | ICVSFolder.UNMANAGED_MEMBERS);
			List resourceChildren = new ArrayList();
			for (int i = 0; i < cvsChildren.length; i++) {
				ICVSResource cvsResource = cvsChildren[i];
				resourceChildren.add(cvsResource.getIResource());
			}
			localChildren = (IResource[]) resourceChildren.toArray(new IResource[resourceChildren.size()]);
		} else {
			localChildren = new IResource[0];
		}
		return localChildren;
	}
	
	/*
	 * Returns a handle to a non-existing resource.
	 */
	private IResource getResourceChild(IResource parent, String childName, boolean isContainer) {
		if (parent.getType() == IResource.FILE) {
			return null;
		}
		if (isContainer) {
			return ((IContainer) parent).getFolder(new Path(childName));
		} else {
			return ((IContainer) parent).getFile(new Path(childName));
		}
	}
	
	/**
	 * 
	 * @param resource
	 * @return
	 * @throws TeamException
	 */
	public IRemoteResource getRemoteResource(IResource resource) throws TeamException {
		byte[] remoteBytes = getSyncBytes(resource);
		if (remoteBytes == null || remoteDoesNotExist(remoteBytes)) {
			// The remote is known to not exist or there is no base
			return null;
		} else {
			return super.getRemoteResource(resource);
		}
	}
	
	/*
	 * Return true if the given bytes indocate that the remote does not exist.
	 * The provided byte array must not be null;
	 */
	private boolean remoteDoesNotExist(byte[] remoteBytes) {
		Assert.isNotNull(remoteBytes);
		return Util.equals(remoteBytes, NO_REMOTE);
	}
	
	/**
	 * @return
	 */
	public IResource[] getChangedResources() {
		return (IResource[]) changedResources.toArray(new IResource[changedResources.size()]);
	}
	
	public void resetChanges() {
		changedResources.clear();
	}

	/**
	 * Refreshes the contents of the remote synchronizer and returns the list
	 * of resources whose remote sync state changed.
	 * 
	 * @param resources
	 * @param depth
	 * @param monitor
	 * @return
	 * @throws TeamException
	 */
	public IResource[] refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		int work = 100 * resources.length;
		monitor.beginTask(null, work);
		resetChanges();
		try {
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];	
				
				// build the remote tree only if an initial tree hasn't been provided
				ICVSRemoteResource	tree = buildRemoteTree(resource, depth, Policy.subMonitorFor(monitor, 70));
				
				// update the known remote handles 
				IProgressMonitor sub = Policy.infiniteSubMonitorFor(monitor, 30);
				try {
					sub.beginTask(null, 512);
					removeSyncBytes(resource, IResource.DEPTH_INFINITE);
					collectChanges(resource, tree, depth, sub);
				} finally {
					sub.done();	 
				}
			}
		} finally {
			monitor.done();
		}
		IResource[] changes = getChangedResources();
		resetChanges();
		return changes;
	}
	
	/**
	 * Build a remote tree for the given parameters.
	 */
	protected ICVSRemoteResource buildRemoteTree(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
		// TODO: we are currently ignoring the depth parameter because the build remote tree is
		// by default deep!
		return CVSWorkspaceRoot.getRemoteTree(resource, tag, monitor);
	}
}
