/*******************************************************************************
 * Copyright (c) 2006, 2015 The Pampered Chef and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.mask.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.examples.databinding.mask.EditMaskParseException;

/**
 * Lexical analyzer and token for an input mask. Since input masks have exactly
 * one token type, we use the same class to be the recognizer and the token
 * itself.
 *
 * @since 3.3
 */
public class EditMaskLexerAndToken {

	/*
	 * First the literals that represent the types of characters
	 */
	private static List<String> reservedWords = new ArrayList<>();
	{
		reservedWords.add("#");
		reservedWords.add("A");
		reservedWords.add("a");
		reservedWords.add("n");
	}

	/*
	 * ...and their corresponding regular expressions
	 */
	private static List<String> inputRegexes = new ArrayList<>();

	{
		inputRegexes.add("^[0-9]$");
		inputRegexes.add("^[A-Z]$");
		inputRegexes.add("^[a-zA-Z]$");
		inputRegexes.add("^[0-9a-zA-Z]$");
	}

	private String charRegex = null; // A regex for matching input characters or null
	private String literal = null; // The literal character if charRegex is null
	private boolean readOnly;
	private String input = null; // The user's input

	private boolean recognizeReservedWord(String inputMask, int position) {
		String input = inputMask.substring(position, position + 1);
		for (int reservedWord = 0; reservedWord < reservedWords.size(); reservedWord++) {
			if (input.equals(reservedWords.get(reservedWord))) {
				charRegex = inputRegexes.get(reservedWord);
				literal = null;
				input = null;
				readOnly = false;
				return true;
			}
		}
		return false;
	}

	private boolean recognizeBackslashLiteral(String inputMask, int position) throws EditMaskParseException {
		String input = inputMask.substring(position, position + 1);
		if (input.equals("\\")) {
			try {
				input = inputMask.substring(position + 1, position + 2);
				charRegex = null;
				this.input = input;
				literal = input;
				readOnly = true;
				return true;
			} catch (Throwable t) {
				throw new EditMaskParseException("Found a \\ without a character after it: " + inputMask);
			}
		}
		return false;
	}

	private boolean recognizeLiteral(String inputMask, int position) {
		literal = inputMask.substring(position, position + 1);
		this.input = literal;
		charRegex = null;
		readOnly = true;
		return true;
	}

	/**
	 * Initializes itself based on characters in edit mask starting at position;
	 * returns number of chars consumed
	 *
	 * @param inputMask The entire edit mask
	 * @param position  The position to begin parsing
	 * @return The number of characters consumed
	 * @throws EditMaskParseException If it encountered a syntax error during the
	 *                                parse
	 */
	public int initializeEditMask(String inputMask, int position) throws EditMaskParseException {
		clear();
		if (recognizeReservedWord(inputMask, position)) {
			return 1;
		}
		if (recognizeBackslashLiteral(inputMask, position)) {
			return 2;
		}
		if (!recognizeLiteral(inputMask, position)) {
			throw new EditMaskParseException("Should never see this error in this implementation!");
		}
		readOnly = true;
		return 1;
	}

	/**
	 * ignores invalid input; stores valid input
	 */
	public boolean accept(String inputCharacter) {
		if (readOnly) {
			return false;
		}
		if (literal != null) {
			return false;
		}
		if (!canAcceptMoreCharacters()) {
			return false;
		}
		if (inputCharacter.matches(charRegex)) {
			this.input = inputCharacter;
			return true;
		}
		return false;
	}

	/**
	 * @return Returns the characters it has accepted. In the current
	 *         implementation, this is exactly one character. Once quantifiers are
	 *         implemented, this could be many characters. If no characters have
	 *         been accepted, returns null.
	 */
	public String getInput() {
		return input;
	}

	/**
	 * Clear any accepted input
	 */
	public void clear() {
		if (!isReadOnly())
			input = null;
	}

	/**
	 * @return true if it's a literal; false if it's a placeholder
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @return true if it is a literal or if it has accepted the minimum required
	 *         number of characters
	 */
	public boolean isComplete() {
		if (input != null) {
			return true;
		}
		return false;
	}

	/**
	 * @return A position may be complete and yet able to accept more characters if
	 *         the position includes optional characters via a quantifier of some
	 *         type. Not implemented right now.
	 */
	public boolean canAcceptMoreCharacters() {
		return !isComplete();
	}

	/**
	 * @return the minimum number of characters this RegexLexer must accept in order
	 *         to be complete. Because we don't yet support quantifiers, this is
	 *         currently always 1.
	 */
	public int getMinimumLength() {
		return 1;
	}
}
