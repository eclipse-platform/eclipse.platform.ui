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
import org.eclipse.team.core.mapping.provider.IResourceMappingScopeParticipant;
import org.eclipse.team.core.mapping.provider.ResourceMappingScopeManager;

/**
 * A scope manager is responsible for ensuring that the resources
 * contained within an {@link IResourceMappingScope} stay up-to-date
 * with the model elements (represented as {@link ResourceMapping} instances)
 * contained in the scope. The task of keeping a scope up-to-date is
 * accomplished by obtaining {@link IResourceMappingScopeParticipant} instances
 * for each model that has elements contained in the scope.
 * 
 * <p>
 * This interface is not intended to be implemented by clients. Clients can instead
 * subclass {@link ResourceMappingScopeManager}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * @see ResourceMappingScopeManager
 * @see IResourceMappingScopeParticipant
 * 
 * @since 3.2
 */
public interface IResourceMappingScopeManager {
	
	/**
	 * Return the scope that is managed by this manager.
	 * @return the scope that is managed by this manager
	 */
	IResourceMappingScope getScope();
	
	/**
	 * Return the projects that apply to this manager.
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
	 * Add a listener to this scope. Listeners will be notified whenever the
	 * list of projects that apply to this scope change. Events will also be
	 * issued if the resource mapping context of this manager is a
	 * {@link RemoteResourceMappingContext} and the remote state of a resource
	 * that is a child of the projects.
	 * <p>
	 * Participants that wich toknow if the contents of the scope chaneg can add
	 * a property change listener to the scope using
	 * {@link ISynchronizationScope#addPropertyChangeListener(IPropertyChangeListener)}.
	 * 
	 * @param listener
	 *            a change listener
	 */
	void addListener(IScopeContextChangeListener listener);
	
	/**
	 * Remov the listener from the manager. Removing a listener that
	 * is not present has no effect.
	 * @param listener the listener
	 */
	void removeListener(IScopeContextChangeListener listener);

}
