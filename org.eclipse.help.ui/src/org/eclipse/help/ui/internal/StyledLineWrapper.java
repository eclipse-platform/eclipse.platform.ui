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
package org.eclipse.help.ui.internal;
import java.text.*;
import java.util.*;

import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.context.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;

public class StyledLineWrapper implements StyledTextContent {
	/** Lines after splitting */
	private ArrayList lines = new ArrayList();
	/** Style ranges, per line */
	private ArrayList lineStyleRanges = new ArrayList();
	/** Character count */
	private int charCount = -1;
	/** Line breaker */
	private static BreakIterator lineBreaker = BreakIterator.getLineInstance();
	/** Beyond this length, lines should wrap */
	public final static int MAX_LINE_LENGTH = 72;
	/**
	 * Constructor
	 */
	public StyledLineWrapper(String text) {
		if (text == null || text.length() == 0)
			text = " "; // use one blank space //$NON-NLS-1$
		setText(text);
	}
	/**
	 * @see StyledTextContent#addTextChangeListener(TextChangeListener)
	 */
	public void addTextChangeListener(TextChangeListener l) {
		// do nothing
	}
	/**
	 * @see StyledTextContent#getCharCount()
	 */
	public int getCharCount() {
		if (charCount != -1)
			return charCount;
		charCount = 0;
		for (Iterator i = lines.iterator(); i.hasNext();)
			charCount += ((String) i.next()).length();
		return charCount;
	}
	/**
	 * @see StyledTextContent#getLine(int)
	 */
	public String getLine(int i) {
		if ((i >= lines.size()) || (i < 0))
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		return (String) lines.get(i);
	}
	/**
	 * @see StyledTextContent#getLineAtOffset(int)
	 */
	public int getLineAtOffset(int offset) {
		if (offset >= getCharCount())
			return getLineCount() - 1;
		int count = 0;
		int line = -1;
		while (count <= offset) {
			count += getLine(++line).length();
		}
		return line;
	}
	/**
	 * @see StyledTextContent#getLineCount()
	 */
	public int getLineCount() {
		if (lines.size() == 0)
			return 1;
		else
			return lines.size();
	}
	/**
	 * @see StyledTextContent#getLineDelimiter()
	 */
	public String getLineDelimiter() {
		return null;
	}
	/**
	 * @see StyledTextContent#getOffsetAtLine(int)
	 */
	public int getOffsetAtLine(int line) {
		if (lines.size() == 0)
			return 0;
		int offset = 0;
		for (int i = 0; i < line; i++)
			offset += getLine(i).length();
		return offset;
	}
	/**
	 * @see StyledTextContent#getTextRange(int, int)
	 */
	public String getTextRange(int start, int end) {
		int l1 = getLineAtOffset(start);
		int l2 = getLineAtOffset(end);
		if (l1 == l2)
			return getLine(l1).substring(start - getOffsetAtLine(l1),
					end - start);
		else {
			StringBuffer range = new StringBuffer(getLine(l1).substring(
					start - getOffsetAtLine(l1)));
			for (int i = l1 + 1; i < l2; i++)
				range.append(getLine(i));
			range.append(getLine(l2).substring(0, end - getOffsetAtLine(l2)));
			return range.toString();
		}
	}
	/**
	 * @see StyledTextContent#removeTextChangeListener(TextChangeListener)
	 */
	public void removeTextChangeListener(TextChangeListener arg0) {
		// do nothing
	}
	/**
	 * @see StyledTextContent#replaceTextRange(int, int, String)
	 */
	public void replaceTextRange(int arg0, int arg1, String arg2) {
		// do nothing
	}
	/**
	 * @see StyledTextContent#setText(String)
	 */
	public void setText(String text) {
		if (text == null)
			text = " "; //$NON-NLS-1$
		processLineBreaks(text);
		processStyles(text);
	}
	/**
	 * Returns the array of styles.
	 */
	public StyleRange[] getStyles() {
		StyleRange[] array = new StyleRange[lineStyleRanges.size()];
		lineStyleRanges.toArray(array);
		return array;
	}
	/**
	 * Create an array of lines with sytles stripped off. Each lines is at most
	 * MAX_LINE_LENGTH characters.
	 */
	private void processLineBreaks(String text) {
		// Create the original lines with style stripped
		lines = new ArrayList();
		char[] textChars = getUnstyledText(text).toCharArray();
		int start = 0;
		for (int i = start; i < textChars.length; i++) {
			char ch = textChars[i];
			if (ch == SWT.CR) {
				lines.add(new String(textChars, start, i - start));
				start = i + 1;
				// if we reached the end, stop
				if (start >= textChars.length)
					break;
				else { // see if the next character is an LF
					ch = textChars[start];
					if (ch == SWT.LF) {
						start++;
						i++;
						if (start >= textChars.length)
							break;
					}
				}
			} else if (ch == SWT.LF) {
				lines.add(new String(textChars, start, i - start));
				start = i + 1;
				if (start >= textChars.length)
					break;
			} else if (i == textChars.length - 1) {
				lines.add(new String(textChars, start, i - start + 1));
			}
		}
		// Break long lines
		for (int i = 0; i < lines.size(); i++) {
			String line = (String) lines.get(i);
			while (line.length() > 0) {
				int linebreak = getLineBreak(line);
				if (linebreak == 0 || linebreak == line.length())
					break;
				String newline = line.substring(0, linebreak);
				lines.remove(i);
				lines.add(i, newline);
				line = line.substring(linebreak);
				lines.add(++i, line);
			}
		}
	}
	/**
	 * Returns the text without the style
	 */
	private static String getUnstyledText(String styledText) {
		String s = TString.change(styledText, ContextsNode.BOLD_TAG, ""); //$NON-NLS-1$
		s = TString.change(s, ContextsNode.BOLD_CLOSE_TAG, ""); //$NON-NLS-1$
		return s;
	}
	/**
	 * Finds a good line breaking point
	 */
	private static int getLineBreak(String line) {
		lineBreaker.setText(line);
		int lastGoodIndex = 0;
		int currentIndex = lineBreaker.first();
		while (currentIndex < MAX_LINE_LENGTH
				&& currentIndex != BreakIterator.DONE) {
			lastGoodIndex = currentIndex;
			currentIndex = lineBreaker.next();
		}
		return lastGoodIndex;
	}
	/**
	 * Creates all the (bold) style ranges for the text. It is assumed that the
	 * text has been split across lines.
	 */
	private void processStyles(String text) {
		// create a new array of styles
		lineStyleRanges = new ArrayList();
		// first, remove the line breaks
		text = TString.change(text, "\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = TString.change(text, "\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
		int offset = 0;
		do {
			// create a style
			StyleRange style = new StyleRange();
			style.fontStyle = SWT.BOLD;
			// the index of the starting style in styled text
			int start = text.indexOf(ContextsNode.BOLD_TAG, offset);
			if (start == -1)
				break;
			String prefix = getUnstyledText(text.substring(0, start));
			style.start = prefix.length();
			// the index of the ending style in styled text
			offset = start + 1;
			int end = text.indexOf(ContextsNode.BOLD_CLOSE_TAG, offset);
			if (end == -1)
				break;
			prefix = getUnstyledText(text.substring(0, end));
			style.length = prefix.length() - style.start;
			lineStyleRanges.add(style);
			offset = end + 1;
		} while (offset < text.length());
	}
}
