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
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
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
 * <li>Project mappings onto resources using the appropriate context(s) in
 * order to obtain a set of ResourceTraverals
 * <li>Determine what model providers are interested in the targeted resources
 * <li>From those model providers, obtain the set of affected resource mappings
 * <li>If the original set is the same as the new set, we are done.
 * <li>if the set differs from the original selection, rerun the mapping
 * process for any new mappings
 * <ul>
 * <li>Only need to query model providers for mappings for new resources
 * <li>keep repeating until no new mappings or resources are added
 * </ul>
 * </ol>
 * <p>
 * This implementation does not involve participants in the scope management
 * process. It is up to subclasses that wish to support a longer life cycle for
 * scopes to provide for participation. For example, the
 * {@link SubscriberScopeManager} class includes participates in the scope
 * management process.
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
 * @see SubscriberScopeManager
 * 
 * @since 3.2
 */
public class SynchronizationScopeManager implements ISynchronizationScopeManager {

	private static final int MAX_ITERATION = 10;
	private final ResourceMappingContext context;
	private final boolean consultModels;
	private ISynchronizationScope scope;
	private boolean initialized;
	private ScopeManagerEventHandler handler;

	/**
	 * Convenience method for obtaining the set of resource
	 * mappings from all model providers that overlap
	 * with the given resources.
	 * @param traversals the resource traversals
	 * @param context the resource mapping context
	 * @param monitor a progress monitor
	 * @return the resource mappings
	 * @throws CoreException
	 */
	public static ResourceMapping[] getMappingsFromProviders(ResourceTraversal[] traversals,
			ResourceMappingContext context,
			IProgressMonitor monitor) throws CoreException {
		Set result = new HashSet();
		IModelProviderDescriptor[] descriptors = ModelProvider
				.getModelProviderDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			IModelProviderDescriptor descriptor = descriptors[i];
			ResourceMapping[] mappings = getMappings(descriptor, traversals,
					context, monitor);
			result.addAll(Arrays.asList(mappings));
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
	}
	
	private static ResourceMapping[] getMappings(IModelProviderDescriptor descriptor,
			ResourceTraversal[] traversals,
			ResourceMappingContext context, IProgressMonitor monitor)
			throws CoreException {
		ResourceTraversal[] matchingTraversals = descriptor.getMatchingTraversals(
				traversals);
		return descriptor.getModelProvider().getMappings(matchingTraversals,
				context, monitor);
	}
	
