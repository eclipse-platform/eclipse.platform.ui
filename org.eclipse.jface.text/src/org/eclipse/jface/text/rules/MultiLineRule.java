/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.rules;


/**
 * A rule for detecting patterns which begin with a given
 * sequence and may end with a given sequence thereby spanning
 * multiple lines.
 */
public class MultiLineRule extends PatternRule {

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence
	 * @param token the token to be returned on success
	 */
	public MultiLineRule(String startSequence, String endSequence, IToken token) {
		this(startSequence, endSequence, token, (char) 0);
	}

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specific token.
	 * Any character which follows the given escape character will be ignored.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 */
	public MultiLineRule(String startSequence, String endSequence, IToken token, char escapeCharacter) {
		this(startSequence, endSequence, token, escapeCharacter, false);
	}

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specific token. Any character that follows the
	 * given escape character will be ignored. <code>breakOnEOF</code> indicates whether
	 * EOF is equivalent to detecting the <code>endSequence</code>.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 * @param breaksOnEOF indicates whether the end of the file terminates this rule successfully
	 * @since 2.1
	 */
	public MultiLineRule(String startSequence, String endSequence, IToken token, char escapeCharacter, boolean breaksOnEOF) {
		super(startSequence, endSequence, token, escapeCharacter, false, breaksOnEOF);
	}
}
