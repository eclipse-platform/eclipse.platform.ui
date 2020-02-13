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

import org.eclipse.core.runtime.Assert;

/**
 * Project of a refactoring history.
 *
 * @since 3.2
 */
public final class RefactoringHistoryProject extends RefactoringHistoryNode {

	/** The project */
	private final String fProject;

	/**
	 * Creates a new refactoring history project.
	 *
	 * @param project the project
	 */
	public RefactoringHistoryProject(final String project) {
		Assert.isNotNull(project);
		Assert.isTrue(!"".equals(project)); //$NON-NLS-1$
		fProject= project;
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof RefactoringHistoryProject) {
			final RefactoringHistoryProject node= (RefactoringHistoryProject) object;
			return super.equals(object) && getProject().equals(node.getProject()) && getKind() == node.getKind();
		}
		return false;
	}

	@Override
	public int getKind() {
		return PROJECT;
	}

	@Override
	public RefactoringHistoryNode getParent() {
		return null;
	}

	/**
	 * Returns the project.
	 *
	 * @return the project
	 */
	public String getProject() {
		return fProject;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 17 * getKind() + 31 * getProject().hashCode();
	}
}