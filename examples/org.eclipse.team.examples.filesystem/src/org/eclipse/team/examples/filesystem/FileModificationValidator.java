/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.examples.filesystem;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations;
import sun.security.action.GetPropertyAction;

/**
 * This class models a sentry that verifies whether resources are available for editing or overwriting.
 * This has been made a separate clas for illustration purposes. It may have been more apporpriate
 * to have FileSystemProvider implement IFileModificationValidator itself since the interface
 * only has two methods and their implementation is straight forward.
 */
public final class FileModificationValidator implements IFileModificationValidator {
	//Used to avoid creating multiple copies of the OK status:
	private static final IStatus OK_STATUS = new Status(Status.OK, FileSystemPlugin.ID, Status.OK, Policy.bind("ok"), null);

	private RepositoryProvider provider;
	private SimpleAccessOperations operations;

	/**
	 * Constructor for FileModificationValidator.
	 */
	public FileModificationValidator(RepositoryProvider provider) {
		this.provider = provider;
		operations = provider.getSimpleAccess();
	}

	/**
	 * This method will convert any exceptions thrown by the SimpleAccessOperations.chechout() to a Status.
	 * @param resources the resources that are to be checked out
	 * @return IStatus a status indicator that reports whether the operation went smoothly or not.
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#chechout(IResource[] resources, int depth, IProgressMonitor progress)
	 */
	private IStatus checkout(IResource[] resources) {
		try {
			operations.checkout(resources, IResource.DEPTH_INFINITE, null);
		} catch (TeamException e) {
			return new Status(Status.ERROR, FileSystemPlugin.ID, Status.ERROR, e.getLocalizedMessage(), e);
		}
		return OK_STATUS;
	}

	/**
	 * This method will be called by the workbench/editor before it tries to edit one or more files.
	 * The idea is to prevent anyone from accidentally working on a file that they won't be able to check in changes to.
	 * @see org.eclipse.core.resources.IFileModificationValidator#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
		Collection toBeCheckedOut = new ArrayList();

		//Make a list of all the files that need to be checked out:
		for (int i = 0; i < files.length; i++) {
			if (!operations.isCheckedOut(files[i])) {
				toBeCheckedOut.add(files[i]);
			}
		}
		
		return checkout((IResource[]) toBeCheckedOut.toArray(new IResource[toBeCheckedOut.size()]));
	}

	/**
	 * This method will be called by the workbench before it tries to save a file.
	 * It should not attempt to save any files that don't recieve an OK status here.
	 * @see org.eclipse.core.resources.IFileModificationValidator#validateSave(IFile)
	 */
	public IStatus validateSave(IFile file) {
		return checkout(new IResource[] { file });
	}

}
