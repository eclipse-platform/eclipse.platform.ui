/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.mask.internal;

import java.util.LinkedList;

import org.eclipse.jface.examples.databinding.mask.EditMaskParseException;

/**
 * @since 3.3
 */
public class EditMaskParser {
	private EditMaskLexerAndToken[] expectedTokens;
	private char placeholder = ' ';
	
	/**
	 * @param editMask The complete edit mask 
	 * @throws EditMaskParseException
	 */
	public EditMaskParser(String editMask) throws EditMaskParseException {
		LinkedList tokens = new LinkedList();
		int position = 0;
		while (position < editMask.length()) {
			EditMaskLexerAndToken token = new EditMaskLexerAndToken();
			position += token.initializeEditMask(editMask, position);
			tokens.add(token);
		}
		expectedTokens = (EditMaskLexerAndToken[]) tokens.toArray(new EditMaskLexerAndToken[tokens.size()]);
	}
	
	/**
	 * @param input the user input which may or may not be in valid format
	 */
	public void setInput(String input) {
		for (int i = 0; i < expectedTokens.length; i++) {
			expectedTokens[i].clear();
		}
		int tokenPosition = 0;
		int inputPosition = 0;
		while (inputPosition < input.length() && tokenPosition < expectedTokens.length) {
			while (tokenPosition < expectedTokens.length &&
					(expectedTokens[tokenPosition].isComplete() || 
					 expectedTokens[tokenPosition].isReadOnly())) 
			{
				++tokenPosition;
			}
			if (tokenPosition < expectedTokens.length) {
				while (inputPosition < input.length() && !expectedTokens[tokenPosition].isComplete()) {
					String inputChar = input.substring(inputPosition, inputPosition+1);
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
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < expectedTokens.length; i++) {
			String outputChar = expectedTokens[i].getInput();
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
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < expectedTokens.length; i++) {
			if (expectedTokens[i].isReadOnly()) {
				continue;
			}
			String outputChar = expectedTokens[i].getInput();
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
		for (int i = 0; i < expectedTokens.length; i++) {
			if (!expectedTokens[i].isComplete()) {
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
		while (startingAt < expectedTokens.length-1 && expectedTokens[startingAt].isReadOnly()) {
			++startingAt;
		}
		return startingAt;
	}
	
	/**
	 * @return the first input position whose token is not marked as complete.  Returns -1 if all are complete
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
