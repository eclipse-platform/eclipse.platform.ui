package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
 
/**
 * Standard implementation of <code>IToken</code>.
 */
public class Token implements IToken {
	
	/** Standard undefined token */
	public static final IToken UNDEFINED= new Token(true, false, false);
	/** Standard End Of File token */
	public static final IToken EOF= new Token(false, true, false);
	/** Standard whitespace token */
	public static final IToken WHITESPACE= new Token(false, false, true);
	/** Standard token neither undefine, whitespace, nor whitespace */
	public static final IToken OTHER= new Token(false, false, false);
			
	private boolean fIsUndefined;
	private boolean fIsEOF;
	private boolean fIsWhitespace;
	private Object fData;
	
	/**
	 * Creates a new token which represents neither undefined, whitespace, nor EOF.
	 * The newly created token has the given data attached to it.
	 *
	 * @param data the data attached to the newly created token
	 */
	public Token(Object data) {
		fIsUndefined= false;
		fIsEOF= false;
		fIsWhitespace= false;
		fData= data;
	}
	/**
	 * Creates a new token according to the given specification which does not
	 * have any data attached to it.
	 *
	 * @param isUndefined indicates whether the token is undefined
	 * @param isEOF indicates whether the token represents EOF
	 * @param isWhitespace indicates whether the token represents a whitespace
	 */
	private Token(boolean isUndefined, boolean isEOF, boolean isWhitespace) {
		fIsUndefined= isUndefined;
		fIsEOF= isEOF;
		fIsWhitespace= isWhitespace;
		fData= null;
	}
	/*
	 * @see IToken#getData()
	 */
	public Object getData() {
		return fData;
	}
	/*
	 * @see IToken#isEOF()
	 */
	public boolean isEOF() {
		return fIsEOF;
	}
	/*
	 * @see IToken#isOther()
	 */
	public boolean isOther() {
		return ! (fIsUndefined || fIsEOF || fIsWhitespace);
	}
	/*
	 * @see IToken#isUndefined()
	 */
	public boolean isUndefined() {
		return fIsUndefined;
	}
	/*
	 * @see IToken#isWhitespace()
	 */
	public boolean isWhitespace() {
		return fIsWhitespace;
	}
}
