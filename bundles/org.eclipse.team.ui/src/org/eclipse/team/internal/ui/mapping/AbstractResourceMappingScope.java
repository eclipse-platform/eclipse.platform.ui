/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.team.ui.mapping.IResourceMappingScope;
import org.eclipse.team.ui.synchronize.AbstractSynchronizeScope;

/**
 * Class that contains common resource mapping scope code.
 */
public abstract class AbstractResourceMappingScope extends
		AbstractSynchronizeScope implements IResourceMappingScope {

	protected IResource[] roots;

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeScope#contains(org.eclipse.core.resources.IResource)
	 */
	public boolean contains(IResource resource) {
		ResourceTraversal[] traversals = getTraversals();
		for (int i = 0; i < traversals.length; i++) {
			ResourceTraversal traversal = traversals[i];
			if (traversal.contains(resource)) {
				return true;
			}
		}
		return false;
	}

	public IResource[] getRoots() {
		if (roots == null) {
			Set result = new HashSet();
			ResourceTraversal[] traversals = getTraversals();
			for (int i = 0; i < traversals.length; i++) {
				ResourceTraversal traversal = traversals[i];
				IResource[] resources = traversal.getResources();
				for (int j = 0; j < resources.length; j++) {
					IResource resource = resources[j];
					//TODO: should we check for parent/child relationships?
					result.add(resource);
				}
			}
			roots = (IResource[]) result.toArray(new IResource[result.size()]);
		}
		return roots;
	}

	public ResourceMapping getMapping(Object modelObject) {
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			if (mapping.getModelObject().equals(modelObject))
				return mapping;
		}
		return null;
	}

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

	public ModelProvider[] getModelProviders() {
		Set result = new HashSet();
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			result.add(mapping.getModelProvider());
		}
		return (ModelProvider[]) result.toArray(new ModelProvider[result.size()]);
	}
	
}
