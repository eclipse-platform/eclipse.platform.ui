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
package org.eclipse.team.core.mapping.provider;

import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.mapping.ResourceMappingInputScope;
import org.eclipse.team.internal.core.mapping.ResourceMappingScope;

/**
 * Class for translating a set of <code>ResourceMapping</code> objects
 * representing a view selection into the complete set of resources to be
 * operated on.
 * <p>
 * This class is can be subclasses by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * 
 * @since 3.2
 */
public class ScopeGenerator {

	/**
	 * Build the scope that is used to determine the complete set of resource
	 * mappings, and hence resources, that an operation should be performed on.
	 * 
	 * @param selectedMappings the selected set of resource mappings
	 * @param context the resource mapping context used to determine the
	 *            resources to be oeprated on
	 * @param monitor a progress monitor
	 * @return a scope that defines the complete set of resources to be operated
	 *         on
	 * @throws CoreException
	 */
	public IResourceMappingScope prepareScope(
			String label,
			ResourceMapping[] selectedMappings, ResourceMappingContext context,
			IProgressMonitor monitor) throws CoreException {

		monitor.beginTask(null, IProgressMonitor.UNKNOWN);

		// Create the scope
		ResourceMappingScope scope = createScope(label, selectedMappings);

		// Accumulate the initial set of mappings we need traversals for
		Set targetMappings = new HashSet();
		for (int i = 0; i < selectedMappings.length; i++) {
			ResourceMapping mapping = selectedMappings[i];
			targetMappings.add(mapping);
		}
		Set handledResources = new HashSet();
		Set newResources;
		boolean firstTime = true;
		boolean hasAdditionalResources = false;
		do {
			newResources = addMappingsToScope(scope, targetMappings, context,
					Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
			IResource[] adjusted = adjustNewResources(newResources);
			targetMappings = internalGetMappingsFromProviders(adjusted,
					getAffectedNatures(targetMappings), context, Policy
							.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));

			// TODO: The new resources aren't really just the new ones so reduce
			// the set if needed
			if (!handledResources.isEmpty()) {
				for (Iterator iter = newResources.iterator(); iter.hasNext();) {
					IResource resource = (IResource) iter.next();
					if (handledResources.contains(resource)) {
						iter.remove();
					}
				}
			}

			handledResources.addAll(newResources);
			if (firstTime) {
				firstTime = false;
			} else if (!hasAdditionalResources) {
				hasAdditionalResources = !newResources.isEmpty();
			}
		} while (!newResources.isEmpty());
		setHasAdditionalMappings(scope, hasAdditionalMappings(scope));
		scope.setHasAdditionalResources(hasAdditionalResources);
		return scope;
	}

	/**
	 * set whether the scope has additional mappings. This method is not
	 * intended to be subclassed.
	 * 
	 * @param scope the scope
	 * @param hasAdditionalMappings a boolean indicating if the scope has
	 *            additional mappings
	 */
	protected void setHasAdditionalMappings(
			ResourceMappingScope scope, boolean hasAdditionalMappings) {
		scope.setHasAdditionalMappings(hasAdditionalMappings);
	}

	/**
	 * Create the scope that will be populated and returned by the builder. This
	 * method is not intended to be overridden by clients.
	 * @param label a label that describes the operation
	 * @param inputMappings the input mappings
	 * @return a newly created scope that will be populated and returned by the
	 *         builder
	 */
	protected ResourceMappingScope createScope(
			String label, ResourceMapping[] inputMappings) {
		return new ResourceMappingScope(label, inputMappings);
	}

	/**
	 * Adjust the given set of input resources to include any additional
	 * resources required by a particular repository provider for the current
	 * operation. By default the original set is returned but subclasses may
	 * override. Overriding methods should return a set of resources that
	 * include the original resource either explicitly or implicitly as a child
	 * of a returned resource.
	 * 
	 * @param resources the input resources
	 * @return the input resources adjusted to include any additional resources
	 *         required for the current operation
	 */
	protected IResource[] adjustInputResources(IResource[] resources) {
		return resources;
	}

