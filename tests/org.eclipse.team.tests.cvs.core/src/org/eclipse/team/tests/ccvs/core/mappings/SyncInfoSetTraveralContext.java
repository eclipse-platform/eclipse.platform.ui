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
package org.eclipse.team.tests.ccvs.core.mappings;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.RemoteResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.variants.IResourceVariant;

/**
 * A traversal context that traverses the local workspace but also
 * adds resources that exist in the given sync info set but do not exist
 * locally.
 */
public class SyncInfoSetTraveralContext extends RemoteResourceMappingContext {
    
    SyncInfoTree set;
    
    public SyncInfoSetTraveralContext(SyncInfoSet set) {
        this.set = new SyncInfoTree();
        this.set.addAll(set);
    }

    protected SyncInfo getSyncInfo(IFile file) {
        return set.getSyncInfo(file);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ITraversalContext#contentDiffers(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean contentDiffers(IFile file, IProgressMonitor monitor) throws CoreException {
        return getSyncInfo(file) != null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ITraversalContext#fetchContents(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStorage fetchContents(IFile file, IProgressMonitor monitor) throws CoreException {
        SyncInfo info = getSyncInfo(file);
        //TODO: Speced to throw an exception when remote doesn't exist
        if (info == null)
            return file;
        IResourceVariant remote = info.getRemote();
        if (remote == null)
            return null;
        return remote.getStorage(monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.mapping.ITraversalContext#fetchMembers(org.eclipse.core.resources.IContainer, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IResource[] fetchMembers(IContainer container, IProgressMonitor monitor) throws CoreException {
        Set members = new HashSet();
        members.addAll(Arrays.asList(container.members(false)));
        members.addAll(Arrays.asList(set.members(container)));
        return (IResource[]) members.toArray(new IResource[members.size()]);
    }

    public void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException {
        // Do nothing
    }

}
