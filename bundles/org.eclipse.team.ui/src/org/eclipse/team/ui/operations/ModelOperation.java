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
import org.eclipse.team.ui.TeamOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * An abstract operation that uses a set of input mappings to create
 * an operation scope that includes the complete set of mappings
 * that must be included in the operation to ensure model consistency.
 * The scope generation phase will prompt the user if additional resources
 * have been added to the scope.
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
public abstract class ModelOperation extends TeamOperation {
	
	/**
	 * Status code that can be returned from the {@link #performMerge(IProgressMonitor)}
	 * method to indicate that a subclass would liek to force a preview of the merge.
	 * The message of such a status should be ignored.
	 */
	public static final int REQUEST_PREVIEW = 1024;
	
	private final ResourceMapping[] selectedMappings;
	private IResourceMappingScope scope;
	private boolean previewRequested;
	
	/**
	 * Return the list of provides sorted by their extends relationship.
	 * Extended model providers will appear later in the list then those
	 * that extends them. The order of model providers that independant
	 * (i.e. no extends relationship between them) will be indeterminate.
	 * @param providers the model providers
	 * @return the list of provides sorted by their extends relationship
	 */
	public static ModelProvider[] sortByExtension(ModelProvider[] providers) {
		List result = new ArrayList();
		for (int i = 0; i < providers.length; i++) {
			ModelProvider providerToInsert = providers[i];
			int index = result.size();
			for (int j = 0; j < result.size(); j++) {
				ModelProvider provider = (ModelProvider) result.get(j);
				if (extendsProvider(providerToInsert, provider)) {
					index = j;
					break;
				}
			}
			result.add(index, providerToInsert);
		}
		return (ModelProvider[]) result.toArray(new ModelProvider[result.size()]);
	}

