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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.team.core.mapping.IResourceMappingScope;

/**
 * This scope wraps another scope and treats the input mappings of
 * that wrapped scope as the mappings of this scope.
 */
public class ResourceMappingInputScope extends AbstractResourceMappingScope {

	IResourceMappingScope wrappedScope;
	
	public ResourceMappingInputScope(IResourceMappingScope wrappedScope) {
		
		this.wrappedScope = wrappedScope;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingScope#getInputMappings()
	 */
	public ResourceMapping[] getInputMappings() {
		return wrappedScope.getInputMappings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingScope#getMappings()
	 */
	public ResourceMapping[] getMappings() {
		return getInputMappings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingScope#getTraversals()
	 */
	public ResourceTraversal[] getTraversals() {
		ResourceMapping[] mappings = getMappings();
		List result = new ArrayList();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			ResourceTraversal[] traversals = getTraversals(mapping);
			for (int j = 0; j < traversals.length; j++) {
				ResourceTraversal traversal = traversals[j];
				result.add(traversal);
			}
		}
		return ResourceMappingScope.combineTraversals((ResourceTraversal[]) result.toArray(new ResourceTraversal[result.size()]));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingScope#getTraversals(org.eclipse.core.resources.mapping.ResourceMapping)
	 */
	public ResourceTraversal[] getTraversals(ResourceMapping mapping) {
		if (!contains(mapping)) {
			return null;
		}
		return wrappedScope.getTraversals(mapping);
	}

	private boolean contains(ResourceMapping mapping) {
		ResourceMapping[] mappings = getMappings();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping child = mappings[i];
			if (child.equals(mapping)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingScope#hasAdditionalMappings()
	 */
	public boolean hasAdditionalMappings() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScope#hasAdditonalResources()
	 */
	public boolean hasAdditonalResources() {
		return false;
	}
}