	private Set addMappingsToScope(ResourceMappingScope scope,
			Set targetMappings, ResourceMappingContext context,
			IProgressMonitor monitor) throws CoreException {
		Set newResources = new HashSet();
		for (Iterator iter = targetMappings.iterator(); iter.hasNext();) {
			ResourceMapping mapping = (ResourceMapping) iter.next();
			if (scope.getTraversals(mapping) == null) {
				ResourceTraversal[] traversals = mapping.getTraversals(context,
						Policy.subMonitorFor(monitor, 100));
				addMappingToScope(scope, mapping, traversals);
				newResources.addAll(internalGetResources(traversals));
			}
		}
		return newResources;
	}

	/**
	 * Add the mapping and its caclulated traversals to the scope. This method
	 * is not intended to be subclassed by clients.
	 * 
	 * @param scope the scope
	 * @param mapping the resource mapping
	 * @param traversals the resource mapping's traversals
	 */
	protected void addMappingToScope(ResourceMappingScope scope,
			ResourceMapping mapping, ResourceTraversal[] traversals) {
		scope.addMapping(mapping, traversals);
	}

	private Collection internalGetResources(ResourceTraversal[] traversals) {
		Set result = new HashSet();
		for (int i = 0; i < traversals.length; i++) {
			ResourceTraversal traversal = traversals[i];
			IResource[] resources = traversal.getResources();
			for (int j = 0; j < resources.length; j++) {
				IResource resource = resources[j];
				// TODO: should we check for parent/child relationships?
				result.add(resource);
			}
		}
		return result;
	}

	/*
	 * Give the subclass a chance to add resources to the set of affected
	 * resources
	 */
	private IResource[] adjustNewResources(Set newResources) {
		IResource[] resources = (IResource[]) newResources
				.toArray(new IResource[newResources.size()]);
		IResource[] adjusted = adjustInputResources(resources);
		return adjusted;
	}

	private String[] getAffectedNatures(Set targetMappings) {
		Set result = new HashSet();
		for (Iterator iter = targetMappings.iterator(); iter.hasNext();) {
			ResourceMapping mapping = (ResourceMapping) iter.next();
			IProject[] projects = mapping.getProjects();
			for (int j = 0; j < projects.length; j++) {
				IProject project = projects[j];
				try {
					result.addAll(Arrays.asList(project.getDescription()
							.getNatureIds()));
				} catch (CoreException e) {
					TeamPlugin.log(e);
				}
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	private Set internalGetMappingsFromProviders(IResource[] resources,
			String[] affectedNatures, ResourceMappingContext context,
			IProgressMonitor monitor) throws CoreException {
		Set result = new HashSet();
		IModelProviderDescriptor[] descriptors = ModelProvider
				.getModelProviderDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			IModelProviderDescriptor descriptor = descriptors[i];
			ResourceMapping[] mappings = getMappings(descriptor, resources,
					affectedNatures, context, monitor);
			result.addAll(Arrays.asList(mappings));
		}
		return result;
	}

	private ResourceMapping[] getMappings(IModelProviderDescriptor descriptor,
			IResource[] resources, String[] affectedNatures,
			ResourceMappingContext context, IProgressMonitor monitor)
			throws CoreException {
		IResource[] matchingResources = descriptor.getMatchingResources(
				resources, affectedNatures);
		return descriptor.getModelProvider().getMappings(matchingResources,
				context, monitor);
	}

	private boolean hasAdditionalMappings(ResourceMappingScope scope) {
		ResourceMapping[] inputMappings = scope.getInputMappings();
		ResourceMapping[] mappings = scope.getMappings();
		if (inputMappings.length == mappings.length) {
			Set testSet = new HashSet();
			for (int i = 0; i < mappings.length; i++) {
				ResourceMapping mapping = mappings[i];
				testSet.add(mapping);
			}
			for (int i = 0; i < inputMappings.length; i++) {
				ResourceMapping mapping = inputMappings[i];
				if (!testSet.contains(mapping)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Return a scope that provides a client access to 
	 * the input mappings of the given scope as if
	 * they were the complete resource mapping scope.
	 * This is provided as a means to display the 
	 * input resource mappings only.
	 * @param scope a complete resource mapping scope
	 * @return a scope that provides a client access to 
	 * the input mappings of the given scope as if
	 * they were the complete resource mapping scope
	 */
	public final IResourceMappingScope asInputScope(IResourceMappingScope scope) {
		return new ResourceMappingInputScope(scope);
	}
}
