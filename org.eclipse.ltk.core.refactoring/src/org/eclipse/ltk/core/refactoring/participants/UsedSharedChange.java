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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * A special change indicating that the participant has added
 * its modifications to a shared text change.
 * 
 * @since 3.1
 */
public final class UsedSharedChange extends Change {

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return RefactoringCoreMessages.getString("UsedSharedChange.name"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public void initializeValidationData(IProgressMonitor pm) {
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		return new UsedSharedChange();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModifiedElement() {
		return null;
	}
}
