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
package org.eclipse.team.core.mapping.provider;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.RemoteResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.team.core.mapping.*;

/**
 * A scope participant is responsible for ensuring that the resources contained
 * within an {@link IResourceMappingScope} that overlap with the participant's
 * model provider stay up-to-date with the model elements (represented as
 * {@link ResourceMapping} instances) contained in the scope.
 * 
 * <p>
 * This interface is not intended to be implemented by clients. CLients should instead subclass
 * {@link ResourceMappingScopeParticipant}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * @see IResourceMappingScopeManager
 * 
 * @since 3.2
 */
public interface IResourceMappingScopeParticipant {

	/**
	 * Callback that the manager makes to participants when the state of
	 * resources that are contained in the resource mapping context of the
	 * manager change. This method will only be invoked when the context of the
	 * manager is a {@link RemoteResourceMappingContext} and the state of one or
	 * more resources changes w.r.t. the context. It is the responsibility of the
	 * participant to react to local changes that affect the resources in the
	 * scope by calling
	 * {@link IResourceMappingScopeManager#refresh(ResourceMapping[], org.eclipse.core.runtime.IProgressMonitor)}.
	 * 
	 * @param manager
	 *            the scope manager
	 * @param resources
	 *            the changed resources
	 * @param projects 
	 *            projects that were either added or removed
	 * @return the resource mappings that need to be refreshed.
	 */
	ResourceMapping[] handleContextChange(
			IResourceMappingScopeManager manager, IResource[] resources, IProject[] projects);

	/**
	 * Callback from the scope manager when the scope is no longer needed.
	 * This si done to give participants a chance to remove a
	 * registered {@link IResourceChangeListener} or any other listeners.
	 */
	void dispose();

}
