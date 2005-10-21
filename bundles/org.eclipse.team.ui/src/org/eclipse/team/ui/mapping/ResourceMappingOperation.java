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
package org.eclipse.team.ui.mapping;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.internal.ui.dialogs.AdditionalMappingsDialog;
import org.eclipse.team.internal.ui.mapping.DefaultResourceMappingMerger;
import org.eclipse.team.ui.TeamOperation;
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
public abstract class ResourceMappingOperation extends TeamOperation {

	private final IResourceMappingOperationInput input;
    
    /**
     * Create a resource mapping based operation
     * @param part the workspace part from which the operation was launched
     * @param input the input to the operation (which must have already been built by
     * invoking <code>buildInput</code>.
     */
	protected ResourceMappingOperation(IWorkbenchPart part, IResourceMappingOperationInput input) {
		super(part);
		this.input = input;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		buildInput(monitor);
		execute(monitor);
	}

	/**
	 * Adjust the input of the operation according to the selected
	 * resource mappings and the set of interested participants
	 * @param monitor 
	 */
	protected void buildInput(IProgressMonitor monitor) throws InvocationTargetException {
		try {
			input.buildInput(monitor);
			if (input.hasAdditionalMappings()) {
				promptForInputChange(monitor);
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * Prompt the user to inform them that additional resource mappings
	 * have been included in the operations.
	 * @param monitor a progress monitor
	 * @throws OperationCanceledException if the user choose to cancel
	 */
	protected void promptForInputChange(IProgressMonitor monitor) {
		showAllMappings(input.getSeedMappings(), input.getInputMappings());
	}

    private void showAllMappings(final ResourceMapping[] selectedMappings, final ResourceMapping[] allMappings) {
        final boolean[] canceled = new boolean[] { false };
        getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                AdditionalMappingsDialog dialog = new AdditionalMappingsDialog(getShell(), "Participating Elements", selectedMappings, new TeamViewerContext(allMappings));
                int result = dialog.open();
                canceled[0] = result != Dialog.OK;
            }
        
        });
        
        if (canceled[0]) {
            throw new OperationCanceledException();
        }
    }
    
	protected abstract void execute(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException;

	/**
	 * Return the auto-merger associated with the given model provider
	 * view the adaptable mechanism.
	 * If the model provider does not have a merger associated with
	 * it, a default merger that performs the merge at the file level
	 * is returned.
	 * @param provider the model provider of the elements to be merged
	 * @return a merger
	 */
	protected IResourceMappingMerger getMerger(ModelProvider provider) {
		Object o = provider.getAdapter(IResourceMappingMerger.class);
		if (o instanceof IResourceMappingMerger) {
			return (IResourceMappingMerger) o;	
		}
		return new DefaultResourceMappingMerger(provider, getInput());
	}

	public IResourceMappingOperationInput getInput() {
		return input;
	}
	
}
