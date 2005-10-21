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

import java.util.*;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;

/**
 * A concrete implementation of the <code>ITeamViewerContext</code>
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 *
 * @since 3.2
 */
public class TeamViewerContext implements ITeamViewerContext {
    
	private final ResourceMapping[] mappings;

	public TeamViewerContext(ResourceMapping[] mappings) {
		this.mappings = mappings;
	}

	public ResourceMapping[] getResourceMappings(String modelProviderId) {
		if (modelProviderId.equals(ALL_MAPPINGS)) {
			return mappings;
		}
		List result = new ArrayList();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			if (mapping.getModelProviderId().equals(modelProviderId)) {
				result.add(mapping);
			}
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
	}

	public ModelProvider[] getModelProviders() {
		Set providers = new HashSet();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			providers.add(mapping.getModelProvider());
		}
		return (ModelProvider[]) providers.toArray(new ModelProvider[providers.size()]);
	}

}
