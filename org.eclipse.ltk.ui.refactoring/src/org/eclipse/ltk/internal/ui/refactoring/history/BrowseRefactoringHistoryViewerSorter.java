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
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Viewer sorter for the browse refactoring history control.
 *
 * @since 3.2
 */
public final class BrowseRefactoringHistoryViewerSorter extends ViewerComparator {

	/**
	 * {@inheritDoc}
	 */
	public int category(final Object element) {
		if (element instanceof RefactoringHistoryProject)
			return 0;
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(final Viewer viewer, final Object first, final Object second) {
		if (first instanceof RefactoringHistoryProject && second instanceof RefactoringHistoryProject) {
			final RefactoringHistoryProject predecessor= (RefactoringHistoryProject) first;
			final RefactoringHistoryProject successor= (RefactoringHistoryProject) second;
			return getComparator().compare(predecessor.getProject(), successor.getProject());
		} else if (first instanceof RefactoringHistoryDate && second instanceof RefactoringHistoryDate) {
			final RefactoringHistoryDate predecessor= (RefactoringHistoryDate) first;
			final RefactoringHistoryDate successor= (RefactoringHistoryDate) second;
			final int delta= predecessor.getKind() - successor.getKind();
			if (delta != 0)
				return delta;
			final long result= successor.getTimeStamp() - predecessor.getTimeStamp();
			if (result < 0)
				return -1;
			else if (result > 0)
				return 1;
			return 0;
		} else if (first instanceof RefactoringHistoryEntry && second instanceof RefactoringHistoryEntry) {
			final RefactoringHistoryEntry predecessor= (RefactoringHistoryEntry) first;
			final RefactoringHistoryEntry successor= (RefactoringHistoryEntry) second;
			final long delta= successor.getDescriptor().getTimeStamp() - predecessor.getDescriptor().getTimeStamp();
			if (delta < 0)
				return -1;
			else if (delta > 0)
				return 1;
			else
				return 0;
		}
		return super.compare(viewer, first, second);
	}
}