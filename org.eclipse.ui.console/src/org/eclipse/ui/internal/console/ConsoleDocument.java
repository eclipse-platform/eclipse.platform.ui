/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Philippe Marschall <philippe.marschall@netcetera.ch> - Bug 76936
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

/**
 * A console document. Requires synchronization for multi-threaded access.
 */
public class ConsoleDocument extends Document {

	private static final char NEW_LINE = '\n';

	private static final char BACK_SPACE = '\b';

	private static final char CARRIAGE_RETURN = '\r';

	private static final char FORM_FEED = '\f';

	static final class DelimiterInfo {
		int delimiterIndex;
		int delimiterLength;

		DelimiterInfo(int delimiterIndex, int delimiterLength) {
			this.delimiterIndex = delimiterIndex;
			this.delimiterLength = delimiterLength;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#get(int, int)
	 */
	@Override
	public synchronized String get(int pos, int length) throws BadLocationException {
		return super.get(pos, length);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLength()
	 */
	@Override
	public synchronized int getLength() {
		return super.getLength();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineDelimiter(int)
	 */
	@Override
	public synchronized String getLineDelimiter(int line) throws BadLocationException {
		return super.getLineDelimiter(line);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineInformation(int)
	 */
	@Override
	public synchronized IRegion getLineInformation(int line) throws BadLocationException {
		return super.getLineInformation(line);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineInformationOfOffset(int)
	 */
	@Override
	public synchronized IRegion getLineInformationOfOffset(int offset) throws BadLocationException {
		return super.getLineInformationOfOffset(offset);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineLength(int)
	 */
	@Override
	public synchronized int getLineLength(int line) throws BadLocationException {
		return super.getLineLength(line);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineOffset(int)
	 */
	@Override
	public synchronized int getLineOffset(int line) throws BadLocationException {
		return super.getLineOffset(line);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineOfOffset(int)
	 */
	@Override
	public int getLineOfOffset(int pos) throws BadLocationException {
		return super.getLineOfOffset(pos);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getNumberOfLines()
	 */
	@Override
	public synchronized int getNumberOfLines() {
		return super.getNumberOfLines();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#replace(int, int, java.lang.String)
	 */
	@Override
	public synchronized void replace(int pos, int length, String text) throws BadLocationException {
		if (containsControlCharacter(text)) {
			int lineNumber = this.getTracker().getLineNumberOfOffset(pos);
			boolean endsWithNewLIne = getLineDelimiter(lineNumber) != null;
			int actualPosition;
			StringBuilder buffer;
			int lengthDelta;
			if (endsWithNewLIne) {
				// pos is the start of a new line
				// assume form feeds are rare and therefore don't add additional
				// capacity
				buffer = new StringBuilder(length);
				actualPosition = pos;
				lengthDelta = 0;
			} else {
				// there already exists a line which we may have to modify
				IRegion lineInformation = getLineInformation(lineNumber);
				actualPosition = lineInformation.getOffset();
				// assume form feeds are rare and therefore don't add additional
				// capacity
				int lineLength = lineInformation.getLength();
				buffer = new StringBuilder(length + lineLength);
				String line = get(lineInformation.getOffset(), lineLength);
				buffer.append(line);
				lengthDelta = lineLength;
			}

			int offset = 0;
			int lineStart = 0; // start of the current line in the output buffer
			DelimiterInfo delimiterInfo = nextDelimiterInfo(text, offset);
			while (delimiterInfo != null) {
				processLine(text, offset, lineStart, delimiterInfo.delimiterIndex - offset, buffer);
				buffer.append(text, delimiterInfo.delimiterIndex, delimiterInfo.delimiterIndex + delimiterInfo.delimiterLength);

				offset = delimiterInfo.delimiterIndex + delimiterInfo.delimiterLength;
				delimiterInfo = nextDelimiterInfo(text, offset);
				lineStart = buffer.length();

			}
			processLine(text, offset, lineStart, text.length() - offset, buffer);

			String processedText = buffer.toString();
			super.replace(actualPosition, length + lengthDelta, processedText);
		} else {
			super.replace(pos, length, text);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#set(java.lang.String)
	 */
    @Override
	public synchronized void set(String text) {
        super.set(text);
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.AbstractDocument#completeInitialization()
	 */
    @Override
	protected void completeInitialization() {
        super.completeInitialization();
        addPositionUpdater(new HyperlinkUpdater());
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#addPosition(java.lang.String, org.eclipse.jface.text.Position)
	 */
	@Override
	public synchronized void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
        super.addPosition(category, position);
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#removePosition(java.lang.String, org.eclipse.jface.text.Position)
	 */
    @Override
	public synchronized void removePosition(String category, Position position) throws BadPositionCategoryException {
        super.removePosition(category, position);
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getPositions(java.lang.String)
	 */
    @Override
	public synchronized Position[] getPositions(String category) throws BadPositionCategoryException {
        return super.getPositions(category);
    }

	private static boolean containsControlCharacter(String text) {
		int length = text.length();
		int i = 0;
		while (i < length) {
			char ch = text.charAt(i);
			if (ch == BACK_SPACE || ch == FORM_FEED) {
				return true;
			}
			if (ch == CARRIAGE_RETURN) {
				if (i + 1 < length && text.charAt(i + 1) == NEW_LINE) {
					// don't treat CR LF on Windows as control character
					// skip the character after the current one since we already
					// know it's a newline
					i += 2;
					continue;
				}
				return true;
			}
			i += 1;
		}
		return false;
	}

	private static void processLine(String text, int start, int initialLineStart, int length, StringBuilder buffer) {
		if (length == 0) {
			return;
		}

		// position where the next character insert should happen
		int insertIndex = buffer.length();
		// start index of the current line in the output buffer
		int lineStart = initialLineStart;
		// end index of the line in text
		int end = start + length;
		for (int i = start; i < end; i++) {
			char ch = text.charAt(i);
			if (ch == BACK_SPACE) {
				if (insertIndex > lineStart) {
					// can backtrack only in current line
					insertIndex -= 1;
				}
			} else if (ch == CARRIAGE_RETURN) {
				insertIndex = lineStart;
			} else if (ch == FORM_FEED) {
				int headPosition = insertIndex - lineStart;
				buffer.append('\n');
				lineStart = buffer.length();
				for (int j = 0; j < headPosition; j++) {
					buffer.append(' ');
				}
				insertIndex = lineStart + headPosition;
			} else {
				// other character insert at insertIndex
				if (insertIndex == buffer.length()) {
					buffer.append(ch);
				} else {
					// #charAt does not work when index == length
					buffer.setCharAt(insertIndex, ch);
				}
				insertIndex += 1;
			}
		}
	}

	private static DelimiterInfo nextDelimiterInfo(String text, int offset) {
		int length = text.length();
		for (int i = offset; i < length; i++) {
			char ch = text.charAt(i);
			if (ch == '\r') {
				// \r\n -> will be treated as a new line
				// \r something else -> will be treated as \r
				if (i + 1 < length) {
					if (text.charAt(i + 1) == '\n') {
						return new DelimiterInfo(i, 2);
					}
				}
			} else if (ch == '\n') {
				return new DelimiterInfo(i, 1);
			}
		}
		return null;
	}
}
