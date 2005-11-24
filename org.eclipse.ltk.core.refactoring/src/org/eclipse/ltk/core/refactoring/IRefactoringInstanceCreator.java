/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.CoreException;


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