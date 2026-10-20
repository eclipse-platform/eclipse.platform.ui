/*******************************************************************************
 * Copyright (c) 2007, 2026 IBM Corporation and others.
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
 *     Felix Schmid - adapted for copy resource descriptor
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.resource;

import java.text.MessageFormat;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

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
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.resource.CopyResourcesProcessor;

/**
 * Refactoring descriptor for the copy resource refactoring.
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
 * @since 3.16
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class CopyResourcesDescriptor extends RefactoringDescriptor {
	/**
	 * Refactoring id of the 'Copy Resource' refactoring (value:
	 * <code>org.eclipse.ltk.core.refactoring.copy.resources</code>).
	 * <p>
	 * Clients may safely cast the obtained refactoring descriptor to
	 * {@link CopyResourcesDescriptor}.
	 * </p>
	 */
	public static final String ID= "org.eclipse.ltk.core.refactoring.copy.resources"; //$NON-NLS-1$

	private IPath[] resourcePaths;

	private IPath[] destinationPaths;

	/**
	 * Creates a new refactoring descriptor.
	 * <p>
	 * Clients should not instantiated this class but use
	 * {@link RefactoringCore#getRefactoringContribution(String)} with {@link #ID} to get the
	 * contribution that can create the descriptor.
	 * </p>
	 */
	public CopyResourcesDescriptor() {
		super(ID, null, RefactoringCoreMessages.RenameResourceDescriptor_unnamed_descriptor, null,
				RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
	}

	public IPath[] getResourcePaths() {
		return resourcePaths;
	}

	public IPath[] getDestinationPaths() {
		return destinationPaths;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		IWorkspaceRoot wsRoot= ResourcesPlugin.getWorkspace().getRoot();
		IResource[] resources= new IResource[resourcePaths.length];
		for (int i= 0; i < resourcePaths.length; i++) {
			IResource resource= wsRoot.findMember(resourcePaths[i]);
			if (resource == null || !resource.exists()) {
				status.addFatalError(MessageFormat.format(
						RefactoringCoreMessages.CopyResourcesDescriptor_error_resource_not_exists,
						BasicElementLabels.getPathLabel(resourcePaths[i], false)));
				return null;
			}
			resources[i]= resource;
		}
		return new CopyRefactoring(new CopyResourcesProcessor(resources, destinationPaths));
	}

	public void setResourcePaths(IPath[] resourcePaths) {
		Objects.requireNonNull(resourcePaths);
		this.resourcePaths= resourcePaths;
	}

	public void setResources(IResource[] resources) {
		Objects.requireNonNull(resources);
		IPath[] paths= new IPath[resources.length];
		for (int i= 0; i < paths.length; i++) {
			paths[i]= resources[i].getFullPath();
		}
		setResourcePaths(paths);
	}

	public void setDestinationPaths(IPath[] destinationPaths) {
		Objects.requireNonNull(destinationPaths);
		this.destinationPaths= destinationPaths;
	}
}
