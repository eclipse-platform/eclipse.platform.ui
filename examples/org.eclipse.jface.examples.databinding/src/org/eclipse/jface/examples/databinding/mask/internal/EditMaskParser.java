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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.examples.databinding.mask.EditMaskParseException;

/**
 * @since 3.3
 */
public class EditMaskParser {
	private final EditMaskLexerAndToken[] expectedTokens;
	private char placeholder = ' ';

	/**
	 * @param editMask The complete edit mask
	 */
	public EditMaskParser(String editMask) throws EditMaskParseException {
		List<EditMaskLexerAndToken> tokens = new LinkedList<>();
		int position = 0;
		while (position < editMask.length()) {
			EditMaskLexerAndToken token = new EditMaskLexerAndToken();
			position += token.initializeEditMask(editMask, position);
			tokens.add(token);
		}
		expectedTokens = tokens.toArray(new EditMaskLexerAndToken[tokens.size()]);
	}

	/**
	 * @param input the user input which may or may not be in valid format
	 */
	public void setInput(String input) {
		for (EditMaskLexerAndToken expectedToken : expectedTokens) {
			expectedToken.clear();
		}
		int tokenPosition = 0;
		int inputPosition = 0;
		while (inputPosition < input.length() && tokenPosition < expectedTokens.length) {
			while (tokenPosition < expectedTokens.length
					&& (expectedTokens[tokenPosition].isComplete() || expectedTokens[tokenPosition].isReadOnly())) {
				++tokenPosition;
			}
			if (tokenPosition < expectedTokens.length) {
				while (inputPosition < input.length() && !expectedTokens[tokenPosition].isComplete()) {
					String inputChar = input.substring(inputPosition, inputPosition + 1);
					expectedTokens[tokenPosition].accept(inputChar);
					++inputPosition;
				}
			}
		}
	}

	/**
	 * @return the formatted version of the user input
	 */
	public String getFormattedResult() {
		StringBuilder result = new StringBuilder();
		for (EditMaskLexerAndToken expectedToken : expectedTokens) {
			String outputChar = expectedToken.getInput();
			if (outputChar == null) {
				outputChar = "" + placeholder;
			}
			result.append(outputChar);
		}
		return result.toString();
	}

	/**
	 * @return the user input with all literals removed
	 */
	public String getRawResult() {
		StringBuilder result = new StringBuilder();
		for (EditMaskLexerAndToken expectedToken : expectedTokens) {
			if (expectedToken.isReadOnly()) {
				continue;
			}
			String outputChar = expectedToken.getInput();
			if (outputChar == null) {
				outputChar = "";
			}
			result.append(outputChar);
		}
		return result.toString();
	}

	/**
	 * @return true if the current input is a valid input
	 */
	public boolean isComplete() {
		for (EditMaskLexerAndToken expectedToken : expectedTokens) {
			if (!expectedToken.isComplete()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param startingAt The current index within the user input string
	 * @return The first non-read-only index greater than or equal to startingAt
	 */
	public int getNextInputPosition(int startingAt) {
		while (startingAt < expectedTokens.length - 1 && expectedTokens[startingAt].isReadOnly()) {
			++startingAt;
		}
		return startingAt;
	}

	/**
	 * @return the first input position whose token is not marked as complete.
	 *         Returns -1 if all are complete
	 */
	public int getFirstIncompleteInputPosition() {
		for (int position = 0; position < expectedTokens.length; position++) {
			if (!expectedTokens[position].isComplete()) {
				return position;
			}
		}
		return -1;
	}

	/**
	 * @return Returns the placeholder.
	 */
	public char getPlaceholder() {
		return placeholder;
	}

	/**
	 * @param placeholder The placeholder to set.
	 */
	public void setPlaceholder(char placeholder) {
		this.placeholder = placeholder;
	}
}
