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
package org.eclipse.team.internal.core.subscribers.caches;

import java.util.*;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.IResourceVariant;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.Policy;

/**
 * This class provides the logic for refreshing a resource variant tree. 
 * It provides the logic to traverse the local resource and variant resource trees in 
 * order to update the bytes stored in
 * a <code>ResourceVariantTree</code>. It also accumulates and returns all local resources 
 * for which the corresponding resource variant has changed.
 */
public abstract class ResourceVariantTreeRefresh {
	
	/**
	 * Refreshes the resource variant tree for the specified resources and possibly their descendants,
	 * depending on the depth. The default implementation of this method invokes
	 * <code>refresh(IResource, int, IProgressMonitor)</code> for each resource.
	 * Subclasses may override but should either invoke the above mentioned refresh or 
	 * <code>collectChanges</code> in order to reconcile the resource variant tree.
	 * @param resources the resources whose variants should be refreshed
	 * @param depth the depth of the refresh (one of <code>IResource.DEPTH_ZERO</code>,
	 * <code>IResource.DEPTH_ONE</code>, or <code>IResource.DEPTH_INFINITE</code>)
	 * @param monitor a progress monitor
	 * @return the array of resources whose corresponding varianst have changed
	 * @throws TeamException
	 */
	public IResource[] refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		List changedResources = new ArrayList();
		monitor.beginTask(null, 100 * resources.length);
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			IResource[] changed = refresh(resource, depth, Policy.subMonitorFor(monitor, 100));
			changedResources.addAll(Arrays.asList(changed));
		}
		monitor.done();
		if (changedResources == null) return new IResource[0];
		return (IResource[]) changedResources.toArray(new IResource[changedResources.size()]);
	}

	/**
	 * Helper method invoked from <code>refresh(IResource[], int, IProgressMonitor monitor)</code>
	 * for each resource. The default implementation performs the following steps:
	 * <ol>
	 * <li>obtaine the scheduling rule for the resource
	 * as returned from <code>getSchedulingRule(IResource)</code>. 
	 * <li>get the resource variant handle corresponding to the local resource by calling
	 * <code>getRemoteTree</code>.
	 * <li>pass the local resource and the resource variant handle to <code>collectChanges</code>
	 * </ol>
	 * Subclasses may override but should perform roughly the same steps.
	 * @param resource the resoure being refreshed
	 * @param depth the depth of the refresh  (one of <code>IResource.DEPTH_ZERO</code>,
	 * <code>IResource.DEPTH_ONE</code>, or <code>IResource.DEPTH_INFINITE</code>)
	 * @param monitor a progress monitor
	 * @return the resource's whose variants have changed
	 * @throws TeamException
	 */
	protected IResource[] refresh(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
		IResource[] changedResources = null;
		monitor.beginTask(null, 100);
		ISchedulingRule rule = getSchedulingRule(resource);
		try {
			Platform.getJobManager().beginRule(rule, monitor);
			if (!resource.getProject().isAccessible()) {
				// The project is closed so silently skip it
				return new IResource[0];
			}
			
			monitor.setTaskName(Policy.bind("SynchronizationCacheRefreshOperation.0", resource.getFullPath().makeRelative().toString())); //$NON-NLS-1$
			
			// build the remote tree only if an initial tree hasn't been provided
			IResourceVariant tree = fetchVariant(resource, depth, Policy.subMonitorFor(monitor, 70));
			
			// update the known remote handles 
			IProgressMonitor sub = Policy.infiniteSubMonitorFor(monitor, 30);
			try {
				sub.beginTask(null, 64);
				changedResources = collectChanges(resource, tree, depth, sub);
			} finally {
				sub.done();	 
			}
		} finally {
			Platform.getJobManager().endRule(rule);
			monitor.done();
		}
		if (changedResources == null) return new IResource[0];
		return changedResources;
	}

	/**
	 * Collect the changes in the remote tree to the specified depth.
	 * @param local the local resource being refreshed
	 * @param remote the corresponding resource variant
	 * @param depth the depth of the refresh  (one of <code>IResource.DEPTH_ZERO</code>,
	 * <code>IResource.DEPTH_ONE</code>, or <code>IResource.DEPTH_INFINITE</code>)
	 * @param monitor a progress monitor
	 * @return the resource's whose variants have changed
	 * @throws TeamException
	 */
	protected IResource[] collectChanges(IResource local, IResourceVariant remote, int depth, IProgressMonitor monitor) throws TeamException {
		List changedResources = new ArrayList();
		collectChanges(local, remote, changedResources, depth, monitor);
		return (IResource[]) changedResources.toArray(new IResource[changedResources.size()]);
	}
	
	/**
	 * Returns the resource variant tree that is being refreshed.
	 * @return the resource variant tree that is being refreshed.
	 */
	protected abstract ResourceVariantTree getResourceVariantTree();
	
	/**
	 * Get the bytes to be stored in the <code>ResourceVariantTree</code> 
	 * from the given resource variant.
	 * @param local the local resource
	 * @param remote the corresponding resource variant handle
	 * @return the bytes for the resource variant.
	 */
	protected abstract byte[] getBytes(IResource local, IResourceVariant remote) throws TeamException;
	
	/**
	 * Fetch the members of the given resource variant handle. This method may
	 * return members that were fetched when <code>getRemoteTree</code> was called or
	 * may fetch the children directly. 
	 * @param variant the resource variant
	 * @param progress a progress monitor
	 * @return the members of the resource variant.
	 */
	protected abstract IResourceVariant[] fetchMembers(IResourceVariant variant, IProgressMonitor progress) throws TeamException;

	/**
	 * Returns the members of the local resource. This may include all the members of
	 * the local resource or a subset that is of ineterest to the implementor.
	 * @param parent the local resource
	 * @return the members of the local resource
	 */
	protected abstract IResource[] members(IResource parent) throws TeamException;

	/**
	 * Fetch the resource variant corresponding to the given resource.
	 * The depth
	 * parameter indicates the depth of the refresh operation and also indicates the
	 * depth to which the resource variant's desendants will be traversed. 
	 * This method may prefetch the descendants to the provided depth
	 * or may just return the variant handle corresponding to the given 
	 * local resource, in which case
	 * the descendant variants will be fetched by <code>fecthMembers(IResourceVariant, IProgressMonitor)</code>.
	 * @param resource the local resource
	 * @param depth the depth of the refresh  (one of <code>IResource.DEPTH_ZERO</code>,
	 * <code>IResource.DEPTH_ONE</code>, or <code>IResource.DEPTH_INFINITE</code>)
	 * @param monitor a progress monitor
	 * @return the resource variant corresponding to the given local resource
	 */
	protected abstract IResourceVariant fetchVariant(IResource resource, int depth, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Return the scheduling rule that should be obtained for the given resource.
	 * This method is invoked from <code>refresh(IResource, int, IProgressMonitor)</code>.
	 * By default, the resource's project is returned. Subclasses may override.
	 * @param resource the resource being refreshed
	 * @return a scheduling rule or <code>null</code>
	 */
	protected ISchedulingRule getSchedulingRule(IResource resource) {
		return resource.getProject();
	}
	
	private void collectChanges(IResource local, IResourceVariant remote, Collection changedResources, int depth, IProgressMonitor monitor) throws TeamException {
		ResourceVariantTree cache = getResourceVariantTree();
		byte[] newRemoteBytes = getBytes(local, remote);
		boolean changed;
		if (newRemoteBytes == null) {
			changed = cache.setVariantDoesNotExist(local);
		} else {
			changed = cache.setBytes(local, newRemoteBytes);
		}
		if (changed) {
			changedResources.add(local);
		}
		if (depth == IResource.DEPTH_ZERO) return;
		Map children = mergedMembers(local, remote, monitor);	
		for (Iterator it = children.keySet().iterator(); it.hasNext();) {
			IResource localChild = (IResource) it.next();
			IResourceVariant remoteChild = (IResourceVariant)children.get(localChild);
			collectChanges(localChild, remoteChild, changedResources,
					depth == IResource.DEPTH_INFINITE ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, 
					monitor);
		}
		
		removeStaleBytes(local, children, changedResources);
	}

	private void removeStaleBytes(IResource local, Map children, Collection changedResources) throws TeamException {
		// Look for resources that have sync bytes but are not in the resources we care about
		ResourceVariantTree cache = getResourceVariantTree();
		IResource[] resources = getChildrenWithBytes(local);
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (!children.containsKey(resource)) {
				// These sync bytes are stale. Purge them
				cache.removeBytes(resource, IResource.DEPTH_INFINITE);
				changedResources.add(resource);
			}
		}
	}

	/*
	 * Return all the children of the local resource, including phantoms, that have bytes 
	 * associated with them in the resource varant tree of this operation. 
	 * @param local the local resource
	 * @return all children that have bytes stored in the tree.
	 * @throws TeamException
	 */
	private IResource[] getChildrenWithBytes(IResource local) throws TeamException {			
		try {
			if (local.getType() != IResource.FILE && (local.exists() || local.isPhantom())) {
				IResource[] allChildren = ((IContainer)local).members(true /* include phantoms */);
				List childrenWithSyncBytes = new ArrayList();
				for (int i = 0; i < allChildren.length; i++) {
					IResource resource = allChildren[i];
					if (getResourceVariantTree().getBytes(resource) != null) {
						childrenWithSyncBytes.add(resource);
					}
				}
				return (IResource[]) childrenWithSyncBytes.toArray(
						new IResource[childrenWithSyncBytes.size()]);
			}
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
		return new IResource[0];
	}
	
	private Map mergedMembers(IResource local, IResourceVariant remote, IProgressMonitor progress) throws TeamException {
		
		// {IResource -> IRemoteResource}
		Map mergedResources = new HashMap();
		
		IResourceVariant[] remoteChildren;
		if (remote == null) {
			remoteChildren = new IResourceVariant[0];
		} else {
			remoteChildren = fetchMembers(remote, progress);
		}
		
		
		IResource[] localChildren = members(local);		

		if (remoteChildren.length > 0 || localChildren.length > 0) {
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
					IResourceVariant remoteChild = remoteChildren[i];
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

					IResourceVariant remoteChild =
						remoteSet != null ? (IResourceVariant) remoteSet.get(keyChildName) : null;
						
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
	
	/*
	 * Create a local resource handle for a resource variant whose
	 * corresponding local resource does not exist.
	 * @param parent the local parent
	 * @param childName the name of the local resource
	 * @param isContainer the type of resource (file or folder)
	 * @return a local resource handle
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
}
