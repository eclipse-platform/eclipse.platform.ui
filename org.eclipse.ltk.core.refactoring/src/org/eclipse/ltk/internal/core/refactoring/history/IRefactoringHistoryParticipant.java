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

/**
 * Interface for objects which participate in refactoring history maintenance.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringHistoryParticipant {

	/**
	 * Connects the participant to the refactoring history.
	 * <p>
	 * If the participant is already connected, nothing happens.
	 * </p>
	 */
	public void connect();

	/**
	 * Disconnects the participant from the refactoring history.
	 * <p>
	 * If the participant is not connected, nothing happens.
	 * </p>
	 */
	public void disconnect();

	/**
	 * Pops the specified refactoring descriptor from the top of the refactoring
	 * history.
	 * 
	 * @param descriptor
	 *            the descriptor to pop
	 * @throws CoreException
	 *             if an error occurs
	 */
	public void pop(RefactoringDescriptor descriptor) throws CoreException;

	/**
	 * Pushes the specified refactoring descriptor onto the refactoring history
	 * stack.
	 * 
	 * @param descriptor
	 *            the descriptor to push
	 * @throws CoreException
	 *             if an error occurs
	 */
	public void push(RefactoringDescriptor descriptor) throws CoreException;
}
