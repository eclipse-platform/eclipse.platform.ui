/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.ui.text;
/**
 * A textual match in a given object. This class may be subclassed (to add
 * additional match state like accuracy, etc). The element a match is reported
 * against is assumed to contain the match, and the UI will group matches
 * against the same element together.
 * 
 * @since 3.0
 */
public class Match {
	private Object fElement;
	private int fOffset;
	private int fLength;
	/**
	 * Constructs a new Match object.
	 * 
	 * @param element
	 *            The element that contains the match
	 * @param offset
	 *            The offset the match starts at
	 * @param length
	 *            The length of the match
	 */
	public Match(Object element, int offset, int length) {
		fElement = element;
		fOffset = offset;
		fLength = length;
	}
	/**
	 * Returns the offset of this match.
	 * 
	 * @return The offset.
	 */
	public int getOffset() {
		return fOffset;
	}
	/**
	 * Sets the offset of this match.
	 * 
	 * @param offset
	 */
	public void setOffset(int offset) {
		fOffset = offset;
	}
	/**
	 * Returns the length of this match.
	 * 
	 * @return The length
	 */
	public int getLength() {
		return fLength;
	}
	/**
	 * Sets the length.
	 * 
	 * @param length
	 */
	public void setLength(int length) {
		fLength = length;
	}
	/**
	 * Returns the element that contains this match.
	 * 
	 * @return The element that contains this match
	 */
	public Object getElement() {
		return fElement;
	}
}