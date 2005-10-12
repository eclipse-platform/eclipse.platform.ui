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
package org.eclipse.ui.internal.ide.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * A resource mapping that contains child mappings which are used to obtain the traversals
 */
public class CompositeResourceMapping extends ResourceMapping {

    private final Object modelObject;
    private final ResourceMapping[] mappings;
    private final IProject[] projects;

    /**
     * Create a composite mapping that obtains it's traversals from a set of sub-mappings.
     * @param modelObject the model object for this mapping
     * @param mappings the sub-mappings from which the traversals are obtained
     */
    public CompositeResourceMapping(Object modelObject, ResourceMapping[] mappings) {
        this.modelObject = modelObject;
        this.mappings = mappings;
        this.projects = getProjects(mappings);
    }

    /*
     * Return an array of all the projects of the given mappings.
     */
    private IProject[] getProjects(ResourceMapping[] mappings) {
        Set result = new HashSet();
        for (int i = 0; i < mappings.length; i++) {
            ResourceMapping mapping = mappings[i];
            result.addAll(Arrays.asList(mapping.getProjects()));
        }
        return (IProject[]) result.toArray(new IProject[result.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.internal.resources.mapping.ResourceMapping#getModelObject()
     */
    public Object getModelObject() {
        return modelObject;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.internal.resources.mapping.ResourceMapping#getProjects()
     */
    public IProject[] getProjects() {
        return projects;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.internal.resources.mapping.ResourceMapping#getTraversals(org.eclipse.core.internal.resources.mapping.ResourceMappingContext, org.eclipse.core.runtime.IProgressMonitor)
     */
    public ResourceTraversal[] getTraversals(ResourceMappingContext context,
            IProgressMonitor monitor) throws CoreException {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        try {
            monitor.beginTask(null, 100 * mappings.length);
            List result = new ArrayList();
            for (int i = 0; i < mappings.length; i++) {
                ResourceMapping mapping = mappings[i];
                result.addAll(Arrays.asList(mapping.getTraversals(context, new SubProgressMonitor(monitor, 100))));
            }
            return (ResourceTraversal[]) result.toArray(new ResourceTraversal[result.size()]);
        } finally {
            monitor.done();
        }
    }

}
