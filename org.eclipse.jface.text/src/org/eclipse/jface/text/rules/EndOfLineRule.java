package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A specific configuration of a single line rule
 * whereby the pattern begins with a specific sequence but 
 * is only ended by a line delimiter.
 */
public class EndOfLineRule extends SingleLineRule {
	
	/**
	 * Creates a rule for the given starting sequence
	 * which, if detected, will return the specified token.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param token the token to be returned on success
	 */
	public EndOfLineRule(String startSequence, IToken token) {
		this(startSequence, token, (char) 0);
	}

	/**
	 * Creates a rule for the given starting sequence
	 * which, if detected, will return the specified token.
	 * Any character which follows the given escape character
	 * will be ignored.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 */
	public EndOfLineRule(String startSequence, IToken token, char escapeCharacter) {
		super(startSequence, null, token, escapeCharacter, true);
	}
}