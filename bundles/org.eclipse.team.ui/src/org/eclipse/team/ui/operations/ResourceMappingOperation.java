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

import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.window.Window;
import org.eclipse.team.core.mapping.*;
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
	 * resource mappings and the set of interested participants
	 * @param monitor a progress monitor
	 */
	protected void buildScope(IProgressMonitor monitor) throws InvocationTargetException {
		try {
			scope = getScopeBuilder().prepareScope(getJobName(), selectedMappings, context, monitor);
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
					// The input had mixed mappings so just prompt with the additional mappings
					// TODO: Perhaps we could do better (bug 119921)
					promptForInputChange(monitor);
				}
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
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
                AdditionalMappingsDialog dialog = new AdditionalMappingsDialog(getShell(), "Selection Adjustment Required", getScope(), getContext());
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
