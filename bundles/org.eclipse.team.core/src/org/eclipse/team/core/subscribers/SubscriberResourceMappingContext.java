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
package org.eclipse.team.core.subscribers;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoFilter;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * A traversal context that uses the remote state of a subscriber.
 * It does not refresh it's state.
 * @since 3.1
 */
public class SubscriberResourceMappingContext extends ResourceMappingContext {
    
    Subscriber subscriber;
    private final SyncInfoFilter contentDiffFilter;
    
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
                    // When updating, only outgoing and conflicting changes are needed
                    return direction == SyncInfo.INCOMING || direction == SyncInfo.CONFLICTING ;
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
    public boolean contentDiffers(IFile file, IProgressMonitor monitor) throws CoreException {
        SyncInfo syncInfo = subscriber.getSyncInfo(file);
        validateRemote(file, syncInfo);
        return syncInfo != null && contentDiffFilter.select(syncInfo, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ResourceMappingContext#fetchContents(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStorage fetchContents(IFile file, IProgressMonitor monitor) throws CoreException {
        SyncInfo syncInfo = subscriber.getSyncInfo(file);
        IResourceVariant remote = validateRemote(file, syncInfo);
        if (remote == null) {
            return null;
        }
        return remote.getStorage(monitor);
    }

    private IResourceVariant validateRemote(IResource resource, SyncInfo syncInfo) throws CoreException {
        if (syncInfo == null) return null;
        IResourceVariant remote = syncInfo.getRemote();
        if (syncInfo == null) return null;
        boolean containerExpected = resource.getType() != IResource.FILE;
        if (remote.isContainer() && !containerExpected) {
            throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.RESOURCE_WRONG_TYPE, "Remote conterpart of {0} is a container" + resource.getFullPath().toString(), null));
        } else if (!remote.isContainer() && containerExpected) {
            throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.RESOURCE_WRONG_TYPE, "Remote conterpart of {0} is not a container" + resource.getFullPath().toString(), null));
        }
        return remote;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ResourceMappingContext#fetchMembers(org.eclipse.core.resources.IContainer, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IResource[] fetchMembers(IContainer container, IProgressMonitor monitor) throws CoreException {
        SyncInfo syncInfo = subscriber.getSyncInfo(container);
        if (validateRemote(container, syncInfo) == null) {
            // There is no remote so return null to indicate this
            return null;
        }
        return subscriber.members(container);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ResourceMappingContext#refresh(org.eclipse.core.resources.mapping.ResourceTraversal[], int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException {
        // TODO For now, refresh for each traversal.
        for (int i = 0; i < traversals.length; i++) {
            ResourceTraversal traversal = traversals[i];
            subscriber.refresh(traversal.getResources(), traversal.getDepth(), monitor);
        }
    }

}
