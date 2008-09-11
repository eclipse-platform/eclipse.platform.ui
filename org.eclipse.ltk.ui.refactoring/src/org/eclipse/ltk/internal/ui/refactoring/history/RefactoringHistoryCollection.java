/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/**
	 * {@inheritDoc}
	 */
	public int getKind() {
		return RefactoringHistoryNode.COLLECTION;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistoryNode getParent() {
		return null;
	}
}
