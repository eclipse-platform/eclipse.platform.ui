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

import org.eclipse.core.resources.IProject;

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
 * @since 3.2
 */
public interface IRefactoringHistoryControl {

	/**
	 * Creates the control.
	 * <p>
	 * The content provider and the label provider must have been set before
	 * calling this method.
	 * </p>
	 */
	public void createControl();

	/**
	 * Returns the selected refactoring descriptors.
	 * 
	 * @return the selected refactoring descriptors
	 */
	public RefactoringDescriptorProxy[] getSelectedDescriptors();

	/**
	 * Sets the content provider to use.
	 * 
	 * @param provider
	 *            the content provider
	 */
	public void setContentProvider(RefactoringHistoryContentProvider provider);

	/**
	 * Determines whether time information should be displayed.
	 * <p>
	 * Note: the default value is <code>true</code>.
	 * </p>
	 * 
	 * @param display
	 *            <code>true</code> to display time information,
	 *            <code>false</code> otherwise
	 */
	public void setDisplayTime(boolean display);

	/**
	 * Sets the label provider to use.
	 * 
	 * @param provider
	 *            the label provider to use
	 */
	public void setLabelProvider(RefactoringHistoryLabelProvider provider);

	/**
	 * Sets the message to display below the refactoring tree.
	 * 
	 * @param message
	 *            the message to display, or <code>null</code>
	 */
	public void setMessage(String message);

	/**
	 * Sets the project which the history belongs to.
	 * <p>
	 * Note: the project does not have to exist.
	 * </p>
	 * 
	 * @param project
	 *            the project, or <code>null</code>
	 */
	public void setProject(IProject project);

	/**
	 * Sets the refactoring history of this control.
	 * 
	 * @param history
	 *            the refactoring history, or <code>null</code>
	 */
	public void setRefactoringHistory(RefactoringHistory history);
}
