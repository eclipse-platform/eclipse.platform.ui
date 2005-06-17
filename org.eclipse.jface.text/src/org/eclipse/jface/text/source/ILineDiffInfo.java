/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;


/**
 * Describes the change state of one line, which consists of the state of the line itself, which
 * can be <code>UNCHANGED</code>, <code>CHANGED</code> or <code>ADDED</code>, and the number of
 * deleted lines before and after this line.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.0
 */
public interface ILineDiffInfo {

	/** Denotes an unchanged line. */
	static final int UNCHANGED= 0;

	/** Denotes an added line. */
	static final int ADDED= 1;

	/** Denotes a changed line. */
	static final int CHANGED= 2;

	/**
	 * Returns the number of deleted lines after this line.
	 *
	 * @return the number of lines after this line.
	 */
	int getRemovedLinesBelow();

	/**
	 * Returns the number of deleted lines before this line.
	 *
	 * @return the number of lines before this line.
	 */
	int getRemovedLinesAbove();

	/**
	 * Returns the type of this line, one out of <code>UNCHANGED</code>, <code>CHANGED</code> or
	 * <code>ADDED</code>.
	 *
	 * @return the type of this line.
	 */
	int getChangeType();

	/**
	 * Returns whether this line has any changes (to itself, or any deletions before or after it).
	 *
	 * @return <code>true</code>, if the line's state (as returned by <code>getType</code>) is
	 * either <code>CHANGED</code> or <code>ADDED</code> or either of <code>getRemovedLinesBelow</code>
	 * and <code>getRemovedLinesAbove</code> would return a number &gt; 0
	 */
	boolean hasChanges();

	/**
	 * Returns the original text of this changed region
	 *
	 * @return the original text of this changed region, including any deleted lines. The returned
	 * value and its elements may not be <code>null/code>, it may however be of zero length
	 */
	String[] getOriginalText();
}
