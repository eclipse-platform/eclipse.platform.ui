/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
 * Standard implementation of {@link org.eclipse.jface.text.ITextSelection}.
 * <p>
 * Makes advantage of the weak contract of correctness of its interface. If
 * generated from a selection provider, it only remembers its offset and length
 * and computes the remaining information on request.</p>
 */
public class TextSelection implements ITextSelection {

	/** Internal empty text selection */
	private final static ITextSelection NULL= new TextSelection();

	/**
	 * Returns a shared instance of an empty text selection.
	 *
	 * @return a shared instance of an empty text selection
	 */
	public static ITextSelection emptySelection() {
		return NULL;
	}

	/** Document which delivers the data of the selection */
	private IDocument fDocument;
	/** Offset of the selection */
	private int fOffset;
	/** Length of the selection */
	private int fLength;


	/**
	 * Creates an empty text selection.
	 */
	private TextSelection() {
		this(null, -1, -1);
	}

	/**
	 * Creates a text selection for the given range. This
	 * selection object describes generically a text range and
	 * is intended to be an argument for the <code>setSelection</code>
	 * method of selection providers.
	 *
	 * @param offset the offset of the range
	 * @param length the length of the range
	 */
	public TextSelection(int offset, int length) {
		this(null, offset, length);
	}

	/**
	 * Creates a text selection for the given range of the given document.
	 * This selection object is created by selection providers in responds
	 * <code>getSelection</code>.
	 *
	 * @param document the document whose text range is selected in a viewer
	 * @param offset the offset of the selected range
	 * @param length the length of the selected range
	 */
	public TextSelection(IDocument document, int offset, int length) {
		fDocument= document;
		fOffset= offset;
		fLength= length;
	}

	/**
	 *
	 * Returns true if the offset and length are smaller than 0.
	 * A selection of length 0, is a valid text selection as it
	 * describes, e.g., the cursor position in a viewer.
	 *
	 * @return <code>true</code> if this selection is empty
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	public boolean isEmpty() {
		return fOffset < 0 || fLength < 0;
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getOffset()
	 */
	public int getOffset() {
		return fOffset;
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getStartLine()
	 */
	public int getStartLine() {

		try {
			if (fDocument != null)
				return fDocument.getLineOfOffset(fOffset);
		} catch (BadLocationException x) {
		}

		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getEndLine()
	 */
	public int getEndLine() {
		try {
			if (fDocument != null) {
				int endOffset= fOffset + fLength;
				if (fLength != 0)
					endOffset--;
				return fDocument.getLineOfOffset(endOffset);
			}
		} catch (BadLocationException x) {
		}

		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.ITextSelection#getText()
	 */
	public String getText() {
		try {
			if (fDocument != null)
				return fDocument.get(fOffset, fLength);
		} catch (BadLocationException x) {
		}

		return null;
	}

	/*
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		TextSelection s= (TextSelection) obj;
		boolean sameRange= (s.fOffset == fOffset && s.fLength == fLength);
		if (sameRange) {

			if (s.fDocument == null && fDocument == null)
				return true;
			if (s.fDocument == null || fDocument == null)
				return false;

			try {
				String sContent= s.fDocument.get(fOffset, fLength);
				String content= fDocument.get(fOffset, fLength);
				return sContent.equals(content);
			} catch (BadLocationException x) {
			}
		}

		return false;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
	 	int low= fDocument != null ? fDocument.hashCode() : 0;
	 	return (fOffset << 24) | (fLength << 16) | low;
	}
}

