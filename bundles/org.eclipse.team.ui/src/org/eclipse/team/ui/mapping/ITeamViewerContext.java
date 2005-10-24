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
 * A context determined by Team providers and passed to model views
 * in order to display a subset of a model that is involved in a team
 * operation
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
public interface ITeamViewerContext {

	public static final String ALL_MAPPINGS = "";
	
	/**
	 * Return the model providers that have mappings
	 * in this context.
	 * @return the model providers that have mappings
	 * in this context
	 */
	public ModelProvider[] getModelProviders();
	
	/**
	 * Return the set of resource mappings associated with 
	 * the given model provider affected by the
	 * team operation that provided the context. If all
	 * resource mappings are desired, pass <code>ALL_MAPPINGS</code>
	 * as the model provider id.
	 * @param modelProviderId the model provider id.
	 * @return a set of resource mappings
	 */
	public ResourceMapping[] getResourceMappings(String modelProviderId);
	
	/**
	 * Return the set of traversals that cover the 
	 * resource mappings in this context.
	 * @return the complete set of mappings to be operated on
	 */
	public ResourceTraversal[] getTraversals();
	
	/**
	 * Return the traversals that cover the given mapping.
	 * @param mapping a resource mapping being operated on
	 * @return the traversals that cover the given resource mapping
	 * (or <code>null</code> if the mapping is not in the context)
	 */
	public ResourceTraversal[] getTraversals(ResourceMapping mapping);

}
