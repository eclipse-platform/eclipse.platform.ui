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
package org.eclipse.ltk.internal.core.refactoring.model;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;

/**
 * Default implementation for a resource mapping for the refactoring model
 * provider.
 * 
 * @since 3.2
 */
public class RefactoringResourceMapping extends ResourceMapping {

	/** The model provider id */
	private final String fProviderId;

	/** The resource to map */
	private final IResource fResource;

	/** The resource traversals */
	private ResourceTraversal[] fResourceTraversals= null;

	/**
	 * Creates a new refactoring resource mapping.
	 * 
	 * @param resource
	 *            the resource to map
	 * @param id
	 *            the fully qualified id of the model provider
	 */
	public RefactoringResourceMapping(final IResource resource, final String id) {
		Assert.isNotNull(resource);
		Assert.isNotNull(id);
		fResource= resource;
		fProviderId= id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModelObject() {
		return fResource;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getModelProviderId() {
		return fProviderId;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProject[] getProjects() {
		return new IProject[] { fResource.getProject()};
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceTraversal[] getTraversals(final ResourceMappingContext context, final IProgressMonitor monitor) {
		if (fResourceTraversals == null)
			fResourceTraversals= new ResourceTraversal[] { new ResourceTraversal(new IResource[] { fResource}, IResource.DEPTH_INFINITE, IResource.NONE)};
		return fResourceTraversals;
	}
}