package org.eclipse.ui.examples.javaeditor.java;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.graphics.Point;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Detects Java words in documents.
 */
public class JavaWordDetector {

	/**
	 * Find the location of the word at offset in document.
	 * @returns Point - x is the start position, y is the end position.
	 * 	Return null if it is not found.
	 * @param document the document being searched.
	 * @param offset - the position to start searching from.
	 */
	 public static Point findWord(IDocument document, int offset) {

		int start= -1;
		int end= -1;

		try {

			int position= offset;
			char character;

			while (position >= 0) {
				character= document.getChar(position);
				if (!Character.isJavaIdentifierPart(character))
					break;
				--position;
			}

			start= position;

			position= offset;
			int length= document.getLength();

			while (position < length) {
				character= document.getChar(position);
				if (!Character.isJavaIdentifierPart(character))
					break;
				++position;
			}

			end= position;

			if (end > start)
				return new Point(start, end - start);

		} catch (BadLocationException x) {
		}

		return null;
	}
}
