/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jface.text;

/*
 * @see IMarkSelection
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
