package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Defines the interface by which <code>WhitespaceRule</code>
 * determines whether a given character is to be considered
 * whitespace in the current context.
 */
public interface IWhitespaceDetector {

	/**
	 * Returns whether the specified character is whitespace.
	 */
	boolean isWhitespace(char c);
}