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
package org.eclipse.ltk.internal.ui.refactoring.history;

/**
 * Collection node of a refactoring history.
 *
 * @since 3.2
 */
public final class RefactoringHistoryCollection extends RefactoringHistoryNode {

	@Override
	public int getKind() {
		return RefactoringHistoryNode.COLLECTION;
	}

	@Override
	public RefactoringHistoryNode getParent() {
		return null;
	}
}
