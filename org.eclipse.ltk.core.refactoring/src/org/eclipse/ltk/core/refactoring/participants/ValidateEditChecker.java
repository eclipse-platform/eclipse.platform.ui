/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.Resources;

/**
 * A validate edit checker is a shared checker to collect files
 * to be validated all at once. A validate edit checker checks
 * if the files are in sync with the underlying files system.
 * Additionally <code>IWorkspace#validateEdit</code> is called for
 * all read-only resources.
 * <p> 
 * Note: this class is not intended to be extended by clients.
 * </p>
 * 
 * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
 * 
 * @since 3.0
 */
public class ValidateEditChecker implements IConditionChecker {

	private List fFiles= new ArrayList();
	private Object fContext;
	
	/**
	 * The context passed to the validate edit call.
	 * 
	 * @param context the <code>org.eclipse.swt.widgets.Shell</code> that is
	 * to be used to parent any dialogs with the user, or <code>null</code> if
	 * there is no UI context (declared as an <code>Object</code> to avoid any
	 * direct references on the SWT component)
	 * 
	 * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
	 */
	public ValidateEditChecker(Object context) {
		fContext= context;
	}
	
	/**
	 * Adds the given file to this checker.
	 * 
	 * @param file the file to add
	 */
	public void addFile(IFile file) {
		Assert.isNotNull(file);
		fFiles.add(file);
	}
	
	/**
	 * Adds the given array of files.
	 * 
	 * @param files the array of files to add
	 */
	public void addFiles(IFile[] files) {
		Assert.isNotNull(files);
		fFiles.addAll(Arrays.asList(files));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus check(IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor= new NullProgressMonitor();
		IResource[] resources= (IResource[])fFiles.toArray(new IResource[fFiles.size()]);
		RefactoringStatus result= new RefactoringStatus();
		IStatus status= Resources.checkInSync(resources);
		if (!status.isOK())
			result.merge(RefactoringStatus.create(status));
		status= Resources.makeCommittable(resources, fContext);
		if (!status.isOK()) {
			result.merge(RefactoringStatus.create(status));
			if (!result.hasFatalError()) {
				result.addFatalError(RefactoringCoreMessages.getString("ValidateEditChecker.failed")); //$NON-NLS-1$
			}
		}
		return result;
	}
}
