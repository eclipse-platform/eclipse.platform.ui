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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * {@link Change} that copies a project
 *
 * @since 3.15
 */
public class CopyProjectChange extends ResourceChange {

	private final IProject fSourceProject;

	private ChangeDescriptor fDescriptor;

	private String fNewName;

	private IPath fNewLocation;

	/**
	 * Copy a project.
	 *
	 * @param resourcePath the project path
	 * @param newLocation location of the new project
	 * @param newName name of the new project
	 */
	public CopyProjectChange(IProject resourcePath, IPath newLocation, String newName) {
		Assert.isNotNull(resourcePath);
		fNewName= newName;
		fNewLocation= newLocation;
		fSourceProject= resourcePath;
		setValidationMethod(SAVE_IF_DIRTY);
	}

	@Override
	protected IResource getModifiedResource() {
		return fSourceProject;
	}


	@Override
	public String getName() {
		return RefactoringCoreMessages.CopyProjectChange_Name + fSourceProject.getName();
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		SubMonitor subMonitor= SubMonitor.convert(pm, RefactoringCoreMessages.CopyProjectChange_copying, 10);

		if (fSourceProject == null || !fSourceProject.exists()) {
			String message= Messages.format(RefactoringCoreMessages.CopyProjectChange_error_resource_not_exists,
					BasicElementLabels.getPathLabel(fSourceProject.getFullPath().makeRelative(), false));
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), message));
		}

		// make sure all files inside the resource are saved
		if (fSourceProject.isAccessible()) {
			fSourceProject.accept((IResourceVisitor) curr -> {
				try {
					if (curr instanceof IFile) {
						// progress is covered outside.
						saveFileIfNeeded((IFile) curr, new NullProgressMonitor());
					}
				} catch (CoreException e) {
					// ignore
				}
				return true;
			}, IResource.DEPTH_INFINITE, false);
		}

		IProjectDescription description= fSourceProject.getDescription();

		if (fNewLocation != null && (fNewLocation.equals(Platform.getLocation()) || fNewLocation.isRoot())) {
			fNewLocation= null;
		}

		description.setName(fNewName);
		description.setLocation(fNewLocation);

		fSourceProject.copy(description, IResource.FORCE | IResource.SHALLOW, subMonitor.newChild(10));

		IProject targetProject= fSourceProject.getWorkspace().getRoot().getProject(fNewName);

		return new DeleteResourceChange(targetProject.getFullPath(), true, true);

	}

	private static void saveFileIfNeeded(IFile file, IProgressMonitor pm) throws CoreException {
		ITextFileBuffer buffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
		SubMonitor subMonitor= SubMonitor.convert(pm, 2);
		if (buffer != null && buffer.isDirty() && buffer.isStateValidated() && buffer.isSynchronized()) {
			buffer.commit(subMonitor.newChild(1), false);
			file.refreshLocal(IResource.DEPTH_ONE, subMonitor.newChild(1));
			buffer.commit(subMonitor.newChild(1), false);
			file.refreshLocal(IResource.DEPTH_ONE, subMonitor.newChild(1));
		} else {
			subMonitor.worked(2);
		}
	}

	@Override
	public ChangeDescriptor getDescriptor() {
		return fDescriptor;
	}

	/**
	 * Sets the change descriptor to be returned by {@link Change#getDescriptor()}.
	 *
	 * @param descriptor the change descriptor
	 */
	public void setDescriptor(ChangeDescriptor descriptor) {
		fDescriptor= descriptor;
	}

}
