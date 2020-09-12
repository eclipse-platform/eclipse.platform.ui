/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
 *     Red Hat Inc. - created based on RenameResourceDescriptor
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.resource;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IContainer;
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
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.resource.MoveRenameResourceProcessor;

/**
 * Refactoring descriptor for the move/rename resource refactoring.
 * <p>
 * An instance of this refactoring descriptor may be obtained by calling
 * {@link RefactoringContribution#createDescriptor()} on a refactoring
 * contribution requested by invoking
 * {@link RefactoringCore#getRefactoringContribution(String)} with the
 * refactoring id ({@link #ID}).
 * </p>
 * <p>
 * Note: this class is not intended to be subclassed or instantiated by clients.
 * </p>
 *
 * @since 3.10
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class MoveRenameResourceDescriptor extends RefactoringDescriptor {

	/**
	 * Refactoring id of the 'Move/Rename Resource' refactoring (value:
	 * <code>org.eclipse.ltk.core.refactoring.moverename.resource</code>).
	 * <p>
	 * Clients may safely cast the obtained refactoring descriptor to
	 * {@link MoveRenameResourceDescriptor}.
	 * </p>
	 */
	public static final String ID= "org.eclipse.ltk.core.refactoring.moverename.resource"; //$NON-NLS-1$


	/** The name attribute */
	private String fNewName;

	/** The target container */
	private IPath fDestinationPath;

	/** The resource path attribute (full path) */
	private IPath fResourcePath;

	/** Configures if references will be updated */
	private boolean fUpdateReferences;

	/**
	 * Creates a new refactoring descriptor.
	 * <p>
	 * Clients should not instantiated this class but use {@link RefactoringCore#getRefactoringContribution(String)}
	 * with {@link #ID} to get the contribution that can create the descriptor.
	 * </p>
	 */
	public MoveRenameResourceDescriptor() {
		super(ID, null, RefactoringCoreMessages.RenameResourceDescriptor_unnamed_descriptor, null, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		fResourcePath= null;
		fNewName= null;
		fDestinationPath= null;
	}

	/**
	 * Sets the new name to rename the resource to.
	 *
	 * @param name
	 *            the non-empty new name to set
	 */
	public void setNewName(final String name) {
		Assert.isNotNull(name);
		Assert.isLegal(!"".equals(name), "Name must not be empty"); //$NON-NLS-1$//$NON-NLS-2$
		fNewName= name;
	}

	/**
	 * Returns the new name to rename the resource to.
	 *
	 * @return
	 *            the new name to rename the resource to
	 */
	public String getNewName() {
		return fNewName;
	}

	/**
	 * Sets the destination container to move the resources in.
	 *
	 * @param container
	 *            the destination
	 */
	public void setDestination(IContainer container) {
		Assert.isNotNull(container);
		fDestinationPath= container.getFullPath();
	}

	/**
	 * Sets the path of the destination container to move the resources in.
	 *
	 * @param path
	 *            the destination path
	 */
	public void setDestinationPath(IPath path) {
		Assert.isNotNull(path);
		fDestinationPath= path;
	}

	/**
	 * Returns the destination container to move the resources in.
	 *
	 * @return
	 *            the destination container to move the resource in
	 */
	public IPath getDestinationPath() {
		return fDestinationPath;
	}

	/**
	 * Sets the project name of this refactoring.
	 * <p>
	 * Note: If the resource to be renamed is of type {@link IResource#PROJECT},
	 * clients are required to to set the project name to <code>null</code>.
	 * </p>
	 * <p>
	 * The default is to associate the refactoring with the workspace.
	 * </p>
	 *
	 * @param project
	 *            the non-empty project name to set, or <code>null</code> for
	 *            the workspace
	 *
	 * @see #getProject()
	 */
	@Override
	public void setProject(final String project) {
		super.setProject(project);
	}

	/**
	 * Sets the resource to be renamed.
	 * <p>
	 * Note: If the resource to be renamed is of type {@link IResource#PROJECT},
	 * clients are required to to set the project name to <code>null</code>.
	 * </p>
	 *
	 * @param resourcePath
	 *            the resource to be renamed
	 */
	public void setResourcePath(IPath resourcePath) {
		Assert.isNotNull(resourcePath);
		fResourcePath= resourcePath;
	}

	/**
	 * Returns the path of the resource to rename.
	 *
	 * @return
	 *          the path of the resource to rename
	 */
	public IPath getResourcePath() {
		return fResourcePath;
	}

	/**
	 * 	If set to <code>true</code>, this rename will also update references. The default is to update references.
	 *
	 * @param updateReferences  <code>true</code> if this rename will update references
	 */
	public void setUpdateReferences(boolean updateReferences) {
		fUpdateReferences= updateReferences;
	}

	/**
	 * Returns if this rename will also update references
	 *
	 * @return returns <code>true</code> if this rename will update references
	 */
	public boolean isUpdateReferences() {
		return fUpdateReferences;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		IPath resourcePath= getResourcePath();
		if (resourcePath == null) {
			status.addFatalError(RefactoringCoreMessages.MoveRenameResourceDescriptor_error_path_not_set);
			return null;
		}

		IResource resource= ResourcesPlugin.getWorkspace().getRoot().findMember(resourcePath);
		if (resource == null || !resource.exists()) {
			status.addFatalError(Messages.format(RefactoringCoreMessages.MoveRenameResourceDescriptor_error_resource_not_existing, BasicElementLabels.getPathLabel(resourcePath, false)));
			return null;
		}

		IPath destinationPath= getDestinationPath();
		if (destinationPath == null) {
			status.addFatalError(RefactoringCoreMessages.MoveResourcesDescriptor_error_destination_not_set);
			return null;
		}

		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IResource destination= root.findMember(destinationPath);
		if ((!(destination instanceof IFolder) && !(destination instanceof IProject)) || !destination.exists()) {
			status.addFatalError(Messages.format(RefactoringCoreMessages.MoveResourcesDescriptor_error_destination_not_exists, BasicElementLabels.getPathLabel(destinationPath, false)));
			return null;
		}

		String newName= getNewName();
		if (newName == null || newName.length() == 0) {
			status.addFatalError(RefactoringCoreMessages.MoveRenameResourceDescriptor_error_name_not_defined);
			return null;
		}
		MoveRenameResourceProcessor processor= new MoveRenameResourceProcessor(resource);
		processor.setNewResourceName(newName);
		processor.setDestination((IContainer)destination);
		processor.setUpdateReferences(isUpdateReferences());

		return new RenameRefactoring(processor);
	}
}