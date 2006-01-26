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

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.mapping.*;

/**
 * Class for translating a set of <code>ResourceMapping</code> objects
 * representing a view selection into the complete set of resources to be
 * operated on.
 * <p>
 * Here's a summary of the scope generation algorithm:
 * <ol>
 * <li>Obtain selected mappings
 * <li>Project mappings onto resources using the appropriate
 * context(s) in order to obtain a set of ResourceTraverals
 * <li>Determine what model providers are interested in the targeted resources
 * <li>From those model providers, obtain the set of affected resource mappings
 * <li>If the original set is the same as the new set, we are done.
 * <li>if the set differs from the original selection, rerun the mapping process
 * for any new mappings
 *     <ul>
 *     <li>Only need to query model providers for mappings for new resources
 *     <li>If new mappings are obtained, 
 *     ask model provider to compress the mappings?
 *     <li>keep repeating until no new mappings or resources are added
 *     </ul> 
 * </ol> 
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

	private static final int MAX_ITERATION = 10;
	private final ResourceMappingContext context;
	private final boolean consultModels;

	public ScopeGenerator(ResourceMappingContext resourceMappingContext, boolean consultModels) {
		this.context = resourceMappingContext;
		this.consultModels = consultModels;
	}

	/**
	 * Build the scope that is used to determine the complete set of resource
	 * mappings, and hence resources, that an operation should be performed on.
	 * 
	 * @param selectedMappings the selected set of resource mappings
	 * @param monitor a progress monitor
	 * @return a scope that defines the complete set of resources to be operated
	 *         on
	 * @throws CoreException
	 */
	public IResourceMappingScope prepareScope(
			ResourceMapping[] selectedMappings,
			IProgressMonitor monitor) throws CoreException {

		monitor.beginTask(null, IProgressMonitor.UNKNOWN);

		// Create the scope
		IResourceMappingScope scope = createScope(selectedMappings);

		// Accumulate the initial set of mappings we need traversals for
		Set targetMappings = new HashSet();
		for (int i = 0; i < selectedMappings.length; i++) {
			ResourceMapping mapping = selectedMappings[i];
			targetMappings.add(mapping);
		}
		IResource[] newResources;
		boolean firstTime = true;
		boolean hasAdditionalResources = false;
		int count = 0;
		do {
			newResources = addMappingsToScope(scope, targetMappings,
					Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
			if (consultModels) {
				IResource[] adjusted = adjustInputResources(newResources);
				targetMappings = internalGetMappingsFromProviders(adjusted,
						context, 
						Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
				if (firstTime) {
					firstTime = false;
				} else if (!hasAdditionalResources) {
					hasAdditionalResources = newResources.length != 0;
				}
			}
		} while (consultModels & newResources.length != 0 && count++ < MAX_ITERATION);
		setHasAdditionalMappings(scope, consultModels && hasAdditionalMappings(scope));
		setHasAdditionalResources(scope, consultModels && hasAdditionalResources);
		return scope;
	}

	/**
	 * Refresh the scope for the given mappings.
	 * @param scope the scope being refreshed
	 * @param mappings the mappings to be refreshed
	 * @param monitor a progress monitor
	 * @return a set of traversals that cover the given mappings
	 * @throws CoreException 
	 */
	public ResourceTraversal[] refreshScope(final IResourceMappingScope scope, final ResourceMapping[] mappings, IProgressMonitor monitor) throws CoreException {
		// We need to lock the workspace when building the scope
		final ResourceTraversal[][] traversals = new ResourceTraversal[][] { new ResourceTraversal[0] };
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				traversals[0] = internalRefreshScope(scope, mappings, monitor);
			}
		}, workspace.getRoot(), IResource.NONE, monitor);
		return traversals[0];
	}

	private ResourceTraversal[] internalRefreshScope(IResourceMappingScope scope, ResourceMapping[] mappings, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(null, 100 * mappings.length + 100);
		ResourceMapping[] originalMappings = scope.getMappings();
		ResourceTraversal[] originalTraversals = scope.getTraversals();
		CompoundResourceTraversal refreshTraversals = new CompoundResourceTraversal();
		boolean expanded = false;
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			ResourceTraversal[] mappingTraversals = mapping.getTraversals(
					context, Policy.subMonitorFor(monitor, 100));
			refreshTraversals.addTraversals(mappingTraversals);
			IResource[] uncovered = getUncoveredResources(scope, mappingTraversals);
			if (uncovered.length > 0) {
				expanded = true;
				ResourceTraversal[] result = performExpandScope(scope, mapping, mappingTraversals, uncovered, monitor);
				refreshTraversals.addTraversals(result);
			}
		}
		if (scope.getMappings().length > originalMappings.length) {
			fileMappingsChangedEvent(scope, originalMappings);
		}
		if (expanded) {
			fireTraversalsChangedEvent(scope, originalTraversals);
		}
		
		monitor.done();
		return refreshTraversals.asTraversals();
	}

	private ResourceTraversal[] performExpandScope(IResourceMappingScope scope,
			ResourceMapping mapping, ResourceTraversal[] mappingTraversals,
			IResource[] uncovered, IProgressMonitor monitor)
			throws CoreException {
		ResourceMapping ancestor = findAncestor(scope, mapping);
		if (ancestor == null) {
			uncovered = addMappingToScope(scope, mapping, mappingTraversals);
			addResourcesToScope(scope, uncovered, monitor);
			return mappingTraversals;
		} else {
			ResourceTraversal[] ancestorTraversals = ancestor.getTraversals(
					context, Policy.subMonitorFor(monitor, 100));
			uncovered = addMappingToScope(scope, ancestor, ancestorTraversals);
			addResourcesToScope(scope, uncovered, monitor);
			return ancestorTraversals;
		}
	}

	private ResourceMapping findAncestor(IResourceMappingScope scope, ResourceMapping mapping) {
		ResourceMapping[] mappings = scope.getMappings(mapping.getModelProviderId());
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping m = mappings[i];
			if (m.contains(mapping)) {
				return m;
			}
		}
		return null;
	}

	private IResource[] getUncoveredResources(IResourceMappingScope scope, ResourceTraversal[] traversals) {
		return ((ResourceMappingScope)scope).getCompoundTraversal().getUncoveredResources(traversals);
	}

	private void addResourcesToScope(IResourceMappingScope scope, IResource[] newResources, IProgressMonitor monitor) throws CoreException {
		if (!consultModels)
			return;
		Set targetMappings;
		int count = 0;
		do {
			IResource[] adjusted = adjustInputResources(newResources);
			targetMappings = internalGetMappingsFromProviders(adjusted,
					context, Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
			newResources = addMappingsToScope(scope, targetMappings,
					Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
		} while (newResources.length != 0 && count++ < MAX_ITERATION);
		if (!scope.hasAdditionalMappings()) {
			setHasAdditionalMappings(scope, hasAdditionalMappings(scope));
		}
		if (!scope.hasAdditonalResources()) {
			setHasAdditionalResources(scope, true);
		}
	}

	/**
	 * Fire a mappings changed event to any listeners on the scope.
	 * The new mappings are obtained from the scope.
	 * @param scope the scope
	 * @param originalMappings the original mappings of the scope.
	 */
	private void fileMappingsChangedEvent(IResourceMappingScope scope, ResourceMapping[] originalMappings) {
		((ResourceMappingScope)scope).fireMappingChangedEvent(originalMappings);
	}
	
	/**
	 * Fire a traversals changed event to any listeners on the scope.
	 * The new traversals are obtained from the scope.
	 * @param scope the scope
	 * @param originalTraversals the original traversals of the scope.
	 */
	protected final void fireTraversalsChangedEvent(IResourceMappingScope scope, ResourceTraversal[] originalTraversals) {
		((ResourceMappingScope)scope).fireTraversalsChangedEvent(originalTraversals);
	}

	/**
	 * set whether the scope has additional mappings. This method is not
	 * intended to be overridden.
	 * 
	 * @param scope the scope
	 * @param hasAdditionalMappings a boolean indicating if the scope has
	 *            additional mappings
	 */
	protected final void setHasAdditionalMappings(
			IResourceMappingScope scope, boolean hasAdditionalMappings) {
		((ResourceMappingScope)scope).setHasAdditionalMappings(hasAdditionalMappings);
	}

	/**
	 * set whether the scope has additional resources. This method is not
	 * intended to be overridden.
	 * 
	 * @param scope the scope
	 * @param hasAdditionalResources a boolean indicating if the scope has
	 *            additional resources
	 */
	protected final void setHasAdditionalResources(IResourceMappingScope scope, boolean hasAdditionalResources) {
		((ResourceMappingScope)scope).setHasAdditionalResources(hasAdditionalResources);
	}
	
	/**
	 * Create the scope that will be populated and returned by the builder. This
	 * method is not intended to be overridden by clients.
	 * @param inputMappings the input mappings
	 * @return a newly created scope that will be populated and returned by the
	 *         builder
	 */
	protected final IResourceMappingScope createScope(
			ResourceMapping[] inputMappings) {
		return new ResourceMappingScope(this, inputMappings);
	}

	/**
	 * Adjust the given set of input resources to include any additional
	 * resources required by a particular repository provider for the current
	 * operation. By default the original set is returned but subclasses may
	 * override. Overriding methods should return a set of resources that
	 * include the original resource either explicitly or implicitly as a child
	 * of a returned resource.
	 * <p>
	 * Subclasses may override this method to include additional resources
	 * 
	 * @param resources the input resources
	 * @return the input resources adjusted to include any additional resources
	 *         required for the current operation
	 */
	protected IResource[] adjustInputResources(IResource[] resources) {
		return resources;
	}

	private IResource[] addMappingsToScope(IResourceMappingScope scope,
			Set targetMappings,
			IProgressMonitor monitor) throws CoreException {
		Set newResources = new HashSet();
		for (Iterator iter = targetMappings.iterator(); iter.hasNext();) {
			ResourceMapping mapping = (ResourceMapping) iter.next();
			if (scope.getTraversals(mapping) == null) {
				ResourceTraversal[] traversals = mapping.getTraversals(context,
						Policy.subMonitorFor(monitor, 100));
				IResource[] newOnes = addMappingToScope(scope, mapping, traversals);
				newResources.addAll(Arrays.asList(newOnes));
			}
		}
		return (IResource[]) newResources.toArray(new IResource[newResources.size()]);
	}

	/**
	 * Add the mapping and its calculated traversals to the scope. Return the
	 * resources that were not previously covered by the scope. This method
	 * is not intended to be subclassed by clients.
	 * 
	 * @param scope the scope
	 * @param mapping the resource mapping
	 * @param traversals the resource mapping's traversals
	 * @return the resources that were not previously covered by the scope
	 */
	protected final IResource[] addMappingToScope(IResourceMappingScope scope,
			ResourceMapping mapping, ResourceTraversal[] traversals) {
		return ((ResourceMappingScope)scope).addMapping(mapping, traversals);
	}

	private Set internalGetMappingsFromProviders(IResource[] resources,
			ResourceMappingContext context,
			IProgressMonitor monitor) throws CoreException {
		Set result = new HashSet();
		IModelProviderDescriptor[] descriptors = ModelProvider
				.getModelProviderDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			IModelProviderDescriptor descriptor = descriptors[i];
			ResourceMapping[] mappings = getMappings(descriptor, resources,
					context, monitor);
			result.addAll(Arrays.asList(mappings));
		}
		return result;
	}

	private ResourceMapping[] getMappings(IModelProviderDescriptor descriptor,
			IResource[] resources,
			ResourceMappingContext context, IProgressMonitor monitor)
			throws CoreException {
		IResource[] matchingResources = descriptor.getMatchingResources(
				resources);
		return descriptor.getModelProvider().getMappings(matchingResources,
				context, monitor);
	}

	private boolean hasAdditionalMappings(IResourceMappingScope scope) {
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

	/**
	 * Return whether the model providers should be consulted in
	 * order to see if the scope needs to be expanded.
	 * @return whether the model providers should be consulted
	 */
	public boolean isConsultModels() {
		return consultModels;
	}

	/**
	 * Return the resource mapping context used during the scope 
	 * generation process in order to determine what resources
	 * are to be included in the scope.
	 * @return the resource mapping context used during the scope 
	 * generation process
	 */
	public ResourceMappingContext getContext() {
		return context;
	}
}
