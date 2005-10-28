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
package org.eclipse.ltk.core.refactoring.history;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

/**
 * Interface for refactoring history listeners.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringHistoryListener {

	/**
	 * Connects the listener to the refactoring history service.
	 * <p>
	 * If the listener is already connected, nothing happens.
	 * </p>
	 */
	public void connect();

	/**
	 * Gets called if the specified refactoring descriptor is pushed onto the
	 * refactoring undo stack.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor to push
	 * @throws CoreException
	 *             if an error occurs
	 */
	public void descriptorAdded(RefactoringDescriptor descriptor) throws CoreException;

	/**
	 * Gets called if the specified refactoring descriptor is popped from the
	 * refactoring undo stack.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor to pop
	 * @throws CoreException
	 *             if an error occurs
	 */
	public void descriptorRemoved(RefactoringDescriptor descriptor) throws CoreException;

	/**
	 * Disconnects the listener from the refactoring history service.
	 * <p>
	 * If the listener is not connected, nothing happens.
	 * </p>
	 */
	public void disconnect();
}
