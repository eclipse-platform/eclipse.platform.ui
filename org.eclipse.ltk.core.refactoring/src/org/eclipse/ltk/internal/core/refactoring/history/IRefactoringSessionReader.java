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
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;

/**
 * Interface for objects which read a refactoring session descriptor from
 * externalized data.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringSessionReader {

	/**
	 * Reads a refactoring session from the specified input object.
	 * 
	 * @param input
	 *            the input object
	 * @return a corresponding refactoring session descriptor, or
	 *         <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while reading form the input
	 */
	public RefactoringSessionDescriptor readSession(Object input) throws CoreException;
}
