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

import org.eclipse.swt.widgets.Control;

/**
 * Control which is capable of displaying parts of a refactoring history.
 * <p>
 * Clients of this interface should call <code>createControl</code> before
 * calling <code>setInput</code>.
 * </p>
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
	 * Creates the refactoring history control.
	 * <p>
	 * This method creates the necessary widgets and initializes the refactoring
	 * history control. It is called only once. Method <code>getControl()</code>
	 * should be used to retrieve the widget hierarchy.
	 * </p>
	 * 
	 * @see #getControl()
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
	 * @return the selected refactoring descriptors, or an empty array.
	 * 
	 * @see IRefactoringHistoryControl#getSelectedDescriptors()
	 * @see RefactoringHistoryControlConfiguration#isCheckableViewer()
	 */
	public RefactoringDescriptorProxy[] getCheckedDescriptors();

	/**
	 * Returns the SWT control of this refactoring history control.
	 * 
	 * @return the SWT control, or <code>null</code> if the control's widget
	 *         hierarchy has not yet been created
	 */
	public Control getControl();

	/**
	 * Returns the selected refactoring descriptors.
	 * 
	 * @return the selected refactoring descriptors, or an empty array.
	 */
	public RefactoringDescriptorProxy[] getSelectedDescriptors();

	/**
	 * Sets the checked refactoring descriptors.
	 * <p>
	 * In case the refactoring history control is created with a non-checkable
	 * tree viewer, this method is equivalent to
	 * {@link #setSelectedDescriptors(RefactoringDescriptorProxy[])}.
	 * </p>
	 * 
	 * @param descriptors
	 *            the refactoring descriptors to check, or an empty array
	 * 
	 * @see IRefactoringHistoryControl#setSelectedDescriptors(RefactoringDescriptorProxy[])
	 * @see RefactoringHistoryControlConfiguration#isCheckableViewer()
	 */
	public void setCheckedDescriptors(RefactoringDescriptorProxy[] descriptors);

	/**
	 * Sets the refactoring history of this control.
	 * 
	 * @param history
	 *            the refactoring history, or <code>null</code>
	 */
	public void setInput(RefactoringHistory history);

	/**
	 * Sets the selected refactoring descriptors.
	 * 
	 * @param descriptors
	 *            the refactoring descriptors to select, or an empty array
	 */
	public void setSelectedDescriptors(RefactoringDescriptorProxy[] descriptors);
}