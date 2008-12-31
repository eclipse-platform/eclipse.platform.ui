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
 * Node of a refactoring history.
 *
 * @since 3.2
 */
public abstract class RefactoringHistoryNode {

	/** The collection kind */
	public static final int COLLECTION= 11;

	/** The day kind */
	public static final int DAY= 9;

	/** The entry kind */
	public static final int ENTRY= 10;

	/** The last month kind */
	public static final int LAST_MONTH= 5;

	/** The last week kind */
	public static final int LAST_WEEK= 3;

	/** The month kind */
	public static final int MONTH= 7;

	/** The project kind */
	public static final int PROJECT= 12;

	/** The this month kind */
	public static final int THIS_MONTH= 4;

	/** The this week kind */
	public static final int THIS_WEEK= 2;

	/** The today kind */
	public static final int TODAY= 0;

	/** The week kind */
	public static final int WEEK= 8;

	/** The year kind */
	public static final int YEAR= 6;

	/** The yesterday kind */
	public static final int YESTERDAY= 1;

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(final Object object) {
		if (object instanceof RefactoringHistoryNode) {
			final RefactoringHistoryNode node= (RefactoringHistoryNode) object;
			final RefactoringHistoryNode parent= getParent();
			if (parent != null) {
				if (!parent.equals(node.getParent()))
					return false;
			} else if (node.getParent() != null)
				return false;
			return getKind() == node.getKind();
		}
		return false;
	}

	/**
	 * Returns the node kind.
	 *
	 * @return the node kind
	 */
	public abstract int getKind();

	/**
	 * Returns the parent node.
	 *
	 * @return the parent node, or <code>null</code>
	 */
	public abstract RefactoringHistoryNode getParent();

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return (getParent() != null ? getParent().hashCode() : 0) + 31 * getKind();
	}
}
