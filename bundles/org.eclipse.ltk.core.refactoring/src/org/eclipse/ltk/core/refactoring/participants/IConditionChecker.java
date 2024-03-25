/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A condition checker can be used to share condition checks
 * across the main processor and all its associated participants.
 * <p>
 * This interface should be implemented by clients wishing to provide a
 * special refactoring processor with special shared condition checks.
 * </p>
 *
 * @see org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext
 *
 * @since 3.0
 */
public interface IConditionChecker {

	/**
	 * Performs the actual condition checking.
	 *
	 * @param monitor a progress monitor to report progress
	 * @return the outcome of the condition check
	 *
	 * @throws CoreException if an error occurred during condition
	 *  checking. The check is interpreted as failed if this happens
	 */
	RefactoringStatus check(IProgressMonitor monitor) throws CoreException;
}
