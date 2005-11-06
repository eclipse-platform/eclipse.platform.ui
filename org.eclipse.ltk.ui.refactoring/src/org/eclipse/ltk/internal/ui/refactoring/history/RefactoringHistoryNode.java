/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
	public static final int COLLECTION= 7;

	/** The day kind */
	public static final int DAY= 0;

	/** The entry kind */
	public static final int ENTRY= 6;

	/** The last month kind */
	public static final int LAST_MONTH= 11;

	/** The last week kind */
	public static final int LAST_WEEK= 1;

	/** The month kind */
	public static final int MONTH= 8;

	/** The this month kind */
	public static final int THIS_MONTH= 10;

	/** The this week kind */
	public static final int THIS_WEEK= 2;

	/** The today kind */
	public static final int TODAY= 3;

	/** The week kind */
	public static final int WEEK= 4;

	/** The year kind */
	public static final int YEAR= 5;

	/** The yesterday kind */
	public static final int YESTERDAY= 9;

	/** The node kind */
	private final int fKind;

	/** The parent node, or <code>null</code> */
	private final RefactoringHistoryNode fParent;

	/**
	 * Creates a new refactoring history node.
	 * 
	 * @param parent
	 *            the parent node, or <code>null</code>
	 * @param kind
	 *            the node kind
	 */
	public RefactoringHistoryNode(final RefactoringHistoryNode parent, final int kind) {
		fParent= parent;
		fKind= kind;
	}

	/**
	 * Returns the node kind.
	 * 
	 * @return the node kind
	 */
	public int getKind() {
		return fKind;
	}

	/**
	 * Returns the parent node.
	 * 
	 * @return the parent node, or <code>null</code>
	 */
	public RefactoringHistoryNode getParent() {
		return fParent;
	}
}
