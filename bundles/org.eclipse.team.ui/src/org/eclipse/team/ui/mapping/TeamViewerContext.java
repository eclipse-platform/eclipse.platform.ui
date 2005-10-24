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

import org.eclipse.core.resources.mapping.*;


/**
 * A concrete implementation of the <code>ITeamViewerContext</code>
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
public class TeamViewerContext implements ITeamViewerContext {
    
	private final IResourceMappingOperationInput input;

	public TeamViewerContext(IResourceMappingOperationInput input) {
		this.input = input;
	}
	
	/**
	 * Return the input used to create this operation context.
	 * @return the input used to create this operation context
	 */
	public IResourceMappingOperationInput getInput() {
		return input;
	}

	public ModelProvider[] getModelProviders() {
		return getInput().getModelProviders();
	}

	public ResourceMapping[] getResourceMappings(String id) {
		if (id.equals(ALL_MAPPINGS))
			return getInput().getInputMappings();
		return getInput().getResourceMappings(id);
	}

	public ResourceTraversal[] getTraversals() {
		return getInput().getInputTraversals();
	}

	public ResourceTraversal[] getTraversals(ResourceMapping mapping) {
		return getInput().getTraversals(mapping);
	}

}
