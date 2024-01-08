/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ltk.internal.core.refactoring.resource.undostates;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * {@link ProjectUndoState} is a lightweight description that describes a project to
 * be created.
 *
 * This class is not intended to be instantiated or used by clients.
 *
 * @since 3.4
 */
public class ProjectUndoState extends ContainerUndoState {

	private IProjectDescription projectDescription;
	private boolean openOnCreate = true;

	/**
	 * Create a {@link ProjectUndoState} from a specified project.
	 *
	 * @param project
	 *            The project to be described. The project must exist.
	 */
	public ProjectUndoState(IProject project) {
		super(project);
		Assert.isLegal(project.exists());
		if (project.isOpen()) {
			try {
				this.projectDescription = project.getDescription();
			} catch (CoreException e) {
				// Eat this exception because it only occurs when the project
				// is not accessible and we have already checked this. We
				// don't want to propagate the CoreException into the
				// constructor
				// API.
			}
		} else {
			openOnCreate = false;
		}
	}

	/**
	 * Create a {@link ProjectUndoState} from a specified IProjectDescription. Used
	 * when the project does not yet exist.
	 *
	 * @param projectDescription
	 *            the project description for the future project
	 */
	public ProjectUndoState(IProjectDescription projectDescription) {
		super();
		this.projectDescription = projectDescription;
	}

	@Override
	public IResource createResourceHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getName());
	}

	@Override
	public void createExistentResourceFromHandle(IResource resource,
			IProgressMonitor monitor) throws CoreException {
		Assert.isLegal(resource instanceof IProject);
		if (resource.exists()) {
			return;
		}
		IProject projectHandle = (IProject) resource;
		SubMonitor subMonitor= SubMonitor.convert(monitor, RefactoringCoreMessages.FolderDescription_NewFolderProgress, 200);
		if (projectDescription == null) {
			projectHandle.create(subMonitor.split(100));
		} else {
			projectHandle.create(projectDescription, subMonitor.split(100));
		}

		if (openOnCreate) {
			projectHandle.open(IResource.NONE, subMonitor.split(100));
		}
		subMonitor.done();
	}



	@Override
	public String getName() {
		if (projectDescription != null) {
			return projectDescription.getName();
		}
		return super.getName();
	}

	@Override
	public boolean verifyExistence(boolean checkMembers) {
		// We can only check members if the project is open.
		IProject projectHandle = (IProject) createResourceHandle();
		if (projectHandle.isAccessible()) {
			return super.verifyExistence(checkMembers);
		}
		return super.verifyExistence(false);
	}
}
