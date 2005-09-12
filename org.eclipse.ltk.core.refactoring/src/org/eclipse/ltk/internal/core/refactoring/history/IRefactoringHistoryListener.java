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

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

/**
 * Interface for listeners to the refactoring history.
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
	 * This method is called by the refactoring history if a refactoring is
	 * about to be performed.
	 * 
	 * @param history
	 *            the refactoring history this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void aboutToPerformRefactoring(IRefactoringHistory history, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history if a refactoring is
	 * about to be redone.
	 * 
	 * @param history
	 *            the refactoring history this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void aboutToRedoRefactoring(IRefactoringHistory history, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history if a refactoring is
	 * about to be undone.
	 * 
	 * @param history
	 *            the refactoring history this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void aboutToUndoRefactoring(IRefactoringHistory history, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history if a refactoring has
	 * been performed.
	 * 
	 * @param history
	 *            the refactoring history this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void refactoringPerformed(IRefactoringHistory history, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history if a refactoring has
	 * been redone.
	 * 
	 * @param history
	 *            the refactoring history this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void refactoringRedone(IRefactoringHistory history, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history if a refactoring has
	 * been undone.
	 * 
	 * @param history
	 *            the refactoring history this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void refactoringUndone(IRefactoringHistory history, RefactoringDescriptor descriptor);
}