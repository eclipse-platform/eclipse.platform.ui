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

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.team.core.mapping.provider.IResourceMappingScopeParticipant;

/**
 * Factory interface for creating a participant for use with an
 * {@link IResourceMappingScopeManager}. This factory should be
 * assocated with a {@link ModelProvider} using the {@link IAdaptable}
 * mechanism.
 * <p>
 * This interface may be implemented by clients.
 * 
 * @see ModelProvider
 * @see IAdaptable
 * @see IAdapterManager
 * @see IResourceMappingScopeManager
 * @see IResourceMappingScopeParticipant
 * @see ResourceMappingScopeParticipant
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @since 3.2
 */
public interface IResourceMappingScopeParticipantFactory {

	/**
	 * Create a participant in the scope management process for the given model provider.
	 * @param provider the model provider
	 * @param manager the scope manager
	 * @return a participant in the scope management process
	 */
	IResourceMappingScopeParticipant createParticipant(ModelProvider provider, IResourceMappingScopeManager manager);

}
