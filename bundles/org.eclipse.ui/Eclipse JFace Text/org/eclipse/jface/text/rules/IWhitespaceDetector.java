package org.eclipse.jface.text.rules;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
