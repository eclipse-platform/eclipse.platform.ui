package org.eclipse.jface.text.rules;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.Assert;

/**
 * A buffered rule based scanner. The buffer always contains a section 
 * of a fixed size of the document to be scanned.
 */
public class BufferedRuleBasedScanner extends RuleBasedScanner {
	
	private final static int DEFAULT_BUFFER_SIZE= 500;
	
	private int fBufferSize= DEFAULT_BUFFER_SIZE;
	private char[] fBuffer;
	
	private int fStart;
	private int fEnd;
	private int	fDocumentLength;
	
	
	/**
	 * Creates a new buffered rule based scanner which does 
	 * not have any rule and a default buffer size of 500 characters.
	 */
	protected BufferedRuleBasedScanner() {
		super();
	}
	/**
	 * Creates a new buffered rule based scanner which does 
	 * not have any rule. The buffer size is set to the given
	 * number of characters.
	 *
	 * @param size the buffer size
	 */
	public BufferedRuleBasedScanner(int size) {
		super();
		setBufferSize(size);
	}
	/*
	 * @see RuleBasedScanner#read
	 */
	public int read() {
		
		try {
			
			if (fOffset >= fRangeEnd)
				return EOF;
				
			if (fOffset == fEnd)
				shiftBuffer(fEnd);
				
			return fBuffer[fOffset - fStart];
			
		} finally {
			++ fOffset;
		}
	}
	/**
	 * Sets the buffer to the given number of characters.
	 *
	 * @param size the buffer size
	 */
	protected void setBufferSize(int size) {
		Assert.isTrue(size > 0);
		fBufferSize= size;
	}
	/*
	 * @see RuleBasedScanner#setRange
	 */
	public void setRange(IDocument document, int offset, int length) {
		
		super.setRange(document, offset, length);
		
		fDocumentLength= document.getLength();
		shiftBuffer(offset);
	}
	/**
	 * Shifts the buffer so that the buffer starts at the 
	 * given document offset.
	 *
	 * @param offset the document offset at which the buffer starts
	 */
	private void shiftBuffer(int offset) {
				
		fStart= offset;
		fEnd= fStart + fBufferSize;
		if (fEnd > fDocumentLength)
			fEnd= fDocumentLength;
			
		try {
			
			String content= fDocument.get(fStart, fEnd - fStart);
			fBuffer= content.toCharArray();
			
		} catch (BadLocationException x) {
		}
	}
	/*
	 * @see RuleBasedScanner#unread
	 */
	public void unread() {
		
		if (fOffset == fStart)
			shiftBuffer(Math.max(0, fStart - (fBufferSize / 2)));
			
		-- fOffset;
	}
}
