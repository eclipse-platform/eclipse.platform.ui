package org.eclipse.jface.text.rules;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * A generic scanner which can be "programmed" with a sequence of rules.
 * The scanner is used to get the next token by evaluating each rule in order.
 * If a rule returns a token which is undefined, the scanner will proceed to 
 * the next rule. Otherwise the token provided by the rule will be returned by 
 * the scanner. If no rule returned a defined token, this scanner returns a token
 * which returns <code>true</code> when calling <code>isOther</code>, unless the end 
 * of the file is reached. In this case the token returns <code>true</code> when calling
 * <code>isEOF</code>.
 *
 * @see IRule
 */
public class RuleBasedScanner implements ICharacterScanner {
	
	protected IRule[] fRules;
	
	protected IDocument fDocument;
	protected char[][] fDelimiters;
	
	protected int fOffset;
	protected int fRangeEnd;
	
	protected int fTokenOffset;
	protected int fColumn;
	
	protected static final int UNDEFINED= -1;
	
	/**
	 * Creates a new rule based scanner which does not have any rule.
	 */
	public RuleBasedScanner() {
	}
	/**
	 * Returns the current column of the scanner.
	 */
	public int getColumn() {
		if (fColumn == UNDEFINED) {
			try {
				int line= fDocument.getLineOfOffset(fOffset);
				int start= fDocument.getLineOffset(line);
				
				fColumn= fOffset - start;
				
			} catch (BadLocationException ex) {
			}
		}
		return fColumn;
	}
	/*
	 * @see ICharacterScanner#getLegalLineDelimiters
	 */
	public char[][] getLegalLineDelimiters() {
		return fDelimiters;
	}
	/**
	 * Returns the length of the last token read by this scanner.
	 *
	 * @return the length of the last token read by this scanner
	 */
	public int getTokenLength() {
		return fOffset - fTokenOffset;
	}
	/**
	 * Returns the offset of the last token read by this scanner.
	 *
	 * @return the offset of the last token read by this scanner
	 */
	public int getTokenOffset() {
		return fTokenOffset;
	}
	/**
	 * Returns the next token in the document.
	 *
	 * @return the next token in the document
	 */
	public IToken nextToken() {
		
		IToken token;
		
		while (true) {
			
			fTokenOffset= fOffset;
			fColumn= UNDEFINED;
			
			for (int i= 0; i < fRules.length; i++) {
				token= (fRules[i].evaluate(this));
				if (!token.isUndefined())
					return token;
			}
			if (read() == EOF)
				return Token.EOF;
			else
				return Token.OTHER;
		}
	}
	/*
	 * @see ICharacterScanner#read
	 */
	public int read() {
		
		try {
			
			if (fOffset < fRangeEnd) {
				try {
					return fDocument.getChar(fOffset);
				} catch (BadLocationException e) {
				}
			}
			
			return EOF;
		
		} finally {
			++ fOffset;
		}
	}
	/**
	 * Configures the scanner by providing access to the document range over which to scan.
	 *
	 * @param document the document to scan
	 * @paran offset the offset of the document range to scan
	 * @param length the length of the document range to scan
	 */
	public void setRange(IDocument document, int offset, int length) {
		
		fDocument= document;
		fOffset= offset;
		fRangeEnd= Math.min(fDocument.getLength(), offset + length);
		
		String[] delimiters= fDocument.getLegalLineDelimiters();
		fDelimiters= new char[delimiters.length][];
		for (int i= 0; i < delimiters.length; i++)
			fDelimiters[i]= delimiters[i].toCharArray();
	}
	/**
	 * Configures the scanner with the given sequence of rules.
	 *
	 * @param rules the sequence of rules controlling this scanner
	 */
	public void setRules(IRule[] rules) {
		fRules= rules;
	}
	/*
	 * @see ICharacterScanner#unread
	 */
	public void unread() {
	    	--fOffset;
	}
}
