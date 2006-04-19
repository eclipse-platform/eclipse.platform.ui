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
			return RefactoringHistoryNode.PROJECT;
		else if (element instanceof RefactoringHistoryNode)
			return ((RefactoringHistoryNode) element).getKind() + 100;
		return Integer.MAX_VALUE;
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
		} else if (first instanceof RefactoringHistoryEntry && second instanceof RefactoringHistoryEntry) {
			final RefactoringHistoryEntry predecessor= (RefactoringHistoryEntry) first;
			final RefactoringHistoryEntry successor= (RefactoringHistoryEntry) second;
			return (int) (successor.getDescriptor().getTimeStamp() - predecessor.getDescriptor().getTimeStamp());
		}
		return super.compare(viewer, first, second);
	}
}