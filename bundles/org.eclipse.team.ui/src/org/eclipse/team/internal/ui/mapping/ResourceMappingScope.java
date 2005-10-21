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
package org.eclipse.team.internal.ui.mapping;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.team.ui.synchronize.AbstractSynchronizeScope;

/**
 * A synchronize scope whose roots are defined by the traversals
 * obtained from a set of resource mappings.
 * @since 3.2
 */
public class ResourceMappingScope extends AbstractSynchronizeScope {

	private ResourceMapping[] mappings;
	private ResourceTraversal[] traversals;
	private String name;
	private IResource[] roots;
	
	/**
	 * Create a resource mapping scope.
	 * @param name the name used to describe the scope (this may be displayed to users)
	 * @param mappings the mappings that define the scope
	 * @param traversals the traversals derived from the mappings using the context of the operation being performed
	 */
	public ResourceMappingScope(String name, ResourceMapping[] mappings, ResourceTraversal[] traversals) {
		this.name = name;
		this.mappings = mappings;
		this.traversals = traversals;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeScope#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeScope#getRoots()
	 */
	public IResource[] getRoots() {
		if (roots == null) {
			Set result = new HashSet();
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

	/**
	 * Return the resource mappings used to define this scope.
	 * @return the resource mappings used to define this scope
	 */
	public ResourceMapping[] getResourceMappings() {
		return mappings;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeScope#contains(org.eclipse.core.resources.IResource)
	 */
	public boolean contains(IResource resource) {
		for (int i = 0; i < traversals.length; i++) {
			ResourceTraversal traversal = traversals[i];
			if (traversal.contains(resource)) {
				return true;
			}
		}
		return false;
	}

}
