/*******************************************************************************
 * Copyright (c) 2005 Tobias Widmer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Tobias Widmer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

/**
 * Interface for objects which are capable of creating a specific refactoring
 * instance.
 * <p>
 * Note: this interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringInstanceCreator {

	/**
	 * Creates the a refactoring arguments for the specified refactoring
	 * descriptor.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @return the refactoring arguments, or <code>null</code>
	 */
	public RefactoringArguments createArguments(RefactoringDescriptor descriptor);

	/**
	 * Creates the a refactoring instance for the specified refactoring
	 * descriptor.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @return the refactoring, or <code>null</code>
	 * @throws CoreException
	 *             if the refactoring could not be created from the descriptor
	 */
	public Refactoring createRefactoring(RefactoringDescriptor descriptor) throws CoreException;
}