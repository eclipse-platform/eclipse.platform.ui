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

package org.eclipse.jface.text;


/**
 * Default implementation of {@link org.eclipse.jface.text.IMarkSelection}.
 *
 * @since 2.0
 */
public class MarkSelection implements IMarkSelection {

	/** The marked document. */
	private final IDocument fDocument;
	/** The offset of the mark selection. */
	private final int fOffset;
	/** The length of the mark selection. */
	private final int fLength;

	/**
	 * Creates a MarkSelection.
	 *
	 * @param document the marked document
	 * @param offset the offset of the mark
	 * @param length the length of the mark, may be negative if caret before offset
	 */
	public MarkSelection(IDocument document, int offset, int length) {
		fDocument= document;
		fOffset= offset;
		fLength= length;
	}

	/*
	 * @see IMarkSelection#getDocument()
	 */
	public IDocument getDocument() {
		return fDocument;
	}

	/*
	 * @see IMarkSelection#getOffset()
	 */
	public int getOffset() {
		return fOffset;
	}

	/*
	 * @see IMarkSelection#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/*
	 * @see ISelection#isEmpty()
	 */
	public boolean isEmpty() {
		return fLength == 0;
	}

}
