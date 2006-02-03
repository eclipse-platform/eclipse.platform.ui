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

import org.eclipse.team.core.mapping.provider.IResourceMappingScopeParticipant;

/**
 * Implementation of the {@link IResourceMappingScopeParticipant} class.
 * 
 * @see IResourceMappingScopeParticipantFactory
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @since 3.2
 */
public abstract class ResourceMappingScopeParticipant implements
		IResourceMappingScopeParticipant {

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.provider.IResourceMappingScopeParticipant#dispose()
	 */
	public void dispose() {
		// Do nothing, by default
	}

}
