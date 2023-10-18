/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring.model;

import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.diff.ITwoWayDiff;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

/**
 * Diff of a refactoring history.
 *
 * @since 3.2
 */
public final class RefactoringHistoryDiff implements IThreeWayDiff {

	/** The diff direction */
	private final int fDirection;

	/** The refactoring history */
	private final RefactoringHistory fHistory;

	/** The diff kind */
	private final int fKind;

	/**
	 * Creates a new refactoring history diff.
	 *
	 * @param history
	 *            the refactoring descriptor
	 * @param kind
	 *            the diff kind
	 * @param direction
	 *            the diff direction
	 */
	public RefactoringHistoryDiff(final RefactoringHistory history, final int kind, final int direction) {
		Assert.isNotNull(history);
		fHistory= history;
		fKind= kind;
		fDirection= direction;
	}

	@Override
	public int getDirection() {
		return fDirection;
	}

	@Override
	public int getKind() {
		return fKind;
	}

	@Override
	public ITwoWayDiff getLocalChange() {
		return null;
	}

	@Override
	public IPath getPath() {
		return null;
	}

	/**
	 * Returns the refactoring history.
	 *
	 * @return the refactoring history
	 */
	public RefactoringHistory getRefactoringHistory() {
		return fHistory;
	}

	@Override
	public ITwoWayDiff getRemoteChange() {
		return null;
	}

	@Override
	public String toDiffString() {
		return ModelMessages.RefactoringHistoryDiff_diff_string;
	}
}
