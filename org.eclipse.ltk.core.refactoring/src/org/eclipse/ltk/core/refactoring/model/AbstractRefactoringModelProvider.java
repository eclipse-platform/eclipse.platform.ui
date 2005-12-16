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
package org.eclipse.ltk.core.refactoring.model;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;

import org.eclipse.ltk.internal.core.refactoring.model.RefactoringResourceMapping;

/**
 * Partial implementation of a refactoring-aware logical model provider.
 * <p>
 * Note: this class is intended to be extended outside the refactoring
 * framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public class AbstractRefactoringModelProvider extends ModelProvider {

	/** The model provider id */
	private final String fProviderId;

	/**
	 * Creates a new abstract refactoring model provider.
	 * 
	 * @param id
	 *            the fully qualified id of the model provider
	 */
	protected AbstractRefactoringModelProvider(final String id) {
		Assert.isNotNull(id);
		fProviderId= id;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceMapping[] getMappings(final IResource resource, final ResourceMappingContext context, final IProgressMonitor monitor) throws CoreException {
		return new ResourceMapping[] { new RefactoringResourceMapping(resource, fProviderId)};
	}
}