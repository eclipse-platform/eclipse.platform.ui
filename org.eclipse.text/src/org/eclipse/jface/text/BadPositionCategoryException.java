package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Indicates the attempt to access a non-existing position
 * category in a document.
 *
 * @see IDocument
 */
public class BadPositionCategoryException extends Exception {
	
	/**
	 * Creates a new bad position category exception.
	 */
	public BadPositionCategoryException() {
		super();
	}
	
	/**
	 * Creates a new bad position category exception.
	 *
	 * @param message the exception's message
	 */
	public BadPositionCategoryException(String message) {
		super(message);
	}
}
