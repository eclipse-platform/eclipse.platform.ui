package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Defines the interface of a character scanner used by rules.
 * Rules may request the next character or ask the character 
 * scanner to unread the last read character.
 */
public interface ICharacterScanner {
	
	
	public static final int EOF= -1;

	/**
	 * Provides rules access to the legal line delimiters.
	 *
	 * @return the legal line delimiters
	 */
	char[][] getLegalLineDelimiters();
	
	/**
	 * Returns the column of the character scanner.
	 *
	 * @return the column of the character scanner
	 */
	int getColumn();
	
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
