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

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;

/**
 * Interface which defines the protocol for translating
 * a set of <code>ResourceMapping</code> objects representing
 * a view selection into the complete set of resources to
 * be operated on.
 * <p>
 * This interface is not intended to be implemented by clients
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
public interface IResourceMappingOperationInput {

	/**
	 * Return the set of mappings that were selected
	 * when the operation was launched. These mappings
	 * are used to seed the input determination process.
	 * @return the set of mappings that were selected
	 * when the operation was launched
	 */
	public ResourceMapping[] getSeedMappings();
	
	/**
	 * Calculate the set of mappings to be operated on.
	 * This method must be called before <code>getInputMappings</code>
	 * or <code>getInputTraversals</code>.
	 * 
	 * @param monitor a progress monitor
	 * @throws CoreException
	 */
	public void buildInput(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Return the complete set of mappings to be operated on.
	 * This method should only be invoked after <code>buildInput</code>
	 * is called.
	 * @return the complete set of mappings to be operated on
	 */
	public ResourceMapping[] getInputMappings();
	
	/**
	 * Return the set of traversals that cover the input
	 * resource mappings.
	 * This method should only be invoked after <code>buildInput</code>
	 * is called.
	 * @return the complete set of mappings to be operated on
	 */
	public ResourceTraversal[] getInputTraversals();
	
	/**
	 * Return the traversals that cover the given mapping.
	 * @param mapping a resource mapping being operated on
	 * @return the traversals that cover the given resource mapping
	 * (or <code>null</code> if the mapping is not contained in the input)
	 */
	public ResourceTraversal[] getTraversals(ResourceMapping mapping);

	/**
	 * Return whether the input has additional mappings added to the
	 * seed mappings.
	 * This method should only be invoked after <code>buildInput</code>
	 * is called.
	 * @return whether the input has additional mappings added to the
	 * seed mappings
	 */
	public boolean hasAdditionalMappings();

	public ModelProvider[] getModelProviders();

	public ISynchronizeScope asSynchronizationScope();

	public ResourceMapping[] getResourceMappings(String id);
}
