/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.resource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.resource.UndoDeleteResourceChange;
import org.eclipse.ltk.internal.core.refactoring.resource.undostates.ResourceUndoState;

/**
 * {@link Change} that deletes a resource.
 *
 * @since 3.4
 */
public class DeleteResourceChange extends ResourceChange {

	private IPath fResourcePath;
	private boolean fForceOutOfSync;
	private boolean fDeleteContent;
	private ChangeDescriptor fDescriptor;

	/**
	 * Delete a resource.
	 *
	 * @param resourcePath the resource path
	 * @param forceOutOfSync if <code>true</code>, deletes the resource with {@link IResource#FORCE}
	 */
	public DeleteResourceChange(IPath resourcePath, boolean forceOutOfSync) {
		this(resourcePath, forceOutOfSync, false);
	}

	/**
	 * Delete a resource.
	 *
	 * @param resourcePath the project path
	 * @param forceOutOfSync if <code>true</code>, deletes the resource with {@link IResource#FORCE}
	 * @param deleteContent if <code>true</code> delete the project contents.
	 * The content delete is not undoable. This setting only applies to projects and is not used when deleting files or folders.
	 */
	public DeleteResourceChange(IPath resourcePath, boolean forceOutOfSync, boolean deleteContent) {
		fResourcePath= resourcePath;
		fForceOutOfSync= forceOutOfSync;
		fDeleteContent= deleteContent;
		setValidationMethod(VALIDATE_NOT_DIRTY);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.resource.ResourceChange#getModifiedResource()
	 */
	protected IResource getModifiedResource() {
		return getResource();
	}

	private IResource getResource() {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(fResourcePath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return Messages.format(RefactoringCoreMessages.DeleteResourceChange_name, BasicElementLabels.getPathLabel(fResourcePath.makeRelative(), false));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();

		pm.beginTask("", 10); //$NON-NLS-1$
		pm.setTaskName(RefactoringCoreMessages.DeleteResourceChange_deleting);

		try {
			IResource resource= getResource();
			if (resource == null || !resource.exists()) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), RefactoringCoreMessages.DeleteResourceChange_error_resource_not_exists));
			}

			// make sure all files inside the resource are saved so restoring works
			if (resource.isAccessible()) {
				resource.accept(new IResourceVisitor() {
					public boolean visit(IResource curr) throws CoreException {
						try {
							if (curr instanceof IFile) {
								// progress is covered outside.
								saveFileIfNeeded((IFile) curr, new NullProgressMonitor());
							}
						} catch (CoreException e) {
							// ignore
						}
						return true;
					}
				}, IResource.DEPTH_INFINITE, false);
			}

			ResourceUndoState desc= ResourceUndoState.fromResource(resource);
			if (resource instanceof IProject) {
				((IProject) resource).delete(fDeleteContent, fForceOutOfSync, new SubProgressMonitor(pm, 10));
			} else {
				int updateFlags;
				if (fForceOutOfSync) {
					updateFlags= IResource.KEEP_HISTORY | IResource.FORCE;
				} else {
					updateFlags= IResource.KEEP_HISTORY;
				}
				resource.delete(updateFlags, new SubProgressMonitor(pm, 5));
				desc.recordStateFromHistory(resource, new SubProgressMonitor(pm, 5));
			}
			return new UndoDeleteResourceChange(desc);
		} finally {
			pm.done();
		}
	}

	private static void saveFileIfNeeded(IFile file, IProgressMonitor pm) throws CoreException {
		ITextFileBuffer buffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
		if (buffer != null && buffer.isDirty() && buffer.isStateValidated() && buffer.isSynchronized()) {
			pm.beginTask("", 2); //$NON-NLS-1$
			buffer.commit(new SubProgressMonitor(pm, 1), false);
			file.refreshLocal(IResource.DEPTH_ONE, new SubProgressMonitor(pm, 1));
			pm.done();
		} else {
			pm.beginTask("", 1); //$NON-NLS-1$
			pm.worked(1);
			pm.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getDescriptor()
	 */
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
