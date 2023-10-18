/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A resource operation checker is a shared checker to collect all
 * changes done by the refactoring and the participants to resources
 * so that they can be validated as one change. A resource operation
 * checker supersedes the {@link ValidateEditChecker}. So if clients
 * add their content changes to this checker there is no need to add
 * them to the {@link ValidateEditChecker} as well.
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @see ResourceChangeValidator
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ResourceChangeChecker implements IConditionChecker {

	private IResourceChangeDescriptionFactory fDeltaFactory;

	public ResourceChangeChecker() {
		fDeltaFactory= ResourceChangeValidator.getValidator().createDeltaFactory();
	}

	/**
	 * A helper method to check a set of changed files.
	 *
	 * @param files the array of files that change
	 * @param monitor a progress monitor to report progress or <code>null</code>
	 *  if progress reporting is not desired
	 *
	 * @return a refactoring status containing the detect problems
	 * @throws CoreException a {@link CoreException} if an error occurs
	 *
	 * @see ResourceChangeValidator#validateChange(IResourceDelta, IProgressMonitor)
	 */
	public static RefactoringStatus checkFilesToBeChanged(IFile[] files, IProgressMonitor monitor) throws CoreException {
		ResourceChangeChecker checker= new ResourceChangeChecker();
		for (IFile file : files) {
			checker.getDeltaFactory().change(file);
		}
		return checker.check(monitor);
	}

	/**
	 * Returns the delta factory to be used to record resource
	 * operations.
	 *
	 * @return the delta factory
	 */
	public IResourceChangeDescriptionFactory getDeltaFactory() {
		return fDeltaFactory;
	}

	@Override
	public RefactoringStatus check(IProgressMonitor monitor) throws CoreException {
		IStatus status= ResourceChangeValidator.getValidator().validateChange(fDeltaFactory.getDelta(), monitor);
		return createFrom(status);
	}

	/* package */ IFile[] getChangedFiles() throws CoreException {
		IResourceDelta root= fDeltaFactory.getDelta();
		final List<IFile> result= new ArrayList<>();
		root.accept(delta -> {
			final IResource resource= delta.getResource();
			if (resource.getType() == IResource.FILE) {
				final int kind= delta.getKind();
				if (isSet(kind, IResourceDelta.CHANGED)) {
					result.add((IFile) resource);
				} else if (isSet(kind, IResourceDelta.ADDED) && isSet(delta.getFlags(), IResourceDelta.CONTENT | IResourceDelta.MOVED_FROM)) {
					final IFile movedFrom= resource.getWorkspace().getRoot().getFile(delta.getMovedFromPath());
					result.add(movedFrom);
				}
			}
			return true;
		});
		return result.toArray(new IFile[result.size()]);
	}

	private static final boolean isSet(int flags, int flag) {
		return (flags & flag) == flag;
	}

	private static RefactoringStatus createFrom(IStatus status) {
		if (status.isOK())
			return new RefactoringStatus();

		if (!status.isMultiStatus()) {
			switch (status.getSeverity()) {
				case IStatus.OK :
					return new RefactoringStatus();
				case IStatus.INFO :
					return RefactoringStatus.createInfoStatus(status.getMessage());
				case IStatus.WARNING :
					return RefactoringStatus.createWarningStatus(status.getMessage());
				case IStatus.ERROR :
					return RefactoringStatus.createErrorStatus(status.getMessage());
				case IStatus.CANCEL :
					return RefactoringStatus.createFatalErrorStatus(status.getMessage());
				default :
					return RefactoringStatus.createFatalErrorStatus(status.getMessage());
			}
		} else {
			IStatus[] children= status.getChildren();
			RefactoringStatus result= new RefactoringStatus();
			for (IStatus child : children) {
				result.merge(createFrom(child));
			}
			return result;
		}
	}
}
