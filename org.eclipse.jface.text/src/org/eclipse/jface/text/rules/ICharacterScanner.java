/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.rules;


/**
 * Defines the interface of a character scanner used by rules.
 * Rules may request the next character or ask the character 
 * scanner to unread the last read character.
 */
public interface ICharacterScanner {
	
	/**
	 * The value returned when this scanner has read EOF.
	 */
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
