package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	private char[] fBuffer= new char[DEFAULT_BUFFER_SIZE];
	
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
	
	/**
	 * Sets the buffer to the given number of characters.
	 *
	 * @param size the buffer size
	 */
	protected void setBufferSize(int size) {
		Assert.isTrue(size > 0);
		fBufferSize= size;
		fBuffer= new char[size];
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
			content.getChars(0, fEnd - fStart, fBuffer, 0);
			
		} catch (BadLocationException x) {
		}
	}
	
	/*
	 * @see RuleBasedScanner#setRange
	 */
	public void setRange(IDocument document, int offset, int length) {
		
		super.setRange(document, offset, length);
		
		fDocumentLength= document.getLength();
		shiftBuffer(offset);
	}
	
	/*
	 * @see RuleBasedScanner#read
	 */
	public int read() {
		
		if (fOffset >= fRangeEnd) {
			++ fOffset;
			return EOF;
		}
				
		if (fOffset == fEnd)
			shiftBuffer(fEnd);
			
		return fBuffer[fOffset++ - fStart];			
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