	private static boolean extendsProvider(ModelProvider providerToInsert, ModelProvider provider) {
		String[] extended = providerToInsert.getDescriptor().getExtendedModels();
		// First search immediate dependents
		for (int i = 0; i < extended.length; i++) {
			String id = extended[i];
			if (id.equals(provider.getDescriptor().getId())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Create a model operation for the given selected mappings.
	 * The scope will be generated when the operation is executed.
	 * @param part the workbench part from which the merge was launched or <code>null</code>
	 * @param selectedMappings the selected mappings
	 */
	protected ModelOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings) {
		super(part);
		this.selectedMappings = selectedMappings;
	}

	/**
	 * Create a model operation that operates on the given scope.
	 * @param part the workbench part from which the merge was launched or <code>null</code>
	 * @param scope the scope of this operation
	 */
	protected ModelOperation(IWorkbenchPart part, IResourceMappingScope scope) {
		this(part, scope.getInputMappings());
		this.scope = scope;
	}
	
	/**
	 * Run the operation. This method first ensures that the scope is built
	 * by calling {@link #prepareScope(IProgressMonitor)} and then invokes the 
	 * {@link #execute(IProgressMonitor)} method.
	 * @param monitor a progress monitor
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		prepareScope(monitor);
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
	 * <p>
	 * The scope of this operation will only be prepared once. Subsequent
	 * calls to this method will do nothing. Also, if the scope was provided
	 * as an argument to a constructor, this method will do nothing (i.e. the
	 * scope will not be prepared again and no prompting will occur).
	 * <p>
	 * Subclasses can customize how the scope is generated by overriding
	 * the {@link #getScopeGenerator()} to return a custom scope generator.
	 * @param monitor a progress monitor
	 */
	protected final void prepareScope(IProgressMonitor monitor) throws InvocationTargetException {
		if (scope == null) {
			try {
				ScopeGenerator scopeGenerator = getScopeGenerator();
				scope = scopeGenerator.prepareScope(selectedMappings, isUseLocalContext(), monitor);
				promptIfInputChange(monitor);
			} catch (CoreException e) {
				throw new InvocationTargetException(e);
			}
		}
	}

	/**
	 * Prompt the user by calling {@link #promptForInputChange(String, IProgressMonitor)}
	 * if the scope of the operation was expanded (as described in 
	 * {@link #prepareScope(IProgressMonitor)}).
	 * @param monitor a progress monitor
	 */
	protected void promptIfInputChange(IProgressMonitor monitor) {
		IResourceMappingScope inputScope = getInputScope();
		if (scope.hasAdditionalMappings()) {
			boolean prompt = false;
			// There are additional mappings so we may need to prompt
			ModelProvider[] inputModelProviders = inputScope.getModelProviders();
			if (hasAdditionalMappingsFromIndependantModel(inputModelProviders, scope.getModelProviders())) {
				// Prompt if the is a new model provider in the scope that is independant
				// of any of the input mappings
				prompt = true;
			} else if (scope.hasAdditonalResources()) {
				// We definitely need to prompt to indicate that additional resources
				prompt = true;
			} else if (inputModelProviders.length == 1) {
				// We may need to prompt depending on the nature of the additional mappings
				// We need to prompt if the additional mappings are from the same model as
				// the input or if they are from a model that has no relationship to the input model
				String modelProviderId = inputModelProviders[0].getDescriptor().getId();
				ResourceMapping[] mappings = scope.getMappings();
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
			} else {
				// We need to prompt if there are additional mappings from an input
				// provider whose traversals overlap those of the input mappings.
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
			}
			if (prompt) {
				String previewMessage = getPreviewRequestMessage();
				previewRequested = promptForInputChange(previewMessage, monitor);
			}
		}
	}

	/**
	 * Return a scope that represents the input to the operation
	 * before the scope was expanded to include any additional models.
	 * @return a scope that represents the input to the operation
	 */
	protected final IResourceMappingScope getInputScope() {
		ScopeGenerator scopeGenerator = getScopeGenerator();
		IResourceMappingScope inputScope = scopeGenerator.asInputScope(scope);
		return inputScope;
	}

	/**
	 * Indicate whether the local context should be used when preparing the scope.
	 * Subclasses may wish to do this when initial creation of the
	 * scope cannot be log running but they will perform future refreshes on the
	 * scope that can be.
	 * @return whether the local context should be used when preparing the scope
	 */
	protected boolean isUseLocalContext() {
		return false;
	}

	/**
	 * Return a string to be used in the preview request on the scope prompt
	 * or <code>null</code> if a preview of the operation results is not possible.
	 * By default, <code>null</code> is returned but subclasses may override.
	 * @return a string to be used in the preview request on the scope prompt
	 * or <code>null</code> if a preview of the operation results is not possible
	 */
	protected String getPreviewRequestMessage() {
		return null;
	}

	private boolean hasAdditionalMappingsFromIndependantModel(ModelProvider[] inputModelProviders, ModelProvider[] modelProviders) {
		ModelProvider[] additionalProviders = getAdditionalProviders(inputModelProviders, modelProviders);
		for (int i = 0; i < additionalProviders.length; i++) {
			ModelProvider additionalProvider = additionalProviders[i];
			boolean independant = true;
			// Return true if the new provider is independant of all input providers
			for (int j = 0; j < inputModelProviders.length; j++) {
				ModelProvider inputProvider = inputModelProviders[j];
				if (!isIndependantModel(additionalProvider.getDescriptor().getId(), inputProvider.getDescriptor().getId())) {
					independant = false;
				}
			}
			if (independant)
				return true;
		}
		return false;
	}

	private ModelProvider[] getAdditionalProviders(ModelProvider[] inputModelProviders, ModelProvider[] modelProviders) {
		Set input = new HashSet();
		List result = new ArrayList();
		input.addAll(Arrays.asList(inputModelProviders));
		for (int i = 0; i < modelProviders.length; i++) {
			ModelProvider provider = modelProviders[i];
			if (!input.contains(provider))
				result.add(provider);
		}
		return (ModelProvider[]) result.toArray(new ModelProvider[result.size()]);
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
	 * operation from the input mappings. By default, this method passes
	 * the resource mapping context and the result of {@link #consultModelsWhenGeneratingScope()}
	 * to the scope builder constructor.
	 * <p>
	 * This method can be overridden by subclasses.
	 * 
	 * @return the scope builder used to build the scope of this
	 * operation from the input mappings.
	 */
	protected ScopeGenerator getScopeGenerator() {
		if (scope != null)
			return ((ResourceMappingScope)scope).getGenerator();
		return new ScopeGenerator(getResourceMappingContext(), consultModelsWhenGeneratingScope());
	}

	/**
	 * Prompt the user to inform them that additional resource mappings
	 * have been included in the operations.
	 * @param requestPreviewMessage message to be displayed for the option to force a preview
	 * (or <code>null</code> if the preview option should not be presented
	 * @param monitor a progress monitor
	 * @return whether a preview of the operation results was requested
	 * @throws OperationCanceledException if the user choose to cancel
	 */
	protected boolean promptForInputChange(String requestPreviewMessage, IProgressMonitor monitor) {
		return showAllMappings(requestPreviewMessage);
	}

    private boolean showAllMappings(final String requestPreviewMessage) {
        final boolean[] canceled = new boolean[] { false };
        final boolean[] forcePreview = new boolean[] { false };
        getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                AdditionalMappingsDialog dialog = new AdditionalMappingsDialog(getShell(), TeamUIMessages.ResourceMappingOperation_0, getScope(), getContext());
                dialog.setPreviewMessage(requestPreviewMessage);
                int result = dialog.open();
                canceled[0] = result != Window.OK;
                if (requestPreviewMessage != null) {
                	forcePreview[0] = dialog.isForcePreview();
                }
            }    
        });
        
        if (canceled[0]) {
            throw new OperationCanceledException();
        }
        return forcePreview[0];
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

	/**
	 * Execute the operation. This method is invoked after the 
	 * scope has been generated.
	 * @param monitor a progress monitor
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	protected abstract void execute(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException;

	/**
	 * Return the scope of this operation.
	 * @return the scope of this operation
	 */
	public IResourceMappingScope getScope() {
		return scope;
	}

	/**
	 * Return whether a preview of the operation before it is performed is
	 * desired.
	 * @return whether a preview of the operation before it is performed is
	 * desired
	 */
	public boolean isPreviewRequested() {
		return previewRequested;
	}
	
	/**
	 * Return whether the model providers should be consulted
	 * when generating the scope. When <code>true</code>, the scope
	 * generation process will consult any model providers to determine
	 * if additional mappings, and hence additional resources, need to
	 * be included in the operation. If <code>false</code>, the models
	 * are not consulted and only the input mappings are included in the 
	 * scope. a value of <code>true</code> is returned by default.
	 * @return whether the model providers should be consulted
	 * when generating the scope
	 */
	protected boolean consultModelsWhenGeneratingScope() {
		return true;
	}
	
	/**
	 * Return the resource mapping context used during the 
	 * scope generation and refresh process to determine
	 * what resources are to be included in the resulting
	 * synchronization context.
	 * Subclasses may override.
	 * @return the resource mapping context
	 */
	protected ResourceMappingContext getResourceMappingContext() {
		return ResourceMappingContext.LOCAL_CONTEXT;
	}
	
}
