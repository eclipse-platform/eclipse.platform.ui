package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Indicates the attempt to access a non-existing position.
 * The attempt has been performed on a text store such as a document or string.
 */
public class BadLocationException extends Exception {
	
	/**
	 * Creates a new bad location exception.
	 */
	public BadLocationException() {
		super();
	}
	
	/**
	 * Creates a new bad location exception.
	 *
	 * @param message the exception message
	 */
	public BadLocationException(String message) {
		super(message);
	}
}
