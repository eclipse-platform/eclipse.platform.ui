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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.team.ui.operations.ScopeGenerator;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;

/**
 * Interface which defines the protocol for translating a set of
 * <code>ResourceMapping</code> objects representing a view selection into the
 * complete set of resources to be operated on.
 * <p>
 * This interface is not intended to be implemented by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * @see ScopeGenerator
 * 
 * @since 3.2
 */
public interface IResourceMappingScope extends ISynchronizeScope {

	/**
	 * Return the array of mappings that acted as the input to the scope builder
	 * that was used to build this scope. This set usually come from a view
	 * selection but could come from another source. In most cases, clients will
	 * want to call the {@link #getMappings()} method instead of this one as it
	 * returns the complete set of mappings to be operated on.
	 * 
	 * @return the set of mappings that acted as the input to the scope builder
	 *         that was used to build this scope
	 */
	public ResourceMapping[] getInputMappings();

	/**
	 * Return an array of all of the mappings to be operated on. The returned
	 * mappings were included in the operation during the scope building
	 * process. The returned mappings may be the same as the input mappings but
	 * may also be a super set. Clients can call the
	 * {@link #hasAdditionalMappings()} method to determine if the two sets are
	 * the same or not.
	 * 
	 * @return an array of all of the mappings to be operated on.
	 */
	public ResourceMapping[] getMappings();

	/**
	 * Return an array of traversals that cover the resource mappings to be
	 * operated on as returned by the {@link #getMappings()} method. The
	 * traversals were calculated during the scope building process and cached
	 * with the scope.
	 * 
	 * @return the complete set of mappings to be operated on
	 */
	public ResourceTraversal[] getTraversals();

	/**
	 * Return an array of traversals that cover the given resource mapping to be
	 * operated on. The traversals were calculated during the scope building
	 * process and cached with the scope.
	 * 
	 * @param mapping a resource mapping being operated on
	 * @return the traversals that cover the given resource mapping (or
	 *         <code>null</code> if the mapping is not contained in the input)
	 */
	public ResourceTraversal[] getTraversals(ResourceMapping mapping);

	/**
	 * Return whether the scope has additional mappings added to the input
	 * mappings during the scope building process.
	 * 
	 * @return whether the input has additional mappings added to the seed
	 *         mappings
	 */
	public boolean hasAdditionalMappings();

	/**
	 * Return all the model providers that have mappings in this scope.
	 * 
	 * @return all the model providers that have mappings in this scope
	 */
	public ModelProvider[] getModelProviders();

	/**
	 * Return all the mappings to be operated on for the given model provider
	 * id.
	 * 
	 * @param modelProviderId the id of the model provider
	 * @return all the mappings for the given model provider id
	 */
	public ResourceMapping[] getMappings(String modelProviderId);

	/**
	 * Return the set of resource that form the roots of this scope.
	 * The roots are obtained from the set of traversals associated
	 * with the scope ignoring the depth of the traversals.
	 * @return the set of resource that form the roots of this scope
	 */
	public IResource[] getRoots();

}
