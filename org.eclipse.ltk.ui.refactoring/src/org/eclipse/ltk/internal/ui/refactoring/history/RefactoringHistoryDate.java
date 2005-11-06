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

import org.eclipse.ltk.internal.ui.refactoring.Assert;

/**
 * Date node of a refactoring history.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryDate extends RefactoringHistoryNode {

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
		super(parent, kind);
		Assert.isTrue(stamp >= 0);
		fStamp= stamp;
	}

	/**
	 * Returns the time stamp.
	 * 
	 * @return the time stamp
	 */
	public long getTimeStamp() {
		return fStamp;
	}
}
