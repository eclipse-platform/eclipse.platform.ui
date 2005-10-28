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

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

/**
 * Interface for refactoring execution listeners. Clients may register a
 * refactoring execution listener with the {@link IRefactoringHistoryService}
 * obtained by calling {@link RefactoringCore#getRefactoringHistoryService()} in
 * order to get informed about refactoring execution events.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringExecutionListener {

	/**
	 * This method is called by the refactoring history service if a refactoring
	 * is about to be performed.
	 * 
	 * @param service
	 *            the refactoring history service this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void aboutToPerformRefactoring(IRefactoringHistoryService service, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history service if a refactoring
	 * is about to be redone.
	 * 
	 * @param service
	 *            the refactoring history service this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void aboutToRedoRefactoring(IRefactoringHistoryService service, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history service if a refactoring
	 * is about to be undone.
	 * 
	 * @param service
	 *            the refactoring history service this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void aboutToUndoRefactoring(IRefactoringHistoryService service, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history service if a refactoring
	 * has been performed.
	 * 
	 * @param service
	 *            the refactoring history service this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void refactoringPerformed(IRefactoringHistoryService service, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history service if a refactoring
	 * has been redone.
	 * 
	 * @param service
	 *            the refactoring history service this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void refactoringRedone(IRefactoringHistoryService service, RefactoringDescriptor descriptor);

	/**
	 * This method is called by the refactoring history service if a refactoring
	 * has been undone.
	 * 
	 * @param service
	 *            the refactoring history service this listener is registered to
	 * @param descriptor
	 *            the descriptor of the refactoring
	 */
	public void refactoringUndone(IRefactoringHistoryService service, RefactoringDescriptor descriptor);
}