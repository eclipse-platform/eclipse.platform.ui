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
package org.eclipse.ui.examples.javaeditor.java;


import org.eclipse.jface.text.*;

/**
 * Auto indent strategy sensitive to brackets.
 */
public class JavaAutoIndentStrategy extends DefaultAutoIndentStrategy {

	public JavaAutoIndentStrategy() {
	}
	
	/* (non-Javadoc)
	 * Method declared on IAutoIndentStrategy
	 */
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text))
			smartIndentAfterNewLine(d, c);
		else if ("}".equals(c.text)) { //$NON-NLS-1$
			smartInsertAfterBracket(d, c);
		}
	}
	
	/**
	 * Returns whether or not the text ends with one of the given search strings.
	 */
	private boolean endsWithDelimiter(IDocument d, String txt) {
		String[] delimiters= d.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.endsWith(delimiters, txt) > -1;
		return false;
	}
	
	/**
	 * Returns the line number of the next bracket after end.
	 * @returns the line number of the next matching bracket after end
	 * @param document - the document being parsed
	 * @param line - the line to start searching back from
	 * @param end - the end position to search back from
	 * @param closingBracketIncrease - the number of brackets to skip
	 */
	 protected int findMatchingOpenBracket(IDocument document, int line, int end, int closingBracketIncrease) throws BadLocationException {

		int start= document.getLineOffset(line);
		int brackcount= getBracketCount(document, start, end, false) - closingBracketIncrease;

		// sum up the brackets counts of each line (closing brackets count negative, 
		// opening positive) until we find a line the brings the count to zero
		while (brackcount < 0) {
			line--;
			if (line < 0) {
				return -1;
			}
			start= document.getLineOffset(line);
			end= start + document.getLineLength(line) - 1;
			brackcount += getBracketCount(document, start, end, false);
		}
		return line;
	}
	
	/**
	 * Returns the bracket value of a section of text. Closing brackets have a value of -1 and 
	 * open brackets have a value of 1.
	 * @returns the line number of the next matching bracket after end
	 * @param document - the document being parsed
	 * @param start - the start position for the search
	 * @param end - the end position for the search
	 * @param ignoreCloseBrackets - whether or not to ignore closing brackets in the count
	 */
	 private int getBracketCount(IDocument document, int start, int end, boolean ignoreCloseBrackets) throws BadLocationException {

		int begin = start;
		int bracketcount= 0;
		while (begin < end) {
			char curr= document.getChar(begin);
			begin++;
			switch (curr) {
				case '/' :
					if (begin < end) {
						char next= document.getChar(begin);
						if (next == '*') {
							// a comment starts, advance to the comment end
							begin= getCommentEnd(document, begin + 1, end);
						} else if (next == '/') {
							// '//'-comment: nothing to do anymore on this line 
							begin= end;
						}
					}
					break;
				case '*' :
					if (begin < end) {
						char next= document.getChar(begin);
						if (next == '/') {
							// we have been in a comment: forget what we read before
							bracketcount= 0;
							begin++;
						}
					}
					break;
				case '{' :
					bracketcount++;
					ignoreCloseBrackets= false;
					break;
				case '}' :
					if (!ignoreCloseBrackets) {
						bracketcount--;
					}
					break;
				case '"' :
				case '\'' :
					begin= getStringEnd(document, begin, end, curr);
					break;
				default :
					}
		}
		return bracketcount;
	}
	
	/**
	 * Returns the end position a comment starting at pos.
	 * @returns the end position a comment starting at pos
	 * @param document - the document being parsed
	 * @param position - the start position for the search
	 * @param end - the end position for the search
	 */
	 private int getCommentEnd(IDocument document, int position, int end) throws BadLocationException {
		int currentPosition = position;
		while (currentPosition < end) {
			char curr= document.getChar(currentPosition);
			currentPosition++;
			if (curr == '*') {
				if (currentPosition < end && document.getChar(currentPosition) == '/') {
					return currentPosition + 1;
				}
			}
		}
		return end;
	}
	
	/**
	 * Returns the String at line with the leading whitespace removed.
	 * @returns the String at line with the leading whitespace removed.
	 * @param document - the document being parsed
	 * @param line - the line being searched
	 */
	 protected String getIndentOfLine(IDocument document, int line) throws BadLocationException {
		if (line > -1) {
			int start= document.getLineOffset(line);
			int end= start + document.getLineLength(line) - 1;
			int whiteend= findEndOfWhiteSpace(document, start, end);
			return document.get(start, whiteend - start);
		} else {
			return ""; //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the position of the character in the document after position.
	 * @returns the next location of character.
	 * @param document - the document being parsed
	 * @param position - the position to start searching from
	 * @param end - the end of the document
	 * @param character - the character you are trying to match
	 */
	 private int getStringEnd(IDocument document, int position, int end, char character) throws BadLocationException {
		int currentPosition = position;
		while (currentPosition < end) {
			char currentCharacter= document.getChar(currentPosition);
			currentPosition++;
			if (currentCharacter == '\\') {
				// ignore escaped characters
				currentPosition++;
			} else if (currentCharacter == character) {
				return currentPosition;
			}
		}
		return end;
	}
	
	/**
	 * Set the indent of a new line based on the command provided in the supplied document.
	 * @param document - the document being parsed
	 * @param command - the command being performed
	 */
	 protected void smartIndentAfterNewLine(IDocument document, DocumentCommand command) {

		int docLength= document.getLength();
		if (command.offset == -1 || docLength == 0)
			return;

		try {
			int p= (command.offset == docLength ? command.offset - 1 : command.offset);
			int line= document.getLineOfOffset(p);

			StringBuffer buf= new StringBuffer(command.text);
			if (command.offset < docLength && document.getChar(command.offset) == '}') {
				int indLine= findMatchingOpenBracket(document, line, command.offset, 0);
				if (indLine == -1) {
					indLine= line;
				}
				buf.append(getIndentOfLine(document, indLine));
			} else {
				int start= document.getLineOffset(line);
				int whiteend= findEndOfWhiteSpace(document, start, command.offset);
				buf.append(document.get(start, whiteend - start));
				if (getBracketCount(document, start, command.offset, true) > 0) {
					buf.append('\t');
				}
			}
			command.text= buf.toString();

		} catch (BadLocationException excp) {
			System.out.println(JavaEditorMessages.getString("AutoIndent.error.bad_location_1")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Set the indent of a bracket based on the command provided in the supplied document.
	 * @param document - the document being parsed
	 * @param command - the command being performed
	 */
	 protected void smartInsertAfterBracket(IDocument document, DocumentCommand command) {
		if (command.offset == -1 || document.getLength() == 0)
			return;

		try {
			int p= (command.offset == document.getLength() ? command.offset - 1 : command.offset);
			int line= document.getLineOfOffset(p);
			int start= document.getLineOffset(line);
			int whiteend= findEndOfWhiteSpace(document, start, command.offset);

			// shift only when line does not contain any text up to the closing bracket
			if (whiteend == command.offset) {
				// evaluate the line with the opening bracket that matches out closing bracket
				int indLine= findMatchingOpenBracket(document, line, command.offset, 1);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					StringBuffer replaceText= new StringBuffer(getIndentOfLine(document, indLine));
					// add the rest of the current line including the just added close bracket
					replaceText.append(document.get(whiteend, command.offset - whiteend));
					replaceText.append(command.text);
					// modify document command
					command.length= command.offset - start;
					command.offset= start;
					command.text= replaceText.toString();
				}
			}
		} catch (BadLocationException excp) {
			System.out.println(JavaEditorMessages.getString("AutoIndent.error.bad_location_2")); //$NON-NLS-1$
		}
	}
}
