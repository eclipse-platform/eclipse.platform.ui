package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Standard implementation of <code>ITextDoubleClickStrategy</code>. Selects words
 * considering any character other than defined by <code>Character.isLetterOrDigit</code>
 * as a word delimiter. This class is not intended to be subclassed.
 */
public class DefaultTextDoubleClickStrategy implements ITextDoubleClickStrategy {
	
	/** Character position representing the offset of the word */
	private int fOffset;
	/** The length of the word */
	private int fLength;
	
	/**
	 * Creates a new default text double click strategy.
	 */
	public DefaultTextDoubleClickStrategy() {
		super();
	}
	/**
	 * Returns
	 */
	
	/*
	 * @see ITextDoubleClickStrategy#doubleClicked
	 */
	public void doubleClicked(ITextViewer text) {
		
		int position= text.getSelectedRange().x;
		
		if (position < 0)
			return;
					
		if (matchWord(text.getDocument(), position))
			text.setSelectedRange(fOffset, fLength);
	}
	/**
	 * Answers whether the given character is consider a
	 * regular part of a word.
	 *
	 * @param c the character to be checked
	 * @return <code>true</code> if character is a valid word character
	 */
	private boolean isWordCharacter(char c) {
		return Character.isLetterOrDigit(c);
	}
	/**
	 * Determines the word underlying or touching the given offset in the
	 * given document.
	 *
	 * @param document the document to search in
	 * @param offset the offset at which search starts
	 * @return <code>true</code> if a word could be found
	 */
	private boolean matchWord(IDocument document, int offset) {
		
		try {
			
			int pos= offset;
			char c;
			
			while (pos >= 0) {
				c= document.getChar(pos);
				if (!isWordCharacter(c))
					break;
				--pos;
			}
			
			fOffset= pos + 1;
			
			pos= offset;
			int length= document.getLength();
			
			while (pos < length) {
				c= document.getChar(pos);
				if (!isWordCharacter(c))
					break;
				++pos;
			}
			
			fLength= pos - fOffset;
			
			return fLength >= 0;
			
		} catch (BadLocationException x) {
		}
		
		return false;
	}
}
