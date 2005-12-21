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
package org.eclipse.team.internal.core.mapping;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.internal.core.subscribers.AbstractSynchronizationScope;

/**
 * Class that contains common resource mapping scope code.
 */
public abstract class AbstractResourceMappingScope extends AbstractSynchronizationScope implements IResourceMappingScope {

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScope#getMapping(java.lang.Object)
	 */
	public ResourceMapping getMapping(Object modelObject) {
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			if (mapping.getModelObject().equals(modelObject))
				return mapping;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScope#getMappings(java.lang.String)
	 */
	public ResourceMapping[] getMappings(String id) {
		Set result = new HashSet();
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			if (mapping.getModelProviderId().equals(id)) {
				result.add(mapping);
			}
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScope#getModelProviders()
	 */
	public ModelProvider[] getModelProviders() {
		Set result = new HashSet();
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			ModelProvider modelProvider = mapping.getModelProvider();
			if (modelProvider != null)
				result.add(modelProvider);
		}
		return (ModelProvider[]) result.toArray(new ModelProvider[result.size()]);
	}
	
}
