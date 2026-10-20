/*******************************************************************************
 * Copyright (c) 2026 Felix Schmid
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Felix Schmid - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.resource;

import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * {@link Change} that copies a resource.
 *
 * @since 3.16
 */
public class CopyResourceChange extends ResourceChange {

	private ChangeDescriptor descriptor;

	private final IResource origin;

	private final ReorgExecutionLog log;

	private final IContainer destination;

	public CopyResourceChange(final IResource origin, final ReorgExecutionLog log, final IContainer destination) {
		Assert.isTrue(origin instanceof IFile || origin instanceof IFolder);
		this.origin= origin;
		this.log= log;
		this.destination= destination;
		setValidationMethod(VALIDATE_NOT_DIRTY);
	}

	@Override
	public String getName() {
		return MessageFormat.format(RefactoringCoreMessages.CopyResourceChange_name,
				BasicElementLabels.getPathLabel(origin.getFullPath(), false),
				BasicElementLabels.getResourceName(destination));
	}

	@Override
	public final Change perform(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		CompositeChange undoChanges= new CompositeChange(
				RefactoringCoreMessages.CopyResourceChange_undo_composite_name);
		copyMerge(origin, destination, undoChanges, pm);
		return undoChanges;
	}

	private void copyMerge(IResource res, IContainer dest, CompositeChange undoChanges,
			IProgressMonitor pm) throws CoreException {
		SubMonitor monitor= SubMonitor.convert(pm, getName(), 1);

		if (res instanceof IFile) { // copy and overwrite normal files
			performResourceCopy(res, dest, undoChanges, monitor.split(1));
		} else if (res instanceof IFolder folder) {
			String newName= log.getNewName(res);
			if (newName == null) {
				newName= res.getName();
			}
			final IResource resAtDest= dest.findMember(newName);

			if (!resourceExists(resAtDest) || !(resAtDest instanceof IFolder folderAtDest) ||
					!homogeneousResources(folder, resAtDest)) { // copy non-existing or non-homogeneous folders
				performResourceCopy(res, dest, undoChanges, monitor.split(1));
			} else { // merge copy members of existing folders
				monitor.setWorkRemaining(folder.members().length);
				for (IResource member : folder.members()) {
					copyMerge(member, folderAtDest, undoChanges, monitor.split(1));
				}
			}
		}
	}

	private void performResourceCopy(IResource res, IContainer dest, CompositeChange undoChanges,
			IProgressMonitor pm) throws CoreException {
		SubMonitor subMonitor= SubMonitor.convert(pm, getName(), 2);
		String newName= log.getNewName(res);
		if (newName == null) {
			newName= res.getName();
		}
		final IResource resAtDest= dest.findMember(newName);

		if (resourceExists(resAtDest) && areEqualInWorkspaceOrOnDisk(res, resAtDest)) {
			log.markAsProcessed(res);
			return;
		}

		final Change undoOverwrite= deleteIfAlreadyExists(resAtDest, subMonitor.newChild(1));
		final IPath copyTo= dest.getFullPath().append(newName);
		res.copy(copyTo, getReorgFlags(), subMonitor.newChild(1));
		log.markAsProcessed(res);

		undoChanges.add(new DeleteResourceChange(copyTo, false));
		if (undoOverwrite != null) {
			undoChanges.add(undoOverwrite);
		}
	}

	@Override
	protected IResource getModifiedResource() {
		return origin;
	}

	/**
	 * deletes a resource if it exists and returns a <code>Change</code> to undo the deletion
	 *
	 * @param resource the resource to delete
	 * @param pm the progress monitor
	 * @return returns an undo <code>Change</code> or <code>null</code> if nothing was deleted
	 * @throws CoreException thrown when the resource cannot be accessed
	 */
	private Change deleteIfAlreadyExists(final IResource resource, final IProgressMonitor pm) throws CoreException {
		if (!resourceExists(resource)) {
			pm.done();
			return null;
		}
		SubMonitor subMonitor= SubMonitor.convert(pm,
				RefactoringCoreMessages.MoveResourceChange_progress_delete_destination, 3);
		DeleteResourceChange deleteChange= new DeleteResourceChange(resource.getFullPath(), true);
		deleteChange.initializeValidationData(subMonitor.newChild(1));
		RefactoringStatus deleteStatus= deleteChange.isValid(subMonitor.newChild(1));
		if (!deleteStatus.hasFatalError()) {
			return deleteChange.perform(subMonitor.newChild(1));
		}
		return null;
	}

	private static boolean areEqualInWorkspaceOrOnDisk(final IResource r1, final IResource r2) {
		if (r1 == null || r2 == null) {
			return false;
		}
		if (r1.equals(r2)) {
			return true;
		}
		final URI r1Location= r1.getLocationURI();
		final URI r2Location= r2.getLocationURI();
		if (r1Location == null || r2Location == null) {
			return false;
		}
		return r1Location.equals(r2Location);
	}

	private static boolean resourceExists(final IResource resource) {
		return resource != null && resource.exists();
	}

	private static boolean homogeneousResources(IResource res1, IResource res2) {
		boolean res1Linked= res1.isLinked();
		boolean res2Linked= res2.isLinked();
		return res1Linked && res2Linked || !res1Linked && !res2Linked;
	}

	private static int getReorgFlags() {
		return IResource.KEEP_HISTORY | IResource.SHALLOW;
	}

	@Override
	public ChangeDescriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(ChangeDescriptor descriptor) {
		this.descriptor= descriptor;
	}
}
