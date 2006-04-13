/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Viewer sorter for the browse refactoring history control.
 * 
 * @since 3.2
 */
public final class BrowseRefactoringHistoryViewerSorter extends ViewerSorter {

	/**
	 * {@inheritDoc}
	 */
	public int category(final Object element) {
		if (element instanceof RefactoringHistoryProject)
			return 0;
		else if (element instanceof RefactoringHistoryDate)
			return 1;
		return 2;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(final Viewer viewer, final Object first, final Object second) {
		if (first instanceof RefactoringHistoryProject && second instanceof RefactoringHistoryProject) {
			final RefactoringHistoryProject predecessor= (RefactoringHistoryProject) first;
			final RefactoringHistoryProject successor= (RefactoringHistoryProject) second;
			return getCollator().compare(predecessor.getProject(), successor.getProject());
		} else if (first instanceof RefactoringHistoryDate && second instanceof RefactoringHistoryDate) {
			final RefactoringHistoryDate predecessor= (RefactoringHistoryDate) first;
			final RefactoringHistoryDate successor= (RefactoringHistoryDate) second;
			return (int) (successor.getTimeStamp() - predecessor.getTimeStamp());
		}
		return super.compare(viewer, first, second);
	}
}