	/**
	 * Create a scope manager that uses the given context to 
	 * determine what resources should be included in the scope.
	 * If <code>consultModels</code> is <code>true</code> then
	 * the moel providers will be queried in order to determine if
	 * additional mappings should be included in the scope
	 * @param inputMappings the input mappings
	 * @param resourceMappingContext a resource mapping context
	 * @param consultModels whether modle providers should be consulted
	 */
	public SynchronizationScopeManager(ResourceMapping[] inputMappings, ResourceMappingContext resourceMappingContext, boolean consultModels) {
		this.context = resourceMappingContext;
		this.consultModels = consultModels;
		scope = createScope(inputMappings);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScopeManager#isInitialized()
	 */
	public boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * Return the scheduling rule that is used when initializing
	 * and refreshing the scope. By default, it is the 
	 * workspace root.
	 * @return the scheduling rule that is used when initializing
	 * and refreshing the scope
	 */
	public ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScopeManager#initialize(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initialize(
			IProgressMonitor monitor) throws CoreException {
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				internalPrepareContext(monitor);
			}
		}, getSchedulingRule(), IResource.NONE, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScopeManager#refresh(org.eclipse.core.resources.mapping.ResourceMapping[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ResourceTraversal[] refresh(final ResourceMapping[] mappings, IProgressMonitor monitor) throws CoreException {
		// We need to lock the workspace when building the scope
		final ResourceTraversal[][] traversals = new ResourceTraversal[][] { new ResourceTraversal[0] };
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				traversals[0] = internalRefreshScope(mappings, monitor);
			}
		}, getSchedulingRule(), IResource.NONE, monitor);
		return traversals[0];
	}
	
	private void internalPrepareContext(IProgressMonitor monitor) throws CoreException {
		if (initialized)
			return;
		monitor.beginTask(null, IProgressMonitor.UNKNOWN);
		// Accumulate the initial set of mappings we need traversals for
		ResourceMapping[] targetMappings = scope.getInputMappings();
		ResourceTraversal[] newTraversals;
		boolean firstTime = true;
		boolean hasAdditionalResources = false;
		int count = 0;
		do {
			newTraversals = addMappingsToScope(targetMappings,
					Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
			if (newTraversals.length > 0 && consultModels) {
				ResourceTraversal[] adjusted = adjustInputTraversals(newTraversals);
				targetMappings = getMappingsFromProviders(adjusted,
						context, 
						Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
				if (firstTime) {
					firstTime = false;
				} else if (!hasAdditionalResources) {
					hasAdditionalResources = newTraversals.length != 0;
				}
			}
		} while (consultModels & newTraversals.length != 0 && count++ < MAX_ITERATION);
		setHasAdditionalMappings(scope, consultModels && internalHasAdditionalMappings());
		setHasAdditionalResources(consultModels && hasAdditionalResources);
		monitor.done();
		initialized = true;
	}

	private ResourceTraversal[] internalRefreshScope(ResourceMapping[] mappings, IProgressMonitor monitor) throws CoreException {
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
			ResourceTraversal[] uncovered = getUncoveredTraversals(mappingTraversals);
			if (uncovered.length > 0) {
				expanded = true;
				ResourceTraversal[] result = performExpandScope(mapping, mappingTraversals, uncovered, monitor);
				refreshTraversals.addTraversals(result);
			}
		}
		ResourceMapping[] currentMappings = scope.getMappings();
		if (expanded || currentMappings.length > originalMappings.length) {
			ResourceTraversal[] newTraversals;
			if (expanded) {
				CompoundResourceTraversal originals = new CompoundResourceTraversal();
				originals.addTraversals(originalTraversals);
				newTraversals = originals.getUncoveredTraversals(refreshTraversals);
			} else {
				newTraversals = new ResourceTraversal[0];
			}
			ResourceMapping[] newMappings;
			if (currentMappings.length > originalMappings.length) {
				Set originalSet = new HashSet();
				List result = new ArrayList();
				for (int i = 0; i < originalMappings.length; i++) {
					ResourceMapping mapping = originalMappings[i];
					originalSet.add(mapping);
				}
				for (int i = 0; i < currentMappings.length; i++) {
					ResourceMapping mapping = currentMappings[i];
					if (!originalSet.contains(mapping)) {
						result.add(mapping);
					}
				}
				newMappings = (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
			} else {
				newMappings = new ResourceMapping[0];
			}
			fireMappingsChangedEvent(newMappings, newTraversals);
		}
		
		monitor.done();
		return refreshTraversals.asTraversals();
	}

	private ResourceTraversal[] performExpandScope(
			ResourceMapping mapping, ResourceTraversal[] mappingTraversals,
			ResourceTraversal[] uncovered, IProgressMonitor monitor)
			throws CoreException {
		ResourceMapping ancestor = findAncestor(mapping);
		if (ancestor == null) {
			uncovered = addMappingToScope(mapping, mappingTraversals);
			addResourcesToScope(uncovered, monitor);
			return mappingTraversals;
		} else {
			ResourceTraversal[] ancestorTraversals = ancestor.getTraversals(
					context, Policy.subMonitorFor(monitor, 100));
			uncovered = addMappingToScope(ancestor, ancestorTraversals);
			addResourcesToScope(uncovered, monitor);
			return ancestorTraversals;
		}
	}

	private ResourceMapping findAncestor(ResourceMapping mapping) {
		ResourceMapping[] mappings = scope.getMappings(mapping.getModelProviderId());
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping m = mappings[i];
			if (m.contains(mapping)) {
				return m;
			}
		}
		return null;
	}

	private ResourceTraversal[] getUncoveredTraversals(ResourceTraversal[] traversals) {
		return ((ResourceMappingScope)scope).getCompoundTraversal().getUncoveredTraversals(traversals);
	}

	private void addResourcesToScope(ResourceTraversal[] newTraversals, IProgressMonitor monitor) throws CoreException {
		if (!consultModels)
			return;
		ResourceMapping[] targetMappings;
		int count = 0;
		do {
			ResourceTraversal[] adjusted = adjustInputTraversals(newTraversals);
			targetMappings = getMappingsFromProviders(adjusted,
					context, Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
			newTraversals = addMappingsToScope(targetMappings,
					Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN));
		} while (newTraversals.length != 0 && count++ < MAX_ITERATION);
		if (!scope.hasAdditionalMappings()) {
			setHasAdditionalMappings(scope, internalHasAdditionalMappings());
		}
		if (!scope.hasAdditonalResources()) {
			setHasAdditionalResources(true);
		}
	}

	/*
	 * Fire a mappings changed event to any listeners on the scope.
	 * The new mappings are obtained from the scope.
	 * @param originalMappings the original mappings of the scope.
	 */
	private void fireMappingsChangedEvent(ResourceMapping[] newMappings, ResourceTraversal[] newTraversals) {
		((ResourceMappingScope)scope).fireTraversalsChangedEvent(newTraversals, newMappings);
	}

	/**
	 * set whether the scope has additional mappings. This method is not
	 * intended to be overridden.
	 * 
	 * @param hasAdditionalMappings a boolean indicating if the scope has
	 *            additional mappings
	 */
	protected final void setHasAdditionalMappings(
			ISynchronizationScope scope, boolean hasAdditionalMappings) {
		((ResourceMappingScope)scope).setHasAdditionalMappings(hasAdditionalMappings);
	}

	/**
	 * set whether the scope has additional resources. This method is not
	 * intended to be overridden.
	 * 
	 * @param hasAdditionalResources a boolean indicating if the scope has
	 *            additional resources
	 */
	protected final void setHasAdditionalResources(boolean hasAdditionalResources) {
		((ResourceMappingScope)scope).setHasAdditionalResources(hasAdditionalResources);
	}
	
	/**
	 * Create the scope that will be populated and returned by the builder. This
	 * method is not intended to be overridden by clients.
	 * @param inputMappings the input mappings
	 * @return a newly created scope that will be populated and returned by the
	 *         builder
	 */
	protected final ISynchronizationScope createScope(
			ResourceMapping[] inputMappings) {
		return new ResourceMappingScope(inputMappings);
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
	 * @param traversals the input resource traversals
	 * @return the input resource traversals adjusted to include any additional resources
	 *         required for the current operation
	 */
	protected ResourceTraversal[] adjustInputTraversals(ResourceTraversal[] traversals) {
		return traversals;
	}

	private ResourceTraversal[] addMappingsToScope(
			ResourceMapping[] targetMappings,
			IProgressMonitor monitor) throws CoreException {
		CompoundResourceTraversal result = new CompoundResourceTraversal();
		ResourceMappingContext context = this.context;
		for (int i = 0; i < targetMappings.length; i++) {
			ResourceMapping mapping = targetMappings[i];
			if (scope.getTraversals(mapping) == null) {
				ResourceTraversal[] traversals = mapping.getTraversals(context,
						Policy.subMonitorFor(monitor, 100));
				ResourceTraversal[] newOnes = addMappingToScope(mapping, traversals);
				result.addTraversals(newOnes);
			}
		}
		return result.asTraversals();
	}

	/**
	 * Add the mapping and its calculated traversals to the scope. Return the
	 * resources that were not previously covered by the scope. This method
	 * is not intended to be subclassed by clients.
	 * 
	 * @param mapping the resource mapping
	 * @param traversals the resource mapping's traversals
	 * @return the resource traversals that were not previously covered by the scope
	 */
	protected final ResourceTraversal[] addMappingToScope(
			ResourceMapping mapping, ResourceTraversal[] traversals) {
		return ((ResourceMappingScope)scope).addMapping(mapping, traversals);
	}

	private boolean internalHasAdditionalMappings() {
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

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScopeManager#getContext()
	 */
	public ResourceMappingContext getContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScopeManager#getScope()
	 */
	public ISynchronizationScope getScope() {
		return scope;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScopeManager#getProjects()
	 */
	public IProject[] getProjects() {
		if (context instanceof RemoteResourceMappingContext) {
			RemoteResourceMappingContext rrmc = (RemoteResourceMappingContext) context;
			return rrmc.getProjects();
		}
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScopeManager#dispose()
	 */
	public void dispose() {
		if (handler != null)
			handler.shutdown();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScopeManager#refresh(org.eclipse.core.resources.mapping.ResourceMapping[])
	 */
	public void refresh(ResourceMapping[] mappings) {
		getHandler().refresh(mappings);
	}

	private synchronized ScopeManagerEventHandler getHandler() {
		if (handler == null)
			handler = new ScopeManagerEventHandler(this);
		return handler;
	}
}
