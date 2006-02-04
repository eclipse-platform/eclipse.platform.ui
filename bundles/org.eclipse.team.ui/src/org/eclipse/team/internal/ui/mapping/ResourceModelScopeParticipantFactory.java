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
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.team.core.mapping.IResourceMappingScopeManager;
import org.eclipse.team.core.mapping.IResourceMappingScopeParticipantFactory;
import org.eclipse.team.core.mapping.provider.IResourceMappingScopeParticipant;

public class ResourceModelScopeParticipantFactory implements
		IResourceMappingScopeParticipantFactory {

	private final ModelProvider provider;

	public ResourceModelScopeParticipantFactory(ModelProvider provider) {
		this.provider = provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScopeParticipantFactory#createParticipant(org.eclipse.core.resources.mapping.ModelProvider, org.eclipse.team.core.mapping.provider.ResourceMappingScopeManager)
	 */
	public IResourceMappingScopeParticipant createParticipant(
			ModelProvider provider, IResourceMappingScopeManager manager) {
		return new ResourceModelScopeParticipant(this.provider, manager);
	}

}
