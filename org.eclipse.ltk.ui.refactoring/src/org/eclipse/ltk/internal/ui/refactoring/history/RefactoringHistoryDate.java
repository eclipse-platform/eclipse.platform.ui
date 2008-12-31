/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
 * Date node of a refactoring history.
 *
 * @since 3.2
 */
public final class RefactoringHistoryDate extends RefactoringHistoryNode {

	/** The refactoring history node kind */
	private final int fKind;

	/** The parent node, or <code>null</code> */
	private final RefactoringHistoryNode fParent;

	/** The time stamp */
	private final long fStamp;

	/**
	 * Creates a new refactoring history node.
	 *
	 * @param parent
	 *            the parent node, or <code>null</code>
	 * @param stamp
	 *            the time stamp
	 * @param kind
	 *            the node kind
	 */
	public RefactoringHistoryDate(final RefactoringHistoryNode parent, final long stamp, final int kind) {
		fParent= parent;
		fStamp= stamp;
		fKind= kind;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(final Object object) {
		if (object instanceof RefactoringHistoryDate) {
			final RefactoringHistoryDate node= (RefactoringHistoryDate) object;
			return super.equals(object) && getTimeStamp() == node.getTimeStamp() && getKind() == node.getKind();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getKind() {
		return fKind;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistoryNode getParent() {
		return fParent;
	}

	/**
	 * Returns the time stamp.
	 *
	 * @return the time stamp
	 */
	public long getTimeStamp() {
		return fStamp;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return (int) (super.hashCode() + 17 * getKind() + 31 * getTimeStamp());
	}
}
