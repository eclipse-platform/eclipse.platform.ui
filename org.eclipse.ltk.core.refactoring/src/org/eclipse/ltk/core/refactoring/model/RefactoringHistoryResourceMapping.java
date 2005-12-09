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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * Resource mapping for a refactoring history object.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryResourceMapping extends ResourceMapping {

	/** The refactoring history */
	private final RefactoringHistory fRefactoringHistory;

	/** The resource traversals */
	private ResourceTraversal[] fResourceTraversals= null;

	/**
	 * Creates a new refactoring history resource mapping.
	 * 
	 * @param history
	 *            the refactoring history
	 */
	public RefactoringHistoryResourceMapping(final RefactoringHistory history) {
		Assert.isNotNull(history);
		fRefactoringHistory= history;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(final Object object) {
		if (object instanceof RefactoringHistoryResourceMapping) {
			final RefactoringHistoryResourceMapping mapping= (RefactoringHistoryResourceMapping) object;
			return mapping.fRefactoringHistory.equals(fRefactoringHistory);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModelObject() {
		return fRefactoringHistory;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProject[] getProjects() {
		final IProject[] projects= { null};
		final RefactoringDescriptorProxy[] proxies= fRefactoringHistory.getDescriptors();
		for (int index= 0; index < proxies.length; index++) {
			final String name= proxies[index].getProject();
			if (name != null && !"".equals(name)) //$NON-NLS-1$
				projects[0]= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		}
		return projects;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceTraversal[] getTraversals(final ResourceMappingContext context, final IProgressMonitor monitor) throws CoreException {
		if (fResourceTraversals == null) {
			final IProject[] projects= getProjects();
			final ResourceTraversal[] traversals= new ResourceTraversal[projects.length];
			for (int index= 0; index < projects.length; index++)
				traversals[index]= new ResourceTraversal(new IResource[] { projects[index].getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER)}, IResource.DEPTH_INFINITE, IResource.NONE);
			fResourceTraversals= traversals;
		}
		return fResourceTraversals;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return fRefactoringHistory.hashCode();
	}
}