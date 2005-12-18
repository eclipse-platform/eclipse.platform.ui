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
package org.eclipse.ltk.ui.refactoring.history;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

/**
 * Control which is capable of displaying parts of a refactoring history.
 * <p>
 * Note: this interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see RefactoringHistoryControlConfiguration
 * @see RefactoringHistoryContentProvider
 * @see RefactoringHistoryLabelProvider
 * 
 * @since 3.2
 */
public interface IRefactoringHistoryControl {

	/**
	 * Creates the control.
	 */
	public void createControl();

	/**
	 * Returns the checked refactoring descriptors.
	 * <p>
	 * In case the refactoring history control is created with a non-checkable
	 * tree viewer, this method is equivalent to
	 * {@link #getSelectedDescriptors()}.
	 * </p>
	 * 
	 * @return the selected refactoring descriptors
	 * @see RefactoringHistoryControlConfiguration#isCheckableViewer()
	 */
	public RefactoringDescriptorProxy[] getCheckedDescriptors();

	/**
	 * Returns the selected refactoring descriptors.
	 * 
	 * @return the selected refactoring descriptors
	 */
	public RefactoringDescriptorProxy[] getSelectedDescriptors();

	/**
	 * Sets the refactoring history of this control.
	 * 
	 * @param history
	 *            the refactoring history, or <code>null</code>
	 */
	public void setInput(RefactoringHistory history);
}