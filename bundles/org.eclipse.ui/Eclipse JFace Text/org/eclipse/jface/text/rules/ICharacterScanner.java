package org.eclipse.jface.text.rules;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Defines the interface of a character scanner used by rules.
 * Rules may request the next character or ask the character 
 * scanner to unread the last read character.
 */
public interface ICharacterScanner {
	
	
	public static final int EOF= -1;

	/**
	 * Returns the column of the character scanner.
	 *
	 * @return the column of the character scanner
	 */
	int getColumn();
	/**
	 * Provides rules access to the legal line delimiters.
	 *
	 * @return the legal line delimiters
	 */
	char[][] getLegalLineDelimiters();
	/**
	 * Returns the next character or EOF if end of file has been reached
	 *
	 * @return the next character or EOF
	 */
	int read();
	/**
	 * Rewinds the scanner before the last read character.
	 */
	void unread();
}
