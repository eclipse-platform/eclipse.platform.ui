/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources.mapping;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A simple resource mapping for converting IResource to ResourceMapping.
 * It uses the resource as the model object and traverses deeply.
 * 
 * @since 3.1
 */
public class SimpleResourceMapping extends ResourceMapping {
	private final IResource resource;
	private org.eclipse.core.resources.mapping.ResourceTraversal[] traversals;

	public SimpleResourceMapping(IResource resource) {
		this.resource = resource;
	}

	/* (non-Javadoc)
	 * Method declared on ResourceMapping.
	 */
	public Object getModelObject() {
		return resource;
	}

	/* (non-Javadoc)
	 * Method declared on ResourceMapping.
	 */
	public IProject[] getProjects() {
		return new IProject[] {resource.getProject()};
	}

	/* (non-Javadoc)
	 * Method declared on ResourceMapping.
	 */
	public org.eclipse.core.resources.mapping.ResourceTraversal[] getTraversals(org.eclipse.core.resources.mapping.ResourceMappingContext context, IProgressMonitor monitor) {
		if (traversals == null) {
			traversals = new org.eclipse.core.resources.mapping.ResourceTraversal[] {new org.eclipse.core.resources.mapping.ResourceTraversal(new IResource[] {resource}, IResource.DEPTH_INFINITE, IResource.NONE)};
		}
		return traversals;
	}
}
