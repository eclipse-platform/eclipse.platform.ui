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
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Utility class used to warp an set of resources in a resource mapper.
 * The resulting mapper will return the workspace root as the model
 * object.
 * 
 * @since 3.1
 */
public final class WorkspaceResourceMapper extends ResourceMapping {
    
    private final IResource resource;
    private final int depth;
    
    /**
     * Convert the provided resources to one or more resource mappers
     * that traverse the elements deeply. The model element of the resource
     * mappers will be the workspace root.
     * @param resources the resources
     * @return a resource mappers that traverses the resources
     */
    public static ResourceMapping[] asResourceMappers(final IResource[] resources, int depth) {
        List result = new ArrayList();
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            result.add(new WorkspaceResourceMapper(resource, depth));
        }
        return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
    }
    
    public WorkspaceResourceMapper(IResource resource, int depth) {
        this.resource = resource;
        this.depth = depth;
    }
    public Object getModelObject() {
        return resource;
    }
    public IProject[] getProjects() {
        return new IProject[] { resource.getProject() };
    }
    public ResourceTraversal[] getTraversals(ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
        return asTraversal(resource, depth, context);
    }
    private ResourceTraversal[] asTraversal(IResource resource, final int depth, ResourceMappingContext context) {
        return new ResourceTraversal[] { new ResourceTraversal(new IResource[] { resource }, depth, IResource.NONE)} ;
    }
}