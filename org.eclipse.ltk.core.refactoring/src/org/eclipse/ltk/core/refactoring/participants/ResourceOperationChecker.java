/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IFile;

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
 * @TODO Make references to the corresponding core API
 * 
 * <p>
 * The API is experimental and might change before its final state.
 * </p>
 * @since 3.2
 */
public class ResourceOperationChecker implements IConditionChecker {

	public RefactoringStatus check(IProgressMonitor monitor) throws CoreException {
		return new RefactoringStatus();
	}

	public IFile[] getChangedFiles() {
		// TODO forward to delta when available
		return new IFile[0];
	}
}