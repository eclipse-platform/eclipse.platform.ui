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
package org.eclipse.team.ui.operations;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.window.Window;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.provider.ScopeGenerator;
import org.eclipse.team.internal.core.mapping.ResourceMappingScope;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.dialogs.AdditionalMappingsDialog;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Here's a summary of the input determination scheme
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
 * <li>Use model provider relationships to result?
 * <li>Display the original set and the new set with an explanation
 *     <ul>
 *     <li>The original set and final set may involve mappings from
 *     multiple providers.
 *     <li>The number of providers can be reduced by assuming that
 *     extending models can display the elements of extended models.
 *     Then we are only left with conflicting models.
 *     <li>Could use a content provider approach a.k.a. Common Navigator
 *     or component based approach
 *     </ul> 
 * </ol> 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class ResourceMappingOperation extends ModelProviderOperation {
	
	private static final ScopeGenerator DEFAULT_SCOPE_BUILDER = new ScopeGenerator();
	private final ResourceMapping[] selectedMappings;
	private final ResourceMappingContext context;
	private IResourceMappingScope scope;
    
    /**
     * Create a resource mapping based operation
     * @param part the workspace part from which the operation was launched
     * @param input the input to the operation (which must have already been built by
     * invoking <code>buildInput</code>.
     */
	protected ResourceMappingOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings, ResourceMappingContext context) {
		super(part);
		this.selectedMappings = selectedMappings;
		this.context = context;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		buildScope(monitor);
		execute(monitor);
	}

	/**
	 * Adjust the input of the operation according to the selected
	 * resource mappings and the set of interested participants. This method
	 * will prompt the user in the following cases:
	 * <ol>
	 * <li>The scope contains additional resources than those in the input.
	 * <li>The scope has additional mappings from a model in the input
	 * <li>The input contains elements from multiple models
	 * </ol>
	 * @param monitor a progress monitor
	 */
	protected void buildScope(IProgressMonitor monitor) throws InvocationTargetException {
		try {
			scope = getScopeBuilder().prepareScope(selectedMappings, context, monitor);
			IResourceMappingScope inputScope = new ScopeGenerator().asInputScope(scope);
			if (scope.hasAdditionalMappings()) {
				// There are additional mappings so we may need to prompt
				ModelProvider[] inputModelProviders = inputScope.getModelProviders();
				if (scope.hasAdditonalResources()) {
					// We definitely need to prompt to indicate that additional resources
					promptForInputChange(monitor);
				} else if (inputModelProviders.length == 1) {
					// We may need to prompt depending on the nature of the additional mappings
					// We need to prompt if the additional mappings are from the same model as
					// the input or if they are from a model that has no relationship to the input model
					String modelProviderId = inputModelProviders[0].getDescriptor().getId();
					ResourceMapping[] mappings = scope.getMappings();
					boolean prompt = false;
					for (int i = 0; i < mappings.length; i++) {
						ResourceMapping mapping = mappings[i];
						if (inputScope.getTraversals(mapping) == null) {
							// This mapping was not in the input
							String id = mapping.getModelProviderId();
							if (id.equals(modelProviderId)) {
								prompt = true;
								break;
							} else if (isIndependantModel(modelProviderId, id)) {
								prompt = true;
								break;
							}
						}
					}
					if (prompt)
						promptForInputChange(monitor);
				} else {
					// We need to prompt if there are additional mappings from an input
					// provider whose traversals overlap those of the input mappings.
					boolean prompt = false;
					for (int i = 0; i < inputModelProviders.length; i++) {
						ModelProvider provider = inputModelProviders[i];
						String id = provider.getDescriptor().getId();
						ResourceMapping[] inputMappings = inputScope.getMappings(id);
						ResourceMapping[] scopeMappings = scope.getMappings(id);
						if (inputMappings.length != scopeMappings.length) {
							// There are more mappings for this provider.
							// We need to see if any of the new ones overlap the old ones.
							for (int j = 0; j < scopeMappings.length; j++) {
								ResourceMapping mapping = scopeMappings[j];
								ResourceTraversal[] inputTraversals = inputScope.getTraversals(mapping);
								if (inputTraversals == null) {
									// This mapping was not in the input.
									// We need to prompt if the traversal for this mapping overlaps with
									// the input mappings for the model provider
									// TODO could check for project overlap first
									ResourceTraversal[] scopeTraversals = scope.getTraversals(mapping);
									ResourceTraversal[] inputModelTraversals = getTraversals(inputScope, inputMappings);
									if (overlaps(scopeTraversals, inputModelTraversals)) {
										prompt = true;
										break;
									}
								}
							}
						}
					}
					
					if (prompt)
						promptForInputChange(monitor);
				}
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	private boolean overlaps(ResourceTraversal[] scopeTraversals, ResourceTraversal[] inputModelTraversals) {
		for (int i = 0; i < inputModelTraversals.length; i++) {
			ResourceTraversal inputTraversal = inputModelTraversals[i];
			for (int j = 0; j < scopeTraversals.length; j++) {
				ResourceTraversal scopeTraversal = scopeTraversals[j];
				if (overlaps(inputTraversal, scopeTraversal)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean overlaps(ResourceTraversal inputTraversal, ResourceTraversal scopeTraversal) {
		IResource[] inputRoots = inputTraversal.getResources();
		IResource[] scopeRoots = scopeTraversal.getResources();
		for (int i = 0; i < scopeRoots.length; i++) {
			IResource scopeResource = scopeRoots[i];
			for (int j = 0; j < inputRoots.length; j++) {
				IResource inputResource = inputRoots[j];
				if (overlaps(scopeResource, scopeTraversal.getDepth(), inputResource, inputTraversal.getDepth()))
					return true;
			}
		}
		return false;
	}

	private boolean overlaps(IResource scopeResource, int scopeDepth, IResource inputResource, int inputDepth) {
		if (scopeResource.equals(inputResource))
			return true;
		if (scopeDepth == IResource.DEPTH_INFINITE && scopeResource.getFullPath().isPrefixOf(inputResource.getFullPath())) {
			return true;
		}
		if (scopeDepth == IResource.DEPTH_ONE && scopeResource.equals(inputResource.getParent())) {
			return true;
		}
		if (inputDepth == IResource.DEPTH_INFINITE && inputResource.getFullPath().isPrefixOf(scopeResource.getFullPath())) {
			return true;
		}
		if (inputDepth == IResource.DEPTH_ONE && inputResource.equals(scopeResource.getParent())) {
			return true;
		}
		return false;
	}

	private ResourceTraversal[] getTraversals(IResourceMappingScope inputScope, ResourceMapping[] inputMappings) {
		List result = new ArrayList();
		for (int i = 0; i < inputMappings.length; i++) {
			ResourceMapping mapping = inputMappings[i];
			result.addAll(Arrays.asList(inputScope.getTraversals(mapping)));
		}
		return ResourceMappingScope.combineTraversals((ResourceTraversal[]) result.toArray(new ResourceTraversal[result.size()]));
	}

	private boolean isIndependantModel(String modelProviderId, String id) {
		IModelProviderDescriptor desc1 = ModelProvider.getModelProviderDescriptor(modelProviderId);
		IModelProviderDescriptor desc2 = ModelProvider.getModelProviderDescriptor(id);
		
		return !(isExtension(desc1, desc2) || isExtension(desc2, desc1));
	}

	/*
	 * Return whether the desc1 model extends the desc2 model
	 */
	private boolean isExtension(IModelProviderDescriptor desc1, IModelProviderDescriptor desc2) {
		String[] ids = desc1.getExtendedModels();
		// First check direct extension
		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];
			if (id.equals(desc2.getId())) {
				return true;
			}
		}
		// Now check for indirect extension
		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];
			IModelProviderDescriptor desc3 = ModelProvider.getModelProviderDescriptor(id);
			if (isExtension(desc3, desc2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the scope builder used to build the scope of this
	 * operation from the input mappings. This method can be
	 * overridden by subclasses.
	 * @return the scope builder used to build the scope of this
	 * operation from the input mappings.
	 */
	protected ScopeGenerator getScopeBuilder() {
		return DEFAULT_SCOPE_BUILDER;
	}

	/**
	 * Prompt the user to inform them that additional resource mappings
	 * have been included in the operations.
	 * @param monitor a progress monitor
	 * @throws OperationCanceledException if the user choose to cancel
	 */
	protected void promptForInputChange(IProgressMonitor monitor) {
		showAllMappings();
	}

    private void showAllMappings() {
        final boolean[] canceled = new boolean[] { false };
        getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                AdditionalMappingsDialog dialog = new AdditionalMappingsDialog(getShell(), TeamUIMessages.ResourceMappingOperation_0, getScope(), getContext());
                int result = dialog.open();
                canceled[0] = result != Window.OK;
            }
        
        });
        
        if (canceled[0]) {
            throw new OperationCanceledException();
        }
    }
    
	/**
	 * Return the synchronization context for the operation or <code>null</code>
	 * if the operation doesn't have one or if it has not yet been created.
	 * By default, the method always returns <code>null</code>. Subclasses may override.
	 * @return the synchronization context for the operation or <code>null</code>
	 */
	protected ISynchronizationContext getContext() {
		return null;
	}

	protected abstract void execute(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException;

	public IResourceMappingScope getScope() {
		return scope;
	}
	
}
