/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.subscribers;

import java.util.*;

import org.eclipse.core.internal.resources.mapping.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoFilter;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.core.*;

/**
 * A resource mapping context that provides the client access to the remote state 
 * of local resources using a subscriber. It uses a <code>SyncInfoFilter</code>
 * to determine whether the local contents differ from the remote contents.
 * This allows the context to be used for different operations (check-in,
 * update and replace).
 * @since 3.1
 */
public class SubscriberResourceMappingContext extends RemoteResourceMappingContext {
    
    private final Subscriber subscriber;
    private final SyncInfoFilter contentDiffFilter;
    
    // Lists used to keep track of resources that have been refreshed
    Set shallowRefresh = new HashSet();
    Set deepRefresh = new HashSet();
    
    /**
     * Return a resource mapping context suitable for a replace operations.
     * @return a resource mapping context suitable for a replace operations
     */
    public static ResourceMappingContext getReplaceContext(Subscriber subscriber) {
        return new SubscriberResourceMappingContext(subscriber, new SyncInfoFilter() {
            public boolean select(SyncInfo info, IProgressMonitor monitor) {
                if (info != null) {
                    int direction = info.getKind() & SyncInfo.DIRECTION_MASK;
                    // When replacing, both incoming and outgoing changes are needed
                    return direction != 0;
                }
                return false;
            }
        
        });
    }
    
    /**
     * Return a resource mapping context suitable for a update operations.
     * That is, operations that fetch the latest remote changes from the 
     * server to update the local workspace resources.
     * @return a resource mapping context suitable for a update operations
     */
    public static ResourceMappingContext getUpdateContext(Subscriber subscriber) {
        return new SubscriberResourceMappingContext(subscriber, new SyncInfoFilter() {
            public boolean select(SyncInfo info, IProgressMonitor monitor) {
                if (info != null) {
                    int direction = info.getKind() & SyncInfo.DIRECTION_MASK;
                    // When updating, only incoming and conflicting changes are needed
                    return direction == SyncInfo.INCOMING || direction == SyncInfo.CONFLICTING ;
                }
                return false;
            }
        
        });
    }
    
    /**
     * Return a resource mapping context suitable for a check-in (or commit) operations.
     * That is, operations that uploads the latest local changes to the 
     * server from the local workspace resources, typically creating a new version of the resource.
     * @return a resource mapping context suitable for a check-in operations
     */
    public static ResourceMappingContext getCheckInContext(Subscriber subscriber) {
        return new SubscriberResourceMappingContext(subscriber, new SyncInfoFilter() {
            public boolean select(SyncInfo info, IProgressMonitor monitor) {
                if (info != null) {
                    int direction = info.getKind() & SyncInfo.DIRECTION_MASK;
                    // When committing, only outgoing and conflicting changes are needed
                    return direction == SyncInfo.OUTGOING || direction == SyncInfo.CONFLICTING ;
                }
                return false;
            }
        
        });
    }

    /**
     * Return a resource mapping context suitable for comparison operations.
     * Comparisons require that any out-of-sync resources have contents
     * that differ.
     * @return a resource mapping context suitable for compare operations
     */
    public static ResourceMappingContext getCompareContext(Subscriber subscriber) {
        return new SubscriberResourceMappingContext(subscriber, new SyncInfoFilter() {
            public boolean select(SyncInfo info, IProgressMonitor monitor) {
                if (info != null) {
                    return info.getKind() != SyncInfo.IN_SYNC;
                }
                return false;
            }
        
        });
    }
    
