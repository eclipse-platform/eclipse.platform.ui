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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.Resources;

/**
 * A validate edit checker is a shared checker to collect files
 * to be validated all at once.
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
	 * @param context the context passed to the validate edit call
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
		return RefactoringStatus.create( 
			Resources.makeCommittable(
				(IResource[]) fFiles.toArray(new IResource[fFiles.size()]), fContext));
	}
}
