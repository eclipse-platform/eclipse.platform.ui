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
package org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer;

import org.eclipse.jface.text.*;

/**
 * Implements the <code>IRangeComparator</code> interface for lines in a document.
 * A <code>DocLineComparator</code> is used as the input for the <code>RangeDifferencer</code>
 * engine to perform a line oriented compare on documents.
 * <p>
 * A <code>DocLineComparator</code> doesn't know anything about line separators because
 * its notion of lines is solely defined in the underlying <code>IDocument</code>.
 */
public final class DocLineComparator implements IRangeComparator {

	/**
	 * Document based character sequence.
	 */
	public static class DocumentCharSequence implements CharSequence {

		/** Document */
		private IDocument fDocument;

		/** Offset */
		private int fOffset;

		/** Length */
		private int fLength;

		/**
		 * Leave uninitialized. The document, offset and length have to be set
		 * before use.
		 */
		public DocumentCharSequence() {
			// do nothing
		}

		/**
		 * Initialize with the sequence of characters in the given document
		 * starting at the given offset with the given length.
		 *
		 * @param document The document
		 * @param offset The offset
		 * @param length The length
		 */
		public DocumentCharSequence(IDocument document, int offset, int length) {
			fDocument= document;
			fOffset= offset;
			fLength= length;
		}

		/*
		 * @see java.lang.CharSequence#length()
		 */
		public int length() {
			return fLength;
		}

		/*
		 * @see java.lang.CharSequence#charAt(int)
		 */
		public char charAt(int index) {
			try {
				return fDocument.getChar(fOffset + index);
			} catch (BadLocationException e) {
				throw new IndexOutOfBoundsException();
			}
		}

		/*
		 * @see java.lang.CharSequence#subSequence(int, int)
		 */
		public CharSequence subSequence(int start, int end) {
			return new DocumentCharSequence(fDocument, start, end - start);
		}


		/*
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			int hash= 0;
			for (int i= 0, n= fLength; i < n; i++)
				hash= 29*hash + charAt(i);
			return hash;
		}


		/*
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof DocumentCharSequence))
				return false;
			DocumentCharSequence buffer= (DocumentCharSequence) obj;
			int length= buffer.length();
			if (length != fLength)
				return false;
			for (int i= 0; i < length; i++)
				if (buffer.charAt(i) != charAt(i))
					return false;
			return true;
		}

		/**
		 * Sets the document to the given.
		 *
		 * @param document the document to be set
		 */
		public void setDocument(IDocument document) {
			fDocument= document;
		}

		/**
		 * Sets the offset to the given value.
		 *
		 * @param offset the offset to be set
		 */
		public void setOffset(int offset) {
			fOffset= offset;
		}

		/**
		 * Sets the length to the given value.
		 *
		 * @param length the length to be set
		 */
		public void setLength(int length) {
			fLength= length;
		}
	}

	private final IDocument fDocument;
	private final int fLineOffset;
	private final int fLineCount;
	private final int fLength;
	private final int fMaxOffset;


	private boolean fSkip= false;
	private int fLastOffset;
	private int fLastLength;

	/** Cached document character sequence */
	private DocumentCharSequence fThisBuffer= new DocumentCharSequence();
	/** Cached document character sequence */
	private DocumentCharSequence fOtherBuffer= new DocumentCharSequence();

	/**
	 * Creates a <code>DocLineComparator</code> for the given document range.
	 * ignoreWhiteSpace controls whether comparing lines (in method
	 * <code>rangesEqual<code>) should ignore whitespace.
	 *
	 * @param document the document from which the lines are taken
	 * @param region if non-<code>null</code> only lines within this range are taken
	 */
	public DocLineComparator(IDocument document, IRegion region) {

		fDocument= document;

		if (region != null) {
			fLength= region.getLength();
			int start= region.getOffset();
			int lineOffset= 0;
			try {
				lineOffset= fDocument.getLineOfOffset(start);
			} catch (BadLocationException ex) {
			}
			fLineOffset= lineOffset;

			fMaxOffset= start + fLength;

			if (fLength == 0)
				fLineCount= 0;
			else {
				int endLine= fDocument.getNumberOfLines();
				try {
					endLine= fDocument.getLineOfOffset(start + fLength);
				} catch (BadLocationException ex) {
				}
				fLineCount= endLine - fLineOffset + 1;
			}

		} else {
			fLineOffset= 0;
			fLength= document.getLength();
			fLineCount= fDocument.getNumberOfLines();
			fMaxOffset= fDocument.getLength();
		}
	}

	/**
	 * Returns the number of lines in the document.
	 *
	 * @return number of lines
	 */
	public int getRangeCount() {
		return fLineCount;
	}

	/**
	 * Computes the length of line <code>line</code>.
	 *
	 * @param line the line requested
	 * @return the line length or <code>0</code> if <code>line</code> is not a valid line in the document
	 */
	private int getLineLength(int line) {
		if (line >= fLineCount)
			return 0;
		try {
			int docLine= fLineOffset + line;
			String delim= fDocument.getLineDelimiter(docLine);
			int length= fDocument.getLineLength(docLine) - (delim == null ? 0 : delim.length());
			if (line == fLineCount - 1) {
				fLastOffset= fDocument.getLineOffset(docLine);
				fLastLength= Math.min(length, fMaxOffset - fLastOffset);
			} else {
				fLastOffset= -1;
				fLastLength= length;
			}
			return fLastLength;
		} catch (BadLocationException e) {
			fLastOffset= 0;
			fLastLength= 0;
			fSkip= true;
			return 0;
		}
	}

	/**
	 * Returns <code>true</code> if a line given by the first index
	 * matches a line specified by the other <code>IRangeComparator</code> and index.
	 *
	 * @param thisIndex	the number of the line within this range comparator
	 * @param other the range comparator to compare this with
	 * @param otherIndex the number of the line within the other comparator
	 * @return <code>true</code> if the lines are equal
	 */
	public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {

		if (other != null && other.getClass() == getClass()) {
			DocLineComparator dlc= (DocLineComparator) other;

			int tlen= getLineLength(thisIndex);
			int olen= dlc.getLineLength(otherIndex);
			if (tlen == olen) {
				extract(thisIndex, fThisBuffer);
				dlc.extract(otherIndex, fOtherBuffer);
				return fThisBuffer.equals(fOtherBuffer);
			}

		}
		return false;
	}

	/**
	 * Aborts the comparison if the number of tokens is too large.
	 *
	 * @param length the current edit distance
	 * @param max the maximal edit distance
	 * @param other the comparator with which to compare
	 * @return <code>true</code> to abort a token comparison
	 */
	public boolean skipRangeComparison(int length, int max, IRangeComparator other) {
		return fSkip;
	}

	//---- private methods

	/**
	 * Extract a single line from the underlying document without the line separator
	 * into the given document based character sequence.
	 *
	 * @param line the number of the line to extract
	 * @param buffer the document based character sequence
	 */
	private void extract(int line, DocumentCharSequence buffer) {
		if (line < fLineCount) {
			try {
				int docLine= fLineOffset + line;
				if (fLastOffset == -1)
					fLastOffset= fDocument.getLineOffset(docLine);

				buffer.setDocument(fDocument);
				buffer.setOffset(fLastOffset);
				buffer.setLength(fLastLength);
				return;
			} catch(BadLocationException e) {
				fSkip= true;
			}
		}
		buffer.setDocument(fDocument);
		buffer.setOffset(0);
		buffer.setLength(0);
	}

}

