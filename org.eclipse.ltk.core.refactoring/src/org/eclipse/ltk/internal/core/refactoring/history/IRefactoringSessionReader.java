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

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;

/**
 * Interface for objects which read refactoring information from externalized
 * data.
 * 
 * @since 3.2
 */
public interface IRefactoringSessionReader {

	/**
	 * Reads a refactoring from the specified input object.
	 * 
	 * @param input
	 *            the input object
	 * @param stamp
	 *            the time stamp of the refactoring
	 * @return a corresponding refactoring descriptor, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while reading form the input
	 */
	public RefactoringDescriptor readDescriptor(Object input, long stamp) throws CoreException;

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