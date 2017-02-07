/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
import org.eclipse.core.runtime.SubMonitor;
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

	@Override
	public Object getModelObject() {
		return set;
	}

	@Override
	public String getModelProviderId() {
		return ModelProvider.RESOURCE_MODEL_PROVIDER_ID;
	}

	@Override
	public IProject[] getProjects() {
		Set<IProject> result = new HashSet<>();
		for (ResourceMapping mapping : getMappings()) {
			for (IProject project : mapping.getProjects()) {
				result.add(project);
			}
		}
		return result.toArray(new IProject[result.size()]);
	}

	@Override
	public ResourceTraversal[] getTraversals(ResourceMappingContext context, IProgressMonitor mon)
			throws CoreException {
		ResourceMapping[] mappings = getMappings();
		SubMonitor subMonitor = SubMonitor.convert(mon, mappings.length);

		List<ResourceTraversal> result = new ArrayList<>();
		for (ResourceMapping mapping : mappings) {
			result.addAll(Arrays.asList(mapping.getTraversals(context, subMonitor.split(1))));
		}
		return result.toArray(new ResourceTraversal[result.size()]);
	}

	/**
	 * Return the mappings contained in the set.
	 * @return the mappings contained in the set
	 */
	private ResourceMapping[] getMappings() {
		List<ResourceMapping> result = new ArrayList<>();
		for (IAdaptable adaptable : set.getElements()) {
			ResourceMapping mapping = WorkingSetAdapterFactory.getContributedResourceMapping(adaptable);
			if (mapping == null) {
				mapping = WorkingSetAdapterFactory.getResourceMapping(adaptable);
			}
			if (mapping != null) {
				result.add(mapping);
			}
		}
		return result.toArray(new ResourceMapping[result.size()]);
	}

	@Override
	public boolean contains(ResourceMapping mapping) {
		for (ResourceMapping childMapping : getMappings()) {
			if (childMapping.contains(mapping)) {
				return true;
			}
		}
		return false;
	}

}
