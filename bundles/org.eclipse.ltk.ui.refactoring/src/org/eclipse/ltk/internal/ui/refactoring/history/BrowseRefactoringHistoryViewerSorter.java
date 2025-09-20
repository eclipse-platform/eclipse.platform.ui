/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public int category(final Object element) {
		if (element instanceof RefactoringHistoryProject) {
			return 0;
		}
		return 1;
	}

	@Override
	public int compare(final Viewer viewer, final Object first, final Object second) {
		if (first instanceof final RefactoringHistoryProject predecessor && second instanceof final RefactoringHistoryProject successor) {
			return getComparator().compare(predecessor.getProject(), successor.getProject());
		} else if (first instanceof final RefactoringHistoryDate predecessor && second instanceof final RefactoringHistoryDate successor) {
			final int delta= predecessor.getKind() - successor.getKind();
			if (delta != 0) {
				return delta;
			}
			final long result= successor.getTimeStamp() - predecessor.getTimeStamp();
			if (result < 0) {
				return -1;
			} else if (result > 0) {
				return 1;
			}
			return 0;
		} else if (first instanceof final RefactoringHistoryEntry predecessor && second instanceof final RefactoringHistoryEntry successor) {
			final long delta= successor.getDescriptor().getTimeStamp() - predecessor.getDescriptor().getTimeStamp();
			if (delta < 0) {
				return -1;
			} else if (delta > 0) {
				return 1;
			} else {
				return 0;
			}
		}
		return super.compare(viewer, first, second);
	}
}