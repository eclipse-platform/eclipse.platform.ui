/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
 * A specific configuration of pattern rule whereby
 * the pattern begins with a specific sequence and may
 * end with a specific sequence, but will not span more
 * than a single line.
 */
public class SingleLineRule extends PatternRule {

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param token the token to be returned on success
	 */
	public SingleLineRule(String startSequence, String endSequence, IToken token) {
		this(startSequence, endSequence, token, (char) 0);
	}

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token.
	 * Any character which follows the given escape character
	 * will be ignored.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 */
	public SingleLineRule(String startSequence, String endSequence, IToken token, char escapeCharacter) {
		this(startSequence, endSequence, token, escapeCharacter, false);
	}
	
	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token. Alternatively, the
	 * line can also be ended with the end of the file.
	 * Any character which follows the given escape character
	 * will be ignored.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 * @param breaksOnEOF indicates whether the end of the file successfully terminates this rule
	 * @since 2.1
	 */
	public SingleLineRule(String startSequence, String endSequence, IToken token, char escapeCharacter, boolean breaksOnEOF) {
		super(startSequence, endSequence, token, escapeCharacter, true, breaksOnEOF);
	}
}
