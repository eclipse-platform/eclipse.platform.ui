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
package org.eclipse.team.core.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;

/**
 * A synchronization scope defines the set of resources involved in a synchronization
 * operation.
 * <p>
 * This interface is not intended to be implemented by clients
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p> 
 * @since 3.2
 */
public interface ISynchronizationScope {

	/**
	 * Property change constant used to indicate that the traversals
	 * of the scope have changed. The value associated with the
	 * change event will be an array of traversals (i.e.
	 * <code>ResourceTraversal[]</code>
	 */
	public static final String TRAVERSALS = "org.eclipse.team.core.traversalsChanged"; //$NON-NLS-1$
	
	/**
	 * Return the set of resource traversals that define this scope.
	 * A resource is considered to be contained in this scope if
	 * the resource is contained in at least one of the returned
	 * traversals.
	 * @return the set of resource traversals that define this scope
	 */
	public ResourceTraversal[] getTraversals();
	
	/**
	 * Return the resources that are the roots of this scope.
	 * The roots are determined by collecting the roots of
	 * the traversals that define this scope.
	 * @return the resources that are the roots of this scope
	 */
	public IResource[] getRoots();
	
	/**
	 * Return whether the given resource is contained in this scope.
	 * A resource is contained by the scope if it is contained in at
	 * least one of the traversals that define this scope.
	 * @param resource the resource to be tested
	 * @return whether the given resource is contained in this scope
	 */
	public boolean contains(IResource resource);
	
	/**
	 * Add a property change listener that will get invoked when a
	 * property of the receiver changes. Adding a listener that is
	 * already added has no effect.
	 * 
	 * @param listener the listener to be added
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener);
	
	/**
	 * Remove a property change listener. Removing an unregistered listener
	 * has no effect.
	 * 
	 * @param listener the listener to be removed
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener);
}
