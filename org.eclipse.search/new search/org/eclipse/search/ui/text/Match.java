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
 * A textual match in a given object. This class may be subclassed to, 
 * for example, add additional match state (i.e. accuracy, etc).
 * 
 * TODO
 *  some words about the fact that the element is used for grouping ....
 *  match is contained in the given element.
 *  Matches contained in the same element are group together in the UI. 
 * 
 * 
 * 
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public class Match {
	private Object fElement;
	private int fOffset;
	private int fLength;
	
	/**
	 * Constructs a new Match object.
	 * 
	 * TODO text missing for params
	 * 
	 * @param element
	 * @param offset
	 * @param length
	 */
	public Match(Object element, int offset, int length) {
		fElement= element;
		fOffset= offset;
		fLength= length;
	}

	/**
	 * @return The offset.
	 */
	public int getOffset() {
		return fOffset;
	}
	
	/**
	 * Sets the offset field.
	 * @param offset
	 */
	public void setOffset(int offset) {
		fOffset= offset;
	}

	/**
	 * TODO missing sentence
	 * @return The length.
	 */
	public int getLength() {
		return fLength;
	}

	/**
	 * Sets the length. 
	 * @param length
	 */
	public void setLength(int length) {
		fLength= length;
	}

	/**
	 * TODO missing sentence
	 * @return The element this match points to.
	 */
	public Object getElement() {
		return fElement;
	}
}
