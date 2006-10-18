/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.IWorkingSet;

/**
 * A resource mapping for working sets
 */
public class WorkingSetResourceMapping extends ResourceMapping {

	private IWorkingSet set;
	
	/**
	 * Create the resource mapping
	 * @param workingSet the working set
	 */
	public WorkingSetResourceMapping(IWorkingSet workingSet) {
		set = workingSet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getModelObject()
	 */
	public Object getModelObject() {
		return set;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getModelProviderId()
	 */
	public String getModelProviderId() {
		return ModelProvider.RESOURCE_MODEL_PROVIDER_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getProjects()
	 */
	public IProject[] getProjects() {
		Set result = new HashSet();
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			IProject[] projects = mapping.getProjects();
			for (int j = 0; j < projects.length; j++) {
				IProject project = projects[j];
				result.add(project);
			}
		}
		return (IProject[]) result.toArray(new IProject[result.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getTraversals(org.eclipse.core.resources.mapping.ResourceMappingContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ResourceTraversal[] getTraversals(ResourceMappingContext context, IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			ResourceMapping[] mappings = getMappings();
			monitor.beginTask("", 100 * mappings.length); //$NON-NLS-1$
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

	/**
	 * Return the mappings contained in the set.
	 * @return the mappings contained in the set
	 */
	private ResourceMapping[] getMappings() {
		IAdaptable[] elements = set.getElements();
		List result = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IAdaptable element = elements[i];
			ResourceMapping mapping = WorkingSetAdapterFactory.getContributedResourceMapping(element);
			if (mapping == null) {
				mapping = WorkingSetAdapterFactory.getResourceMapping(element);
			}
			if (mapping != null) {
				result.add(mapping);
			}
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#contains(org.eclipse.core.resources.mapping.ResourceMapping)
	 */
	public boolean contains(ResourceMapping mapping) {
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping childMapping = mappings[i];
			if (childMapping.contains(mapping)) {
				return true;
			}
		}
		return false;
	}

}