    /**
     * Create a resource mapping context for the given subscriber
     * @param subscriber the subscriber
     * @param contentDiffFilter filter that is used to determine if the remote contents differ 
     * from the local contents
     */
    public SubscriberResourceMappingContext(Subscriber subscriber, SyncInfoFilter contentDiffFilter) {
        this.subscriber = subscriber;
        this.contentDiffFilter = contentDiffFilter;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ResourceMappingContext#contentDiffers(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
     */
    public final boolean contentDiffers(IFile file, IProgressMonitor monitor) throws CoreException {
    	try {
			monitor.beginTask(null, 100);
			ensureRefreshed(file, IResource.DEPTH_ZERO, NONE, Policy.subMonitorFor(monitor, 10));
			SyncInfo syncInfo = subscriber.getSyncInfo(file);
			validateRemote(file, syncInfo);
			return syncInfo != null && contentDiffFilter.select(syncInfo, Policy.subMonitorFor(monitor, 90));
		} finally {
			monitor.done();
		}
    }

	/* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ResourceMappingContext#fetchContents(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
     */
    public final IStorage fetchContents(IFile file, IProgressMonitor monitor) throws CoreException {
    	try {
			monitor.beginTask(null, 100);
	    	ensureRefreshed(file, IResource.DEPTH_ZERO, FILE_CONTENTS_REQUIRED, Policy.subMonitorFor(monitor, 10));
	        SyncInfo syncInfo = subscriber.getSyncInfo(file);
	        IResourceVariant remote = validateRemote(file, syncInfo);
	        if (remote == null) {
	            return null;
	        }
	        return remote.getStorage(Policy.subMonitorFor(monitor, 90));
		} finally {
			monitor.done();
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ResourceMappingContext#fetchMembers(org.eclipse.core.resources.IContainer, org.eclipse.core.runtime.IProgressMonitor)
     */
    public final IResource[] fetchMembers(IContainer container, IProgressMonitor monitor) throws CoreException {
    	try {
			monitor.beginTask(null, 100);
	    	ensureRefreshed(container, IResource.DEPTH_ONE, NONE, Policy.subMonitorFor(monitor, 100));
	        SyncInfo syncInfo = subscriber.getSyncInfo(container);
	        if (validateRemote(container, syncInfo) == null) {
	            // There is no remote so return null to indicate this
	            return null;
	        }
	        return subscriber.members(container);
		} finally {
			monitor.done();
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ResourceMappingContext#refresh(org.eclipse.core.resources.mapping.ResourceTraversal[], int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public final void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException {
        Set zero = new HashSet();
        Set one = new HashSet();
        Set infinite = new HashSet();
        for (int i = 0; i < traversals.length; i++) {
            ResourceTraversal traversal = traversals[i];
            switch (traversal.getDepth()) {
			case IResource.DEPTH_INFINITE:
				infinite.addAll(Arrays.asList(traversal.getResources()));
				break;
			case IResource.DEPTH_ONE:
				one.addAll(Arrays.asList(traversal.getResources()));
				break;
			case IResource.DEPTH_ZERO:
				zero.addAll(Arrays.asList(traversal.getResources()));
				break;
			}
        }
        if (!zero.isEmpty())
            refresh((IResource[]) zero.toArray(new IResource[zero.size()]), IResource.DEPTH_ZERO, flags, monitor);
        if (!one.isEmpty())
            refresh((IResource[]) one.toArray(new IResource[one.size()]), IResource.DEPTH_ONE, flags, monitor);
        if (!infinite.isEmpty())
            refresh((IResource[]) infinite.toArray(new IResource[infinite.size()]), IResource.DEPTH_INFINITE, flags, monitor);
    }

    /**
     * Refresh the subscriber and cache the fact that the resources were refreshed by
     * calling the <code>refreshed</code> method. The default implementation only refreshes
     * the state and does not fetch contents in the <code>FILE_CONTENTS_REQUIRED</code>
     * flag is passed. It is up to subclass to handle this.
     * @param resources the resources to be refreshed
     * @param depth the depth of the refresh
     * @param flags the flags that indicate extra state that shoudl be fetched
     * @param monitor a progress monitor
     * @throws TeamException
     */
	protected void refresh(IResource[] resources, int depth, int flags, IProgressMonitor monitor) throws TeamException {
		subscriber.refresh(resources, depth, monitor);
		refreshed(resources, depth);
	}

	/**
	 * Record the fact that the resources have been refreshed to the given depth.
	 * This is done so that accesses to refreshed resources will not need to perform
	 * another refresh.
	 * @param resources the resources that were refreshed
	 * @param depth the depth to which the resources were refreshed
	 */
	protected final void refreshed(IResource[] resources, int depth) {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			// Include files and depth-one folders as shallow
			if (depth == IResource.DEPTH_ONE || resource.getType() == IResource.FILE) {
				shallowRefresh.add(resource);		
			} else if (depth == IResource.DEPTH_INFINITE) {
				deepRefresh.add(resource);
			}
		}
	}
	
    /*
     * Ensure that the given resource has been refreshed to the specified depth
     * since the context has been created.
     */
    private void ensureRefreshed(IResource resource, int depth, int flags, IProgressMonitor monitor) throws TeamException {
		if (depth == IResource.DEPTH_INFINITE) {
			// If the resource or a parent was refreshed deeply, no need to do it again
			if (wasRefreshedDeeply(resource))
				return;
			// if the resource is a file, a shallow refresh is enough
			if (resource.getType() == IResource.FILE && wasRefreshedShallow(resource))
				return;
		} else {
			if (wasRefreshedShallow(resource))
				return;
		}
		refresh(new IResource[] { resource }, depth, flags, monitor);
	}

    /*
     * Look for a shallow refresh of the resource. If not there,
     * look fir a deep refresh of a parent or a shallow refresh of the
     * direct parent if the resource is a file.
     */
	private boolean wasRefreshedShallow(IResource resource) {
		if  (shallowRefresh.contains(resource)) 
			return true;
		if (resource.getType() == IResource.FILE && shallowRefresh.contains(resource.getParent()))
			return true;
		if (wasRefreshedDeeply(resource))
			return true;
		return false;
	}

	/*
	 * Look for a deep refresh of the resource or any of it's parents
	 */
	private boolean wasRefreshedDeeply(IResource resource) {
		if (resource.getType() == IResource.ROOT)
			return false;
		if (deepRefresh.contains(resource))
			return true;
		return wasRefreshedDeeply(resource.getParent());
	}
	
	/*
	 * Validate that the remote resource is of the proper type and return the
	 * remote resource if it is OK. A return of null indicates that there is no remote.
	 */
    private IResourceVariant validateRemote(IResource resource, SyncInfo syncInfo) throws CoreException {
        if (syncInfo == null) return null;
        IResourceVariant remote = syncInfo.getRemote();
        if (remote == null) return null;
        boolean containerExpected = resource.getType() != IResource.FILE;
        if (remote.isContainer() && !containerExpected) {
            throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.RESOURCE_WRONG_TYPE, Messages.SubscriberResourceMappingContext_0 + resource.getFullPath().toString(), null));
        } else if (!remote.isContainer() && containerExpected) {
            throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.RESOURCE_WRONG_TYPE, Messages.SubscriberResourceMappingContext_1 + resource.getFullPath().toString(), null));
        }
        return remote;
    }
}
