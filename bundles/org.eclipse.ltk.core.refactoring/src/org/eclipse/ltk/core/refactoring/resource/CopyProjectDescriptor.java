/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.resource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CopyRefactoring;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.resource.CopyProjectProcessor;

/**
 * Refactoring descriptor for the copy project refactoring.
 * <p>
 * An instance of this refactoring descriptor may be obtained by calling
 * {@link RefactoringContribution#createDescriptor()} on a refactoring contribution requested by
 * invoking {@link RefactoringCore#getRefactoringContribution(String)} with the refactoring id
 * ({@link #ID}).
 * </p>
 * <p>
 * Note: this class is not intended to be subclassed or instantiated by clients.
 * </p>
 *
 * @since 3.15
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CopyProjectDescriptor extends RefactoringDescriptor {
	/**
	 * Refactoring id of the 'Copy Project' refactoring (value:
	 * <code>org.eclipse.ltk.core.refactoring.copyproject.resources</code>).
	 * <p>
	 * Clients may safely cast the obtained refactoring descriptor to {@link CopyProjectDescriptor}.
	 * </p>
	 */
	public static final String ID= "org.eclipse.ltk.core.refactoring.copyproject.resource"; //$NON-NLS-1$

	private IPath fSourcePath;

	private String fNewName;

	private IPath fNewLocation;

	/**
	 * Creates a new refactoring descriptor.
	 * <p>
	 * Clients should not instantiated this class but use
	 * {@link RefactoringCore#getRefactoringContribution(String)} with {@link #ID} to get the
	 * contribution that can create the descriptor.
	 * </p>
	 */
	public CopyProjectDescriptor() {
		super(ID, null, RefactoringCoreMessages.RenameResourceDescriptor_unnamed_descriptor, null, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
	}

	/**
	 * The resource paths to delete.
	 *
	 * @return an array of IPaths.
	 */
	public IPath getSourcePath() {
		return fSourcePath;
	}

	public String getNewName() {
		return fNewName;
	}

	public IPath getNewLocation() {
		return fNewLocation;
	}

	/**
	 * The paths to the resources to be deleted. The resources can be {@link IProject} or a mixture
	 * of {@link IFile} and {@link IFolder}.
	 *
	 * @param resourcePath paths of the resources to be deleted
	 */
	public void setResourcePath(IPath resourcePath) {
		if (resourcePath == null)
			throw new IllegalArgumentException();
		fSourcePath= resourcePath;
	}

	/**
	 * The project to be copied.
	 *
	 * @param project {@link IProject} to be copied
	 */
	public void setProjectToCopy(IProject project) {
		if (project == null)
			throw new IllegalArgumentException();
		setResourcePath(project.getFullPath());
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		IWorkspaceRoot wsRoot= ResourcesPlugin.getWorkspace().getRoot();
		IResource resource= wsRoot.findMember(fSourcePath);
		if (resource == null || !resource.exists()) {
			status.addFatalError(Messages.format(RefactoringCoreMessages.CopyProjectDescriptor_project_copy_does_not_exist, BasicElementLabels.getPathLabel(fSourcePath, false)));
			return null;
		}
		if (resource instanceof IProject project) {
			return new CopyRefactoring(new CopyProjectProcessor(project, fNewName, fNewLocation));
		}
		return null;
	}

	public void setNewName(String newName) {
		fNewName= newName;

	}

	public void setNewLocation(IPath newLocation) {
		fNewLocation= newLocation;
	}

}
