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
package org.eclipse.team.tests.ccvs.core.mappings;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * A traversal context that uses the remote state of a subscriber.
 * It does not refresh the subscriber's state.
 */
public class SubscriberTraversalContext extends ResourceMappingContext {
    
    Subscriber subscriber;

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ITraversalContext#contentDiffers(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean contentDiffers(IFile file, IProgressMonitor monitor) throws CoreException {
        SyncInfo syncInfo = subscriber.getSyncInfo(file);
        return syncInfo != null && syncInfo.getKind() != SyncInfo.IN_SYNC;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ITraversalContext#fetchContents(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStorage fetchContents(IFile file, IProgressMonitor monitor) throws CoreException {
        SyncInfo syncInfo = subscriber.getSyncInfo(file);
        IResourceVariant remote = syncInfo.getRemote();
        if (remote == null)
            throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.RESOURCE_NOT_FOUND, "The remote counterpart of {0} does not exist" + file.getFullPath(), null));
        return remote.getStorage(monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ITraversalContext#fetchMembers(org.eclipse.core.resources.IContainer, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IResource[] fetchMembers(IContainer container, IProgressMonitor monitor) throws CoreException {
        return subscriber.members(container);
    }

    public void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException {
        Set result = new HashSet();
        for (int i = 0; i < traversals.length; i++) {
            ResourceTraversal traversal = traversals[i];
            IResource[] resources = traversal.getResources();
            for (int j = 0; j < resources.length; j++) {
                IResource resource = resources[j];
                result.add(resource);
            }
        }
        subscriber.refresh((IResource[]) result.toArray(new IResource[result.size()]), IResource.DEPTH_INFINITE, monitor);
    }

}
