package org.eclipse.jface.text.rules;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Defines the interface by which <code>WordRule</code>
 * determines whether a given character is valid as part
 * of a word in the current context.
 */
public interface IWordDetector {

	/**
	 * Returns whether the specified character is
	 * valid as a subsequent character in a word.
	 */
	boolean isWordPart(char c);
	/**
	 * Returns whether the specified character is
	 * valid as the first character in a word.
	 */
	boolean isWordStart(char c);
}
