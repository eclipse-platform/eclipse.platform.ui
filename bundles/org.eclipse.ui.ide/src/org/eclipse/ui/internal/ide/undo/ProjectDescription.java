/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.undo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

/**
 * ProjectDescription is a lightweight description that describes a project to
 * be created.
 *
 * This class is not intended to be instantiated or used by clients.
 *
 * @since 3.3
 *
 */
public class ProjectDescription extends ContainerDescription {

	private IProjectDescription projectDescription;
	private boolean openOnCreate = true;

	/**
	 * Create a project description from a specified project.
	 *
	 * @param project
	 *            The project to be described. The project must exist.
	 */
	public ProjectDescription(IProject project) {
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
	 * Create a project description from a specified IProjectDescription. Used
	 * when the project does not yet exist.
	 *
	 * @param projectDescription
	 *            the project description for the future project
	 */
	public ProjectDescription(IProjectDescription projectDescription) {
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
		SubMonitor subMonitor = SubMonitor.convert(monitor, 200);
		Assert.isLegal(resource instanceof IProject);
		if (resource.exists()) {
			return;
		}
		IProject projectHandle = (IProject) resource;
		subMonitor.setTaskName(UndoMessages.FolderDescription_NewFolderProgress);
		if (projectDescription == null) {
			projectHandle.create(subMonitor.newChild(100));
		} else {
			projectHandle.create(projectDescription, subMonitor.newChild(100));
		}

		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (openOnCreate) {
			projectHandle.open(IResource.NONE, subMonitor.newChild(100));
		}
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