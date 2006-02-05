/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.mapping;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.mapping.provider.SynchronizationScopeManager;

/**
 * A scope manager is responsible for ensuring that the resources
 * contained within an {@link ISynchronizationScope} stay up-to-date
 * with the model elements (represented as {@link ResourceMapping} instances)
 * contained in the scope. The task of keeping a scope up-to-date is
 * accomplished by obtaining {@link ISynchronizationScopeParticipant} instances
 * for each model that has elements contained in the scope.
 * 
 * <p>
 * This interface is not intended to be implemented by clients. Clients can instead
 * subclass {@link SynchronizationScopeManager}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * @see SynchronizationScopeManager
 * @see ISynchronizationScopeParticipant
 * 
 * @since 3.2
 */
public interface ISynchronizationScopeManager {
	
	/**
	 * Return the scope that is managed by this manager.
	 * @return the scope that is managed by this manager
	 */
	ISynchronizationScope getScope();
	
	/**
	 * Return the projects that apply to this manager.
	 * The projects returned will depend on the type of context used
	 * to generate this scope. If the context is a local context,
	 * all workspace projects are returned. If it is a remote context,
	 * the projects are the same as those returned from
	 * {@link RemoteResourceMappingContext#getProjects()}
	 * @return the projects that apply to this manager
	 */
	IProject[] getProjects();
	
	/**
	 * Return the resource mapping contxt that the scope
	 * uses to obtain traversals from resource mappings
	 * in order to determine what resources are in the scope.
	 * 
	 * @see ResourceMapping#getTraversals(ResourceMappingContext, org.eclipse.core.runtime.IProgressMonitor)
	 * 
	 * @return the resource mapping contxt that the scope
	 * uses to obtain traversals from resource mappings
	 */
	ResourceMappingContext getContext();

	/**
	 * Return whether the scope has been initialized.
	 * @return whether the scope has been initialized.
	 */
	boolean isInitialized();
	
	/**
	 * Build the scope that is used to determine the complete set of resource
	 * mappings, and hence resources, that an operation should be performed on.
	 * <p>
	 * This method obtaines a lock on the workspace root to avoid workspace
	 * changes while calculating the scope.
	 * @param monitor a progress monitor
	 * when building the scope
	 * 
	 * @throws CoreException
	 */
	void initialize(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Refresh the scope of this manager for the given mappings.
	 * Changes in the scope will be reported as a property change
	 * event fired from the scope. Clients should call this method
	 * when a change in the workspace or a change issued from this
	 * manager have resulted in a change in the resources that 
	 * should be included in the scope.
	 * @param mappings the mappings to be refreshed
	 * @param monitor a progress monitor
	 * @return a set of traversals that cover the given mappings
	 * @throws CoreException
	 */
	ResourceTraversal[] refresh(ResourceMapping[] mappings, IProgressMonitor monitor) throws CoreException;

	/**
	 * Method to be invoked when the scope of this
	 * manager is no longer needed. It is typically the
	 * reponsibility of the client that creates a scope manager 
	 * to dispose of it.
	 */
	void dispose();

	/**
	 * Refresh the given mapping asynchronously. This method
	 * is called by {@link ISynchronizationScopeParticipant}
	 * instances when they detect changes that require the scope
	 * to be adjusted.
	 * @param mappings the mappings to be refeshed.
	 */
	void refresh(ResourceMapping[] mappings);

}
