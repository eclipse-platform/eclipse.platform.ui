/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.source.inlined;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * Utilities class to retrieve position.
 *
 * @since 3.13
 */
public class Positions {

	/**
	 * Returns the line position by taking care or not of of leading spaces.
	 *
	 * @param lineIndex the line index
	 * @param document the document
	 * @param leadingSpaces true if line spacing must take care of and not otherwise.
	 * @return the line position by taking care of leading spaces.
	 * @throws BadLocationException if the line number is invalid in this document
	 */
	public static Position of(int lineIndex, IDocument document, boolean leadingSpaces) throws BadLocationException {
		int offset= document.getLineOffset(lineIndex);
		int lineLength= document.getLineLength(lineIndex);
		String line= document.get(offset, lineLength);
		if (leadingSpaces) {
			offset+= getLeadingSpaces(line);
		}
		return new Position(offset, 1);
	}

	/**
	 * Returns the leading spaces of the given line text.
	 *
	 * @param line the line text.
	 * @return the leading spaces of the given line text.
	 */
	private static int getLeadingSpaces(String line) {
		int counter= 0;
		char[] chars= line.toCharArray();
		for (char c : chars) {
			if (c == '\t')
				counter++;
			else if (c == ' ')
				counter++;
			else
				break;
		}
		return counter;
	}
}
