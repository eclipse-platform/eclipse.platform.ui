/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.history;

import org.eclipse.ltk.ui.refactoring.RefactoringUI;

/**
 * Extension interface to {@link IRefactoringHistoryControl} which provides
 * facilities to set the sort mode of a refactoring history control.
 * <p>
 * Clients of this interface should call <code>createControl</code> before
 * calling <code>setInput</code>.
 * </p>
 * <p>
 * An instanceof of a sortable refactoring history control may be obtained by
 * calling
 * {@link RefactoringUI#createSortableRefactoringHistoryControl(org.eclipse.swt.widgets.Composite, RefactoringHistoryControlConfiguration)}.
 * </p>
 * <p>
 * Note: this interface is not intended to be implemented by clients.
 * </p>
 *
 * @see RefactoringHistoryControlConfiguration
 * @see RefactoringHistoryContentProvider
 * @see RefactoringHistoryLabelProvider
 *
 * @since 3.3
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISortableRefactoringHistoryControl extends IRefactoringHistoryControl {

	/**
	 * Is sorting by date enabled?
	 *
	 * @return <code>true</code> if it is enabled, <code>false</code>
	 *         otherwise
	 */
	public boolean isSortByDate();

	/**
	 * Is sorting by projects enabled?
	 *
	 * @return <code>true</code> if it is enabled, <code>false</code>
	 *         otherwise
	 */
	public boolean isSortByProjects();

	/**
	 * Sorts the refactorings by date.
	 */
	public void sortByDate();

	/**
	 * Sorts the refactorings by projects.
	 */
	public void sortByProjects();
}