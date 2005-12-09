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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryManager;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * Resource mapping for a refactoring descriptor object.
 * 
 * @since 3.2
 */
public final class RefactoringDescriptorResourceMapping extends ResourceMapping {

	/** The refactoring descriptor */
	private final RefactoringDescriptorProxy fDescriptor;

	/** The resource traversals */
	private ResourceTraversal[] fResourceTraversals= null;

	/**
	 * Creates a new refactoring descriptor resource mapping.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 */
	public RefactoringDescriptorResourceMapping(final RefactoringDescriptorProxy descriptor) {
		Assert.isNotNull(descriptor);
		fDescriptor= descriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(final Object object) {
		if (object instanceof RefactoringDescriptorResourceMapping) {
			final RefactoringDescriptorResourceMapping mapping= (RefactoringDescriptorResourceMapping) object;
			return mapping.fDescriptor.equals(fDescriptor);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModelObject() {
		return fDescriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProject[] getProjects() {
		final String project= fDescriptor.getProject();
		if (project != null && !"".equals(project)) //$NON-NLS-1$
			return new IProject[] { ResourcesPlugin.getWorkspace().getRoot().getProject(project)};
		return new IProject[] {};
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceTraversal[] getTraversals(final ResourceMappingContext context, final IProgressMonitor monitor) throws CoreException {
		if (fResourceTraversals == null) {
			fResourceTraversals= new ResourceTraversal[] {};
			final long stamp= fDescriptor.getTimeStamp();
			if (stamp >= 0) {
				final IPath path= RefactoringHistoryManager.stampToPath(stamp);
				if (path != null) {
					final IProject[] projects= getProjects();
					if (projects != null && projects.length == 1 && projects[0] != null) {
						final IFolder folder= projects[0].getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER).getFolder(path);
						fResourceTraversals= new ResourceTraversal[] { new ResourceTraversal(new IResource[] { folder.getFile(RefactoringHistoryService.NAME_HISTORY_FILE)}, IResource.DEPTH_ZERO, IResource.NONE), new ResourceTraversal(new IResource[] { folder.getFile(RefactoringHistoryService.NAME_INDEX_FILE)}, IResource.DEPTH_ZERO, IResource.NONE)};
					}
				}
			}
		}
		return fResourceTraversals;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return fDescriptor.hashCode();
	}
}