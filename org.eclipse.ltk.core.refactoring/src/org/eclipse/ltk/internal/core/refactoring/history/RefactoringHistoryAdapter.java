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
 * This adapter class provides default implementations for the methods described
 * by the
 * {@link org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistoryListener}
 * interface.
 * <p>
 * Classes that wish to deal with refactoring history events can extend this
 * class and override only the methods which they are interested in.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistoryListener
 * 
 * @since 3.2
 */
public abstract class RefactoringHistoryAdapter implements IRefactoringHistoryListener {

	/*
	 * @see org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistoryListener#aboutToPerformRefactoring(org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistory,org.eclipse.ltk.core.refactoring.RefactoringDescriptor)
	 */
	public void aboutToPerformRefactoring(IRefactoringHistory history, RefactoringDescriptor descriptor) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistoryListener#aboutToRedoRefactoring(org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistory,org.eclipse.ltk.core.refactoring.RefactoringDescriptor)
	 */
	public void aboutToRedoRefactoring(IRefactoringHistory history, RefactoringDescriptor descriptor) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistoryListener#aboutToUndoRefactoring(org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistory,org.eclipse.ltk.core.refactoring.RefactoringDescriptor)
	 */
	public void aboutToUndoRefactoring(IRefactoringHistory history, RefactoringDescriptor descriptor) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistoryListener#refactoringPerformed(org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistory,org.eclipse.ltk.core.refactoring.RefactoringDescriptor)
	 */
	public void refactoringPerformed(IRefactoringHistory history, RefactoringDescriptor descriptor) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistoryListener#refactoringRedone(org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistory,org.eclipse.ltk.core.refactoring.RefactoringDescriptor)
	 */
	public void refactoringRedone(IRefactoringHistory history, RefactoringDescriptor descriptor) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistoryListener#refactoringUndone(org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistory,org.eclipse.ltk.core.refactoring.RefactoringDescriptor)
	 */
	public void refactoringUndone(IRefactoringHistory history, RefactoringDescriptor descriptor) {
		// Do nothing
	}
}